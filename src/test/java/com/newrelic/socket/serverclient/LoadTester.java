package com.newrelic.socket.serverclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import com.newrelic.nio.client.NIOClient;
import com.newrelic.nio.server.Server;
import com.newrelic.nio.server.ServerImpl;

import junit.framework.TestCase;

public class LoadTester  extends TestCase {
	
	final static Logger logger = Logger.getLogger(Server.class);
	private String host = "localhost";
	private int port = 4000;
	private BufferedReader in = null;
	
	@Test
	public void testLoad() {
		try {
			//Start Server
			Server serv = new ServerImpl(host, port);
			serv.start();

			long start = System.currentTimeMillis();
			
			for(int number=0; number<2000000; number=number+3200) {
		    	NIOClient client = new NIOClient("localhost", 4000);
		    	String input = TestUtils.generateZeroPaddedNumbers(number);
		    	client.sendServer(input);		
			}
			
			long end = System.currentTimeMillis();
			System.out.println("Time take for 2M records : "+(end - start));	
			
			Thread.sleep(4000);
			
			serv.shutDown();	
			
			File file = new File(System.getProperty("user.home")+"/numbers.log");
			in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			int count = 1;
			while(line != null) {
				line = in.readLine();
				if(line != null && !line.trim().equals("")) {
					count++;	
				}
			}

			boolean lessThan10s = (end - start) < 10000 ? true : false;
			assertEquals("Time taken for 2M numbers is < 10s ? ", true, lessThan10s);
			
			boolean numOfRecords = count > 1900000 ? true : false;
			assertEquals("The expected and file should match : ", true, numOfRecords);			
			
		}catch(InterruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@After
	public void teardown() throws IOException {
		if (in != null) {
			in.close();
		}
		in = null;
	}

}
