package com.dom.dope;

import java.net.InetAddress;
import java.io.IOException;

public class DOPEClientSocket extends DOPESocket {
	
	private int RWS; /* Receive Window Size */
	private char LAP; /* SeqNum of Largest acceptable packet */
	private char LPF; /* SeqNum of Last Packet Received */

	public DOPEClientSocket(int port, InetAddress address) throws IOException {
		super(port, address);
	}
}