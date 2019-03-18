package com.newrelic.nio.server;
/**
 * Base interface which all the Base processes will inherit (like Server) which has 2 basic operations. 
 * 
 * - init - Initialize all the required resources. 
 * - shutdown - clean up all the initialized resources. 
 * 
 * @author umeshbalegar
 *
 */
public interface IBaseProcess {
	/**
	 * Initialized all the resources
	 */
	public void init();
	
	/**
	 * Shutdown all the resources.
	 */
	public void shutDown();
	
}
