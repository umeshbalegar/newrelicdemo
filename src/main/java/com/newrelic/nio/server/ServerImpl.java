package com.newrelic.nio.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.newrelic.nio.handlers.BatchTotalUpdateHandler;
import com.newrelic.nio.handlers.IEventHandler;
import com.newrelic.nio.handlers.NumberStorageHandler;

public class ServerImpl extends Server {

	protected static final String POISON_PILL = "terminate";
	IEventHandler<String> numberHandler;
	IEventHandler<Integer> batchTotalHandler;

	public ServerImpl(String address, int port) {
		super(address, port);
		numberHandler = new NumberStorageHandler();
		batchTotalHandler = new BatchTotalUpdateHandler();
	}

	@Override
	public void processBuffer(SelectionKey key, SocketChannel channel, int length) throws IOException {

		readBuffer.flip();
		byte[] data = new byte[1000];
		readBuffer.get(data, 0, length);
		String fromclient = new String(data, 0, length, "UTF-8");

		if (logger.isDebugEnabled()) {
			logger.debug("Received: " + fromclient);
		}

		String[] list = fromclient.split("\\r?\\n");
		boolean needsShutdown = false;

		// Counting only those values which are valid for Duplicate count and Total count match. 
		// Anything which does not have length 9 will be considered invalid as we are not storing them. 
		long validInputCount = 0;
		
		for (int i = 0 ; i < list.length ; i++) {
			String item = list[i];
			if (item.equals(POISON_PILL) && !((fromclient.indexOf(POISON_PILL)+POISON_PILL.length()) > fromclient.length()-1)) {
				needsShutdown = true;
				logger.fatal("Poision Value passed, Shutting down server and all connections");
				break;
			} else if (item.length() != 9 || !item.matches("\\d+")) {
				logger.error("Invalid data "+item+", closing connection");
				break;
			} else {
				int index = 0;
				while (index < item.length() && item.charAt(index) - '0' == 0) {
					index++;
				}
				validInputCount++;
				numberHandler.handleData(item);
			}
		}

		//Downsizing long to int, if this count goes beyond int size, then we could loose some bits.
		batchTotalHandler.handleData(Math.toIntExact(validInputCount)); 
		
		key.cancel();
		channel.socket().close();
		channel.close();

		if (needsShutdown) {
			this.shutDown();
		}
	}

	public static void main(String[] args) {
		ServerImpl server = new ServerImpl("localhost", 4000);
		server.start();
	}

	@Override
	public void cleanUpResourceOnShutDown() {
		if (batchTotalHandler != null) {
			batchTotalHandler.shutDown();
		}
		if (numberHandler != null) {
			numberHandler.shutDown();
		}
	}

}
