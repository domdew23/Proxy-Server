package com.dom.dope;

import java.util.Arrays;
import java.nio.ByteBuffer;

import com.dom.util.Control;

/* Dom's Original Protocol Extended */

public class DOPEPacket implements Comparable<DOPEPacket> {
	/*
		Opcodes:
		0 - Read Request
		1 - Data
		2 - Ack
	*/
	
	/* 1 byte for opcode, 2 bytes for sequence number, 1 byte for advertised window if sliding window is being used */
	public static final byte CHAR_SIZE = Character.SIZE / 8;
	public static final byte BYTE_SIZE = Byte.SIZE / 8;
	public static final int HEADER_LENGTH = BYTE_SIZE + CHAR_SIZE;

	private byte[] header;
	private byte[] data;
	private byte[] packet;
	private byte[] seqNumBytes;
	private byte[] opCodeBytes;

	private char seqNum;
	private byte opCode;
	private byte advertisedWindow;

	/* make a new data packet */
	public DOPEPacket(byte opCode, char seqNum, byte[] data){
		this.opCode = opCode;
		this.seqNum = seqNum;
		this.data = data;
		this.header = new byte[HEADER_LENGTH];
		this.packet = new byte[header.length + data.length];
		addHeader(opCode, seqNum);
		makeDataPacket();
	}

	/* make a new request packet */
	public DOPEPacket(byte opCode, byte[] data){
		this.opCode = opCode;
		this.seqNum = 0;
		this.data = data;
		this.header = new byte[HEADER_LENGTH];
		this.packet = new byte[header.length + data.length];
		addHeader(opCode);
		makeRequestPacket();
	}

	/* make a new stop and wait ack packet */
	public DOPEPacket(byte opCode, char seqNum){
		this.opCode = opCode;
		this.seqNum = seqNum;
		this.packet = new byte[HEADER_LENGTH];
		makeAckPacket();
	}

	/* make a new sliding window ack packet */
	public DOPEPacket(byte opCode, char seqNum, byte advertisedWindow){
		System.out.println("Making new ack packet.");
		this.opCode = opCode;
		this.seqNum = seqNum;
		this.advertisedWindow = advertisedWindow;
		this.packet = new byte[HEADER_LENGTH + BYTE_SIZE];
		makeAckPacket();
	}

	/* used by reciever to unwrap packet */	
	public DOPEPacket(byte[] packet){
		ByteBuffer buffer = ByteBuffer.wrap(packet);
		this.packet = packet;
		
		if (Control.slidingWindow)
			this.header = new byte[HEADER_LENGTH + BYTE_SIZE];
		else
			this.header = new byte[HEADER_LENGTH];

		this.data = new byte[packet.length - header.length];

		buffer.get(header);
		buffer.get(data);

		ByteBuffer headerBuffer = ByteBuffer.wrap(header);
		this.opCode = headerBuffer.get();
		this.seqNum = headerBuffer.getChar();
		
		if (Control.slidingWindow) 
			this.advertisedWindow = headerBuffer.get();
	}

	/* add header to data packets*/
	private void addHeader(byte opCode, char seqNum){
		ByteBuffer buffer = ByteBuffer.allocate(header.length);
		buffer.mark();
		buffer.put(opCode);
		buffer.putChar(seqNum).reset();
		buffer.get(header);
	}

	/* add header to request packets */
	private void addHeader(byte opCode){
		ByteBuffer buffer = ByteBuffer.allocate(header.length);
		buffer.mark();
		buffer.put(opCode);
		buffer.putChar(seqNum).reset();
		buffer.get(header);
	}

	private void makeDataPacket(){
		ByteBuffer buffer = ByteBuffer.allocate(packet.length);
		buffer.mark();
		buffer.put(header);
		buffer.put(data).reset();
		buffer.get(packet);
	}

	/* request packet is equivalent to a data packet with request being the data */
	private void makeRequestPacket(){
		makeDataPacket();
	}

	/* make ack packets for both stop and wait and sliding window */
	private void makeAckPacket(){
		ByteBuffer buffer = ByteBuffer.allocate(packet.length);
		buffer.mark();
		buffer.put(opCode);
		buffer.putChar(seqNum);

		if (Control.slidingWindow)
			buffer.put(advertisedWindow);

		buffer.reset();
		buffer.get(packet);
	}

	public byte[] getPacket(){
		return packet;
	}

	public int getPacketLength(){
		return packet.length;
	}

	public int getDataLength(){
		return (packet.length - header.length);
	}

	public char getSequenceNumber(){
		return seqNum;
	}

	public byte getOpcode(){
		return opCode;
	}

	public byte getAdvertisedWindow(){
		return advertisedWindow;
	}

	public byte[] getData(){
		return data;
	}

	public byte[] getHeader(){
		return header;
	}

	/* packets ordered based on smallest sequence number */
	public int compareTo(DOPEPacket other){
		if (other.seqNum == this.seqNum) return 0;
		if (other.seqNum < this.seqNum) return 1;
		return -1;
	}

	public String toString(){
		return "Packet:\nOpcode: " + opCode + "\nSequence Number: " + (int) seqNum;
	}
}
