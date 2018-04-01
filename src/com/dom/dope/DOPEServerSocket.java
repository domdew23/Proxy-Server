package com.dom.dope;

import java.net.InetAddress;
import java.io.IOException;
import com.dom.util.Control;
import java.util.PriorityQueue;

public class DOPEServerSocket extends DOPESocket {
	
	private byte SWS; /* Send window size (# of unacked packets) */
	private char LAR; /* SeqNum of Last Ack Received */ 
	private char LPS; /* SeqNum of Last Packet Sent */

	public DOPEServerSocket(int port) throws IOException {
		super(port);
	}

	public void beginTransfer(DOPEPacket requestPacket) throws IOException {
		/* send first data packet to client */
		byte[] bytes = Control.getImage(requestPacket);
		packets = Control.split(bytes);
		currentSeqNum = 1;
		
		if (Control.slidingWindow) {
			System.out.println("Starting sliding window.");
			this.SWS = Control.WINDOW_SIZE;
			this.LAR = 0;
			this.LPS = 0;
			this.window = new PriorityQueue<DOPEPacket>(SWS);
			sendSlidingWindow(null);
		} else {
			send(packets[currentSeqNum - 1]);
		}
	}

	public void sendStopAndWait(DOPEPacket ackPacket) throws IOException {
		if (currentSeqNum == ackPacket.getSequenceNumber()){
			/* recieved next packet in the chain */
			currentSeqNum++;
			send(packets[currentSeqNum - 1]);
		}
	}

	public void sendSlidingWindow(DOPEPacket ackPacket) throws IOException {
		if (ackPacket != null){
			SWS = ackPacket.getAdvertisedWindow();
			LAR = ackPacket.getSequenceNumber();
			shiftWindow();
		}

		for (int i = LAR; i < SWS + LAR; i++){
			if (i == packets.length - 1){
				resetSenderData();
				return;
			}

			DOPEPacket packet = packets[i];
			send(packet);
			System.out.println("Sent packet: " + i);

			window.add(packet);
			LPS = packet.getSequenceNumber();
		}
	}

	public void continueTransfer(DOPEPacket ackPacket) throws IOException {
		if (currentSeqNum == packets.length) {
			/* done with transfer */
			resetSenderData();
			return;
		}

		if (Control.slidingWindow)
			sendSlidingWindow(ackPacket);
		else
			sendStopAndWait(ackPacket);	
	}

	private void shiftWindow(){
		for (;;){
			DOPEPacket packet = window.poll();
			if (packet.getSequenceNumber() == LAR){
				break;
			}
		}
	}
}
