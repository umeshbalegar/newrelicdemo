package com.newrelic.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.newrelic.nio.server.util.ServerUtil;
import com.newrelic.socket.service.ReportService;

public abstract class Server implements Runnable, IBaseProcess{
	
	final static Logger logger = Logger.getLogger(Server.class);

	Thread thread;
	
	public AtomicReference<ServerStatus> status = new AtomicReference<>(ServerStatus.STOPPED);
	
	protected final int defaultBufferSize = 32768;
	
	private InetSocketAddress listenAddress;
	
	public AtomicInteger clients;
	
	protected ByteBuffer readBuffer;
	
	protected ServerSocketChannel serverChannel;
	
	protected Selector selector;

	protected Map<SocketChannel, ByteBuffer> dataTracking;
	
	private ScheduledExecutorService reportExecutor;
	
	private ExecutorService serverExecutor;
	
	private ExecutorService acceptExecutor;
	

	/**
	 * Constructor which sets up all the defaults and assigns all the executor services. 
	 * @param address
	 * @param port
	 */
	public Server(String address, int port) {
		listenAddress = new InetSocketAddress(address, port);
		dataTracking = new HashMap<SocketChannel, ByteBuffer>();
		reportExecutor  = Executors.newScheduledThreadPool(1);
		clients = new AtomicInteger(0);
		readBuffer = ByteBuffer.allocate(defaultBufferSize);
		serverExecutor = Executors.newSingleThreadExecutor();
		acceptExecutor = ServerUtil.getStandardExecService("AcceptableTask", 5, 5, new ArrayBlockingQueue<Runnable>(65500), Thread.NORM_PRIORITY);
		init();
	}


	/**
	 * init() method overriden from BaseProcess Interface.
	 * Initilizing all the required resources
	 * - Selectors, serverChannel
	 * - Setting the nonblocking mode 
	 * - Then start listening to the port in the Accept mode.
	 * 
	 * 
	 * 
	 * called once from the constructor. 
	 */
	@Override
	public void init() {
		logger.info("initializing server");

		if (selector != null || serverChannel != null)
			return;

		try {
			status.getAndSet(ServerStatus.INITIALIZING);
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(listenAddress);
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);

			logger.info("Initilization Complete, Ready to Start..");
			

		} catch (IOException e) {
			e.printStackTrace();
			shutDown();
		}
	}

	/**
	 * The status variable tells you exactly what the internal state-machine of the server is currently
	 * doing (or trying to do)
	 * 
	 * @see ServerStatus
	 * 
	 * @return current status of the server
	 */
	public ServerStatus getStatus(){
		return status.get();
	}
	
	/**
	 * Starts the server in an executor service. 
	 */
	public void start(){
		serverExecutor.execute(this);
		status.getAndSet(ServerStatus.STARTED);
	}
	
	/**
	 * Main method, which 
	 */
	@Override
	public void run() {
		try {
			status.getAndSet(ServerStatus.RUNNING);
			logger.info("Starting Server...");
			
			//Sets up the Reporting Service 
			reportExecutor.scheduleWithFixedDelay(new ReportService(), 10, 10, TimeUnit.SECONDS);
			
			logger.info("Server started at http://"+listenAddress.getHostName()+":"+listenAddress.getPort());
			
			while (true) {
				int ready = selector.select();
				
				if (ready == 0) {
					continue;
				}


				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					
					keys.remove();
					if (!key.isValid()) {
						logger.info("Key is not valid ");
						continue;
					}

					if (key.isAcceptable()) {
						logger.info("Accepting connection");
						status.getAndSet(ServerStatus.RUNNING);
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
						SocketChannel socketChannel = serverSocketChannel.accept();	
						acceptExecutor.execute(new AcceptableTask(socketChannel, selector));
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

	/**
	 * Method which reads the data sent from the client, 
	 * After getting the buffer, it calls the abstract method which concerete implemenation has to provide the implmentation for. 
	 * @param key
	 * @throws IOException
	 */
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

			processBuffer(key, channel, length);
			
		} catch (IOException e) {
			logger.error("Reading problem, closing connection", e);
			key.cancel();
			channel.close();
		}
	}


	/**
	 * Shutsdown the server
	 * - all the threadpools
	 * - all the resources
	 */
	@Override
	public void shutDown() {
		logger.info("Closing server down");

		status.getAndSet(ServerStatus.STOPPING);
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
		cleanUpResourceOnShutDown();
		ServerUtil.stop(acceptExecutor);
		ServerUtil.stop(reportExecutor);
		ServerUtil.stop(serverExecutor);
		status.getAndSet(ServerStatus.STOPPED);
		logger.info("All ShutDown");
	}
	
	/**
	 * Abstract method to parse the data and do as required. 
	 * @param key
	 * @param channel
	 * @param length
	 * @throws IOException
	 */
	public abstract void processBuffer(SelectionKey key, SocketChannel channel , int length) throws IOException;
	
	/**
	 * Hook for all the Core Implementation classes to clean up their resources. 
	 */
	public abstract void cleanUpResourceOnShutDown();
	
}