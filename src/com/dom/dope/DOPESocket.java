package com.dom.dope;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import java.io.IOException;

import com.dom.util.Control;

import java.util.Arrays;
import java.util.PriorityQueue;
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
	
	private int port, senderPort=-1;
	private InetAddress address;
	private DatagramSocket connection;
	private DOPEPacket[] packets;
	private char currentSeqNum;
	private boolean addressSet;
	private PriorityQueue<DOPEPacket> window;

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
		DatagramPacket dgPacket = makePacket(packet);
		connection.send(dgPacket);
		System.out.println("Packet sent.");
	}

	public void sendSlidingWindow(){
		for (int i = 0; i < 4; i++){
			DOPEPacket packet = packets[i + currentSeqNum - 1];
			DatagramPacket dgPacket = makePacket(packet);
			connection.send(dgPacket);

			window.add(packet);
		}
		
	}

	private DatagramPacket makePacket(DOPEPacket packet){
		byte[] bytes = packet.getPacket();	
		DatagramPacket dgPacket;
		
		if (senderPort != -1) dgPacket = new DatagramPacket(bytes, bytes.length, address, senderPort);
		else dgPacket = new DatagramPacket(bytes, bytes.length, address, port);
		
		return dgPacket;
	}

	public DOPEPacket receive() throws IOException {
		byte[] buffer = new byte[Control.MAX_PACKET_LENGTH];
		DatagramPacket dgPacket = new DatagramPacket(buffer, buffer.length);
		connection.receive(dgPacket);

		byte[] packet = Arrays.copyOfRange(buffer, 0, dgPacket.getLength());
		
		System.out.println("Received packet of size: " + dgPacket.getLength());
		if (!addressSet) {
			address = dgPacket.getAddress();
			senderPort = dgPacket.getPort();
			addressSet = true;
		}
		return (new DOPEPacket(packet));
	}

	public void beginTransfer(DOPEPacket requestPacket) throws IOException {
		/* send first data packet to client */
		byte[] bytes = Control.getImage(requestPacket);
		packets = Control.split(bytes);
		currentSeqNum = 1;
		
		if (Control.slidingWindow) this.window = new PriorityQueue<DOPEPacket>(4); sendSlidingWindow();
		else sendStopAndWait(packets[currentSeqNum - 1]);
		
	}

	public void continueTransfer(DOPEPacket ackPacket) throws IOException {
		if (Control.slidingWindow){

		} else {
			if (currentSeqNum == packets.length){
				addressSet = false;
				senderPort = -1;
				return;
			}
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

	public void setAddress(InetAddress address){
		this.address = address;
	}

	public void close() throws IOException {
		connection.close();
	}
}
