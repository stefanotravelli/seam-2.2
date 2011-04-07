Seam Blog Example
====================
This example demonstrates the use of Seam in a Java EE 5 environment.
Transaction and persistence context management is handled by the
EJB container.

This example runs on JBoss AS as an EAR or Tomcat with JBoss Embedded as a WAR.

JBoss AS 4.2 needs additional Hibernate libraries, use ant target jboss42 for deploying to it.

JBoss AS 6 needs new Hibernate Search with dependencies, and more source code enhancement due to
Hibernate Search and Lucene-Core API changes.
Therefore use ant target jboss6 for deploying to JBoss AS 6 M5 and later. For instance:

	ant -f build-jboss6.xml

example.name=blog
