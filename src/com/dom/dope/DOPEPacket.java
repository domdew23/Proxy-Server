package com.dom.dope;

import java.nio.ByteBuffer;
import java.net.DatagramPacket;
import java.util.Arrays;

/* Dom's Original Protocol Extended */

public class DOPEPacket {
	/*
		Opcodes:
		0 - Read Request
		1 - Data
		2 - Ack
	*/
	
	/* 1 byte for opcode, 2 bytes for sequence number */
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

	/* make a new ack packet */
	public DOPEPacket(byte opCode, char seqNum){
		this.opCode = opCode;
		this.seqNum = seqNum;
		this.packet = new byte[HEADER_LENGTH];
		makeAckPacket();
	}

	/* used by reciever to unwrap packet */	
	public DOPEPacket(byte[] packet){
		//ByteBuffer buffer = ByteBuffer.wrap(packet);
		this.packet = packet;
		//this.header = buffer.get(packet, 0, HEADER_LENGTH).array();
		//this.data = buffer.get(packet, HEADER_LENGTH, packet.length - HEADER_LENGTH).array();
		this.header = Arrays.copyOfRange(packet, 0, HEADER_LENGTH);
		this.data = Arrays.copyOfRange(packet, HEADER_LENGTH, packet.length);
		this.opCodeBytes = Arrays.copyOfRange(header, 0, BYTE_SIZE);
		this.seqNumBytes = Arrays.copyOfRange(header, BYTE_SIZE, header.length);

		System.out.println("Data len: " + data.length);

		//ByteBuffer.wrap(header).get(opCodeBytes, 0, BYTE_SIZE).array();
		//ByteBuffer.wrap(header).get(seqNumBytes, BYTE_SIZE, CHAR_SIZE).array();
		this.seqNum = ByteBuffer.wrap(seqNumBytes).getChar();
		this.opCode = opCodeBytes[0];//ByteBuffeir.wrap(opCodeBytes).get();
	}

	/* add header to data packets*/
	public void addHeader(byte opCode, char seqNum){
		ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE + CHAR_SIZE);
		buffer.put(opCode);
		buffer.putChar(seqNum);

		buffer.get(header, 0, BYTE_SIZE);
		buffer.get(header, BYTE_SIZE, CHAR_SIZE);
		//this.seqNumBytes = ByteBuffer.allocate(CHAR_SIZE).putChar(seqNum).array();
		//this.opCodeBytes = ByteBuffer.allocate(BYTE_SIZE).put(opCode).array();
		//System.arraycopy(seqNumBytes, 0, header, 0, seqNumBytes.length);
		//System.arraycopy(opCodeBytes, 0, header, seqNumBytes.length, opCodeBytes.length);
	}

	/* add header to request packets */
	public void addHeader(byte opCode){
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
		buffer.mark();
		buffer.put(opCode);
		buffer.putChar(seqNum).reset();
		buffer.get(header);
	}

	public void makeDataPacket(){
		System.arraycopy(header, 0, packet, 0, header.length);
		System.arraycopy(data, 0, packet, header.length, data.length);
	}

	public void makeRequestPacket(){
		makeDataPacket();
	}

	public void makeAckPacket(){
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
		buffer.mark();
		buffer.put(opCode);
		buffer.putChar(seqNum).reset();
		buffer.get(packet);
	}

	public byte[] getPacket(){
		return packet;
	}

	public int getPacketLength(){
		return packet.length;
	}

	public int getDataLength(){
		return (packet.length - HEADER_LENGTH);
	}

	public char getSequenceNumber(){
		return seqNum;
	}

	public byte getOpcode(){
		return opCode;
	}

	public byte[] getData(){
		return data;
	}

	public byte[] getHeader(){
		return header;
	}

	public String toString(){
		return "Packet:\nOpcode: " + opCode + "\nSequence Number: " + (int) seqNum;
	}
}