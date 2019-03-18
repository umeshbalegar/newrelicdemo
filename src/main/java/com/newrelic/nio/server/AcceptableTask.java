package com.newrelic.nio.server;

/**
 * This class will handle the Accept operation in a separate thread pool. 
 * this class is created for meeting the criteria of having 5 concurrent threads at anytie to handle the incoming request. 
 * Acceptable task if for those operations where OP_ACCEPT is true. 
 * This class also sets the future query for OP_READ operations. 
 * setTcpNoDeal is used to control the amount of buffering used when transferring data. 
 * tries to send full data segments by waiting, if necessary, for enough writes to come through to fill up the segment. 
 * 
 */


import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.newrelic.nio.server.util.ServerUtil;

public class AcceptableTask implements Runnable{
    final static Logger logger = Logger.getLogger(AcceptableTask.class);
	
    final Selector selector;
	
    SocketChannel socketChannel;

    Socket s;
    
    public AcceptableTask(SocketChannel sc, final Selector selector) throws IOException {

        if (sc == null) {
            throw new NullPointerException("SocketChannel cannot be null in AcceptableTask");
        }

        if (sc.socket() == null) {
            throw new NullPointerException("Null Socket for SocketChannel in AcceptableTask");
        }

        this.socketChannel = sc;
        this.s = sc.socket();
        this.s.setTcpNoDelay(true);
        this.selector = selector;
    }

	@Override
	public void run() {
		try {
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);
						
		}catch(IOException e) {
			ServerUtil.closeSelector(selector);
		}
		
	}

}
