import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Server {

	private static DOPESocket connection;
	private static InetAddress address;
	private static InetAddress senderAddress;
	private static int senderPort;
	private static final int PORT = 2703;
	private static final int HEADER_LENGTH = 6;
	private static final int MAX_SIZE = 2 ^ 16;// - HEADER_LENGTH;
	private static final long MAX_SIZEIPv6 = (2 ^ 32) - 1;// - HEADER_LENGTH;
	private static boolean set = false;

	public static void main(String[] args) {
		try {
			connection = new DOPESocket(PORT);
			for (;;){
				DOPEPacket packet;
				if (Control.slidingWindow) packet = null;
				else packet = connection.receiveStopAndWait();
	            
				switch (packet.getOpcode()){
					/* read request packet */
					case 0: connection.beginTransferStopAndWait(packet); break;
					/* ack packet */
					case 2: connection.continueTransferServer(packet); break; 
				}
			}
		} catch (IOException ex){
			ex.printStackTrace();
		} finally {
			try {
				if (connection != null) connection.close();
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}
	}
}