# CXX GrooveGalaxy / BombAppetit / BlingBank / MediTrack Project Report

## 1. Introduction

(_Provide a brief overview of your project, including the business scenario and the main components: secure documents, infrastructure, and security challenge._)

(_Include a structural diagram, in UML or other standard notation._)

## 2. Project Development

### 2.1. Secure Document Format

#### 2.1.1. Design

(_Outline the design of your custom cryptographic library and the rationale behind your design choices, focusing on how it addresses the specific needs of your chosen business scenario._)

(_Include a complete example of your data format, with the designed protections._)

#### 2.1.2. Implementation

(_Detail the implementation process, including the programming language and cryptographic libraries used._)

(_Include challenges faced and how they were overcome._)

### 2.2. Infrastructure

#### 2.2.1. Network and Machine Setup
We have a total of 4 VMs in our infrastructure. The image below shows how the infrastructure is built up:

![Infrastructure](https://github.com/tecnico-sec/a16-joao-daniel-simen/blob/main/network/Infrastructure.png)

For the communication between the instances, we use HTTP. We made all communications using Java libraries. No major framework was used. As we wanted control over the security and the information flow we think this was the best option. Many frameworks handle a lot of the security properties for you, which for the most part is good, but for this project did not make sense. Using simple libraries allowed us to do all the changes we wanted, without managing the configuration and setup of a framework. 

As for the chosen communication protocol, we choose HTTP, because of its familiarity and properties. For a client sending a request in a client-server communication, HTTP is the go-to protocol used in technology today. This easily allows us to request operations which can execute the CRUD operations. One thing to be aware of is that we did not enable clients to update data in the database, as songs are usually immutable. We will now explain the technology and reasoning for each participant:

##### Client
An HTTP client class is made for asking for songs, adding songs and deleting songs. Built using mainly java.net.http.* packages. A CLI is also provided. This lightweight version of a client was implemented with a focus on being adaptable to a changing application and network. A web interface could also be implemented, but as it would need to be implemented with the security classes already made in Java, this would lead to more complexity. For the project, a simple CLI is sufficient to illustrate the security properties of the application. 

##### Application Server

We manually created a HTTP server, as it was enough for our needs, and allowed for good flexibility and responsiveness. Implementing a framework would make it easier, but would flaw our security objetives/challenges as it does the job for us.
TO ELABORATE

#### Database

We chose PostgreSQL as our database due to its open-source nature and simple to use and deploy. The extensive community support was also an import aspect as well as aligning with our project's requirements, ensuring security, and flexibility.


(_Provide a brief description of the built infrastructure._) DONE

(_Justify the choice of technologies for each server._)

#### 2.2.2. Server Communication Security

One of the ways to secure both the database server and application server was the use of a firewall at the router. The applied rules could be found in /network/router/firewall. We explicitly only allow these communications:
- Any computer can access the application server with HTTP
- The application server can communicate with established HTTP connections
- The application server can initiate a HTTP connection with the database server
- The database server can communicate with an established HTTP connection with the application server.
- All other communication won't be forwarded by the router.

This leads to only the application server being exposed to the outside world, while the database can only be accessed by the application server. This will leave some openings for unauthorized access to the database, but with the implementation of session keys, this is prevented. 

(_Discuss how server communications were secured, including the secure channel solutions implemented and any challenges encountered._)

(_Explain what keys exist at the start and how are they distributed?_)

### 2.3. Security Challenge

#### 2.3.1. Challenge Overview

(_Describe the new requirements introduced in the security challenge and how they impacted your original design._)

#### 2.3.2. Attacker Model

(_Define who is fully trusted, partially trusted, or untrusted._)

(_Define how powerful the attacker is, with capabilities and limitations, i.e., what can he do and what he cannot do_)

#### 2.3.3. Solution Design and Implementation

(_Explain how your team redesigned and extended the solution to meet the security challenge, including key distribution and other security measures._)

(_Identify communication entities and the messages they exchange with a UML sequence or collaboration diagram._)  

## 3. Conclusion

(_State the main achievements of your work._)

(_Describe which requirements were satisfied, partially satisfied, or not satisfied; with a brief justification for each one._)

(_Identify possible enhancements in the future._)

(_Offer a concluding statement, emphasizing the value of the project experience._)

## 4. Bibliography

(_Present bibliographic references, with clickable links. Always include at least the authors, title, "where published", and year._)

----
END OF REPORT
