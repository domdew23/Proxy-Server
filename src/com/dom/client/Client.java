package com.dom.client;

import java.net.InetAddress;

import com.dom.dope.DOPEPacket;
import com.dom.dope.DOPEClientSocket;
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

/*
Packets inside window have been transmitted but not acknowledge
Buffer - store packets that have been transmitted, discard packets that have been acked
Client neeeds to identify duplicate packets and dicard and identiy missing packets
Window size is number of packets yet to be acked
*/

public class Client {
	private static final int PORT = 2703;
	private static final String HOST = "pi.cs.oswego.edu";
	private static final String LINK = "http://www.smashbros.com/images/og/link.jpg";
	private static InetAddress address;
	private static DOPEClientSocket connection;
	
	public static void main(String[] args){
		long start = System.currentTimeMillis();
		Control.parseArgs(args);
		Control.isReceiver = true;

		try {
			address = InetAddress.getByName(HOST);
			connection = new DOPEClientSocket(PORT, address);
			connection.send(new DOPEPacket(Control.RQ_OP_CODE, LINK.getBytes("UTF-8"))); /* send request for to server for a image given a link */

			ArrayList<DOPEPacket> packets;

			if (Control.slidingWindow)
				packets = connection.slidingWindow();
			else
				packets = connection.stopAndWait();

			System.out.println("Took: {" + (System.currentTimeMillis() - start) + "}");
			display(buffer(packets));

		} catch (Exception ex){
			ex.printStackTrace();
		} finally {
			try {
				if (connection != null) 
					connection.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	private static byte[] buffer(ArrayList<DOPEPacket> packets){
		ByteBuffer buffer = ByteBuffer.allocate(Control.dataLength);
		
		for (int i = 0; i < packets.size(); i++)
			buffer.put(packets.get(i).getData());

		return buffer.array();
	}

	private static void display(byte[] bytes) throws IOException {
		JFrame frame = new JFrame();
		ImageIcon icon = new ImageIcon(ImageIO.read(new ByteArrayInputStream(bytes)));
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setSize(1500, 1500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
