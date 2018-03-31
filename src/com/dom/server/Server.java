package com.dom.server;

import java.io.IOException;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

import com.dom.dope.DOPEPacket;
import com.dom.dope.DOPESocket;
import com.dom.util.Control;

public class Server {

	private static DOPESocket connection;
	private static final int PORT = 2703;

	public static void main(String[] args) {
		try {
			connection = new DOPESocket(PORT);
			for (;;){
				DOPEPacket packet = connection.receive();

				switch (packet.getOpcode()){
					/* read request packet */
					case 0: connection.beginTransfer(packet) break;
					/* ack packet */
					case 2: connection.continueTransfer(packet); break;
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
