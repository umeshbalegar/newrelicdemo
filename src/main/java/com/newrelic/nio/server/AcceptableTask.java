package com.newrelic.nio.server;

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
			s.setTcpNoDelay( true );
				
			socketChannel.register(selector, SelectionKey.OP_READ);
						
		}catch(IOException e) {
			ServerUtil.closeSelector(selector);
		}
		
	}
}
