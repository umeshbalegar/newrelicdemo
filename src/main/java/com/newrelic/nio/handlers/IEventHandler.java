package com.newrelic.nio.handlers;

public interface IEventHandler<T> {
	/**
	 * Method needs to be overrwirtten to handle the data
	 * @param data
	 */
	public void handleData(T data);

	
	/**
	 * Method needs to be overrwirtten to shutdown.
	 * 
	 */
	public void shutDown();
}
