import java.nio.ByteBuffer;
import java.net.DatagramPacket;

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
		this.data = data;
		this.header = new byte[Byte.SIZE];
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
		ByteBuffer buffer = ByteBuffer.wrap(packet);
		this.packet = packet;
		this.header = buffer.get(packet, 0, HEADER_LENGTH).array();
		this.data = buffer.get(packet, HEADER_LENGTH, packet.length - HEADER_LENGTH).array();

		this.opCodeBytes = ByteBuffer.wrap(header).get(header, 0, BYTE_SIZE).array();
		this.seqNumBytes = ByteBuffer.wrap(header).get(header, Byte.SIZE, CHAR_SIZE).array();
		this.seqNum = ByteBuffer.wrap(seqNumBytes).getChar();
		this.opCode = ByteBuffer.wrap(opCodeBytes).get();
	}

	/* add header to data packets*/
	public void addHeader(byte opCode, char seqNum){
		this.seqNumBytes = ByteBuffer.allocate(CHAR_SIZE).putChar(seqNum).array();
		this.opCodeBytes = ByteBuffer.allocate(BYTE_SIZE).put(opCode).array();
		System.arraycopy(seqNumBytes, 0, header, 0, seqNumBytes.length);
		System.arraycopy(opCodeBytes, 0, header, seqNumBytes.length, opCodeBytes.length);
	}

	/* add header to request packets */
	public void addHeader(byte opCode){
		this.opCodeBytes = ByteBuffer.allocate(BYTE_SIZE).put(opCode).array();	
		System.arraycopy(opCodeBytes, 0, header, 0, opCodeBytes.length);
	}

	public void makeDataPacket(){
		System.arraycopy(header, 0, packet, 0, header.length);
		System.arraycopy(data, 0, packet, header.length, data.length);
	}

	public void makeRequestPacket(){
		makeDataPacket();
	}

	public void makeAckPacket(){
		this.seqNumBytes = ByteBuffer.allocate(CHAR_SIZE).putChar(seqNum).array();
		this.opCodeBytes = ByteBuffer.allocate(BYTE_SIZE).put(opCode).array();
		System.arraycopy(seqNumBytes, 0, packet, 0, seqNumBytes.length);
		System.arraycopy(opCodeBytes, 0, packet, seqNumBytes.length, opCodeBytes.length);
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
}