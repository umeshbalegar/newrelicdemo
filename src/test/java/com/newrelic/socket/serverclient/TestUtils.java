package com.newrelic.socket.serverclient;

/**
 * Utility class used for testing situations.
 */
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.newrelic.nio.client.NIOClient;

public class TestUtils {
	public static class DoPing implements Callable<Boolean>{
	    private final int number;

	    public DoPing(int number) {
	        this.number = number;
	    }

	    public Boolean call() throws IOException {
        	NIOClient client = new NIOClient("localhost", 4000);
        	if(number < 10) {
        		client.sendServer("31415926"+number+"\n777777777\n00700700"+number+"\n456000000\n600000078\n89000000"+number);	
        	}
        	else {
        		client.sendServer("3141592"+number+"\n777777777\n0070070"+number+"\n456000000\n600000078\n8900000"+number);
        	}

            return Boolean.TRUE;
	    }
	}
	
    public static void stop(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
            System.err.println("termination interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }
    
	public static String generateZeroPaddedNumbers(int starting) {
		StringBuilder s = new StringBuilder();
		
		int i=starting;
		while(i < starting+3200) {
			if(i <= 9) {
				s.append("00000000"+i+"\n");
			}else if(i <= 99) {
				s.append("0000000"+i+"\n");
			}else if(i <= 999) {
				s.append("000000"+i+"\n");
			}else if(i <= 9999) {
				s.append("00000"+i+"\n");
			}else if(i <= 99999) {
				s.append("0000"+i+"\n");
			}else if(i <= 999999) {
				s.append("000"+i+"\n");
			}else if(i <= 9999999) {
				s.append("00"+i+"\n");
			}else if(i <= 99999999) {
				s.append("0"+i+"\n");
			}else {
				s.append(i+"\n");
			}
			i++;
		}
		return s.toString();
	}
}
