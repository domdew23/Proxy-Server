package com.dom.dope;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import java.io.IOException;

import com.dom.util.Control;

import java.util.Arrays;
import java.util.Random;
import java.util.PriorityQueue;

/* Dom's Original Protocol Extended */

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
	
	protected int port, senderPort=-1;
	protected InetAddress address;
	protected char currentSeqNum;
	protected boolean addressSet;
	protected PriorityQueue<DOPEPacket> window;
	protected DatagramSocket connection;

	public DOPESocket(int port, InetAddress address) throws IOException {
		/* new client socket */
		this.port = port;
		this.address = address;
		this.addressSet = true;
		this.connection = new DatagramSocket();
	}

	public DOPESocket(int port) throws IOException {
		/* a new server socket */
		this.port = port;
		this.addressSet = false;
		this.connection = new DatagramSocket(port);
		this.connection.setSoTimeout(0);
	}

	public void send(DOPEPacket packet) throws IOException {
		connection.send(makePacket(packet));
	}

	public DOPEPacket receive() throws IOException {
		byte[] buffer = new byte[Control.MAX_PACKET_LENGTH];
		DatagramPacket dgPacket = new DatagramPacket(buffer, buffer.length);
		connection.receive(dgPacket);
		byte[] bytes = Arrays.copyOfRange(buffer, 0, dgPacket.getLength());
				
		if (!addressSet) 
			setSenderData(dgPacket.getAddress(), dgPacket.getPort());
		
		return (new DOPEPacket(bytes));
	}

	private DatagramPacket makePacket(DOPEPacket packet) throws IOException {
		byte[] bytes = packet.getPacket();	
		DatagramPacket dgPacket;
		
		if (senderPort != -1) 
			dgPacket = new DatagramPacket(bytes, bytes.length, address, senderPort);
		else 
			dgPacket = new DatagramPacket(bytes, bytes.length, address, port);
		
		return dgPacket;
	}

	protected void simulateDrop() throws SocketTimeoutException, InterruptedException {
		if (new Random().nextDouble() < Control.DROP_RATE && Control.dropPackets){
			Thread.sleep(2000);
			throw new SocketTimeoutException();
		}
	}

	public void setSenderData(InetAddress address, int port){
		this.address = address;
		this.senderPort = port;
		this.addressSet = true;
	}

	public void resetSenderData(){
		this.address = null;
		this.senderPort = -1;
		this.addressSet = false;
	}

	public void close() throws IOException {
		connection.close();
	}
}
