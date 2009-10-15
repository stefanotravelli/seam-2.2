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

package org.jboss.seam.example.remoting.helloworld.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.selenium.Wait;

import static org.testng.AssertJUnit.*;

/**
 * This class tests a functionality of remoting/helloworld example.
 * 
 * @author Martin Gencur
 * 
 */
public class HelloworldTest extends SeamSeleniumTest
{
   protected static final String HELLOWORLD_URL = "/helloworld.seam";
   protected static final String HELLOWORLD_TITLE = "Seam Remoting - Hello World Example";
   protected static final String SAYHELLO_BUTTON = "xpath=//button[contains(@onclick,\"sayHello\")]";
   protected static final String PROMPT_ANSWER = "Martin";
   protected static final String ALERT_MESSAGE = "Hello, Martin";
   protected static final Long TIMEOUT = 10000L; //10 seconds         
   
   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + HELLOWORLD_URL);
   }

   @Test
   public void simplePageContentTest()
   {
      assertTrue("Home page of Remoting/Helloworld Example expected", browser.getLocation().contains(HELLOWORLD_URL));      
      assertTrue("Different page title expected",browser.getTitle().contains(HELLOWORLD_TITLE)); 
      assertTrue("Home page should contain Say Hello button", browser.isElementPresent(SAYHELLO_BUTTON));
   }
   
   @Test(dependsOnMethods = {"simplePageContentTest"})
   public void sayHelloButtonTest(){
      String result = "";
      browser.answerOnNextPrompt(PROMPT_ANSWER);
      browser.click(SAYHELLO_BUTTON);
      waitForAlertPresent(TIMEOUT);
      result = browser.getAlert();
      assertTrue("An alert message should show up and should contain \"Hello,\" and name.", result.contains(ALERT_MESSAGE));      
   }   
   
   public void waitForAlertPresent(Long timeout){
      new Wait()
      {
         @Override
         public boolean until()
         {
            return browser.isAlertPresent();
         }
      }.wait("Timeout while waiting for alert window.", timeout);
   }   
}
