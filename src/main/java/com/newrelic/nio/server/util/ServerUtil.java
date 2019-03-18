package com.newrelic.nio.server.util;

/**
 * Utility class for server package. 
 * Common functions will be available for every class inside this package and beyond. 
 */
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public class ServerUtil {
	
	final static Logger logger = Logger.getLogger(ServerUtil.class);
	
	public static ExecutorService getStandardExecService(final String name, final int corePoolSize,
			final int maxPoolSize, BlockingQueue<Runnable> taskQueue, final int threadPriority) {
		
		ThreadPoolExecutor texecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 2 * 60, TimeUnit.SECONDS,
				taskQueue, new ThreadFactory() {

					final AtomicLong l = new AtomicLong(0);

					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, name + " - WorkerTask " + l.getAndIncrement());
						t.setPriority(threadPriority);
						t.setDaemon(true);
						return t;
					}
				});
		texecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {

			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				try {
					if (executor.isShutdown() || executor.isTerminated() || executor.isTerminating()) {
						return;
					}

					final long SLEEP_TIME = Math.round((Math.random() * 400D) + 1);
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (Throwable ignore) {
						if (logger.isDebugEnabled()) {
							ignore.printStackTrace();
						}
					}
					logger.error("RejectedExecutionHandler for " + name + " WorkerTask slept for " + SLEEP_TIME);
					executor.getQueue().put(r);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});

		texecutor.allowCoreThreadTimeOut(true);
		texecutor.prestartAllCoreThreads();

		return texecutor;
	}
	
	
	public static void closeSelector(Selector selector) {
        if (selector != null) {
            try {
                selector.close();
            } catch (Throwable ign) {
                if (logger.isDebugEnabled()) {
                    logger.error("Exceptions closing Selector '" + selector + "'. Cause: ", ign);
                }
            }
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
}
