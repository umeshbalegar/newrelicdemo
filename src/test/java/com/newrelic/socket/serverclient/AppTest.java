package com.newrelic.socket.serverclient;

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
})

public class AppTest {}
