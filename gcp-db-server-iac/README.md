# DB Test Server (GCP IaC)

Terraform stack that provisions a single Spot VM on Google Cloud Compute Engine, pre-loaded via
Docker Compose with 8 databases (MySQL, Postgres, SQL Server, DB2, Oracle XE, Informix, Derby,
ClickHouse) for CI read-only database testing.

## Prerequisites

* [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.5
* [Google Cloud CLI](https://cloud.google.com/sdk/docs/install-sdk) installed

You will need a GCP project with the following API enabled:

* Compute Engine API

You will need the following IAM roles in the project:

* Compute Admin
* Compute Network Admin (to create the static IP and firewall rule)

## Initial Configuration

1. If you have not already done so, initialize the gcloud CLI and select your project:

   ```shell
   gcloud init
   ```

   If you have already initialized the CLI, check the configured project:

   ```shell
   gcloud config list
   ```

   If `project` is not the right one, change it:

   ```shell
   gcloud config set project <your-project-id>
   ```

2. Create a service account with the `Compute Admin` and `Compute Network Admin` roles, and
   download a JSON key for it:

   ```shell
   gcloud iam service-accounts keys create key.json \
     --iam-account=<sa-name>@<your-project-id>.iam.gserviceaccount.com
   ```

   Place `key.json` in this directory (or anywhere else — see the next step). It's already
   excluded in `.gitignore`, so it will never be committed.

3. Set `project_id` and `credentials_file` in `terraform.tfvars`:

   ```hcl
   # terraform.tfvars
   project_id       = "your-project-id"
   region           = "us-west1"
   zone             = "us-west1-b"
   credentials_file = "key.json"   # path to the service account key from step 2
   ```

   See `variables.tf` for all overridable variables. `main.tf` passes `credentials_file` straight
   into the `google` provider block (`credentials = file(var.credentials_file)`), so Terraform
   authenticates as this service account regardless of what account is logged into `gcloud`
   locally — no `gcloud auth application-default login` needed.

## Deploying

Run the following commands in this directory:

```shell
terraform init
terraform validate
terraform plan -out=terraform.tfplan
terraform apply terraform.tfplan
```

The static IP is printed in the `db_server_ip` output, and the ready-to-copy SSH command is
printed in the `ssh_command` output.

> **Note:** `terraform apply` finishing only means the VM was created — it does **not** mean the
> databases are ready. The startup script still needs to install Docker and pull all 8 database
> images (some, like SQL Server/DB2/Oracle, are large), which typically takes **a few minutes**.
> None of the JDBC ports will accept connections until that finishes, so don't point CI at
> `db_server_ip` immediately after `apply` — check readiness first.

You can check progress via SSH:

```shell
gcloud compute ssh db-test-server --zone=us-west1-b
sudo docker compose -f /opt/db-server/docker-compose.yml ps
```

All 8 services should show `Up` (or `healthy`) before connecting from CI. `cat
/opt/db-server/status.txt` will show `DB Server Ready` once the startup script itself has
finished running, though individual containers may take a little longer after that to finish
initializing.

Note: `gcloud compute ssh` uses whatever account/project `gcloud` is currently logged into
(`gcloud auth list` / `gcloud config get-value project`), **not** the `credentials_file` service
account used by Terraform. If `gcloud` is still authenticated as a leftover account from another
project, either switch back to your own account (`gcloud auth login` then
`gcloud config set project <project-id>`), or activate the same service account for `gcloud`
itself (`gcloud auth activate-service-account --key-file=key.json`).

## What Gets Created

* **VM** (`vm.tf`) — `e2-standard-4` (4 vCPU / 16 GB RAM) Spot instance, Ubuntu 22.04, 120 GB
  `pd-standard` boot disk. Spot pricing is used to keep cost low; the instance may be preempted
  and will need to be restarted manually (`instance_termination_action = "STOP"`).
* **Static IP** (`network.tf`) — reserved external IP bound to the VM so the address never
  changes across restarts.
* **Firewall rule** (`network.tf`) — opens SSH (22) and all database ports to `0.0.0.0/0`. This
  is intentionally permissive for CI convenience; restrict `source_ranges` if you need tighter
  security.
* **Startup script** (`startup.sh.tpl`) — runs on first boot:
  1. Adds Docker's official apt repo and installs `docker-ce` + `docker-compose-plugin`
     (Ubuntu's default repos don't ship `docker-compose-plugin` — see Troubleshooting)
  2. Configures `/etc/docker/daemon.json` to cap container logs (`max-size: 50m`, `max-file: 3`)
     so the disk doesn't fill up over time
  3. Writes `docker-compose.yml` (embedded at plan time via `templatefile()`) to
     `/opt/db-server/docker-compose.yml`
  4. Runs `docker compose up -d`

## Database Ports

| DB         | Port  | JDBC example                              |
| ---------- | ----- | ------------------------------------------ |
| MySQL      | 3306  | `jdbc:mysql://<ip>:3306/db`                |
| Postgres   | 5432  | `jdbc:postgresql://<ip>:5432/db`           |
| SQL Server | 1433  | `jdbc:sqlserver://<ip>:1433`               |
| DB2        | 50000 | `jdbc:db2://<ip>:50000/db`                 |
| Oracle XE  | 1521  | `jdbc:oracle:thin:@<ip>:1521/XE`           |
| Informix   | 9088  | `jdbc:informix-sqli://<ip>:9088/db`        |
| Derby      | 1527  | `jdbc:derby://<ip>:1527/db`                |
| ClickHouse | 8123  | `jdbc:clickhouse://<ip>:8123/db`           |

Replace `<ip>` with the `db_server_ip` output.

## Updating the Database Images

Edit `docker-compose.yml` in this directory, then re-run `terraform apply`. Since the file's
content is embedded into the VM's startup-script metadata, changing it forces the VM's metadata
to update; you'll still need to re-run the startup script (see Troubleshooting below) or SSH in
and run `docker compose up -d` again to actually pull the new images, since the startup script
only runs automatically on first boot.

## Troubleshooting

**`docker: command not found` after the VM comes up** — check whether the startup script actually
succeeded:

```shell
sudo journalctl -u google-startup-scripts.service --no-pager | tail -100
```

If you see `E: Unable to locate package docker-compose-plugin`, it's because Ubuntu 22.04's
default apt repos don't carry that package — it only exists in Docker's own apt repo. This stack's
`startup.sh.tpl` already adds that repo before installing, so this should only come up if you're
debugging an older version of the script or a manual install.

**Startup script changed but the VM already exists** — the startup script only runs automatically
on first boot; a `terraform apply` that only changes `startup-script` metadata updates the VM but
does not re-run it. To force a re-run without recreating the VM, SSH in and run:

```shell
sudo google_metadata_script_runner startup
```

This re-fetches the current startup-script from instance metadata and executes it — useful both
after changing `startup.sh.tpl`/`docker-compose.yml` and after a failed first run.

## Destroying

```shell
terraform destroy
```

This removes the VM, static IP, and firewall rule. There is no persistent storage outside the
boot disk, so no data survives a destroy.
