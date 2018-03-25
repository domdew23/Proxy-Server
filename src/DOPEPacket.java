import java.nio.ByteBuffer;
import java.net.DatagramPacket;

/* Dom's Original Protocol Extended */

public class DOPEPacket {
	
	/*2 bytes for seq num, 4 bytes for data len */
	private final int HEADER_LENGTH = Short.SIZE + Integer.SIZE;

	private byte[] header;
	private byte[] seqNumBytes;
	private byte[] lenBytes;
	private byte[] data;
	private byte[] packet;
	private short seqNum;
	private int length;

	public DOPEPacket(byte[] data, short seqNum, int length){
		/* used by sender to wrap packet */
		this.data = data;
		this.seqNum = seqNum;
		this.length = length;
		this.header = new byte[seqNum];
		this.packet = new byte[header.length + data.length];
		addHeader(seqNum, length);
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

	public void addHeader(short seqNum, int len){
		this.seqNumBytes = ByteBuffer.allocate(Short.SIZE).putShort(seqNum).array();
		this.lenBytes = ByteBuffer.allocate(Integer.SIZE).putInt(len).array();
		System.arraycopy(seqNumBytes, 0, header, 0, seqNumBytes.length);
		System.arraycopy(lenBytes, 0, header, seqNumBytes.length, lenBytes.length);
	}

	public DOPEPacket makePacket(){
		System.arraycopy(header, 0, packet, 0, header.length);
		System.arraycopy(data, 0, packet, header.length, data.length);
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

	public byte[] getData(){
		return data;
	}

	public byte[] getHeader(){
		return header;
	}
}