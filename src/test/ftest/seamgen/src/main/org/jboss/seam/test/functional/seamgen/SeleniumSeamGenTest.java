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

import static org.testng.Assert.assertTrue;

import org.jboss.seam.example.common.test.selenium.SeamSelenium;
import org.testng.annotations.BeforeMethod;

/**
 * Base class for Selenium seam-gen tests. Separate browser instance is created
 * prior to each test method execution. Browser instances are stopped by
 * SeleniumTestListener.
 * 
 * @author Jozef Hartinger
 * 
 */
public class SeleniumSeamGenTest extends SeamGenTest
{
   // home page
   public static final String LOGIN = "id=menuLoginId";
   public static final String LOGOUT = "id=menuLogoutId";
   public static final String HOME = "id=menuHomeId";
   public static final String SIGNED_USER = "id=menuWelcomeId";
   // login page
   public static final String LOGIN_USERNAME = "id=loginForm:username";
   public static final String LOGIN_PASSWORD = "id=loginForm:password";
   public static final String LOGIN_REMEMBER_ME = "id=loginForm:rememberMe";
   public static final String LOGIN_SUBMIT = "id=loginForm:submit";
   public static final String LOGIN_FAILED_MESSAGE = "Login failed";

   public static final String MESSAGES = "id=messages";
   public static final String FOOTER = "xpath=//div[@class = 'footer']";
   public static final String MESSAGE_WELCOME = "xpath=//h1[text() = 'Welcome to Seam!']";

   public static final String DEFAULT_USERNAME = "admin";
   public static final String DEFAULT_PASSWORD = "";
   
   protected static Long HOTDEPLOY_TIMEOUT = 30000L;

   public static SeamSelenium browser;

   /**
    * Start new browser instance and store it into static variable. Moreover,
    * open application home page.
    */
   public void initBrowser()
   {
      browser = startBrowser();
      browser.open(HOME_PAGE);
   }

   /**
    * Create new browser instance.
    */
   public SeamSelenium startBrowser()
   {
      SeamSelenium newBrowser = new SeamSelenium(SELENIUM_HOST, SELENIUM_SERVER_PORT, SELENIUM_BROWSER, SELENIUM_BROWSER_URL);
      newBrowser.start();
      newBrowser.allowNativeXpath("false");
      newBrowser.setSpeed(SELENIUM_SPEED);
      newBrowser.setTimeout(SELENIUM_TIMEOUT);
      if (ICEFACES)
      {
         newBrowser.setIcefacesDetection(true);
      }
      newBrowser.setIcefacesWaitTime(SELENIUM_ICEFACES_WAIT_TIME);
      return newBrowser;
   }

   public void stopBrowser()
   {
      browser.stop();
   }

   public void login(String username, String password)
   {
      assertTrue(browser.isElementPresent(LOGIN), "Login link expected.");
      browser.clickAndWait(LOGIN);
      browser.type(LOGIN_USERNAME, username);
      browser.type(LOGIN_PASSWORD, password);
      browser.clickAndWait(LOGIN_SUBMIT);
   }

   public void login()
   {
      login(DEFAULT_USERNAME, DEFAULT_PASSWORD);
   }

   public boolean isLoggedIn()
   {
      return browser.isElementPresent(SIGNED_USER) && browser.isElementPresent(LOGOUT) && !browser.isElementPresent(LOGIN);
   }

   public void logout()
   {
      browser.clickAndWait(LOGOUT);
   }

   @BeforeMethod
   public void beforeMethod()
   {
      initBrowser();
   }

   /**
    * Wait for application (or it's part) to deploy. Separate Selenium browser
    * instance is used to poll server for specified URL, waiting for specified
    * element to appear.
    */
   public void waitForAppToDeploy(String url, String element)
   {
      int step = 5000;
      int i = DEPLOY_TIMEOUT;

      SeamSelenium browser = startBrowser();

      browser.open(url);
      try
      {
         while (!browser.isElementPresent(element))
         {
            i -= step;
            if (i <= 0)
            {
               throw new RuntimeException("Timeout waiting for " + element + " at " + url);
            }
            Thread.sleep(step);
            browser.open(url); // try again
         }
      }
      catch (InterruptedException ie)
      {
         throw new RuntimeException(ie);
      }
      finally
      {
         browser.stop();
      }
   }

}
