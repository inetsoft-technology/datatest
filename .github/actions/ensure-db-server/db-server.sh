#!/usr/bin/env bash
# Helper for the ensure-db-server action.
#   db-server.sh ensure    start the VM if needed, map hostnames, wait for DB ports
#   db-server.sh watchdog  loop forever, restarting the VM if it gets preempted
# Requires PROJECT_ID, INSTANCE, ZONE (and WAIT_MINUTES for ensure) in the environment.
# The repo is public: never print the server IP, and mask it as soon as it is resolved.
set -uo pipefail

DB_PORTS=(3306 5432 1433 50000 1521 9088 1527 8123)
DB_HOSTS=(mysql_server db2_server derby_server oracle_server informix_server
          postgres_server sqlserver_server clickhouse_server)
HOSTS_FILE=${HOSTS_FILE:-/etc/hosts}

log() { echo "[$(date '+%H:%M:%S')] $*"; }

gc() { gcloud compute instances "$@" "$INSTANCE" --zone="$ZONE" --project="$PROJECT_ID" --quiet; }

# Prints the VM status, or nothing (with the gcloud error logged) if the lookup failed.
vm_status() {
  local out
  if out=$(gc describe --format='value(status)' 2>&1); then
    echo "$out"
  else
    log "gcloud describe failed: $out" >&2
  fi
}

vm_ip() { gc describe --format='value(networkInterfaces[0].accessConfigs[0].natIP)' 2>/dev/null; }

# Replace any existing *_server lines so repeated runs don't leave duplicate or stale entries.
write_hosts() {
  local ip=$1 h
  for h in "${DB_HOSTS[@]}"; do
    sudo sed -i "/[[:space:]]${h}\$/d" "$HOSTS_FILE"
    echo "$ip $h" | sudo tee -a "$HOSTS_FILE" > /dev/null
  done
}

# Ask GCP to bring the VM up. Spot capacity may be unavailable, so failures are not fatal;
# callers retry until their own deadline.
start_vm() {
  local status=$1
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
    "")
      log "VM status unknown, will retry" ;;
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
  local status ip="" closed=""

  while :; do
    status=$(vm_status)

    if [ "$status" = RUNNING ]; then
      ip=$(vm_ip)
      if [ -n "$ip" ]; then
        echo "::add-mask::$ip"
        closed=$(closed_ports "$ip")
        if [ -z "$closed" ]; then
          break
        fi
        log "VM RUNNING, waiting for DB ports: $closed"
      fi
    else
      start_vm "$status"
    fi

    if [ "$(date +%s)" -ge "$deadline" ]; then
      echo "::error::DB server not ready after ${WAIT_MINUTES} minutes (status=${status:-unknown}, ip resolved=$([ -n "$ip" ] && echo yes || echo no), closed ports: ${closed:-n/a})"
      exit 1
    fi
    sleep 20
  done

  write_hosts "$ip"
  log "DB server ready, /etc/hosts updated"
}

watchdog() {
  local status ip restarted=0
  log "Watchdog started for $INSTANCE"
  while :; do
    sleep 60
    status=$(vm_status)
    if [ "$status" != RUNNING ]; then
      log "VM status is '${status:-unknown}'"
      start_vm "$status"
      restarted=1
    elif [ "$restarted" = 1 ]; then
      # The external IP is a reserved static address, but refresh hosts anyway in case it changed.
      ip=$(vm_ip)
      if [ -n "$ip" ]; then
        write_hosts "$ip"
        log "VM is RUNNING again, /etc/hosts refreshed"
        restarted=0
      fi
    fi
  done
}

case "${1:-}" in
  ensure)   ensure ;;
  watchdog) watchdog ;;
  *) echo "usage: $0 ensure|watchdog" >&2; exit 2 ;;
esac
