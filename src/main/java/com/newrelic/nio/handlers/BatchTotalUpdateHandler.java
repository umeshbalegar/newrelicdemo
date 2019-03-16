package com.newrelic.nio.handlers;

import org.apache.log4j.Logger;

import com.newrelic.socket.service.MonitorQueueService;

public class BatchTotalUpdateHandler implements IEventHandler<Integer> {

	final static Logger logger = Logger.getLogger(BatchTotalUpdateHandler.class);
	private MonitorQueueService queue;
	
	public BatchTotalUpdateHandler() {
		queue = MonitorQueueService.getInstance();
	}
	
	public MonitorQueueService getQueue() {
		return queue;
	}

	@Override
	public void handleData(Integer total) {
		queue.updateBatchTotal(total);
	}

	@Override
	public void shutDown() {
		logger.info("Shutting Down "+this.getClass().getName());
		if(queue != null) {
			queue.cleanUp();
			queue = null;
		}
	}

}
