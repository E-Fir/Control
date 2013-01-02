package com.illposed.osc;

import java.net.*;
import java.io.IOException;
import java.io.InputStreamReader;

import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import com.illposed.osc.utility.OSCPacketDispatcher;

/**
 * OSCTCPPortIn is TCP server for receive OSC messages.
 * Based on OSCPortIn by Chandrasekhar Ramakrishnan 
 */
public class OSCTCPPortIn extends OSCPort implements Runnable {
	protected boolean isListening;
	protected OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
	protected OSCPacketDispatcher dispatcher = new OSCPacketDispatcher();

	private ServerSocket serverSocket;
	private InputStreamReader reader;
	
	//-----------------------------------------------------------------------------------
	
	public OSCTCPPortIn(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		this.port = port;
	}

	private int readPacketSize() throws IOException {
		return reader.read() + (reader.read() << 8) + (reader.read() << 16) + (reader.read() << 24);
	}
	
	public void run() {
		try {
			Socket socket = serverSocket.accept();
			reader = new InputStreamReader(socket.getInputStream());
			
			try {
				while(true) {
					int packetSize = readPacketSize();
					char[] charBuffer = new char[packetSize];
					reader.read(charBuffer);

					byte[] buffer = new byte[packetSize];
					for(int i = 0; i < packetSize; i++) {
						buffer[i] = (byte) charBuffer[i];
					}
					OSCPacket oscPacket = converter.convert(buffer, packetSize);
					dispatcher.dispatchPacket(oscPacket);
					
				}
			} catch (IOException e) {
			}
			
			reader.close();
			socket.close();
		} catch (IOException e) {
			
		}
	}
	
	public void startListening() {
		isListening = true;
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void stopListening() {
		isListening = false;
	}
	
	public boolean isListening() {
		return isListening;
	}
	
	public void addListener(String anAddress, OSCListener listener) {
		dispatcher.addListener(anAddress, listener);
	}
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}
}
