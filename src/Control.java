import java.io.InputStream;
import java.io.OutputStream;

public class Control {
	
	public static final int MAX_SIZE_IPv4 = 2 ^ 16;// - HEADER_LENGTH;
	public static final long MAX_SIZE_IPv6 = (2 ^ 32) - 1;// - HEADER_LENGTH;
	public static boolean IPv4 = true;
	public static boolean slidingWindow = false;
	public static boolean dropPackets = false;

	public Control(){

	}

	public static byte[] getImage(DOPEPacket packet){
		byte[] bytes;
		try {
			byte[] linkBytes = packet.getData();
			String link = new String(linkBytes);
			URL url = new URL(link);
			InputStream input = url.openStream();
			bytes = bufferImage(input);
			input.close();
		} catch (IOException e){
			e.printStackTrace();
		}
		return bytes;
	}

	private byte[] bufferImage(InputStream input){
		byte[] buffer = new byte[2048];
		byte[] bytes = new byte[0];

		int len = 0;
		int count = 0;
		while ((len = input.read(buffer)) != -1){
			byte[] tempBuffer = new byte[bytes.length + len];
			System.arraycopy(bytes, 0, tempBuffer, 0, bytes.length);
			System.arraycopy(buffer, 0, tempBuffer, bytes.length, len);
			bytes = tempBuffer;
		}	
	}

	public static void cacheImage(byte[] bytes){
		OutputStream output = new FileOutputStream("cache/image.png");
		output.write(bytes);
		output.close();
	}

	public static DOPEPacket[] split(byte[] bytes){
		int packetCount = 0;
		byte opCode = 1;

		if (IPv4) packetCount = (int) Math.ceil((double) bytes.length / MAX_SIZE_IPv4);
		else packetCount = (int) Math.ceil((double) bytes.length / MAX_SIZE_IPv6);
		
		DOPEPacket[] packets = new DOPEPacket[packetCount];
		char seqNum = 1;
		for (int i = 0; i < packetCount; i+=MAX_SIZE, seqNum++){
			byte[] data;
			if (bytes.length - MAX_SIZE >= MAX_SIZE){
				data = Arrays.copyOfRange(bytes, i, MAX_SIZE);
			} else {
				data = Arrays.copyOfRange(bytes, i, bytes.length);
			}
			DOPEPacket dopePacket = new DOPEPacket(opCode, seqNum, data);
			packets[seqNum - 1] = dopePacket;
		}
		return packets;
	}
}