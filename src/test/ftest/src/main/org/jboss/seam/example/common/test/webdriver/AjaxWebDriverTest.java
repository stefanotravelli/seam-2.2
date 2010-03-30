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
package org.jboss.seam.example.common.test.webdriver;

import static org.testng.Assert.fail;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

/**
 * Abstract class for all WebDriver tests. Provides running a WebDriverInstance
 * before each executed test and shuts it down after its execution.
 * 
 * @author kpiwko
 * 
 */
public abstract class AjaxWebDriverTest
{
   protected AjaxWebDriver driver;
   protected String serverURL;
   protected String contextPath;
   protected String browser;
   
   @BeforeMethod
   @Parameters(value = { "browser", "server.url", "context.path" })
   public void createDriver(String browser, String serverURL, String contextPath)
   {
      try
      {
         this.driver = AjaxWebDriverFactory.getDriver(browser);
      }
      catch (IllegalArgumentException e)
      {
         fail("Unable to instantiate browser of type: " + browser + ", available browsers are: " + AjaxWebDriverFactory.availableBrowsers());
      }
      catch (NullPointerException e)
      {
         fail("Unable to instantiate browser of type: " + browser + ", available browsers are: " + AjaxWebDriverFactory.availableBrowsers());
      }
      
      this.serverURL = serverURL;
      this.contextPath = contextPath;
      this.browser = browser;
   }
   
   @AfterMethod
   public void closeDriver()
   {
      driver.close();
   }
   
}
