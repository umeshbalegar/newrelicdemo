package com.newrelic.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Client which can connec to NIO host at the given host and port. 
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
	private boolean waitForResponse = false;
	
	
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
		waitForResponse = waitForServer;
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
	
	
	private int send(String request) throws IOException {
		bb.flip();
		bb.clear();
		bb.put(request.getBytes());
		bb.flip();
		int x = sc.write(bb);
		return x;
	}
	
	private void readResponse() throws IOException {
		Selector selector = Selector.open();
		sc.register(selector, SelectionKey.OP_READ);		 
		while(true) {
			if(selector.select() > 0) {
				if(processResponse(selector)) {
					return;
				}
			}
		}
	}
	
	
	public void close(){
		try{
			sc.close();
		}catch( IOException e ){
			logger.error( "Failed to close SocketChannel in Connector.", e );
		}
	}
	
	
	public void sendServer(String request) {
		try {
			this.connect();

			//send request
			this.send(request);

			//process response
			if(waitForResponse) {
				this.readResponse();
			}
			
			this.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean processResponse(Selector s) {		
		Iterator<SelectionKey> i = s.selectedKeys().iterator();
		while(i.hasNext()) {
			try {
				SelectionKey sk = i.next();
				if (sk.isReadable()) {
					SocketChannel schannel = (SocketChannel) sk.channel();
					bb.flip();
					bb.clear();

					int count = schannel.read(bb);
					if (count > 0) {
						bb.rewind();
						String response = 
						Charset.forName("UTF-8").decode(bb).toString();
						System.out.println("response: "+response);
						
						schannel.close();
						return true;
					}
				}
				i.remove();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static void main(String [] args) {
		NIOClient client = new NIOClient("localhost", 4000);
		client.sendServer("POISON_PILL\n");
	}
}