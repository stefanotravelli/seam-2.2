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
package org.jboss.seam.example.remoting.chatroom.test.webdriver;


//import static junit.framework.Assert.assertTrue;

import org.jboss.seam.example.common.test.webdriver.AjaxWebDriverFactory;
import org.jboss.seam.example.common.test.webdriver.AjaxWebDriverTest;
import org.jboss.seam.example.common.test.webdriver.AjaxWebElement;
import org.jboss.seam.example.common.test.webdriver.AjaxWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * This class tests functionality of remoting/chatroom example. 
 * The test opens two browsers and tests communication between users.
 * 
 * @author Martin Gencur
 * 
 */
public class WebDriverChatroomTest extends AjaxWebDriverTest
{
   public static int timeout = 16000;
   
   public static final String NAME1 = "Martin";
   public static final String NAME2 = "Jozef";
   public static final String MESSAGE_FROM_MARTIN = "Hello";
   public static final String MESSAGE_FROM_JOZEF = "Good evening";
   public static final String HOME_PAGE = "/chatroom.seam";
   
   public static final String HOME_PAGE_TITLE = "Chat Room Example";
   public static final By NAME_INPUT = By.id("username");
   public static final By CONNECT_BUTTON = By.id("btnConnect");
   public static final By DISCONNECT_BUTTON = By.id("btnDisconnect");
   public static final By CONNECT_BUTTON_DISABLED = By.xpath("//input[@id='btnConnect'][@disabled]");
   public static final By DISCONNECT_BUTTON_DISABLED = By.xpath("//input[@id='btnDisconnect'][@disabled]");
   public static final By MESSAGE_INPUT = By.id("messageText");
   public static final By CHAT_AREA = By.id("channelDisplay");
   public static final By MARTIN_CONNECTED = By.xpath("//div[@id='channelDisplay']/span[contains(text(),'Martin connected.')]");
   public static final By MARTIN_LISTED = By.xpath("//div/select[@id='userList']/option[contains(text(),'Martin')]");
   public static final By JOZEF_CONNECTED = By.xpath("//div[@id='channelDisplay']/span[contains(text(),'Jozef connected.')]");
   public static final By JOZEF_LISTED = By.xpath("//div/select[@id='userList']/option[contains(text(),'Jozef')]");
   public static final By JOZEF_DISCONNECTED = By.xpath("//div[@id='channelDisplay']/span[contains(text(),'Jozef disconnected.')]");
   public static final By MARTIN_GT = By.xpath("//div[@id='channelDisplay']/span[contains(text(),'Martin>')]");
   public static final By HELLO = By.xpath("//div[@id='channelDisplay']/text()[contains(.,'"+ MESSAGE_FROM_MARTIN +"')]");   
   public static final By JOZEF_GT = By.xpath("//div[@id='channelDisplay']/span[contains(text(),'Jozef>')]");
   public static final By GOOD_MORNING = By.xpath("//div[@id='channelDisplay']/text()[contains(.,'" + MESSAGE_FROM_JOZEF + "')]");
         
   protected AjaxWebDriver driver2;
   
   @BeforeMethod
   public void setUp() 
   {
      startSecondBrowser();      
      driver.get(serverURL + contextPath + HOME_PAGE);
      driver2.get(serverURL + contextPath + HOME_PAGE);
   }
   
   @AfterMethod
   public void tearDown() 
   {
       stopSecondBrowser();
   }
   
   @Test
   public void homePageLoadTest() 
   {
      assertTrue("Unexpected page title.", driver.getTitle().contains(HOME_PAGE_TITLE));
   }
   
   @Test(dependsOnMethods={"homePageLoadTest"})
   public void connectAndChatTest()
   {
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
      
   public void connect()
   {
      driver.findElement(NAME_INPUT).clearAndSendKeys(NAME1);
      driver.setWaitTime(timeout);
      driver.findElement(CONNECT_BUTTON).clickAndWait();
   }
   
   public void verifyConnecting()
   {
      driver.findElement(MARTIN_LISTED);      
      driver2.setWaitTime(timeout);
      driver2.findElement(NAME_INPUT).clearAndSendKeys(NAME2);
      driver2.findElement(CONNECT_BUTTON).clickAndWait();
      driver2.findElement(JOZEF_LISTED);
      driver2.findElement(MARTIN_LISTED);
      driver.findElement(JOZEF_CONNECTED);
      driver.findElement(JOZEF_LISTED);
   }
   
   public void disconnect()
   {
      driver2.findElement(DISCONNECT_BUTTON).clickAndWait();
   }
   
   public void verifyDisconnecting()
   {
      
      AjaxWebElement el = null;
      try
      {
         el = driver2.findElement(JOZEF_LISTED);
      }
      catch (NoSuchElementException e)
      {         
      }    
      assertNull("Jozef should not be listed in second browser window", el);

      driver2.findElement(DISCONNECT_BUTTON_DISABLED);
      driver.findElement(JOZEF_DISCONNECTED);
      
      AjaxWebElement el2 = null;
      try
      {
         el2 = driver.findElement(JOZEF_LISTED);
      }
      catch (NoSuchElementException e)
      {         
      } 
      assertNull("Jozef should not be listed in first browser window", el2);
      
      driver.findElement(DISCONNECT_BUTTON).clickAndWait();

      AjaxWebElement el3 = null;
      try
      {
         el3 = driver.findElement(MARTIN_LISTED);
      }
      catch (NoSuchElementException e)
      {         
      }
      assertNull("Martin should not be listed in first browser window", el3);

      driver.findElement(DISCONNECT_BUTTON_DISABLED);
   }
   
   public void chat()
   {
      /*first user is sending a message*/
      driver.findElement(MESSAGE_INPUT).clearAndSendKeys(MESSAGE_FROM_MARTIN);
      driver.findElement(MESSAGE_INPUT).sendKeys(Keys.RETURN);

      try 
      {
         Thread.sleep(timeout);
      } catch (InterruptedException e) 
      {
      }
      
      driver.findElement(MARTIN_GT);
      driver.findElement(HELLO);
      driver2.findElement(MARTIN_GT);
      driver2.findElement(HELLO);   
      driver2.findElement(MESSAGE_INPUT).clearAndSendKeys(MESSAGE_FROM_JOZEF, Keys.RETURN);
      
      try 
      {
         Thread.sleep(timeout);
      } catch (InterruptedException e) 
      {
      }
      
      driver2.findElement(JOZEF_GT);
      driver2.findElement(GOOD_MORNING);
      driver.findElement(JOZEF_GT);
      driver.findElement(GOOD_MORNING);
   }
      
   public void startSecondBrowser()
   {
      try
      {
         this.driver2 = AjaxWebDriverFactory.getDriver(this.browser);
      }
      catch (IllegalArgumentException e)
      {
         fail("Unable to instantiate browser of type: " + this.browser + ", available browsers are: " + AjaxWebDriverFactory.availableBrowsers());
      }
      catch (NullPointerException e)
      {
         fail("Unable to instantiate browser of type: " + this.browser + ", available browsers are: " + AjaxWebDriverFactory.availableBrowsers());
      }
   }
   
   public void stopSecondBrowser()
   {
      driver2.close();
   }
}
