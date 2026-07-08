output "db_server_ip" {

 value = google_compute_address.db_ip.address

}

output "ssh_command" {

 value = "gcloud compute ssh db-test-server --zone=${var.zone} --project=${var.project_id}"

}