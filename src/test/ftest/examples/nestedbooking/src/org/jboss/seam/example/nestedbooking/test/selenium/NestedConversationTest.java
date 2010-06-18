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
package org.jboss.seam.example.nestedbooking.test.selenium;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;
import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.jboss.seam.example.common.test.selenium.SeamSelenium;
import org.jboss.seam.example.common.test.booking.selenium.SeleniumBookingTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests only nested conversations
 *
 * @author mgencur
 */
public class NestedConversationTest extends SeamSeleniumTest 
{
   
   protected final int timeout = 5000;
   protected final String CREDIT_CARD = "0123456789012345";
   protected final String CREDIT_CARD_NAME = "visa";
   
   @BeforeMethod
   public void setUp() 
   {
       super.setUp();
   }
  
   @AfterMethod
   public void tearDown() 
   {
       super.tearDown();
   }   
   
   @Test
   public void nestedConversationTest() 
   {
      browser.open(CONTEXT_PATH + getProperty("MAIN_PAGE"));
      pause(timeout);
      browser.openWindow(CONTEXT_PATH + getProperty("MAIN_PAGE"), "0");
      pause(timeout);
      browser.selectWindow("0");
      login("demo", "demo");
      SeleniumBookingTest t = new SeleniumBookingTest();
      t.enterSearchQuery("W Hotel");
      browser.click(getProperty("SEARCH_RESULT_TABLE_SECOND_ROW_LINK"));
      pause(timeout);

      //open the url in a second window
      String url = browser.getLocation();
      browser.openWindow(url, "1");
      
      //go next to confirm button in browser 1
      browser.selectWindow("0");
      browser.clickAndWait(getProperty("BOOKING_BOOK"));
      browser.clickAndWait(getProperty("SELECT_ROOM_BUTTON"));
      browser.clickAndWait(getProperty("SELECT_WONDERFUL_ROOM"));      
      browser.type(getProperty("PAYMENT_CREDIT_CARD"), CREDIT_CARD); 
      browser.type(getProperty("PAYMENT_CREDIT_CARD_NAME"), CREDIT_CARD_NAME);
      browser.click(getProperty("PAYMENT_PROCEED"));
      
      //go next to confirm button in browser 2
      browser.selectWindow("1");
      browser.clickAndWait(getProperty("BOOKING_BOOK"));
      browser.clickAndWait(getProperty("SELECT_ROOM_BUTTON"));
      browser.clickAndWait(getProperty("SELECT_FANTASTIC_ROOM"));      
      browser.type(getProperty("PAYMENT_CREDIT_CARD"), CREDIT_CARD); 
      browser.type(getProperty("PAYMENT_CREDIT_CARD_NAME"), CREDIT_CARD_NAME);
      browser.click(getProperty("PAYMENT_PROCEED"));
      
      //confirm in browser 1 (WONDERFUL room should be selected)
      browser.selectWindow("0");
      browser.clickAndWait(getProperty("CONFIRM_CONFIRM"));
      
      assertTrue(browser.isTextPresent("$450.00") && browser.isTextPresent("Wonderful Room"));
      assertFalse(browser.isTextPresent("$1,000.00") || browser.isTextPresent("Fantastic Suite"));
   }
   
   public void login(String username, String password) 
   {
      //browser.waitForPageToLoad(TIMEOUT);
      browser.type(getProperty("LOGIN_USERNAME_FIELD"), username);
      browser.type(getProperty("LOGIN_PASSWORD_FIELD"), password);
      browser.click(getProperty("LOGIN_SUBMIT"));
      browser.waitForPageToLoad(TIMEOUT);
   }
   
   private void pause(int millis)
   {
      try 
      {
         Thread.sleep(millis);
      } 
      catch (InterruptedException e) 
      {
      }
   }
}
