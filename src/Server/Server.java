import java.io.IOException;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

public class Server {

	private static DOPESocket connection;
	private static final int PORT = 2703;

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
				connection.close();
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}
	}
}