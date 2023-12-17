#!/bin/bash


# Update the system's package list
sudo apt update

# Install PostgreSQL
sudo apt install -y postgresql

# Install PostgreSQL-contrib
sudo apt install -y postgresql-contrib

# Install Maven
sudo apt install -y maven

# Run Maven clean and install
sudo mvn clean install
