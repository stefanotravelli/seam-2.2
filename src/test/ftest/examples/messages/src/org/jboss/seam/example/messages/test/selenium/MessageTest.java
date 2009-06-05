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
package org.jboss.seam.example.messages.test.selenium;

import java.text.MessageFormat;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Test for messages example
 * @author Jozef Hartinger
 *
 */
public class MessageTest extends SeamSeleniumTest
{

   public static final String MESSAGES_URL = "/messages.seam";
   public static final String MESSAGES_LINK = "messages:{0}:link";
   public static final String MESSAGES_DELETE = "messages:{0}:delete";
   public static final String MESSAGES_CHECKBOX = "messages:{0}:read";
   public static final String MESSAGES_COUNT = "//table[@id='messages']/tbody/tr";
   public static final String MESSAGE_TITLE = "title";
   public static final String MESSAGE_TEXT = "text";

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + MESSAGES_URL);
   }

   @Test(dataProvider = "messages")
   public void readMessageTest(int i, String title, String text)
   {
      browser.clickAndWait(MessageFormat.format(MESSAGES_LINK, i));
      assertEquals("Unexpected message title displayed.", title, browser.getText(MESSAGE_TITLE));
      assertEquals("Unexpected message text displayed.", text, browser.getText(MESSAGE_TEXT));
      assertTrue("Checkbox should be checked after message is read.", browser.isChecked(MessageFormat.format(MESSAGES_CHECKBOX, i)));
   }

   @Test(dependsOnMethods = {"readMessageTest"}, dataProvider = "messages")
   public void deleteMessageTest(int i, String title, String name)
   {
      int messageCount = browser.getXpathCount(MESSAGES_COUNT).intValue();
      // delete first message in a table
      browser.clickAndWait(MessageFormat.format(MESSAGES_DELETE, 0));
      assertEquals("Unexpected count of messages.", --messageCount, browser.getXpathCount(MESSAGES_COUNT));
      assertFalse("Message title still present.", browser.isTextPresent(title));
   }

   @DataProvider(name = "messages")
   public Object[][] getMessages()
   {
      Object[][] messages = { { 0, "Greetings Earthling", "This is another example of a message." }, { 1, "Hello World", "This is an example of a message." } };
      return messages;
   }
}
