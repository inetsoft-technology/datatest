resource "google_compute_address" "db_ip" {
  name   = "db-test-server-ip"
  region = var.region
}


resource "google_compute_firewall" "db_firewall" {

  name = "db-server-firewall"

  network = "default"

  allow {
    protocol = "tcp"

    ports = [
      "22",
      "3306",
      "5432",
      "1433",
      "50000",
      "1521",
      "9088",
      "1527",
      "8123",
      "9000",
      "9009"
    ]
  }

  source_ranges = [
    "0.0.0.0/0"
  ]
}