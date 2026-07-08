variable "project_id" {

  description = "GCP project id"

  type = string

}


variable "region" {

  default = "us-west1"

}


variable "zone" {

  default = "us-west1-b"

}


variable "credentials_file" {

  description = "Path to the service account JSON key file used to authenticate the google provider"

  type = string

  default = "key.json"

}