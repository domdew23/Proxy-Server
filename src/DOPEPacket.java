import java.nio.ByteBuffer;
import java.net.DatagramPacket;

/* Dom's Original Protocol Extended */

public class DOPEPacket {
	
	/*2 bytes for seq num, 4 bytes for data len */
	private final int HEADER_LENGTH = Short.SIZE + Integer.SIZE;

	private byte[] header;
	private byte[] seqNumBytes;
	private byte[] lenBytes;
	private byte[] opCodeBytes;
	private byte[] data;
	private byte[] packet;
	private char seqNum;
	private int length;
	private byte opCode;

	public DOPEPacket(byte[] data, char seqNum, int length){
		/* used by sender to wrap packet */
		this.data = data;
		this.seqNum = seqNum;
		this.length = length;
		this.header = new byte[seqNum];
		this.packet = new byte[header.length + data.length];
		addHeader(seqNum, length);
		makePacket();
	}

	public DOPEPacket(byte[] data, int length){
		/* used by sender to wrap packet */
		this.data = data;
		this.seqNum = 0;
		this.length = length;
		this.header = new byte[seqNum];
		this.packet = new byte[header.length + data.length];
		addHeader(seqNum, length);
		makePacket();
	}

	public DOPEPacket(byte opCode, char seqNum, byte[] data){
		/* make a new data packet */
		this.opCode = opCode;
		this.seqNum = seqNum;
		this.data = data;
		this.header = new byte[Byte.SIZE + Char.SIZE];
		this.packet = new byte[header.length + data.length];
		addHeader(opCode, seqNum);
		makeDataPacket();
	}

	public DOPEPacket(byte opCode, byte[] data){
		/* make a new request packet */
		this.opCode = opCode;
		this.data = data;
		this.header = new byte[Byte.SIZE];
		this.packet = new byte[header.length + data.length];
		addHeader(opCode);
		makeRequestPacket();
	}

	public DOPEPacket(byte opCode, char seqNum){
		/* make a new ack packet */
		this.opCode = opCode;
		this.seqNum = seqNum;
		this.packet = new byte[Byte.SIZE + Char.SIZE];
		makeAckPacket();
	}

	public DOPEPacket(byte[] packet){
		/* used by reciever to unwrap packet */
		ByteBuffer buffer = ByteBuffer.wrap(packet);
		this.header = buffer.get(packet, 0, HEADER_LENGTH);
		this.data = buffer.get(packet, HEADER_LENGTH, packet.length);

		this.seqNumBytes = ByteBuffer.wrap(header).get(header, 0, Short.SIZE);
		this.lenBytes = ByteBuffer.wrap(header).get(header, Short.SIZE, header.length);
		this.seqNum = ByteBuffer.wrap(seqNumBytes).getShort();
		this.length = ByteBuffer.wrap(lenBytes).getInt();
	}

	public void addHeader(byte opCode, char seqNum){
		this.seqNumBytes = ByteBuffer.allocate(Char.SIZE).putShort(seqNum).array();
		this.opCodeBytes = ByteBuffer.allocate(Byte.SIZE).putByte(opCode).array();
		System.arraycopy(seqNumBytes, 0, header, 0, seqNumBytes.length);
		System.arraycopy(opCodeBytes, 0, header, seqNumBytes.length, opCodeBytes.length);
	}

	public void addHeader(byte opCode){
		this.opCodeBytes = ByteBuffer.allocate(Byte.SIZE).putByte(opCode).array();	
		System.arraycopy(opCodeBytes, 0, header, 0, opCodeBytes.length);
	}

	public DOPEPacket makeDataPacket(){
		System.arraycopy(header, 0, packet, 0, header.length);
		System.arraycopy(data, 0, packet, header.length, data.length);
	}

	public DOPEPacket makeRequestPacket(){
		makeDataPacket();
	}

	public DOPEPacket makeAckPacket(){
		this.seqNumBytes = ByteBuffer.allocate(Char.SIZE).putShort(seqNum).array();
		this.opCodeBytes = ByteBuffer.allocate(Byte.SIZE).putByte(opCode).array();
		System.arraycopy(seqNumBytes, 0, packet, 0, seqNumBytes.length);
		System.arraycopy(opCodeBytes, 0, packet, seqNumBytes.length, opCodeBytes.length);
	}

	public byte[] getPacket(){
		return packet;
	}

	public int getLength(){
		return length;
	}

	public short getSequenceNumber(){
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