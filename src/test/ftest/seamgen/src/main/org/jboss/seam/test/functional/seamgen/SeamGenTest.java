/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.test.functional.seamgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jboss.seam.test.functional.seamgen.utils.SeamGenAdapter;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base class for seam-gen functional tests.
 * 
 * @author Jozef Hartinger
 */
public class SeamGenTest
{
   
   protected static SeamGenAdapter seamGen;
   protected static Properties seamGenProperties;
   protected static Properties ftestProperties = new Properties();
   
   protected static String SEAM_DIR;
   protected static String SEAM_FTEST_PROPERTIES_FILE;
   protected static String SEAMGEN_BUILDFILE;
   protected static String SEAMGEN_PROPERTIES_FILE;
   protected static String WORKSPACE;
   protected static String ANT_EXECUTABLE;

   // container specific properties
   protected static String CONTAINER;
   protected static String CONTAINER_LOCATION;
   protected static int DEPLOY_TIMEOUT;
   
   protected static boolean ICEFACES;
   protected static boolean WAR;
   
   protected static boolean DELETE_PROJECT;
   
   protected static String TEST_SEAMGEN_PROPERTIES_FILE;
   
   // Selenium related constants
   protected static String SELENIUM_HOST;
   protected static String SELENIUM_BROWSER;
   protected static String SELENIUM_BROWSER_URL;
   protected static int SELENIUM_SERVER_PORT;
   protected static String SELENIUM_SPEED;
   protected static String SELENIUM_TIMEOUT;
   protected static long SELENIUM_ICEFACES_WAIT_TIME;
   protected static String SELENIUM_SERVER_ARGS;
   
   protected static String OUTPUT_DIR;
   
   // Test application specific constants
   protected static String APP_NAME;
   protected static String HOME_PAGE;
   
   // Selenium server instance
   protected static SeleniumServer seleniumServer;
   
   @BeforeSuite
   @Parameters("seam.dir")
   public void beforeSuite(@Optional(".") String seamDir) throws Exception
   {
      // Seam location
      SEAM_DIR = seamDir;
      
      // ftest configuration file
      String relativeLocation = System.getProperty("ftest.config.location");
      if (relativeLocation.equals("${ftest.config.location}"))
      {
         SEAM_FTEST_PROPERTIES_FILE = SEAM_DIR + "/src/test/ftest/ftest.properties";
      }
      else
      {
         SEAM_FTEST_PROPERTIES_FILE = SEAM_DIR + "/" + relativeLocation;
      }
      SEAMGEN_BUILDFILE = SEAM_DIR + "/seam-gen/build.xml";
      SEAMGEN_PROPERTIES_FILE = SEAM_DIR + "/seam-gen/build.properties";
      OUTPUT_DIR = SEAM_DIR + "/test-output/functional-framework/";
      
      loadFtestProperties();
      createOutputDir();
      startSeleniumServer();
   }
   
   @AfterSuite
   public void afterSuite()
   {
      seleniumServer.stop();
   }
   
   @BeforeTest
   @Parameters( { "icefaces", "type", "suffix", "explode" })
   public void setUp(@Optional("false") boolean icefaces, @Optional("ear") String type, @Optional("") String suffix, @Optional("true") boolean explode) throws Exception
   {
      ICEFACES = icefaces;
      WAR = type.equalsIgnoreCase("war");
      APP_NAME = "seamGenTestApp" + (ICEFACES ? "Ice" : "Rich") + (WAR ? "War" : "Ear") + (explode ? "E" : "D") + suffix;
      HOME_PAGE = "/" + APP_NAME + "/home.seam";
      
      setSeamGenProperties();
      
      seamGen = new SeamGenAdapter(ANT_EXECUTABLE, SEAMGEN_BUILDFILE);
      seamGen.setExplode(explode);
      
   }
   
   @AfterTest
   public void tearDown()
   {
      if (DELETE_PROJECT)
      {
         seamGen.deleteProject();
      }
      else
      {
         seamGen.undeploy();
      }
   }
   
   private void loadFtestProperties() throws FileNotFoundException, IOException
   {
      // load general properties
      ftestProperties.load(new FileInputStream(SEAM_FTEST_PROPERTIES_FILE));
      
      WORKSPACE = getProperty(ftestProperties,"workspace.home");
      ANT_EXECUTABLE = getProperty(ftestProperties, "ant.exec", "ant");
      
      // container specific
      CONTAINER = getProperty(ftestProperties, "container", "jboss5");
      CONTAINER_LOCATION = getProperty(ftestProperties, CONTAINER + ".home");
      DEPLOY_TIMEOUT = Integer.parseInt(getProperty(ftestProperties, CONTAINER + ".deploy.waittime")) * 1000; // miliseconds
      DELETE_PROJECT = Boolean.valueOf(getProperty(ftestProperties, "seamgen.delete.project", "false"));
      
      // load selenium constants
      SELENIUM_HOST = getProperty(ftestProperties, "selenium.host");
      SELENIUM_BROWSER = getProperty(ftestProperties, "selenium.browser");
      SELENIUM_BROWSER_URL = getProperty(ftestProperties, "selenium.browser.url");
      SELENIUM_SERVER_PORT = Integer.parseInt(getProperty(ftestProperties, "selenium.server.port"));
      SELENIUM_SPEED = getProperty(ftestProperties, "selenium.speed");
      SELENIUM_TIMEOUT = getProperty(ftestProperties, "selenium.timeout");
      SELENIUM_ICEFACES_WAIT_TIME = Long.valueOf(getProperty(ftestProperties, "selenium.icefaces.wait.time", "2000"));
      SELENIUM_SERVER_ARGS = getProperty(ftestProperties, "selenium.server.cmd.args", "");
   }
   
   /**
    * Gets property from properties in safe manner, property must be present in bundle, 
    * otherwise test fails.
    * @param properties Property bundle
    * @param key Key
    * @return Value of property or default value
    */
   private String getProperty(Properties properties, String key) {
      return getProperty(properties, key, null);
   }
   
   /**
    * Gets property from properties in safe manner
    * @param properties Property bundle
    * @param key Key
    * @param defaultValue Default value, if property not found, 
    * if @{code null}, property must be present in bundle otherwise test fails 
    * @return Value of property or default value
    */
   private String getProperty(Properties properties, String key, String defaultValue) {
      String value = properties.getProperty(key); 
      if(value==null && defaultValue==null) {
         Assert.fail("Property " + key +" is not set");
      }
      
      return (value!=null) ? value : defaultValue;
   }
   
   private void setSeamGenProperties()
   {
      seamGenProperties = new Properties();
      
      String[] propertiesToCopy = { "database.type", "database.exists", "database.drop", "driver.jar", "driver.license.jar", "hibernate.connection.username", "hibernate.connection.password", "hibernate.connection.driver_class", "hibernate.connection.dataSource_class", "hibernate.cache.provider_class", "hibernate.default_catalog.null", "hibernate.default_schema.null", "hibernate.dialect", "hibernate.connection.url", "model.package", "action.package", "test.package", "richfaces.skin", "icefaces.home", "jboss.domain" };
      
      for (String property : propertiesToCopy)
      {
         if (ftestProperties.get(property) != null)
         {
            seamGenProperties.put(property, ftestProperties.get(property));
         }
      }
      
      // override with ftest.properties
      seamGenProperties.put("workspace.home", WORKSPACE);
      seamGenProperties.put("jboss.home", CONTAINER_LOCATION);
      seamGenProperties.put("icefaces", ICEFACES ? "y" : "n");
      seamGenProperties.put("project.type", WAR ? "war" : "ear");
      seamGenProperties.put("project.name", APP_NAME);
   }
   
   /**
    * Parses some of Selenium command line arguments stated in ftest.properties.
    * There is not orthogonality between arguments of command line and 
    * Java configuration interface, so some arguments cannot be set by this method
    * @param rcc RC configuration to be modified
    */
   private void setSeleniumServerProperties(RemoteControlConfiguration rcc)
   {
      StringTokenizer parameters = new StringTokenizer(SELENIUM_SERVER_ARGS, " ");
      try
      {
         while (parameters.hasMoreTokens())
         {
            String cmd = parameters.nextToken();
            if ("-firefoxProfileTemplate".equals(cmd))
            {
               rcc.setFirefoxProfileTemplate(new File(parameters.nextToken()));
            }
            else if("-log".equals(cmd)) {
               rcc.setLogOutFileName(parameters.nextToken());
            }
            /*else if("-singleWindow".equals(cmd)) {
               rcc.setMultiWindow(false);
            }*/
            else if("-avoidProxy".equals(cmd)) {
               rcc.setHonorSystemProxy(false);
            }
            else if("-profilesLocation".equals(cmd)) {
               rcc.setProfilesLocation(new File(parameters.nextToken()));
            }
            else if("-trustAllSSLCertificates".equals(cmd)){
               rcc.setTrustAllSSLCertificates(true);
            }
            else if("-interactive".equals(cmd)){
               rcc.setInteractive(true);
            }
            else if("-userExtensions".equals(cmd)){
               rcc.setUserExtensions(new File(parameters.nextToken()));
            }
            // injection modes
            else if("-proxyInjectionMode".equals(cmd)){
               rcc.setProxyInjectionModeArg(true);
            }
            else if("-dontInjectRegex".equals(cmd) && rcc.getProxyInjectionModeArg()) {
               rcc.setDontInjectRegex(parameters.nextToken());
            }
            else if("-userJsInjection".equals(cmd) && rcc.getProxyInjectionModeArg()) {
               rcc.setUserJSInjection(true);
            }
            else if("-ensureCleanSession".equals(cmd)) {
               rcc.setReuseBrowserSessions(false);
            }
            else {
               System.err.println("Unknown selenium server argument: " + cmd);
            }
         }
      }
      catch (NoSuchElementException nsee)
      {
         System.err.println("Invalid command line arguments in selenium.server.cmd.args (" + SELENIUM_SERVER_ARGS + ")");
      }
      catch (NullPointerException ioe)
      {
         System.err.println("Unable to open empty filename in selenium.server.cmd.args (" + SELENIUM_SERVER_ARGS+")");
      }
      
   }
   
   private void startSeleniumServer() throws Exception
   {
      RemoteControlConfiguration rcc = new RemoteControlConfiguration();
      rcc.setPort(SELENIUM_SERVER_PORT);
      rcc.setLogOutFileName(OUTPUT_DIR + "/selenium-server.log");
      setSeleniumServerProperties(rcc);
      seleniumServer = new SeleniumServer(rcc);
      seleniumServer.start();
   }
   
   private void createOutputDir()
   {
      File dir = new File(OUTPUT_DIR);
      if (!dir.exists())
      {
         dir.mkdir();
      }
   }  
}
