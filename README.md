# newrelicdemo

# This application demonstrates the Server - Client using Java NIO.

Server starter file is ServerImpl.java in com.newrelic.nio.server package
This class is a concrete implementation of com.newrelic.nio.server.Server Abstract class, which pretty much sets up all the necessary resources for the Server to start. 
1: Initilizes the NIO Selector and opens the ServerSocket for channels. 
2: Sets up an executorservice for 5 concurrent connections (acceptexecutor). 
3: Sets up an executorservice for ReportingService to log report every 10s.
4: Provides 2 additional hooks for processing the data and clean up for the concrete class to implement. 


Package com.newrelic.nio.handlers has all the EventHandlers
- NumberStorageHandler : Responsible for adding the incoming message into an internal cache and then adding this to the Queue.
- BatchTotalUpdateHandler : Responsible for appending the counts inbetween 10s intervals for ReportingService. (This value is ultimately used to get the duplicates, unique counts for each batch)


Package com.newrelic.nio.service has all the Services
- InMemoryCacheService : Singleton responsible for holding all the numbers which have come to the server so far. Maintains Unique values. 
- MonitorQueueService : Singleton responsible for maintaining the Queue of incoming messages and also a writer to the file "numbers.log"
- ReportingService : Service responsible for Reporting the results so far every 10s.


Package com.newrelic.nio.client has the Client to invoke the Server and pass the number

Package com.newrelic.nio.util has a util class which has a threadpool generator and stop utility functions. 

Package com.newrelic.nio has the mainclass which will start both Server and client for the demo. You have the option to run in an autoshutdown mode. 



Building Jar file
To buiid a jar use below command
mvn clean compile assembly:single

After the jar is built use below command to run in an autoshutdown mode.
java -jar target/serverclient-0.0.1-SNAPSHOT-jar-with-dependencies.jar autoshutdown

No Autoshutdown mode
java -jar target/serverclient-0.0.1-SNAPSHOT-jar-with-dependencies.jar



Building Docker image
Run the command 
docker build . -t <tag>

This will creat the required dockercontainer to run. 

and then you can run this container using docker run command.


All the required test files are in test folder. 



  


