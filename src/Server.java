import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import java.io.FileOutputStream;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Server {

	private static DatagramSocket connection;
	private static InetAddress address;
	private static InputStream input;
	private static OutputStream output;
	private static InetAddress senderAddress;
	private static int senderPort;
	private static final int PORT = 2703;
	private static final int HEADER_LENGTH = 6;
	private static final int MAX_SIZE = 64000 - HEADER_LENGTH;

	public static void main(String[] args) {
		try {
			connection = new DatagramSocket(PORT);

			byte[] linkBytes = new byte[43];
			DatagramPacket request = new DatagramPacket(linkBytes, linkBytes.length);
			connection.receive(request);
            senderAddress = request.getAddress();
            senderPort = request.getPort();

			System.out.println("got packet.");

			String link = new String(linkBytes);
			URL url = new URL(link);
			InputStream input = url.openStream();
			OutputStream output = new FileOutputStream("cache/image.png");

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
			output.write(bytes);

			int packetCount = (int) Math.ceil((double) bytes.length / MAX_SIZE);
			
			for (int i = 0, seq = 0; i < packetCount; i+=MAX_SIZE, seq++){
				byte[] data;
				if (bytes.length - MAX_SIZE >= MAX_SIZE){
					data = Arrays.copyOfRange(bytes, i, MAX_SIZE);
				} else {
					data = Arrays.copyOfRange(bytes, i, bytes.length);
				}

				DOPEPacket dopePacket = new DOPEPacket(data, seq, bytes.length);
				byte[] chunk = dopePacket.makePacket().getPacket();

				DatagramPacket packet  = new DatagramPacket(chunk, chunk.length, senderAddress, senderPort);
            	connection.send(packet);
			}
		} catch (IOException ex){
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) input.close();
				if (output != null) output.close();
				if (connection != null) connection.close();
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}
	}
}