package com.newrelic.socket.serverclient;

/**
 * if class is the main Test sutie class.
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Unit test for simple App.
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
   NumberStorageHandlerTest.class,
   MonitorQueueServiceTest.class,
   ServerTest.class,
   ServerTestPoisonPill.class,
   LoadTester.class
})

public class AppTest {}
