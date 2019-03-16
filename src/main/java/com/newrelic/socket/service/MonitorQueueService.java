package com.newrelic.socket.service;

/**
 * MonitorQueueService is a Singleton class 
 * - Puts the EventData in the BlockingQueue 
 * - Process the EventData and Writes it to the file.
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.newrelic.nio.server.util.ServerUtil;

public class MonitorQueueService implements IService{
	final static Logger logger = Logger.getLogger(MonitorQueueService.class);
	private static final String logFilePath = "numbers.log";
    private static MonitorQueueService instance = null;
    private static LinkedBlockingQueue<String> eventQueue = null;
    private ExecutorService writerService;
    private AtomicInteger batchTotal;
    
    
    private MonitorQueueService() {
    	initialize();
    }
    
    public static MonitorQueueService getInstance() {
        if (instance == null) {
            instance = new MonitorQueueService();
        }
        return instance;
    }
	
    /**
     * Initilizer method to set up the required resources for singleton, 
     * A queue is initilized to broker between receiving and writing into the file.  
     * 
     */
    private void initialize() {
        if (eventQueue == null) {
            eventQueue = new LinkedBlockingQueue<String> ();
            batchTotal = new AtomicInteger(0);
            writerService = Executors.newSingleThreadExecutor();
            writerService.execute(new EventWriter());
        }
    }
    
    /**
     * This method will be called when the request comes in with the data.
     * This method will add the data into the
     * - Check if these entries are already there in cache. If it is not then,  
     * - Stores in Internal Cache and
     * - Write to the file
     * - Otherwise just updates the count of the key in the cache.  
     * @param eventData
     * @throws InterruptedException 
     */
    public void putEventInQueue(String eventData) throws InterruptedException {
            eventQueue.put(eventData);
    }
    
    
    public void updateBatchTotal(int total) {
    	batchTotal.getAndAdd(total);
    }
    
    public void resetBatchTotal() {
    	batchTotal.getAndSet(0);
    }
    
    public int getBatchTotal() {
    	return batchTotal.get();
    }
    
    public int getQueuSize() {
    	return eventQueue.size();
    }
    
    /**
     * Private Inner class which processess the eventData and writes it to the log file
     * - Initilized at the time of our BlockingQueue's initialization. 
     * - This thread will never expire, helping enable continuous monitoring.
     * - A single EventProcessor thread will handle all requests for the BlockingQueue. 
     * @author umeshbalegar
     *
     */
    class EventWriter extends Thread{

    	@Override
        public void run() {
        	
        	try(PrintWriter pw = new PrintWriter(logFilePath);){
                while(true) {
                    String eventData = null;
                    eventData = eventQueue.take();
                    System.out.println("Process Event Data : Type : " + eventData);
        			pw.write(eventData + "\n");
        			pw.flush();
                }
        	} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

	@Override
	public void cleanUp() {
		logger.info("Cleaning Up "+this.getClass().getName());
		ServerUtil.stop(writerService);
	}
}
