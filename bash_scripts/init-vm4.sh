#!/bin/bash

# Update the system's package list
sudo apt update

# Install Maven
sudo apt install -y maven

# Run Maven clean and install
sudo mvn clean install
