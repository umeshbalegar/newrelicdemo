package com.newrelic.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.newrelic.nio.handlers.IEventHandler;
import com.newrelic.nio.handlers.NumberStorageHandler;
import com.newrelic.nio.server.util.ServerUtil;
import com.newrelic.socket.service.ReportService;

public class NIOServer implements Runnable {

	
	final static Logger logger = Logger.getLogger(NIOServer.class);

	private static final int MAX_CONNECTIONS = 5;
	private InetSocketAddress listenAddress;
	public AtomicInteger clients;
	ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	private ServerSocketChannel serverChannel;
	private Selector selector;

	private Map<SocketChannel, byte[]> dataTracking;

	private static final String POISON_PILL = "POISON_PILL";

	private IEventHandler eHandler;
	
	private ScheduledExecutorService reportExecutor;
	

	public NIOServer(String address, int port) {
		listenAddress = new InetSocketAddress(address, port);
		dataTracking = new HashMap<SocketChannel, byte[]>();
		eHandler = new NumberStorageHandler();
		reportExecutor  = Executors.newScheduledThreadPool(1);
		clients = new AtomicInteger(0);
		init();
	}

//	@Override
	public void init() {
		logger.info("initializing server");

		if (selector != null)
			return;
		if (serverChannel != null)
			return;

		try {

			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(listenAddress);
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			logger.info("Now accepting connections...");
			
			reportExecutor.scheduleWithFixedDelay(new ReportService(), 10, 10, TimeUnit.SECONDS);

		} catch (IOException e) {
			e.printStackTrace();
			shutDown();
		}
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				
				int ready = clients.get() > MAX_CONNECTIONS ? 0 : selector.select();
				
				if (ready == 0) {
					logger.info("Max Currently connected with : "+clients+" clients.");
					continue;
				}

				logger.info("Currently connected with : "+clients+" clients.");
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					
					keys.remove();
					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						logger.info("Accepting connection");
						accept(key);
					}

					if (key.isWritable()) {
						logger.info("Writing...");

						write(key);
					}

					if (key.isReadable()) {
						logger.info("Reading from connection");
						read(key);
					}
				}
			}			
		}catch(IOException e) {
			logger.error("Main Server thread threw an exception : ");
		}finally {
			shutDown();
		}
	}

//	@Override
	public void write(SelectionKey key) throws IOException {

		SocketChannel channel = (SocketChannel) key.channel();
		byte[] data = dataTracking.get(channel);
		dataTracking.remove(channel);
		int count = channel.write(ByteBuffer.wrap(data));
		if (count == 0) {
			key.interestOps(SelectionKey.OP_WRITE);
			return;
		} else if (count > 0) {
			key.interestOps(0);
			key.interestOps(SelectionKey.OP_READ);
		}

	}

//	@Override
	public void accept(SelectionKey key) throws IOException {
		clients.getAndAdd(1);
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		if (socketChannel == null) {
			throw new IOException();
		}		

		socketChannel.configureBlocking(false);
			
		SelectionKey skey = socketChannel.register(selector, SelectionKey.OP_READ);

		byte[] hello = new String("Hello from server").getBytes();
		dataTracking.put(socketChannel, hello);

		try {
			write(skey);
		} catch (IOException e) {
			logger.error("Problem in initial hello from Server  " + e);
		}	
	}

//	@Override
	public void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		readBuffer.clear();
		int length;
		try {
			length = channel.read(readBuffer);

			if (length == -1) {
				logger.error("Nothing was there to be read, closing connection");
				channel.close();
				key.cancel();
				return;
			}

			readBuffer.flip();
			byte[] data = new byte[1000];
			readBuffer.get(data, 0, length);
			String fromclient = new String(data, 0, length, "UTF-8");
			
			if(logger.isDebugEnabled()) {
				logger.debug("Received: " + fromclient);
			}
			
			
			String [] list = fromclient.split("\\r?\\n");
			
			if (list.length == 0 ) {
				clients.getAndDecrement();
				logger.error("Invalid data, closing connection");
				key.cancel();
				channel.close();		
				return;
			}else if(list.length == 1 && list[0].equals(POISON_PILL)) {
				logger.fatal("Poision Value passed, Shutting down server and all connections");
				channel.close();
				key.cancel();
				this.shutDown();				
			}else {
				Arrays.stream(list)
				.filter(val -> !val.trim().equals(""))
				.filter(val -> val.length() == 9)
				.forEach(val -> {
					eHandler.handleData(val);
				});
				key.cancel();
				channel.close();
				clients.getAndDecrement();
				return;
			}


		} catch (IOException e) {
			clients.getAndDecrement();
			logger.error("Reading problem, closing connection", e);
			key.cancel();
			channel.close();
			return;
		}
	}

	public void send(SelectionKey key, byte[] data) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		dataTracking.put(socketChannel, data);
		try {
			write(key);
		} catch (IOException e) {
			logger.error("Problem sending acknowledgement " + e);
			e.printStackTrace();
		}
	}

//	@Override
	public void shutDown() {
		logger.info("Closing server down");
		ServerUtil.stop(reportExecutor);
		if (selector != null) {
			try {
				selector.close();
				serverChannel.socket().close();
				serverChannel.close();
				clients.getAndSet(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(eHandler != null) {
			eHandler.shutDown();
		}
		logger.info("All ShutDown");
		System.exit(0);
	}
}
