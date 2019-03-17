package com.newrelic.nio.handlers;

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

	@Override
	public void handleData(String data) {
		cacheService.getMap().computeIfAbsent(data, val -> {
			try {
				queue.putEventInQueue(val);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return Integer.valueOf(1);
		});
	}
	
	public MonitorQueueService getQueue() {
		return queue;
	}
	
	public InMemoryCacheService getCache() {
		return cacheService;
	}

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
