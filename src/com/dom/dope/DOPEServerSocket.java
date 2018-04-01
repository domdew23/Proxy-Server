package com.dom.dope;

import java.net.InetAddress;
import java.io.IOException;
import com.dom.util.Control;
import java.util.PriorityQueue;

public class DOPEServerSocket extends DOPESocket {
	
	private int SWS; /* Send window size (# of unacked packets) */
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
			this.SWS = 4;
			this.LAR = 0;
			this.LPS = 0;
			this.window = new PriorityQueue<DOPEPacket>(SWS);
			sendSlidingWindow();
		} else {
			send(packets[currentSeqNum - 1]);
		}
	}

	public void sendSlidingWindow() throws IOException {
		for (int i = 0; i < SWS; i++){
			DOPEPacket packet = packets[i + currentSeqNum - 1];
			send(packet);
			window.add(packet);
		}
	}

	public void continueTransfer(DOPEPacket ackPacket) throws IOException {
		if (Control.slidingWindow){

		} else {
			if (currentSeqNum == packets.length){
				addressSet = false;
				senderPort = -1;
			} else if (currentSeqNum == ackPacket.getSequenceNumber()){
				/* recieved next packet in the chain */
				currentSeqNum++;
				send(packets[currentSeqNum - 1]);
			}
		}
	}
}