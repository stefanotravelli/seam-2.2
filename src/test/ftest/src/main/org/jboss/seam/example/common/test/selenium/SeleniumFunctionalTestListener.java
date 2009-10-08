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

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * This listener class is used for capturing snapshot of running web application and storing 
 * html source of the html file.
 * 
 * @author Martin Gencur
 *
 */
public class SeleniumFunctionalTestListener extends SeamSeleniumTest implements ITestListener
{
   
   public void onFinish(ITestContext arg0)
   {
   }
   
   public void onStart(ITestContext arg0)
   {
   }
   
   public void onTestFailedButWithinSuccessPercentage(ITestResult arg0)
   {
   }
   
   public void onTestFailure(ITestResult arg0)
   {
      String logPath = OUTPUT_DIR + APP_NAME + "/" + arg0.getName();
      try {
         SeamSeleniumTest.browser.captureScreenshot(logPath + ".png");
      } catch (Exception e) {         
      } finally {
         SeamSeleniumTest.browser.logHTMLContext(logPath + ".html");
         stopBrowser();   
      }      
   }
   
   public void onTestSkipped(ITestResult arg0)
   {
      stopBrowser();
   }
   
   public void onTestStart(ITestResult arg0)
   {
   }
   
   public void onTestSuccess(ITestResult arg0)
   {
      stopBrowser();
   }   
}