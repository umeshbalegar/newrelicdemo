package com.newrelic.nio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.newrelic.nio.client.NIOClient;
import com.newrelic.nio.server.Server;
import com.newrelic.nio.server.ServerImpl;

/**
 * Demo Class which will start the server and also post sample input.
 * 
 * By Default the server keeps running 
 * If you want it to be stopped at the end, add in a parameter "autoshutdown" when running the file.  
 *
 */
public class AppStarter {
	static class DoPing implements Callable<Boolean>{
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
	
	public static void main(String [] args) throws InterruptedException, ExecutionException {
		
		boolean killServer = false;
		if (args.length > 0 && args[0].equals("autoshutdown")){
			killServer = true;
		}
		
		//Start Server
		Server serv = new ServerImpl("localhost", 4000);
		serv.start();

	
		
		int size = 10;
	    ExecutorService threads = Executors.newFixedThreadPool(size);
	    List<Callable<Boolean>> torun = new ArrayList<>(size);
	    for (int i = 0; i < size; i++) {
	        torun.add(new DoPing(i));
	    }
	    
	    // all tasks executed in different threads, at 'once'.
	    List<Future<Boolean>> futures = threads.invokeAll(torun);

	    // no more need for the threadpool
	    threads.shutdown();

	    // check the results of the tasks...throwing the first exception, if any.
	    for (Future<Boolean> fut : futures) {
	        fut.get();
	    }
	    
	    
	    if(killServer) {
		    //Sending only POISON_PILL
		    ExecutorService threads1 = Executors.newFixedThreadPool(1);
		    List<Callable<Boolean>> torun1 = new ArrayList<>(1);
		    torun1.add(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					NIOClient client = new NIOClient("localhost", 4000);
					client.sendServer("POISON_PILL");
					return null;
				}
		    	
		    });
		    List<Future<Boolean>> futures1 = threads1.invokeAll(torun1);
		    threads1.shutdown();
		    for(Future<Boolean> fut : futures1) {
		    	fut.get();
		    }	    	
	    }
	}
}
