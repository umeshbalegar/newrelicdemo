package com.newrelic.socket.service;

import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

/**
 * This task is generating the report and printing the report to the console.
 * This class uses cachehandler and queue service to compute the requried reporting data.
 * @author umesh
 *
 */
public class ReportService implements IService, Runnable {

	final static Logger logger = Logger.getLogger(ReportService.class);
	private Set<String> previousNumbers;
	private InMemoryCacheService cacheHandler;
	private MonitorQueueService queueService;

	public ReportService() {
		super();
		this.previousNumbers = Collections.emptySet();
		this.cacheHandler = InMemoryCacheService.INSTANCE;
		this.queueService = MonitorQueueService.getInstance();
	}

	@Override
	public void run() {

		Set<String> numbers = this.cacheHandler.getNumberSet();
		
		int totalInBatch = queueService.getBatchTotal();
		queueService.resetBatchTotal();
		
		int newlyAddedCount = Sets.difference(numbers, previousNumbers).size();
		int duplicateCount = totalInBatch - newlyAddedCount;

		int totalUnique = Sets.union(numbers, previousNumbers).size();

		this.previousNumbers = Sets.union(numbers, previousNumbers);

		String report = String.format("Received %d unique numbers, %d duplicates. Unique total: %d", newlyAddedCount,
				duplicateCount, totalUnique);

		logger.info(report);

	}

	@Override
	public void cleanUp() {
		logger.info("Cleaning Up "+this.getClass().getName());
		if(previousNumbers != null) {
			previousNumbers.clear();
		}
		if(cacheHandler != null) {
			cacheHandler.cleanUp();
			cacheHandler = null;
		}
		if(queueService != null) {
			queueService.cleanUp();
			queueService = null;
		}
	}

}
