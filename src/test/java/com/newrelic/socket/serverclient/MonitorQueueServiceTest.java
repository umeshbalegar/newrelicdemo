package com.newrelic.socket.serverclient;

/**
 * Test class for the MonitorQueueService singleton
 * checks for its validity. 
 */
import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.newrelic.socket.service.MonitorQueueService;

import junit.framework.TestCase;

public class MonitorQueueServiceTest extends TestCase{

	private MonitorQueueService currObj;

	@Before
	public void setUp() {
		currObj = MonitorQueueService.getInstance();
	}

	@Test
	public void testInitilization() {
		try {
			assertEquals("BatchTotal is initilized : ", 0 , currObj.getBatchTotal());

			String home = System.getProperty("user.home")+"/numbers.log";
			
			File file = new File(home);
			assertEquals("File number.log created : ", file.exists(), true);
			
			long initialSize = file.length();
			
			currObj.putEventInQueue("test123");
			
			long afterSize = file.length();
			
			assertEquals("File should have the latest value inserted in the queue :", initialSize < afterSize, false);
			
			int x = currObj.getBatchTotal();
			
			assertEquals("Check get BatchTotal : ", 0, x);
			
			currObj.updateBatchTotal(10);
			
			assertEquals("Check update BatchTotal : ", 10, currObj.getBatchTotal());
			
			currObj.resetBatchTotal();
			
			assertEquals("Check reset BatchTotal : ", 0, currObj.getBatchTotal());			
		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@After
	public void tearDown() {
		currObj.cleanUp();
	}
	
}
