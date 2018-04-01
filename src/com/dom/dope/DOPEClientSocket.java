package com.dom.dope;

import java.net.InetAddress;
import java.net.SocketTimeoutException;

import com.dom.util.Control;
import java.io.IOException;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Iterator;

public class DOPEClientSocket extends DOPESocket {
	
	private int RWS; /* Receive Window Size (# of out-of-order packets) */
	private char LAP; /* SeqNum of Largest acceptable packet */
	private char LPF; /* SeqNum of Last Packet Received */
	private char seqNumToAck; /* Largest SeqNum not yet acked - all packets with SeqNum <= SeqNumToAck have been received */
	private byte advertisedWindow; /* window size that receiver can currently accept */

	/*
	Sliding Window:
		If a packet is received out of order it will still be added to the buffer (window)
		Until there is a timeout receiver will keep accepting packets until the window is full
		Once the window is full the packets will be ordered by sequence number in the queue and transfer will continue
		If there is a timeout receiver will send advertised window back of how much space is left in the buffer (window)
		Sender will send a window size of specified advertised window and transfer will continue as usual once all packets in window are received
	*/

	public DOPEClientSocket(int port, InetAddress address) throws IOException {
		super(port, address);
	}

	public ArrayList<DOPEPacket> stopAndWait() throws IOException {
		int len = 0;
		DOPEPacket packet;
		ArrayList<DOPEPacket> packets = new ArrayList<DOPEPacket>();

		for (;;){
			try {
				packet = receive();
				len += packet.getDataLength();
				packets.add(packet);
				sendAck(packet);
				if (packet.getDataLength() < Control.MAX_SIZE_IPV4) break;
			} catch (SocketTimeoutException e){
				sendAck(packet);
			}
		}

		Control.dataLength = len;
		
		return packets;
	}

	public ArrayList<DOPEPacket> slidingWindow() throws IOException {
		this.RWS = 4;
		this.seqNumToAck = 0;
		this.LPR = seqNumToAck;
		this.LAP = LPR + RWS;
		this.window = new PriorityQueue<DOPEPacket>(RWS);

		int len = 0;
		DOPEPacket packet;
		ArrayList<DOPEPacket> packets = new ArrayList<DOPEPacket>();

		for (;;){
			try {
				packet = receive();
				if (packet.getSequenceNumber() <= LAP && packet.getSequenceNumber() > LPR && !contains(packet)){
					window.add(packet);
					len += packet.getDataLength();

					if (seqNumToAck + 1 == packet.getSequenceNumber()){
						/* received next packet in the chain */
						seqNumToAck = packet.getSequenceNumber();
					}

					if (seqNumToAck == LAP || window.size() == RWS){
						/* received all packets in the window - send ack/shift window */
						seqNumToAck = LAP;
						advertisedWindow = RWS;
						sendAck(seqNumToAck, advertisedWindow);
						shiftWindow(packets);
					}
				}
				if (packet.getDataLength() < Control.MAX_SIZE_IPV4) break;
			} catch (SocketTimeoutException ex){
				advertisedWindow = RWS - window.size();
				sendAck(seqNumToAck, advertisedWindow);
			}
		}

		Control.dataLength = len;

		return packets;
	}

	private void sendAck(DOPEPacket packet) throws IOException {
		/* send a stop and wait ack */
		DOPEPacket ack = new DOPEPacket(Control.ACK_OP_CODE, packet.getSequenceNumber());
		send(ack);
		System.out.println("Sent ack:\n" + ack);	
	}

	private void sendAck(char seqNum, byte advertisedWindow) throws IOException {
		/* send a sliding window ack */
		DOPEPacket ack = new DOPEPacket(Control.ACK_OP_CODE, seqNum, advertisedWindow);
		send(ack);
		System.out.println("Sent ack:\n" + ack);	
	}

	private void shiftWindow(ArrayList<DOPEPacket> packets){
		this.LPR = seqNumToAck;
		this.LAP = LPR + RWS;
		while (!window.isEmpty()){
			packets.add(window.poll());
		}
	}

	private boolean contains(DOPEPacket packet){
		/* check for duplicate packets */
		for (Iterator<DOPEPacket> it = window.iterator(); it.hasNext();){
			if (packet.compareTo(it.next) == 0){
				return true;
			}
		}
		return false;
	}
}