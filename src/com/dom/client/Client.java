package com.dom.client;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

import com.dom.dope.DOPEPacket;
import com.dom.dope.DOPESocket;
import com.dom.util.Control;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;

public class Client {
	private static final int PORT = 2703;
	private static final String HOST = "pi.cs.oswego.edu";
	private static final String link = "http://www.smashbros.com/images/og/link.jpg";
	private static InetAddress address;
	private static DatagramSocket connection;
	
	public static void main(String[] args){
		try {
			address = InetAddress.getByName(HOST);
			//address = InetAddress.getLocalHost();

			DOPESocket connection = new DOPESocket(PORT, address);
			System.out.println("Created connection.");

			byte[] linkBytes = link.getBytes("UTF-8");
			System.out.println("Sending link: " + new String(linkBytes) + " | size: " + linkBytes.length);

			DOPEPacket request = new DOPEPacket(Control.RQ_OP_CODE, linkBytes);
			connection.sendStopAndWait(request);

			DOPEPacket packet;
			byte[] buffer = new byte[Control.MAX_SIZE_IPV4];
			byte[] bytes = new byte[0];
			int i = 0;

			while ((packet = connection.receiveStopAndWait()).getDataLength() == Control.MAX_SIZE_IPV4){
				byte[] tempBuffer = loadBuffer(packet, bytes, buffer, i);		
				bytes = tempBuffer;
				sendAck(packet, connection);
			}

			byte[] tempBuffer = loadBuffer(packet, bytes, buffer, i);
			bytes = tempBuffer;
			System.out.println(bytes.length);
			sendAck(packet, connection);

			display(bytes);
		} catch (Exception ex){
			ex.printStackTrace();
		} finally {
			connection.close();
		}
	}

	private static byte[] loadBuffer(DOPEPacket packet, byte[] bytes, byte[] buffer, int i) {
		System.out.println("Recieved data packet: " + i++ + "\n" + packet);
		byte[] tempBuffer = new byte[bytes.length + packet.getDataLength()];
		System.arraycopy(bytes, 0, tempBuffer, 0, bytes.length);
		System.arraycopy(buffer, 0, tempBuffer, bytes.length, packet.getDataLength());
		return tempBuffer;
	}

	private static void sendAck(DOPEPacket packet, DOPESocket connection) throws IOException {
		DOPEPacket ack = new DOPEPacket(Control.ACK_OP_CODE, packet.getSequenceNumber());
		connection.sendStopAndWait(ack);
		System.out.println("Sent ack:\n" + ack);	
	}

	private static void display(byte[] bytes) throws IOException {
		OutputStream out = null;
		out = new FileOutputStream("img.jpg");
		out.write(bytes);
		out.close();
		
		/*ImageInputStream in = ImageIO.createImageInputStream(bytes);
		BufferedImage img = ImageIO.read(in);
		JFrame frame = new JFrame();
		ImageIcon icon = new ImageIcon(img);
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setSize(1500, 1500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
	}
}
