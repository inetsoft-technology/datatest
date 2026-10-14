#!/usr/bin/env bash
# Helper for the ensure-db-server action.
#   db-server.sh ensure    start the VM if needed, map hostnames, wait for DB ports
#   db-server.sh watchdog  loop forever, restarting the VM if it gets preempted
# Requires PROJECT_ID, INSTANCE, ZONE (and WAIT_MINUTES for ensure) in the environment.
set -uo pipefail

DB_PORTS=(3306 5432 1433 50000 1521 9088 1527 8123)
DB_HOSTS=(mysql_server db2_server derby_server oracle_server informix_server
          postgres_server sqlserver_server clickhouse_server)

log() { echo "[$(date '+%H:%M:%S')] $*"; }

gc() { gcloud compute instances "$@" "$INSTANCE" --zone="$ZONE" --project="$PROJECT_ID" --quiet; }

vm_status() { gc describe --format='value(status)' 2>/dev/null; }

vm_ip() { gc describe --format='value(networkInterfaces[0].accessConfigs[0].natIP)' 2>/dev/null; }

# Ask GCP to bring the VM up. Spot capacity may be unavailable, so failures are not fatal;
# callers retry until their own deadline.
start_vm() {
  local status
  status=$(vm_status)
  case "$status" in
    RUNNING|PROVISIONING|STAGING|REPAIRING)
      return 0 ;;
    TERMINATED|STOPPED)
      log "VM is $status (likely preempted), starting it"
      gc start --async || log "start request failed (spot capacity?), will retry" ;;
    SUSPENDED)
      log "VM is SUSPENDED, resuming it"
      gc resume --async || log "resume request failed, will retry" ;;
    STOPPING|SUSPENDING)
      log "VM is $status, waiting for it to settle" ;;
    *)
      log "Unexpected VM status '$status'" ;;
  esac
}

port_open() { timeout 3 bash -c "</dev/tcp/$1/$2" 2>/dev/null; }

closed_ports() {
  local ip=$1 p closed=()
  for p in "${DB_PORTS[@]}"; do
    port_open "$ip" "$p" || closed+=("$p")
  done
  echo "${closed[*]}"
}

ensure() {
  local deadline=$(( $(date +%s) + WAIT_MINUTES * 60 ))
  local status ip="" closed

  while :; do
    status=$(vm_status)
    if [ -z "$status" ]; then
      echo "::error::Cannot find VM $INSTANCE in $ZONE (project $PROJECT_ID). Was it destroyed?"
      exit 1
    fi

    if [ "$status" = RUNNING ]; then
      ip=$(vm_ip)
      if [ -n "$ip" ]; then
        closed=$(closed_ports "$ip")
        if [ -z "$closed" ]; then
          break
        fi
        log "VM RUNNING at $ip, waiting for DB ports: $closed"
      fi
    else
      start_vm
    fi

    if [ "$(date +%s)" -ge "$deadline" ]; then
      echo "::error::DB server not ready after ${WAIT_MINUTES} minutes (status=$status, ip=${ip:-none}, closed ports: ${closed:-n/a})"
      exit 1
    fi
    sleep 20
  done

  log "DB server ready at $ip"
  for h in "${DB_HOSTS[@]}"; do
    echo "$ip $h" | sudo tee -a /etc/hosts > /dev/null
  done
  echo "ip=$ip" >> "$GITHUB_OUTPUT"
}

watchdog() {
  local status
  log "Watchdog started for $INSTANCE"
  while :; do
    sleep 60
    status=$(vm_status)
    if [ "$status" != RUNNING ]; then
      log "VM status is '${status:-unknown}'"
      start_vm
    fi
  done
}

case "${1:-}" in
  ensure)   ensure ;;
  watchdog) watchdog ;;
  *) echo "usage: $0 ensure|watchdog" >&2; exit 2 ;;
esac
