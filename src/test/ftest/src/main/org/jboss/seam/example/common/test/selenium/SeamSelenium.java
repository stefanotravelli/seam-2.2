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
package org.jboss.seam.example.common.test.selenium;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.Wait;

/**
 * This class slightly enhaces a Selenium API for controlling a browser.
 * 
 * @author Jozef Hartinger
 * 
 */
public class SeamSelenium extends DefaultSelenium
{

   private String timeout = "30000";
   private boolean icefacesDetection = false;
   private long icefacesWaitTime = 1000;
   private long windowMaximizeWaitTime = 5000L;

   private final String ICEFACES_CONNECTION_STATUS = "xpath=//div[@class='iceOutConStat connectionStatus']";
   private final String ICEFACES_IDLE_VISIBLE = "xpath=//div[@class='iceOutConStatInactv connectionStatusInactv'][@style='visibility: visible;']";

   public SeamSelenium(String serverHost, int serverPort, String browserStartCommand, String browserURL)
   {
      super(serverHost, serverPort, browserStartCommand, browserURL);
   }

   /**
    * Same as click method but waits for page to load after clicking. Default
    * timeout can be changed by setTimeout() method.
    * 
    * @param locator
    */
   public void clickAndWait(String locator)
   {
      click(locator);
      waitForPageToLoad();
   }

   /**
    * Simulates a user pressing "back" button and waits for page to load.
    * Default timeout can be changed by setTimeout() method.
    */
   public void goBackAndWait()
   {
      super.goBack();
      super.waitForPageToLoad(timeout);
   }

   /**
    * Simulates a user pressing "refresh" button and waits for page to load.
    * Default timeout can be changed by setTimeout() method.
    */
   public void refreshAndWait()
   {
      super.refresh();
      super.waitForPageToLoad(timeout);
   }

   @Override
   public void setTimeout(String timeout)
   {
      super.setTimeout(timeout);
      this.timeout = timeout;
   }

   public String getTimeout()
   {
      return timeout;
   }

   public void waitForPageToLoad()
   {

      waitForPageToLoad(timeout);
   }

   @Override
   public void waitForPageToLoad(String timeout)
   {
      if (icefacesDetection && isElementPresent(ICEFACES_CONNECTION_STATUS))
      {
         waitForIcefaces(icefacesWaitTime, Long.valueOf(timeout));
      }
      else
      {
         super.waitForPageToLoad(timeout);
      }
   }

   /**
    * Waits until any AJAX worker is registered by underlying framework performs update.
    * Uses default (global) timeout
    */
   public void waitForAJAXUpdate()
   {
      waitForAJAXUpdate(timeout);
   }

   /**
    * Waits until any AJAX worker is registered by underlying framework performs update.
    * 
    * @param timeout Timeout in milliseconds
    */
   public void waitForAJAXUpdate(String timeout)
   {
      // there are two frameworks supposed, either jQuery or Prototype
      if (icefacesDetection)
      {
         // Prototype style
         waitForCondition("selenium.browserbot.getCurrentWindow().Ajax.activeRequestCount===0", timeout);
      }
      else
      {
         // jQuery style
         waitForCondition("selenium.browserbot.getCurrentWindow().jQuery.active===0", timeout);
      }
   }

   /**
    * Waits until element is asynchronously loaded on page. Uses global Selenium
    * timeout
    * 
    * @param locator Locator of element
    */
   public void waitForElement(final String locator)
   {
      waitForElement(locator, Long.valueOf(timeout));
   }

   /**
    * Waits until element is asynchronously loaded on page.
    * 
    * @param timeout Timeout in milliseconds
    * @param locator Locator of element
    */
   public void waitForElement(final String locator, long timeout)
   {
      new Wait()
      {
         @Override
         public boolean until()
         {
            return isElementPresent(locator);
         }
      }.wait("Timeout while waiting for asynchronous update of " + locator, timeout);
   }

   /**
    * Waits until element is asynchronously unloaded from page.
    * 
    * @param timeout Timeout in milliseconds
    * @param locator Locator of element
    */
   public void waitForElementNotPresent(final String locator, long timeout)
   {
      new Wait()
      {
         @Override
         public boolean until()
         {
            return !isElementPresent(locator);
         }
      }.wait("Timeout while waiting for asynchronous update of " + locator, timeout);
   }

   /**
    * Selects windows by its id. Waits until windows is refreshed.
    * 
    * @param windowID Identification of window which is selected
    */
   @Override
   public void selectWindow(String windowID)
   {
      super.selectWindow(windowID);
      refresh();
      waitForPageToLoad();
   }

   /**
    * Returns true if icefaces detection is turned on
    */
   public boolean isIcefacesDetection()
   {
      return icefacesDetection;
   }

   /**
    * Switch icefaces detection on/off
    * 
    * @param icefacesDetection
    */
   public void setIcefacesDetection(boolean icefacesDetection)
   {
      this.icefacesDetection = icefacesDetection;
   }

   /**
    * This wait time will be used when waiting for response after invoking
    * icefaces action
    */
   public long getIcefacesWaitTime()
   {
      return icefacesWaitTime;
   }

   /**
    * This wait time will be used when waiting for response after invoking
    * icefaces action
    * 
    * @param icefacesWaitTime
    */
   public void setIcefacesWaitTime(long icefacesWaitTime)
   {
      this.icefacesWaitTime = icefacesWaitTime;
   }

   /**
    * Captures a screenshot and stores it into a file. Active windows is
    * maximized before capturing a screenshot.
    */
   @Override
   public void captureScreenshot(String path)
   {
      windowMaximize();
      try
      {
         Thread.sleep(windowMaximizeWaitTime);
      }
      catch (InterruptedException e)
      {
      }
      super.captureScreenshot(path);
   }

   /**
    * Logs HTML body into a file.
    * 
    * @param path
    */
   public void logHTMLContext(String path)
   {
      String source = getHtmlSource();
      BufferedWriter writer = null;
      try
      {
         writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
         writer.write(source);
         writer.flush();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to save HTML body", e);
      }
      finally
      {
         try
         {
            writer.close();
         }
         catch (Exception e)
         {
         }
      }
   }

   private void waitForIcefaces(Long waitTime, Long timeout)
   {
      new Wait()
      {
         @Override
         public boolean until()
         {
            return isElementPresent(ICEFACES_IDLE_VISIBLE);
         }
      }.wait("Timeout while waiting for icefaces idle state.", timeout);
      try
      {
         Thread.sleep(icefacesWaitTime);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
      new Wait()
      {
         @Override
         public boolean until()
         {
            return isElementPresent("xpath=//body");
         }
      }.wait("Timeout while waiting for document body after icefaces click.", timeout);
   }

   @Override
   public void open(String url)
   {
      try
      {
         super.open(url);
      }
      catch (SeleniumException e)
      {
         // since 1.0.3 Selenium throws SeleniumException when a server returns 404
         // we suppress this behavior in order to retain backward compatibility 
      }
   }
   
   
}
