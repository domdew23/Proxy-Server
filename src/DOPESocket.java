import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/*
A packet less than 512 (Max bytes per packet) signals termination of transfer
If packet is dropped, intended recipiant times out and retransmit last packet (data or ack)
Sender keeps one packet on hand for retransmission (SaW)
Server sends data and recieves acks
Client sends acks and recieves data

Client sends request to read; Server responds with first packet of data
Ack packet will contain block number of data packet being acknowledged
Seq nums begin at 1
Each end of connection chooes TID (Transfer identifier) for itself - should be random
Each packet has associated with it the source TID and dest TID - handed to UDP as src and dest PORTS - used for remainder of transfer
*/
public class DOPESocket {
	
	private int PORT;
	private InetAddress ADDRESS;
	private DatagramSocket connection;
	private DatagramPacket[] packets;
	private char currentSeqNum;

	public DOPESocket(int port, InetAddress address){
		this.PORT = port;
		this.ADDRESS = address;
		this.connection = new DatagramSocket(PORT);
	}

	public DOPESocket(int port){
		this.PORT = port;
		this.connection = new DatagramSocket(PORT);
	}

	public void sendStopAndWait(DOPEPacket packet){
		byte[] bytes = packet.makePacket().getPacket();
		DatagramPacket dgPacket = new DatagramPacket(bytes, bytes.length, ADDRESS, PORT);
		connection.send(dgPacket);
	}

	public void sendSlidingWindow(DOPEPacket packet){

	}

	public DOPEPacket receiveStopAndWait(){
		byte[] packet = new byte[Control.MAX_PACKET_LENGTH];
		DatagramPacket dgPacket = new DatagramPacket(packet, packet.length);
		connection.receive(dgPacket);
		return dopePacket;
	}

	public void beginTransferStopAndWait(DOPEPacket requestPacket){
		/* send first data packet to client */
		byte[] bytes = Control.getImage(requestPacket);
		packets = Control.split(bytes);
		currentSeqNum = 1;
		sendStopAndWait(packets[currentSeqNum - 1]);
	}

	public void continueTransferServer(DOPEPacket ackPacket){
		if (Control.slidingWindow){

		} else {
			if (currentSeqNum == ackPacket.getSeqNum()){
				/* recieved next packet in the chain */
				currentSeqNum++;
				sendStopAndWait(packets[currentSeqNum - 1]);
			} else {
				/* recieved wrong packet in the chain */
				System.out.println("Received wrong packet in chain - should not happen for stop and wait");
				System.exit(0);
			}
		}
	}

	public void receiveSlidingWindow(DOPEPacket packet){

	}
}