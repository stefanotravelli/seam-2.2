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
package org.jboss.seam.example.remoting.chatroom.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSelenium;
import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class tests functionality of remoting/chatroom example. 
 * The test opens two browsers and tests communication between users.
 * 
 * @author Martin Gencur
 * 
 */
public class SeleniumChatroomTest extends SeamSeleniumTest
{
   public static long timeout = 22000;
   
   public static final String HOME_PAGE = "/chatroom.seam";
   public static final String HOME_PAGE_TITLE = "Chat Room Example";
   public static final String NAME_INPUT = "id=username";
   public static final String CONNECT_BUTTON = "id=btnConnect";
   public static final String DISCONNECT_BUTTON = "id=btnDisconnect";
   public static final String CONNECT_BUTTON_DISABLED = "xpath=//input[@id='btnConnect'][@disabled]";
   public static final String DISCONNECT_BUTTON_DISABLED = "xpath=//input[@id='btnDisconnect'][@disabled]";
   public static final String MESSAGE_INPUT = "id=messageText";
   public static final String CHAT_AREA = "id=channelDisplay";
   public static final String NAME1 = "Martin";
   public static final String NAME2 = "Jozef";
   public static final String MARTIN_CONNECTED = "xpath=//div[@id='channelDisplay']/span[contains(text(),'Martin connected.')]";
   public static final String MARTIN_LISTED = "xpath=//div/select[@id='userList']/option[contains(text(),'Martin')]";
   public static final String JOZEF_CONNECTED = "xpath=//div[@id='channelDisplay']/span[contains(text(),'Jozef connected.')]";
   public static final String JOZEF_LISTED = "xpath=//div/select[@id='userList']/option[contains(text(),'Jozef')]";
   public static final String JOZEF_DISCONNECTED = "xpath=//div[@id='channelDisplay']/span[contains(text(),'Jozef disconnected.')]";
   public static final String MESSAGE_FROM_MARTIN = "Hello";
   public static final String MESSAGE_FROM_JOZEF = "Good evening";
   public static final String MARTIN_GT = "xpath=//div[@id='channelDisplay']/span[contains(text(),'Martin>')]";
   public static final String HELLO = "xpath=//div[@id='channelDisplay']/text()[contains(.,'"+ MESSAGE_FROM_MARTIN +"')]";   
   public static final String JOZEF_GT = "xpath=//div[@id='channelDisplay']/span[contains(text(),'Jozef>')]";
   public static final String GOOD_MORNING = "xpath=//div[@id='channelDisplay']/text()[contains(.,'" + MESSAGE_FROM_JOZEF + "')]";
      
   protected SeamSelenium browser2;
   
   @BeforeMethod
   @Override
   public void setUp() {
      super.setUp();
      startSecondBrowser();      
      browser.open(CONTEXT_PATH + HOME_PAGE);      
      browser2.open(CONTEXT_PATH + HOME_PAGE);
   }
   
   @AfterMethod
   @Override
   public void tearDown() {
       stopSecondBrowser();
       super.tearDown();
   }
   
   @Test // place holder - should be replaced by better tests as soon as JBSEAM-3944 is resolved
   public void homePageLoadTest() {
      assertEquals("Unexpected page title.", HOME_PAGE_TITLE, browser.getTitle());
   }
   
   @Test(dependsOnMethods={"homePageLoadTest"})
   public void connectAndChatTest(){
      /*connect user to chat*/
      connect();      
      /*verify that user is connected and is seen by other users*/
      verifyConnecting();      
      /*exchange several messages*/
      chat();      
      /*disconnect user from chat*/
      disconnect();      
      /*verify that user is disconnected and is not in a list of users anymore*/
      verifyDisconnecting();
   }
      
   public void connect(){
      browser.type(NAME_INPUT, NAME1);
      browser.click(CONNECT_BUTTON);
   }
   
   public void verifyConnecting(){
      //browser.waitForElement(MARTIN_CONNECTED, timeout);
      browser.waitForElement(MARTIN_LISTED, timeout);      
      browser2.type(NAME_INPUT, NAME2);
      browser2.click(CONNECT_BUTTON);
      browser2.waitForElement(JOZEF_LISTED, timeout);
      browser2.waitForElement(MARTIN_LISTED, timeout);
      browser.waitForElement(JOZEF_CONNECTED, timeout);
      browser.waitForElement(JOZEF_LISTED, timeout);
   }
   
   public void disconnect(){
      browser2.click(DISCONNECT_BUTTON);
   }
   
   public void verifyDisconnecting(){
      browser2.waitForElementNotPresent(JOZEF_LISTED, timeout);
      browser2.waitForElement(DISCONNECT_BUTTON_DISABLED, timeout);
      browser.waitForElement(JOZEF_DISCONNECTED, timeout);
      browser.waitForElementNotPresent(JOZEF_LISTED, timeout);
      browser.click(DISCONNECT_BUTTON);
      browser.waitForElementNotPresent(MARTIN_LISTED, timeout);
      browser.waitForElement(DISCONNECT_BUTTON_DISABLED, timeout);
   }
   
   public void chat(){
      /*first user is sending a message*/
      browser.type(MESSAGE_INPUT, MESSAGE_FROM_MARTIN);
      browser.focus(MESSAGE_INPUT);
      browser.keyPressNative("10");//browser.keyPressNative("13");      
      browser.keyPress(MESSAGE_INPUT,"13");
      browser.waitForElement(MARTIN_GT, timeout);
      browser.waitForElement(HELLO, timeout);
      browser2.waitForElement(MARTIN_GT, timeout);
      browser2.waitForElement(HELLO, timeout);       
      /*second user is sending a message*/
      browser2.type(MESSAGE_INPUT, MESSAGE_FROM_JOZEF);
      browser2.focus(MESSAGE_INPUT);
      browser2.keyPressNative("10");
      browser2.keyPress(MESSAGE_INPUT,"13");
      browser2.waitForElement(JOZEF_GT, timeout);
      browser2.waitForElement(GOOD_MORNING, timeout);
      browser.waitForElement(JOZEF_GT, timeout);
      browser.waitForElement(GOOD_MORNING, timeout); 
   }
      
   public void startSecondBrowser(){
      browser2 = super.startBrowser();
   }
   
   public void stopSecondBrowser(){
      browser2.stop();
   }
}
