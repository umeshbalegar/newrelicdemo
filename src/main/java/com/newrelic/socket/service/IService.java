package com.newrelic.socket.service;

/**
 * Service interface which all services have to implement. 
 * has one implementable method to clean up all the resources set by the server implementation class. 
 * @author umeshbalegar
 *
 */
public interface IService {
	public void cleanUp();
}
