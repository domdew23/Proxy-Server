import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class Control {
	
	public static final byte RQ_OP_CODE = 0;
	public static final byte DATA_OP_CODE = 1;
	public static final byte ACK_OP_CODE = 2;

	public static final int MAX_SIZE_IPV4 = 2 ^ 16;
	public static final long MAX_SIZE_IPV6 = (2 ^ 32) - 1;
	public static final int MAX_PACKET_LENGTH = MAX_SIZE_IPV4 - DOPEPacket.HEADER_LENGTH;
	public static boolean IPv4 = true;
	public static boolean slidingWindow = false;
	public static boolean dropPackets = false;

	public Control(){

	}

	public static byte[] getImage(DOPEPacket packet){
		byte[] linkBytes = packet.getData();
		String link = new String(linkBytes);

		try {
			URL url = new URL(link);
			InputStream input = url.openStream();
			byte[] bytes = bufferImage(input);
			input.close();
			return bytes;
		} catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] bufferImage(InputStream input){
		byte[] buffer = new byte[2048];
		byte[] bytes = new byte[0];

		int len = 0;
		int count = 0;
		
		try {
			while ((len = input.read(buffer)) != -1){
				byte[] tempBuffer = new byte[bytes.length + len];
				System.arraycopy(bytes, 0, tempBuffer, 0, bytes.length);
				System.arraycopy(buffer, 0, tempBuffer, bytes.length, len);
				bytes = tempBuffer;
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		return bytes;	
	}

	public static void cacheImage(byte[] bytes){
		try {
			OutputStream output = new FileOutputStream("cache/image.png");
			output.write(bytes);
			output.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static DOPEPacket[] split(byte[] bytes){
		int packetCount = 0;
		long max = 0;

		if (IPv4) {
			packetCount = (int) Math.ceil((double) bytes.length / MAX_SIZE_IPV4);
			max = (long) MAX_SIZE_IPV4;
		} else {
			packetCount = (int) Math.ceil((double) bytes.length / MAX_SIZE_IPV6); 
			max = MAX_SIZE_IPV6;
		}

		DOPEPacket[] packets = new DOPEPacket[packetCount];
		char seqNum = 1;
		for (int i = 0; i < packetCount; i+=max, seqNum++){
			byte[] data;
			if (bytes.length - max >= max){
				data = Arrays.copyOfRange(bytes, i, (int) max);
			} else {
				data = Arrays.copyOfRange(bytes, i, bytes.length);
			}
			DOPEPacket dopePacket = new DOPEPacket(DATA_OP_CODE, seqNum, data);
			packets[seqNum - 1] = dopePacket;
		}
		return packets;
	}
}