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
package org.jboss.seam.example.common.test.booking.cluster.selenium;

import java.io.IOException;
import java.text.MessageFormat;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import org.jboss.seam.example.common.test.selenium.SeamSelenium;
import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;

import com.thoughtworks.selenium.Wait;

/**
 * This class tests booking functionality of the example. Two instances of JBoss AS are
 * being used. First part of test is executed at first (master) instance. Then the first 
 * instance is killed and a second (slave) instance takes over executing of the application.
 * This behaviour simulates recovery from breakdown.
 * 
 * Prior to executing this test it is needed to start both JBoss AS instances manually. 
 * For example (assuming you have created second "all" configuration ("all2")):
 * ./run.sh -c all -g DocsPartition -u 239.255.101.101 -b localhost -Djboss.messaging.ServerPeerID=1 
 * -Djboss.service.binding.set=ports-default
 * ./run.sh -c all2 -g DocsPartition -u 239.255.101.101 -b localhost -Djboss.messaging.ServerPeerID=2
 * -Djboss.service.binding.set=ports-01
 * The configuration all is considered to be master jboss instance (related to 
 * jboss.service.binding.set=ports-default) and the application is deployed to server/all/farm directory
 * at "jboss5.home" location specified in ftest.properties
 * 
 * 
 * @author mgencur
 * @author jharting
 * 
 */
public class FailoverTest extends SeamSeleniumTest 
{
    private final String HOTEL_NAME = "W Hotel";
    private final String DEFAULT_USERNAME = "demo";
    private final String DEFAULT_PASSWORD = "demo";
    private final String EXPECTED_NAME = "Demo User";
    private final String CREDIT_CARD = "0123456789012345";
    private final String CREDIT_CARD_NAME = "visa";
    private final long JBOSS_SHUTDOWN_TIMEOUT = 20000;
    private final int SECOND_BROWSER_PORT = 8180;
    private final String SECOND_INSTANCE_BROWSER_URL = "http://localhost:" + SECOND_BROWSER_PORT;
       
    private SeamSelenium browser2;
    
    @Override
    @BeforeMethod
    public void setUp() 
    {
        super.setUp();        
        deleteCookies(browser);
        browser2 = startSecondBrowser();
        deleteCookies(browser2);
        assertTrue("Login failed.", login(browser));
    }

    @Override
    @AfterMethod
    public void tearDown() 
    {
        logout(browser2);
        super.tearDown();
        stopSecondBrowser();
    }    
    
    
    
    /**
     * Simply books hotel with failover during booking.
     */
    @Test
    public void simpleBookingWithFailoverTest() 
    {        
        preFailurePart(browser);
        
        String newAddress = getAddressForSecondInstance(browser);
        
        shutdownMasterJBossInstance();     
                              
        postFailurePart(browser2, newAddress);
        
        // main page
        String message = browser2.getText(getProperty("HOTEL_MESSAGE"));
        assertTrue("Booking failed. Confirmation message does not match.", message.matches(
              MessageFormat.format(getProperty("BOOKING_CONFIRMATION_MESSAGE"), EXPECTED_NAME, HOTEL_NAME))); 
    }
    
    
    
    public void shutdownMasterJBossInstance()
    {
      String command = ""; 
      
      if (CONTAINER.contains("jboss6"))
      {
         command = JBOSS_HOME + "/bin/shutdown.sh -s service:jmx:rmi:///jndi/rmi://localhost:1090/jmxrmi -S";
      }
      else
      {
         command = JBOSS_HOME + "/bin/shutdown.sh -s localhost:1099 -S";
      }

      try
      {
         Process process = Runtime.getRuntime().exec(command);
         process.waitFor();
         Thread.sleep(JBOSS_SHUTDOWN_TIMEOUT);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e.getCause());
      }
      catch (InterruptedException e)
      {
      }
    }

    public void preFailurePart(SeamSelenium browser)
    {
      if (!isLoggedIn(browser))
          fail();
      if (!browser.isElementPresent(getProperty("SEARCH_SUBMIT"))) {
          browser.open(CONTEXT_PATH + getProperty("MAIN_PAGE"));
          browser.waitForPageToLoad(TIMEOUT);
      }
      enterSearchQueryUsingAJAX(browser, HOTEL_NAME);
      browser.click(getProperty("SEARCH_RESULT_TABLE_FIRST_ROW_LINK"));
      browser.waitForPageToLoad(TIMEOUT);
      // go to booking page
      browser.click(getProperty("BOOKING_BOOK"));
      browser.waitForPageToLoad(TIMEOUT);
    }
    
    
    public void postFailurePart(SeamSelenium browser, String newAddress)
    {
      browser2.open(newAddress);
       
       //booking page
      populateBookingFields(browser); 

      //a jsessionid cookie has to be deleted because at this moment there already exists one
      deleteCookies(browser);
      
      browser.click(getProperty("HOTEL_PROCEED"));
      browser.waitForPageToLoad(TIMEOUT);
      
      deleteCookies(browser);
      
      // confirm page
      browser.click(getProperty("HOTEL_CONFIRM"));
      browser.waitForPageToLoad(TIMEOUT);
    }
    
    public SeamSelenium startSecondBrowser() 
    {
       BROWSER_URL = SECOND_INSTANCE_BROWSER_URL;
       //System.out.println("host: " + HOST + ", port: " + PORT + ", browser_url: " + BROWSER_URL);
       return super.startBrowser();
    }    
    
    public void stopSecondBrowser()
    {
       browser2.stop();
    }
    
    public String getAddressForSecondInstance(SeamSelenium browser)
    {
       String[] parsedStrings = browser.getLocation().split("/");
       StringBuilder sb = new StringBuilder();
       for (int i = 3; i != parsedStrings.length; i++){
          sb.append("/").append(parsedStrings[i]);
       }      
       String sid = browser.getCookieByName("JSESSIONID");
       String newAddress = sb.toString();
       String firstPart = newAddress.substring(0, newAddress.indexOf("?"));
       String secondPart = newAddress.substring(newAddress.indexOf("?") , newAddress.length());

       newAddress = firstPart + ";jsessionid=" + sid + secondPart;
    
       return newAddress;      
    }
    
    protected void populateBookingFields(SeamSelenium browser) 
    {
       populateBookingFields(browser, 2, 0, CREDIT_CARD, CREDIT_CARD_NAME);
    }
    
    protected void populateBookingFields(SeamSelenium browser, int bed, int smoking, String creditCard, String creditCardName) 
    {
         browser.select(getProperty("HOTEL_BED_FIELD"),
                 getProperty("HOTEL_BED_FIELD_SELECT_CRITERIA") + bed);
         if (smoking == 1) {
             browser.check(getProperty("HOTEL_SMOKING_1"));
         } else {
             browser.check(getProperty("HOTEL_SMOKING_2"));
         }
         browser.type(getProperty("HOTEL_CREDIT_CARD"), creditCard);
         browser.type(getProperty("HOTEL_CREDIT_CARD_NAME"), creditCardName);
    } 
    
    private void deleteCookies(SeamSelenium browser)
    {
       browser.deleteCookie("JSESSIONID", "path=" + CONTEXT_PATH + ", domain=localhost, recurse=true");
    }     
   
    public boolean login(SeamSelenium browser) 
    {
        return login(browser, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
   
    public boolean login(SeamSelenium browser, String username, String password) 
    {
         if (isLoggedIn(browser)) 
         {
             fail("User already logged in.");
         }
         
         browser.open(CONTEXT_PATH + getProperty("HOME_PAGE"));
         browser.waitForPageToLoad(TIMEOUT);
         
         if (!browser.getTitle().equals(getProperty("PAGE_TITLE"))) 
         {
             return false;
         }
         
         browser.type(getProperty("LOGIN_USERNAME_FIELD"), username);
         browser.type(getProperty("LOGIN_PASSWORD_FIELD"), password);
         browser.click(getProperty("LOGIN_SUBMIT"));
         browser.waitForPageToLoad(TIMEOUT);
         
         return isLoggedIn(browser);
    }
   
    public void logout(SeamSelenium browser) 
    {
        if (isLoggedIn(browser)) 
        {
             browser.click(getProperty("LOGOUT"));
             browser.waitForPageToLoad(TIMEOUT);
        }
    }
   
    public boolean isLoggedIn(SeamSelenium browser) 
    {
        return browser.isElementPresent(getProperty("LOGOUT"));
    }    
   
    public void enterSearchQueryUsingAJAX(final SeamSelenium browser, String query) 
    {
       browser.type(getProperty("SEARCH_STRING_FIELD"), "");
       browser.type(getProperty("SEARCH_STRING_FIELD"), query.substring(0, query.length() - 1));
       browser.typeKeys(getProperty("SEARCH_STRING_FIELD"), query.substring(query.length() - 1));
       
       browser.click(getProperty("SEARCH_SUBMIT"));
      
       // wait for javascript to show spinner
       try 
       {
           Thread.sleep(1000);
       } 
       catch (InterruptedException e)
       {
       }
       
       // wait for page to get updated
       new Wait() 
       {
           @Override
           public boolean until() 
           {
               return !browser.isVisible(getProperty("SPINNER"));
           }
       }.wait("Spinner hasn't come out.");
       
       new Wait() 
       {
           @Override
           public boolean until() 
           {
               return (browser.isElementPresent(getProperty("SEARCH_RESULT_TABLE")) || 
                       browser.isElementPresent(getProperty("NO_HOTELS_FOUND")));
           }
       }.wait("Search results not found.");
    }    
}
