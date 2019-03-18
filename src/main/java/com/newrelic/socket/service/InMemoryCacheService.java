package com.newrelic.socket.service;

/**
 * Class which implements the inmemory cache for the application
 * The concurrent hashmap will store all the unique numbers which has been processed so far. 
 */
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public enum InMemoryCacheService implements IService{
	
	INSTANCE;
	
	final static Logger logger = Logger.getLogger(InMemoryCacheService.class);
	
	private final ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<String, Integer>();
	
	private InMemoryCacheService() {}
	
	public Set<String> getNumberSet(){
		return cache.keySet();
	}
	
	public ConcurrentHashMap<String, Integer> getMap(){
		return cache;
	}

	@Override
	public void cleanUp() {
		logger.info("Cleaning Up "+this.getClass().getName());
		cache.clear();
	}
}
