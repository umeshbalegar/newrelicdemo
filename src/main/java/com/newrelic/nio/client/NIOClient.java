package com.newrelic.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * Client which can connect to NIO host at the given host and port. 
 * 
 * @author umeshbalegar
 *
 */
public class NIOClient {
	final static Logger logger = Logger.getLogger(NIOClient.class);
	public static int defaultByteBufferSize = 32768;
	private int port;
	private String hostName;
	private ByteBuffer bb ;
	protected SocketChannel sc;
	Selector selector;
	
	
	//Constructors 
	public NIOClient(String host, int p) {
		hostName = host;
		port = p;
		bb = ByteBuffer.allocate(defaultByteBufferSize);
//		pingServer();
	}
	
	public NIOClient(String host, int p, boolean waitForServer) {
		hostName = host;
		port = p;
		bb = ByteBuffer.allocate(defaultByteBufferSize);
		pingServer();
	}
	
	private void pingServer() {
		sendServer("Hello");
	}
	
	public boolean isConnected() {
		if( sc != null )
			return sc.isConnected();
		else
			return false;
	}
	
	/**
	 * method to connect to the server.
	 * @throws IOException
	 */
	private void connect() throws IOException {
		sc = SocketChannel.open();
		sc.configureBlocking(false);

		InetSocketAddress addr = new InetSocketAddress(hostName, port);		 
		sc.connect(addr);			 

		while (!sc.finishConnect()) {
			try {
				Thread.sleep(10);	
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}	
	
	/**
	 * method which serializes the data and sends it to the server.
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private int send(String request) throws IOException {
		bb.flip();
		bb.clear();
		bb.put(request.getBytes());
		bb.flip();
		int x = sc.write(bb);
		return x;
	}

	/**
	 * Clena up all the resources.
	 */
	public void close(){
		try{
			sc.close();
		}catch( IOException e ){
			logger.error( "Failed to close SocketChannel in Connector.", e );
		}
	}
	
	/**
	 * Public method to send the data to the server.
	 * @param request
	 */
	public void sendServer(String request) {
		try {
			this.connect();

			//send request
			this.send(request);

			
			this.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}