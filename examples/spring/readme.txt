Spring Example
===============

This example shows Seam/Spring integration. Currently it runs on JBoss AS 4.2
as a WAR. It is deployed using the command "ant jbosswar" and undeployed using
the command "ant jbosswar.undeploy".

JBoss AS 4.2 needs additional hibernate libraries, use ant target jboss42 for deploying to it.

JBoss AS 6 needs new hibernate search and hibernate-commons-annotations, and more source code enhancement due Hibernate Search and Lucene-Core
API changes. Therefore use ant target jboss6 for deploying to JBoss AS 6 M5 and later. For instance:

	ant -f build-jboss6.xml

Visit http://localhost:8080/jboss-seam-spring
