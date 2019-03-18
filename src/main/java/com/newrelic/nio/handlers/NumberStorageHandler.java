package com.newrelic.nio.handlers;

/**
 * Important Handler to handle the numbers submitted by the clients. 
 * Key processing of that information by adding the unique number into the inmemory cache and also into the queue for writing into the number.log file. 
 */
import org.apache.log4j.Logger;

import com.newrelic.socket.service.InMemoryCacheService;
import com.newrelic.socket.service.MonitorQueueService;

public class NumberStorageHandler implements IEventHandler<String>{

	final static Logger logger = Logger.getLogger(NumberStorageHandler.class);
	private MonitorQueueService queue;
	private InMemoryCacheService cacheService;

	public NumberStorageHandler() {
		queue = MonitorQueueService.getInstance();
		cacheService = InMemoryCacheService.INSTANCE;
	}

	/**
	 * uses concurrentHashMaps computeIfAbsent functional method to do the atomic update to the queue and cache. 
	 */
	@Override
	public void handleData(String data) {
		cacheService.getMap().computeIfAbsent(data, val -> {
			try {
				queue.putEventInQueue(val);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
			return Integer.valueOf(1);
		});
	}
	
	/**
	 * Method to access queue from handler. 
	 * @return
	 */
	public MonitorQueueService getQueue() {
		return queue;
	}
	
	/**
	 * Method to access cache from handler. 
	 * @return
	 */
	public InMemoryCacheService getCache() {
		return cacheService;
	}

	/**
	 * Override shutdown method to clean up all the data. 
	 */
	@Override
	public void shutDown() {
		logger.info("Shutting Down "+this.getClass().getName());
		if(queue != null) {
			queue.cleanUp();
			queue = null;
		}
		if(cacheService != null) {
			cacheService.cleanUp();
			cacheService = null;			
		}
	}
}
