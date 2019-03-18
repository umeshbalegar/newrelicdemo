package com.newrelic.socket.serverclient;
/**
 * Comprehensive set of Unit Tests for the Server start and shutdown functions along with concurrent connections.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.newrelic.nio.client.NIOClient;
import com.newrelic.nio.server.Server;
import com.newrelic.nio.server.ServerImpl;
import com.newrelic.nio.server.ServerStatus;

import junit.framework.TestCase;

public class ServerTest extends TestCase{
	final static Logger logger = Logger.getLogger(Server.class);
	private String host = "localhost";
	private int port = 4000;
    
    @Test
	public void testStartStop(){
    	ServerImpl privateServ = null;
    	try {
    		privateServ = new ServerImpl( "localhost", 4000 );
			privateServ.start();
			assertEquals( "Server has started", ServerStatus.STARTED, privateServ.getStatus() );
			Thread.sleep(10);
			assertEquals( "Server has started", ServerStatus.RUNNING, privateServ.getStatus() );
			Thread.sleep(1000);
			privateServ.shutDown();
			Thread.sleep(10);
			assertEquals( "Server has stopped", ServerStatus.STOPPED, privateServ.getStatus() );
		}catch(Exception e) {
			e.printStackTrace();

		}catch (Throwable e) {
			if(privateServ != null) {
				privateServ.shutDown();		
			}
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConcurrentConnections() {
		
		ServerImpl privateServ = null;
		try {
			Thread.sleep(3000);
			
			privateServ = new ServerImpl(host, port);
			privateServ.start();

			int size = 12;
		    ExecutorService threads = Executors.newFixedThreadPool(size);
		    List<Callable<Boolean>> torun = new ArrayList<>(size);
		    for (int i = 0; i < size; i++) {
		        torun.add(new TestUtils.DoPing(i));
		    }
		    
		    List<Future<Boolean>> futures = threads.invokeAll(torun);

		    TestUtils.stop(threads);
		    
		    Thread.sleep(2000);
		    
		    
		    privateServ.shutDown();
		    
		    Thread.sleep(3000);
		    
		    // check the results of the tasks...throwing the first exception, if any.
		    for (Future<Boolean> fut : futures) {
		    	assertEquals("Should be true for all pings : ", Boolean.valueOf(fut.get()), Boolean.TRUE);
		    }
		    
		}catch(Exception e) {
			if(privateServ != null && privateServ.status.get() != ServerStatus.STOPPED) {
				privateServ.shutDown();			
			}			
			e.printStackTrace();
		}		
	}
	
	@Test
	public void testWithInvalidData() {
		ServerImpl privateServ = null;
		try {
			Thread.sleep(3000);
			
			privateServ = new ServerImpl(host, port);
			privateServ.start();

			int size = 2;
		    ExecutorService threads = Executors.newFixedThreadPool(size);
		    List<Callable<Boolean>> torun = new ArrayList<>(size);

		    torun.add(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
		        	NIOClient client = new NIOClient("localhost", 4000);
		        	//3 invalid entries 31415926, n00700700 and extra line at the end
		        	client.sendServer("31415926\n777777777\n00700700\n456000000\n600000078\n890000000\n");	
					return null;
				}
		    	
		    });
		    
		    List<Future<Boolean>> futures = threads.invokeAll(torun);

		    TestUtils.stop(threads);
		    
		    Thread.sleep(2000);
		    
		    
		    privateServ.shutDown();
		    
		    Thread.sleep(3000);
		    
		    // check the results of the tasks...throwing the first exception, if any.
		    for (Future<Boolean> fut : futures) {
		    	assertEquals("Should be true for all pings : ", Boolean.valueOf(fut.get()), Boolean.TRUE);
		    }
		    
		}catch(Exception e) {
			if(privateServ != null && privateServ.status.get() != ServerStatus.STOPPED) {
				privateServ.shutDown();			
			}			
			e.printStackTrace();
		}	
	}
}
