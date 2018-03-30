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

import java.nio.ByteBuffer;

import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
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
			DOPESocket connection = new DOPESocket(PORT, address);
			System.out.println("Created connection.");

			byte[] linkBytes = link.getBytes("UTF-8");
			System.out.println("Sending link: " + new String(linkBytes) + " | size: " + linkBytes.length);

			DOPEPacket request = new DOPEPacket(Control.RQ_OP_CODE, linkBytes);
			connection.sendStopAndWait(request);

			DOPEPacket packet;
			ArrayList<DOPEPacket> packets = new ArrayList<DOPEPacket>();

			int len = 0;
			for (; (packet = connection.receiveStopAndWait()).getDataLength() == Control.MAX_SIZE_IPV4; len += packet.getDataLength()){
				packets.add(packet);
				sendAck(packet, connection);
			}

			packets.add(packet);
			len+= packet.getDataLength();
			sendAck(packet, connection);

			display(buffer(packets, len));

		} catch (Exception ex){
			ex.printStackTrace();
		} finally {
			if (connection != null) connection.close();
		}
	}

	private static byte[] buffer(ArrayList<DOPEPacket> packets, int len){
		ByteBuffer buffer = ByteBuffer.allocate(len);
		for (int i = 0; i < packets.size(); i++){
			buffer.put(packets.get(i).getData());
		}
		return buffer.array();
	}

	private static void sendAck(DOPEPacket packet, DOPESocket connection) throws IOException {
		DOPEPacket ack = new DOPEPacket(Control.ACK_OP_CODE, packet.getSequenceNumber());
		connection.sendStopAndWait(ack);
		System.out.println("Sent ack:\n" + ack);	
	}

	private static void display(byte[] bytes) throws IOException {
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
		JFrame frame = new JFrame();
		ImageIcon icon = new ImageIcon(img);
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setSize(1500, 1500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
