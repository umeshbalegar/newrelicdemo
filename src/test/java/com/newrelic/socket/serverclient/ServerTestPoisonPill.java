package com.newrelic.socket.serverclient;
/**
 * Comprehensive set of Unit Tests for the function to kill the server when Poison pill message is sent
 * "terminate" is the poisonpill message. 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import com.newrelic.nio.client.NIOClient;
import com.newrelic.nio.server.Server;
import com.newrelic.nio.server.ServerImpl;
import com.newrelic.nio.server.ServerStatus;

import junit.framework.TestCase;

public class ServerTestPoisonPill extends TestCase {
	final static Logger logger = Logger.getLogger(Server.class);
	private String host = "localhost";
	private int port = 4000;
	private BufferedReader in = null;

    
	@Test
	public void testPoisonPill() {
		
		ServerImpl privateServ = null;
		try {
			Thread.sleep(3000);
			
			privateServ = new ServerImpl(host, port);
			privateServ.start();

		    ExecutorService threads = Executors.newFixedThreadPool(2);
		    List<Callable<Boolean>> torun = new ArrayList<>();
		    
		    torun.add(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					NIOClient client = new NIOClient("localhost", 4000);
					client.sendServer("314159261\n777777777\n007007001\n456000000\n600000078\n890000001\nterminate");
					return true;
				}
		    	
		    });
		    
		    List<Future<Boolean>> futures = threads.invokeAll(torun);
		    
		    Thread.sleep(3000);
		    
		    // check the results of the tasks...throwing the first exception, if any.
		    for (Future<Boolean> fut : futures) {
		    	fut.get();
		    	assertEquals("Should be true for all pings : ", Boolean.valueOf(fut.get()), Boolean.TRUE);
		    	assertEquals("Should not terminate as there is no newline character", ServerStatus.RUNNING, privateServ.getStatus());
		    }
		    
		    
		    List<Callable<Boolean>> torun2 = new ArrayList<>();
		    
		    torun2.add(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					NIOClient client = new NIOClient("localhost", 4000);
					client.sendServer("314159261\n777777777\n007007001\n456000000\n600000078\n890000001\nterminate\n");
					return true;
				}
		    	
		    });
		    
		    List<Future<Boolean>> futures2 = threads.invokeAll(torun2);
		    
		    for (Future<Boolean> fut : futures2) {
		    	fut.get();
		    	assertEquals("Should be true for all pings : ", Boolean.valueOf(fut.get()), Boolean.TRUE);
		    }		    
		    
		    TestUtils.stop(threads);
		    
		    Thread.sleep(3000);
		    
		    assertEquals("Should terminate as there is newline character", ServerStatus.STOPPED, privateServ.getStatus());
		    
		}catch(Exception e) {		
			e.printStackTrace();
		}		
	}
	
	@Test
	public void testEntriesInFile() throws IOException {
		File file = new File(System.getProperty("user.home")+"/numbers.log");
		in = new BufferedReader(new FileReader(file));
		int expectedCountInCache = 6;
		String line = in.readLine();
		int count = 1;
		while(line != null) {
			line = in.readLine();
			if(line != null && !line.trim().equals("")) {
				count++;	
			}
		}

		assertEquals("The expected and file should match : ", expectedCountInCache, count);
		
	}
	
	@After
	public void teardown() throws IOException {
		if (in != null) {
			in.close();
		}
		in = null;
	}
}
