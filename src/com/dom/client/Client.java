package com.dom.client;

import java.net.InetAddress;

import com.dom.dope.DOPEPacket;
import com.dom.dope.DOPEClientSocket;
import com.dom.util.Control;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.nio.ByteBuffer;

import java.util.ArrayList;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;

/*
Packets inside window have been transmitted but not acknowledge
Buffer - store packets that have been transmitted, discard packets that have been acked
Client neeeds to identify duplicate packets and dicard and identiy missing packets
Window size is number of packets yet to be acked
*/

public class Client {
	private static final int PORT = 2703;
	private static final String HOST = "pi.cs.oswego.edu";
	//private static final String link = "https://upload.wikimedia.org/wikipedia/commons/f/f1/Atlantis_overheadview_STS115.jpg";
	private static InetAddress address;
	private static DOPEClientSocket connection;
	private static PrintWriter fileWriter;

	public static void main(String[] args){
		String link = JOptionPane.showInputDialog("Enter a image link: ");
		Control.parseArgs(args);
		Control.isReceiver = true;

		try {
			address = InetAddress.getByName(HOST);
			connection = new DOPEClientSocket(PORT, address);

			ArrayList<DOPEPacket> packets = null;
			fileWriter = null;

			connection.send(new DOPEPacket(Control.RQ_OP_CODE, link.getBytes("UTF-8"))); /* send request for to server for a image given a link */
			long start = System.currentTimeMillis();

			if (Control.slidingWindow)
				packets = connection.slidingWindow();
			else
				packets = connection.stopAndWait();

			long time = (System.currentTimeMillis() - start)/1000; /* seconds */
			System.out.println("Took: {" + time + "} seconds");

			display(buffer(packets));

		} catch (IOException ex){
			ex.printStackTrace();
		} finally {
			try {
				if (connection != null) 
					connection.close();
				if (fileWriter != null)
					fileWriter.close();
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
		System.out.println("Len: " + bytes.length);

		int width, height;
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));

		if (img.getWidth() > 1000 || img.getHeight() > 1000){
			width=img.getWidth()/2;
			height=img.getHeight()/2;
		} else {
			width=img.getWidth();
			height=img.getHeight();
		}

		JFrame frame = new JFrame();
		ImageIcon icon = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setSize(width, height);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static PrintWriter createWriter(PrintWriter fileWriter, String fileName) throws IOException{
		PrintWriter writer = fileWriter;
		if (writer == null){
			writer = new PrintWriter(new File("csv/" + fileName + ".csv"));
			writer.write("Time:\n");
		}
		return writer;
	}
}
