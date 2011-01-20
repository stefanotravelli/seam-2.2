Seam Remoting/Chatroom Example
==============================

This example shows using Seam Remoting to subscribe and publish messages to JMS. 
It runs on JBoss AS as an EAR and Tomcat with Embedded JBoss as a WAR.

NOTE:
JBoss AS 6 has got new default JMS provider Hornetq, which requires different JMS configuration.
JBoss AS 5 can have also Hornetq instead of default JBoss Messaging,
therefore use JVM variable -Dhornetq=yes for deploying the configuration for Hornetq.

For instance JBoss AS 6 with Hornetq:
$ ant -Dhornetq=yes

Otherwise without JVM variable it copies JBoss Messsaging configuration:
$ ant

example.name=chatroom
