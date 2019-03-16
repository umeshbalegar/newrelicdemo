# Server Client NIO Demo

## This application demonstrates the Server - Client using Java NIO.

### Server starter file is ServerImpl.java in com.newrelic.nio.server package
This class is a concrete implementation of the server, which provides the implementation for processing the data and cleanup on shutdown methods. 


### com.newrelic.nio.server.Server.java 
Abstract class which pretty much sets up all the necessary resources for the Server to start. 
- Initilizes the NIO Selector and opens the ServerSocket for channels. 
- Sets up an executorservice for 5 concurrent connections (acceptexecutor). 
- Sets up an executorservice for ReportingService to log report every 10s.
- Provides 2 additional hooks for processing the data and clean up for the concrete class to implement. 


### Package com.newrelic.nio.handlers has all the EventHandlers
- NumberStorageHandler : Responsible for adding the incoming message into an internal cache and then adding this to the Queue.
- BatchTotalUpdateHandler : Responsible for appending the counts inbetween 10s intervals for ReportingService. (This value is ultimately used to get the duplicates, unique counts for each batch)


### Package com.newrelic.socket.service has all the Services
- InMemoryCacheService : Singleton responsible for holding all the numbers which have come to the server so far. Maintains Unique values. 
- MonitorQueueService : Singleton responsible for maintaining the Queue of incoming messages and also a writer to the file "numbers.log"
- ReportingService : Service responsible for Reporting the results so far every 10s.


### Package com.newrelic.nio.client 
- Has the Client to invoke the Server and transfer the numbers required.

### Package com.newrelic.nio.util 
- Has a util class which has a threadpool generator and stop utility functions. 

### Package com.newrelic.nio 
- Has the mainclass which will start both Server and client for the demo. 
You have the option to run in an autoshutdown mode. 



# Building Jar file
To buiid a jar use below command
```bash
mvn clean compile assembly:single
```

After the jar is built use below command to run in an autoshutdown mode.
```bash
java -jar target/serverclient-0.0.1-SNAPSHOT-jar-with-dependencies.jar autoshutdown
```

No Autoshutdown mode
```bash
java -jar target/serverclient-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```



# Building Docker image
Run the command 
```bash
docker build . -t tagName
```

This will creat the required dockercontainer to run. 

and then you can run this container using docker run command.

I have an optimized docker image created and uploaded in dockerhub, you can pull that image to start quick. [serverclient](https://cloud.docker.com/u/umeshbalegar/repository/docker/umeshbalegar/serverclient)
```bash
docker pull umeshbalegar/serverclient:demo

docker run umeshbalegar/serverclient:demo

OR (below is for autoshutdown feature)

docker run umeshbalegar/serverclient:demo autoshutdown
```

After running the container if you want to look at the "numbers.log" file, you can get into docker shell by executing below commands. 

```bash
docker ps
(this lists all the running containers)

docker exec -ti <containerId from above cmd> /bin/sh
(this gets you into the docker shell)

cat ~/numbers.log
(this prints the file on console)
```

All the required test files are in test folder. 



  


