#
 # JBoss, Home of Professional Open Source
 # Copyright 2008, Red Hat Middleware LLC, and individual contributors
 # by the @authors tag. See the copyright.txt in the distribution for a
 # full listing of individual contributors.
 #
 # This is free software; you can redistribute it and/or modify it
 # under the terms of the GNU Lesser General Public License as
 # published by the Free Software Foundation; either version 2.1 of
 # the License, or (at your option) any later version.
 #
 # This software is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 # Lesser General Public License for more details.
 #
 # You should have received a copy of the GNU Lesser General Public
 # License along with this software; if not, write to the Free
 # Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 # 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 # 
 
Core functional test framework for Seam.

UNDER DEVELOPMENT NOT COMPLETE

How To:
----------
* Build seam from $SEAM_HOME directory to update maven repository for current snapshot
* Follow specific instructions for your OS
* Change to the $SEAM_HOME/src/test/ftest directory
* Set jboss*.home properties in ftest.properties to point to your application server locations
* Set tomcat.home in ftest.properties to be able to run tests on plain Tomcat
* Set jboss-embedded.home in ftest.properties to be able to run tests on Tomcat (with JBoss Embedded)
* Check if testng jar file was downloaded - its location should be $SEAM_HOME/lib/ (if not, tests will miss some classes and fail)
  - if there is no testng jar file, change to the $SEAM_HOME directory and run "ant copyseamdependencies"

  To run all the functional tests run:
  * "ant testall" for JBoss AS 5
  * "ant testall.jboss4" for JBoss AS 4.2
  * "ant testall.jboss-embedded" for Tomcat + JBoss Embedded
  * "ant testall.tomcat6" for plain Tomcat6
  To run functional tests for single example run:
  * "ant test -Dtest=example_name" for JBoss AS 5
  * "ant test.jboss4 -Dtest=example_name" for JBoss AS 4.2
  * "ant test.jboss-embedded -Dtest=example_name" for Tomcat + JBoss Embedded
  * "ant test.tomcat6 -Dtest=example_name" for Tomcat6
  
How To Test Cluster Environment:
--------------------------------
* Currently there is one test for cluster environment - booking. The main goal of this test is
* to simulate recovery from breakdown. Two instances of JBoss AS are being used. First part of 
* the test is executed at first (master) instance. Then the first instance is killed and a second (slave)
* instance takes over executing of the application. 
* This test should be executed autonomously (not as a part of test bundle).

* Prior to executing of this test it is needed to start both JBoss AS instances manually. 
* For example (assuming you have created second "all" configuration ("all2")):
* JBOSS_HOME/bin/run.sh -c all -g DocsPartition -u 239.255.101.101 -b localhost -Djboss.messaging.ServerPeerID=1 
* -Djboss.service.binding.set=ports-default
* JBOSS_HOME/bin/run.sh -c all2 -g DocsPartition -u 239.255.101.101 -b localhost -Djboss.messaging.ServerPeerID=2
* -Djboss.service.binding.set=ports-01
* The configuration "all" is considered to be master jboss instance (related to 
* jboss.service.binding.set=ports-default) and the application is deployed to server/all/farm 
* directory at "jboss5.home" location specified in ftest.properties
 
To run cluster test for booking example:
ant test -Dtest=booking -Dcluster=true

Known Limitations:
---------------------
* Only tested on Firefox and IE
* Container must be started (preferably with nothing deployed)

Windows Setup
--------------
* Running testsuite on Firefox
	* Install firefox browser
	* Set selenium.browser property to *firefox or *firefoxproxy in ftest.properties
* Running testsuite on Internet Explorer
	* Set selenium.browser=*iexploreproxy in ftest.properties
	* Set selenium.server.cmd.args= -singleWindow in ftest.properties - some tests are behaving unexpectedly in multiwindow mode on IE
	* Set security and privacy levels to lowest possible and turn off pop-up blocker in Internet options -> Security -> Custom level


Unix/Linux Setup
-----------------
* You must set to the location of your firefox browser like this:
   export LD_LIBRARY_PATH=/jboss/projects/seam/automation/firefox:$LD_LIBRARY_PATH
   export PATH=/jboss/projects/seam/automation/firefox:$PATH
Otherwise, you get the message:
   Error: com.thoughtworks.selenium.SeleniumException: ERROR Server Exception: sessionId should not be null; has this session been started yet?

Mac OS Setup
--------------
TBD

KNOWN TEST FAILURES
-------------------
blog - all tests fail if you don't delete blogindexes folder from application server prior running testsuite
seambay - testEmptyRegistration - JBSEAM-3893
wicket - simpleBookingTest, testJBSEAM3288 - JBSEAM-3818
icefaces - several tests fail on IE

TODO's:
-------
* There are several TODO's in the source code
* Write up detailed instructions for adding tests, containers, etc...
* Add the ability to download,extract,started, stop, remove containers (jboss 4.2.X, JBoss 5, tomcat 6)
* Consolidate the test reports
* Expose more options to users for tweaking
* headless env and selenium RC integration for CI 
* Test and update for more Browsers, Operating Systems, and Containers
* set up a project file for these tests, or update the existing?
* Describe how to debug the tests using eclipse
  - start server, sel server, and in eclipse set props in ftest.prop file and testng plugin
* We currently have 3 required jars in the $SEAM_HOME/src/test/ftest/lib directory
  - When the build system is updated these will be removed and dependencies will
    be handled as the rest of the source is.
  - We are trying to keep the ftest builds as separate from seam builds as possible.
  - FYI selenium versions are "1.0-beta-2" and testng.jar is 5.8-200803291025

OPEN QUESTIONS:
-----------------
* I'm not sure I like the package name for the common example test code
* I don't like how we are using property files for all the variables
  - Using property files for examples with common test classes and interface/class constants for unique examples
  - Is there any way to get rid of property files?
  	- For now, we are using property files for testcases that share test class and constants in Java code for examples that are unique
