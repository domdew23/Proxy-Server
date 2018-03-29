package com.dom.dope;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import java.io.IOException;

import com.dom.util.Control;
import java.util.Arrays;

/*
A packet less than 512 (Max bytes per packet) signals termination of transfer
If packet is dropped, intended recipiant times out and retransmit last packet (data or ack)
Sender keeps one packet on hand for retransmission (SaW)
Server sends data and recieves acks
Client sends acks and recieves data

Client sends request to read; Server responds with first packet of data
Ack packet will contain block number of data packet being acknowledged
Seq nums begin at 1
Each end of connection chooes TID (Transfer identifier) for itself - should be random
Each packet has associated with it the source TID and dest TID - handed to UDP as src and dest PORTS - used for remainder of transfer
*/

public class DOPESocket {
	
	private int port, senderPort;
	private InetAddress address;
	private DatagramSocket connection;
	private DOPEPacket[] packets;
	private char currentSeqNum;
	private boolean addressSet;

	public DOPESocket(int port, InetAddress address) throws IOException {
		/* new client socket */
		this.port = port;
		this.address = address;
		this.connection = new DatagramSocket();
		this.addressSet = true;
	}

	public DOPESocket(int port) throws IOException {
		this.port = port;
		this.connection = new DatagramSocket(port);
		this.addressSet = false;
	}

	public DOPESocket(InetAddress address) throws IOException {
		this.connection = new DatagramSocket();
		this.address = address;
	}

	public void sendStopAndWait(DOPEPacket packet) throws IOException {
		byte[] bytes = packet.getPacket();
		System.out.println("Sending " + bytes.length + " bytes.");
		DatagramPacket dgPacket = new DatagramPacket(bytes, bytes.length, address, port);
		connection.send(dgPacket);
		System.out.println("Packet sent.");
	}

	public void sendSlidingWindow(DOPEPacket packet){

	}

	public DOPEPacket receiveStopAndWait() throws IOException {
		byte[] buffer = new byte[515];//43 + DOPEPacket.HEADER_LENGTH];//Control.MAX_PACKET_LENGTH]; /* change to length of data + header length */
		DatagramPacket dgPacket = new DatagramPacket(buffer, buffer.length);
		connection.receive(dgPacket);

		byte[] packet = Arrays.copyOfRange(buffer, 0, dgPacket.getLength());
		
		System.out.println("Received packet of size: " + dgPacket.getLength());
		if (!addressSet) {
			address = dgPacket.getAddress();
			senderPort = dgPacket.getPort();
			addressSet = true;
			System.out.println("Set address.");
		}
		return (new DOPEPacket(packet));
	}

	public void beginTransferStopAndWait(DOPEPacket requestPacket) throws IOException {
		/* send first data packet to client */
		byte[] bytes = Control.getImage(requestPacket);
		packets = Control.split(bytes);
		currentSeqNum = 1;
		sendStopAndWait(packets[currentSeqNum - 1]);
	}

	public void continueTransferServer(DOPEPacket ackPacket) throws IOException {
		if (Control.slidingWindow){

		} else {
			if (currentSeqNum == ackPacket.getSequenceNumber()){
				/* recieved next packet in the chain */
				currentSeqNum++;
				sendStopAndWait(packets[currentSeqNum - 1]);
			} else {
				/* recieved wrong packet in the chain */
				System.out.println("Received wrong packet in chain - should not happen for stop and wait");
				System.exit(0);
			}
		}
	}

	public void receiveSlidingWindow(DOPEPacket packet){

	}

	public void setAddress(InetAddress address){
		this.address = address;
	}

	public void close() throws IOException {
		connection.close();
	}
}