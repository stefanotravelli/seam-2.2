Seam DVD Store Example
======================

This example demonstrates the use of Seam with jBPM pageflow and business
process management. It runs on JBoss AS as an EAR and Tomcat with Embedded
JBoss as a WAR.

JBoss AS 4.2 needs additional hibernate libraries, use ant target jboss42 for deploying to it.

JBoss AS 6 needs new hibernate search and hibernate-commons-annotations, and more source code enhancement due Hibernate Search and Lucene-Core
API changes. Therefore use ant target jboss6 for deploying to JBoss AS 6 M5 and later. For instance:
$ant jboss6 -Djboss6=yes

example.name=dvdstore
