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

package org.jboss.seam.example.remoting.gwt.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.selenium.Wait;

import static org.testng.AssertJUnit.*;

/**
 * This class tests a functionality of remoting/gwt example.
 * 
 * @author Martin Gencur
 * 
 */
public class GwtTest extends SeamSeleniumTest
{
   protected static final String GWT_URL = "/HelloWorld.html";
   protected static final String GWT_TITLE = "Wrapper HTML for HelloWorld";   
   protected static final String ASK_BUTTON = "dom=document.getElementsByTagName('button')[0]";
   protected static final String TEXT_FIELD = "dom=document.getElementsByTagName('input')[0]";
   protected static final Long TIMEOUT = 2000L; //2 seconds      
   
   protected static final String ENTER_TEXT_WITHOUT = "Text without question mark at the end";
   protected static final String ENTER_TEXT_WITH = "Text WITH question mark at the end?";
   
   protected static final String MESSAGE_WITHOUT = "A question has to end with a \'?\'";
   protected static final String MESSAGE_WITH = "Its the real question that you seek now";
 
   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + GWT_URL);
   }

   @Test
   public void simplePageContentTest()
   {      
      assertTrue("Home page of Remoting/Gwt Example expected", browser.getLocation().contains(GWT_URL));      
      assertTrue("Different page title expected ale je:"+ browser.getTitle(),browser.getTitle().contains(GWT_TITLE));
      assertTrue("Home page should contain Text field", browser.isElementPresent(TEXT_FIELD));
      assertTrue("Home page should contain Ask button", browser.isElementPresent(ASK_BUTTON));
   }
   
   @Test(dependsOnMethods = {"simplePageContentTest"})
   public void withoutQuestionMarkTest(){
      String result = "";   
      browser.type(TEXT_FIELD, ENTER_TEXT_WITHOUT);
      browser.click(ASK_BUTTON);
      waitForAlertPresent(TIMEOUT);
      result = browser.getAlert();      
      assertTrue("An alert message should show up and should contain message \"" + MESSAGE_WITHOUT + "\"", result.contains(MESSAGE_WITHOUT));       
   } 
   
   @Test(dependsOnMethods = {"simplePageContentTest"})
   public void withQuestionMarkTest(){
      String result = "";   
      browser.type(TEXT_FIELD, ENTER_TEXT_WITH);
      browser.click(ASK_BUTTON);
      waitForAlertPresent(TIMEOUT);
      result = browser.getAlert();      
      assertTrue("An alert message should show up and should contain message \"" + MESSAGE_WITH + "\"", result.contains(MESSAGE_WITH));       
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
