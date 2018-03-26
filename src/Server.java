import java.io.IOException;

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
	private static DOPESocket connection;
	private static InetAddress address;
	private static InputStream input;
	private static OutputStream output;
	private static InetAddress senderAddress;
	private static int senderPort;
	private static final int PORT = 2703;
	private static final int HEADER_LENGTH = 6;
	private static final int MAX_SIZE = 2 ^ 16;// - HEADER_LENGTH;
	private static final long MAX_SIZEIPv6 = (2 ^ 32) - 1;// - HEADER_LENGTH;

	public static void main(String[] args) {
		try {
			connection = new DOPESocket(PORT);
			DOPEPacket packet;
			if (Control.slidingWindow) packet = null;
			else packet = connection.receiveStopAndWait();
            
			switch (packet.getOpcode()){
				/* read request packet */
				case 0: connection.beginTransferStopAndWait(packet); break;
				/* data packet */
				case 1: connection.continueTransferClient(packet); break;
				/* ack packet */
				case 2: connection.continueTransferServer(packet); break; 
			}

            senderAddress = request.getAddress();
            senderPort = request.getPort();

			byte[] chunk = dopePacket.makePacket().getPacket();

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