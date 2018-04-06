package com.dom.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import com.dom.dope.DOPEPacket;

public class Control {

	/*
	A class that holds the settings for the application and contains some methods the Server relies on to grab an image,
	cache it, and split the up the image up into packets.
	*/

	public static final byte RQ_OP_CODE = 0;
	public static final byte DATA_OP_CODE = 1;
	public static final byte ACK_OP_CODE = 2;
	public static final byte WINDOW_SIZE = 4;

	public static final int MAX_SIZE_IPV4 = 512;
	public static final int MAX_PACKET_LENGTH = MAX_SIZE_IPV4 + DOPEPacket.HEADER_LENGTH;
	public static final double DROP_RATE = 0.01;
	public static int dataLength;

	public static boolean IPv4 = true;
	public static boolean slidingWindow = true;
	public static boolean dropPackets = false;
	public static boolean isReceiver = false;
	private static boolean cache = false;

	public Control(){
	}

	public static byte[] getImage(DOPEPacket packet) throws IOException {
		byte[] linkBytes = packet.getData();
		String link = new String(linkBytes).trim();
		String fileName = link.replace("/", "");
		System.out.println("Received link: " + link);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream input = null;

		if (inCache(fileName)){
			System.out.println("Getting image from cache.");
			input = new FileInputStream("cache/" + fileName);
			cache = false;
		} else{
			URL url = new URL(link);
			input = url.openStream();
			cache = true;
		}

		byte[] buffer = new byte[1024];
		int len = 0;

		while ((len = input.read(buffer)) != -1)
			out.write(buffer, 0, len);

		if (cache)
			cacheImage(out.toByteArray(), fileName);

		out.close();
		input.close();
		return out.toByteArray();
	}

	public static void cacheImage(byte[] bytes, String fileName) throws IOException {
		OutputStream output = new FileOutputStream("cache/" + fileName);
		output.write(bytes);
		output.close();
	}

	public static DOPEPacket[] split(byte[] bytes){
		int packetCount = 0;

		int max = MAX_SIZE_IPV4;
		packetCount = (int) Math.ceil((double) bytes.length / max);

		DOPEPacket[] packets = new DOPEPacket[packetCount];
		char seqNum = 1;
		for (int offset = 0,bytesLeft=bytes.length; (int) seqNum < packetCount + 1; offset+=max, seqNum++){
			byte[] data;
			if (bytesLeft <= max){
				/* last packet */
				data = new byte[bytesLeft];
				data = Arrays.copyOfRange(bytes, offset, offset + bytesLeft);
				bytesLeft = 0;
			} else {
				/* not last packet */
				data = new byte[max];
				data = Arrays.copyOfRange(bytes, offset, offset + max);
				bytesLeft -= max;
			}
			DOPEPacket dopePacket = new DOPEPacket(DATA_OP_CODE, seqNum, data);
			packets[seqNum - 1] = dopePacket;
		}
		System.out.println("Split into " + packets.length + " packets.");
		return packets;
	}

	private static boolean inCache(String link) throws IOException {
		if (new File("cache", link).exists())
			return true;
		return false;
	}

	public static void parseArgs(String[] args){
		for (int i = 0; i < args.length; i++){
			switch(args[i]){
				case "-sw": slidingWindow = false; break;
				case "-d": dropPackets = true; break;
				default: System.out.println("Invalid args."); System.exit(0);
			}
		}
	}
}
