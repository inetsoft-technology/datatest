resource "google_compute_instance" "db_server" {

  name = "db-test-server"

  zone = var.zone


  machine_type = "e2-standard-4"


  scheduling {

    preemptible = true

    provisioning_model = "SPOT"

    instance_termination_action = "STOP"

    automatic_restart = false
  }


  boot_disk {

    initialize_params {

      image = "ubuntu-os-cloud/ubuntu-2204-lts"

      size = 120

      type = "pd-standard"

    }
  }


  network_interface {

    network = "default"


    access_config {

      nat_ip = google_compute_address.db_ip.address

    }
  }


  metadata = {
    startup-script = templatefile("${path.module}/startup.sh.tpl", {
      docker_compose_content = file("${path.module}/docker-compose.yml")
    })
  }


  tags = [
    "db-server"
  ]
}