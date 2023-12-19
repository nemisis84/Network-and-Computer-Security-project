#!/bin/bash

# Delete rules:
sudo /sbin/iptables -F
sudo /sbin/iptables -t nat -F

# Redirect all HTTP to VM3:
# sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j DNAT --to-destination 192.168.1.1

# Allow all HTTP to VM3:
sudo iptables -A FORWARD -p tcp -d 192.168.1.1 --dport 80 -j ACCEPT

# Allow all HTTP outgoing from VM3:
sudo iptables -A FORWARD -p tcp -s 192.168.1.1 --sport 80 -m state --state ESTABLISHED,RELATED -j ACCEPT

# Allow HTTP from VM3 to VM1:
sudo iptables -A FORWARD -p tcp -s 192.168.1.1 -d 192.168.0.100 --dport 80 -j ACCEPT

# Allow HTTP from VM1 to VM3
sudo iptables -A FORWARD -p tcp -s 192.168.0.100 -d 192.168.1.1 --sport 80 -m state --state ESTABLISHED,RELATED -j ACCEPT

Reject all other Traffic:
sudo iptables -A INPUT -j REJECT
sudo iptables -A FORWARD -j REJECT

# Show rules:
sudo /sbin/iptables -L
sudo /sbin/iptables -t nat -L

