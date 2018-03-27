import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import java.io.IOException;

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
	
	private int port;
	private InetAddress address;
	private DatagramSocket connection;
	private DOPEPacket[] packets;
	private char currentSeqNum;
	private boolean addressSet;

	public DOPESocket(int port, InetAddress address) throws IOException {
		this.port = port;
		this.address = address;
		this.addressSet = true;
		this.connection = new DatagramSocket(port, address);
	}

	public DOPESocket(int port) throws IOException {
		this.port = port;
		this.addressSet = false;
		this.connection = new DatagramSocket(port);
	}

	public void sendStopAndWait(DOPEPacket packet) throws IOException {
		byte[] bytes = packet.getPacket();
		DatagramPacket dgPacket = new DatagramPacket(bytes, bytes.length, address, port);
		connection.send(dgPacket);
	}

	public void sendSlidingWindow(DOPEPacket packet){

	}

	public DOPEPacket receiveStopAndWait() throws IOException {
		byte[] packet = new byte[Control.MAX_PACKET_LENGTH];
		DatagramPacket dgPacket = new DatagramPacket(packet, packet.length);
		
		if (!addressSet) {
			address = dgPacket.getAddress();
			addressSet = true;
		}
		
		connection.receive(dgPacket);
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