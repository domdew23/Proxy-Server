# Proxy Server

This is a client server application where the Client sends a URL (at this time the proxy server only supports links to jpg images), and the Proxy Server will fetch the image on the Client's behalf, cache it and then send it back to the Client. The application uses Dom's Original Protocol Extended (DOPE) to transfer the image from the Server to the Client. DOPE is an extension of the protocol [TFTP](http://www.ietf.org/rfc/rfc1350.txt) which adds additional functionality to improve performance. The DOPE Protocol is implemented on top of UDP. Below is a graph illustrating the difference in throughput using a "Stop and Wait" type protocol versus using a TCP-style "Sliding Window" type protocol. The time is over the course of 50 transfers of a 1.3 megabyte file and time is put on a logarithmic base 10 scale. The default is for the application to using sliding windows, but running both the Client and Server with the command line argument '-sw' will run stop and wait.

<h2>Stop and Wait vs Sliding Window</h2><br>
<img src="graphs/stopAndWait_vs_slidingWindow_ipv4_nodrops.png"><br>

## Getting Started

To run the proxy server you need to choose a host as your Server for whom you want to make requests on your behalf. Pull or download the repository on both your machine and the Server's machine. Type the following commands to run the application.

### Run the Server:
#### Windows:
* run_server.bat

#### Mac/Linux:
* $ java -cp bin com.dom.server.Server

### Run the Client:
#### Windows:
* run_client.bat

#### Mac/Linux:
* $ java -cp bin com.dom.client.Client

### Command Line Arguments:
#### Specify use of IPv4:
* -Djava.net.preferIPv4Stack=true

#### Specify use of IPv6:
* -Djava.net.preferIPv6Addresses=true

#### Stop and Wait:
* -sw

#### Simulate Dropping 1% of Packets:
* -d

### Prerequisites

To run this application you must have Java installed on both the Client and Server.
* [Click here to download Java](https://java.com/en/download/)
* [Click here to download the JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Click here to download the JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

## Built With
* [Java](https://java.com/)

## Author

* **Dominic Dewhurst**

## Links
* [Linkedin](https://www.linkedin.com/in/dominic-dewhurst-b1a971129)
* [Youtube](https://www.youtube.com/channel/UCPrj3XZlY39YiaHc6yaodLg)
