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
 
Seam-gen functional testsuite
------------------------------------

UNDER DEVELOPMENT NOT COMPLETE

How to run the testsuite:
----------------------
To run seam-gen functional testsuite, please follow these steps:

1.) Export following script to MySQL database

CREATE TABLE `Person` (
  `username` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `birthdate` date default NULL,
  `address` varchar(600) NOT NULL,
  PRIMARY KEY  (`username`)
) ENGINE=InnoDB;

CREATE TABLE `Vehicle` (
  `ownerUsername` varchar(10) default NULL,
  `make` varchar(50) NOT NULL,
  `model` varchar(50) NOT NULL,
  `year` int(11) NOT NULL,
  `registration` varchar(8) NOT NULL,
  `state` varchar(2) NOT NULL,
  PRIMARY KEY USING BTREE (`registration`,`state`),
  KEY `ownerUsername` (`ownerUsername`),
  CONSTRAINT `owner` FOREIGN KEY (`ownerUsername`) REFERENCES `Person` (`username`)
) ENGINE=InnoDB;


2.) Check setup in ../ftest.properties
	a) check that selenium.browser property is set to your desired browser
	b) check that container property is set to either jboss4 or jboss5
	c) check that jboss*.deploy.waittime is set to sensible value
	d) check that jboss*.home points to the location of your application server
	e) check that seamgen.delete.project property is set to true if you want the generated applications
		to be deleted immediately after testrun
	f) check that workspace.home property points to a folder where generated applications should be stored temporarily
	g) check that seam-gen project properties (at the end of ftest.properties) are set correctly.
		Pay special attention to database connection related properties (Configure to use MySQL with exported tables).
		
3.) Start application server. Make sure there are no application deployed from previous test run.
#TODO automate this step

4.) Run tests.
	a) run "ant seam-gen.richfaces.testsuite" to test seam-gen with RichFaces		
	b) run "ant seam-gen.icefaces.testsuite" to test seam-gen with ICEfaces
	
	The matrix describing these testsuites follows:
	---------------------------------------------
	|						EAR			WAR		|
	|use deploy target		X			X		|
	|use explode target		X			X		|
	---------------------------------------------
	
	
5.) Run "ant testreport" in ${seam.dir} to generate nice looking testreports. 
	For every failed test, there is a screenshot and HTML source stored in test-output/functional-framework	


Known Limitations:
---------------------
* Container must be started (preferably with nothing deployed)

Running tests from Eclipse
---------------------
In order to run the testsuite from IDE, you need testng plugin http://testng.org/doc/eclipse.html
The procedure of running the testsuite from Eclipse is identical to instructions above, however you must replace step 3 with
following steps:
a) Rightclick on the testsuite's xml file, choose 'Run As' and then 'Run Configurations'
b) Modify the classpath to contain ${seam.dir}/lib/gen folder as well as mysql driver jar file
c) Run the testsuite by rightclick -> 'Run As' -> 'TestNG Suite'


Testsuite customization
---------------------
To customize the testsuite, edit particular xml file in this folder.
You can use following parameters to influence seam-gen application type:
1.) type - set to ear or war, ear is the default value
2.) explode - set to true or false, if set to false, application will be deployed and undeployed using (un)deploy target.
	(un)explode target will be used otherwise. True is the default value.
3.) icefaces - set to true to create an ICEfaces project. RichFaces project will be created otherwise.
	This parameter defaults to false.
4.) suffix - value of this parameter will be appended to the application name.

