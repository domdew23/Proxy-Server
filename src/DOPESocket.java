import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DOPESocket {
	
	private int PORT;
	private int ADDRESS;
	private DatagramSocket connection;

	public DOPESocket(int port, int address){
		this.PORT = port;
		this.ADDRESS = address;
		this.connection = new DatagramSocket(PORT);
	}

	public void send(DOPEPacket packet){
		byte[] bytes = packet.makePacket().getPacket();
		DatagramPacket dgPacket = new DatagramPacket(bytes, bytes.length, ADDRESS, PORT);
		connection.send(dgPacket);
	}

	public void receive(DOPEPacket packet){
		
	}
}