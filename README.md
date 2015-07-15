# storm-wiki
Analyze wiki stream using Storm, and present the result in a browser.


Project Setup 

This document describes the project setup requirements like pre-requisite, environment and instructions to run the project.

1.	Install Java
Download and install a JDK (Storm works with both Oracle and OpenJDK 6/7). For this setup I used JDK 7 from Oracle. I installed Java in: C:\Java\jdk1.7.0_45\

2.	Install Storm
Download and install Apache Storm from https://storm.apache.org/downloads.html. I installed storm in C:\Storm_Home\apache-storm-0.9.4

3.	Install Maven
Install Maven from http://maven.apache.org/download.cgi. I installed maven in folder C:\MAVEN_HOME\apache-maven-3.3.3

4.	Install Redis for Windows
Install Redis for windows from https://github.com/MSOpenTech/Redis. I installed Redis in folder C:\redis_home\Redis-64.2.8.19

5.	Install Apache ANT
Install the Apache ant build system from http://ant.apache.org/bindownload.cgi. I installed the apache ant in folder C:\apache-ant.

6.	Install Socket.IO – Client for Java
Install the socket.io client for Java from https://github.com/Gottox/socket.io-java-client. 
Create a folder from command prompt and change directory into that. Checkout and compile the project:
git clone git://github.com/Gottox/socket.io-java-client.git
cd socket.io-java-client
ant jar

7.	Install Socket.IO – Client for Java in Maven repository
Start a command prompt and issue following command 
mvn install:install-file -Dfile="Jar file path for socket.io client" -DgroupId=gottox -DartifactId=socket_io_client -Dversion=1.0.0 -Dpackaging=jar

8.	Configure Environment Variables
On Windows Storm requires the STORM_HOME and JAVA_HOME environment variables to be set, as well as some additions to the PATH variable:
JAVA_HOME = C:\Java\jdk1.7.0_45\
STORM_HOME = C:\Storm_Home\apache-storm-0.9.4
REDIS_HOME = C:\redis_home\Redis-64.2.8.19
ANT_HOME=C:\apache-ant\apache-ant-1.9.5
PATH Add:
%STORM_HOME%\bin;%JAVA_HOME%\bin; C:\MAVEN_HOME\apache-maven-3.3.3;%REDIS_HOME%;%ANT_HOME%\bin;


Building and running Wiki changes topology

1.	Building Storm project
Extract the code from the zip into a folder. I extracted the code to c:\repos\ wiki-recent-changes. Open a command prompt and change director into above folder. Then issue the following command to build the project using Maven build system.
       >mvn clean install

2.	Running storm topology
If the build is successful, then run the following command to execute the topology
➢	storm jar target\wiki-recent-changes-1.0-SNAPSHOT-jar-with-dependencies.jar emse.rts.summer_2015.wiki_changes.WikiRecentChangesTopology

3.	Start Redis server
Make sure that Redis_Home is set properly as described earlier. Then issue following command on the command prompt
        >redis-server
This will start the Redis server. Now the topology can send the change count to the channel for broadcast.


Presentation Side Setup

1. Install Node
https://nodejs.org/download/
Node comes with npm installed

2. Node components
Node components are already installed and are inside ClientSide/node_modules directory.
If node components are not there, run as root:
$ npm install [this installs npm components listed in package.json]

3. Bower components
Bower Components are already installed and are inside ClientSide/bower_components directory
If bower components are not there, run
$ npm install –g bower [this installs bower]
$ bower install   [this installs bower components, listed in bower.json]

Running Application Website

1. Run node server, in dir ClientSide
$ node server.js

2. Open application site
http://127.0.0.1:3000/
http://localhost:3000/
