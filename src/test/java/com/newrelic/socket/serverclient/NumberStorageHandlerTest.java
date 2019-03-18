package com.newrelic.socket.serverclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.newrelic.nio.handlers.NumberStorageHandler;

import junit.framework.TestCase;

@RunWith(Parameterized.class)
public class NumberStorageHandlerTest extends TestCase {
	@Parameters
	public static Collection<Object[][]> data() {

		List<String> values = new ArrayList<>();
		values.add("one");
		values.add("two");
		values.add("three");
		values.add("four");

		List<String> values1 = new ArrayList<>();
		values1.add("five");
		values1.add("six");
		values1.add("seven");
		values1.add("eight");
		values1.add("four");

		List<String> values2 = new ArrayList<>();
		values1.add("five");
		values1.add("six");

		return Arrays.asList(
				new Object[][][] { { { values, 4 }, { 4 } }, { { values1, 5 }, { 8 } }, { { values2, 2 }, { 8 } } });
	}

	private NumberStorageHandler eHandler;
	private List<String> items;
	private int expectedCountInCache;
	private BufferedReader in = null;

	public NumberStorageHandlerTest(Object[] i, Object[] expec) {
		eHandler = new NumberStorageHandler();
		items = (List<String>) i[0];
		expectedCountInCache = (int) expec[0];
	}

	@Before
	public void setUpEvenHandler() throws Exception {
		String home = System.getProperty("user.home")+"/numbers.log";
		File file = new File(home);
		in = new BufferedReader(new FileReader(file));
	}

	@Test
	public void testSingleTons() {
		NumberStorageHandler e1 = new NumberStorageHandler();
		NumberStorageHandler e2 = new NumberStorageHandler();
		
		assertEquals("Hash Codes for cache should match : ", e1.getCache().hashCode(), e2.getCache().hashCode());
		assertEquals("Hash Codes for MonitorQueue should match : ", e1.getQueue().hashCode(), e2.getQueue().hashCode());
	}
	
	@Test
	public void testhandleData() {
		for (String s : items) {
			eHandler.handleData(s);
		}
		assertEquals("The cache should have all the unique messages only : ", eHandler.getCache().getMap().size(),
				expectedCountInCache);
	}

	@Test
	public void testEntriesInFile() throws IOException {
		String line = in.readLine();
		int count = 1;
		while(line != null) {
			line = in.readLine();
			if(line != null && !line.trim().equals("")) {
				count++;	
			}
		}
		assertEquals("The cache and file should match : ", eHandler.getCache().getMap().size(), count);
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
