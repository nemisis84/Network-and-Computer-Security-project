#!/bin/bash

# Update the system's package list
sudo apt update

# Install PostgreSQL
sudo apt install -y postgresql-14.10

# Install PostgreSQL-contrib
sudo apt install -y postgresql-contrib-14.10

# Install Maven
sudo apt install -y maven

# Run Maven clean and install
sudo mvn clean install
