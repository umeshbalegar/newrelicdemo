package com.newrelic.nio.server;

/**
 * Enum class which has all the different states of the server.
 * @author umeshbalegar
 *
 */
public enum ServerStatus {
	INITIALIZING, 
	STARTED,
	RUNNING, 
	STOPPING, 
	STOPPED;
}
