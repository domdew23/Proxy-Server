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

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;

public class Client {
	private static final int PORT = 2703;
	private static final String HOST = "rho.cs.oswego.edu";//"129.3.20.24";
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

			System.out.println("Sending packet: " + new String(request.getPacket()) + " | size: " + request.getPacketLength());
			connection.sendStopAndWait(request);
			System.out.println("Sent request.");

			DOPEPacket packet;
			byte[] buffer = new byte[Control.MAX_SIZE_IPV4];
			byte[] bytes = new byte[0];
			
			while ((packet = connection.receiveStopAndWait()).getDataLength() > Control.MAX_SIZE_IPV4){
				byte[] tempBuffer = new byte[bytes.length + packet.getDataLength()];
				System.arraycopy(bytes, 0, tempBuffer, 0, bytes.length);
				System.arraycopy(buffer, 0, tempBuffer, bytes.length, packet.getDataLength());
				bytes = tempBuffer;
				DOPEPacket ack = new DOPEPacket(Control.ACK_OP_CODE, packet.getSequenceNumber());
				connection.sendStopAndWait(ack);
			}

			display(bytes);
		} catch (IOException ex){
			ex.printStackTrace();
		} finally {
			connection.close();
		}
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
