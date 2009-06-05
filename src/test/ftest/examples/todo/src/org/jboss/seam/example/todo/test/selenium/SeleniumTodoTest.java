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
package org.jboss.seam.example.todo.test.selenium;

import java.text.MessageFormat;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This is a base class for todo example selenium tests
 * @author Jozef Hartinger
 *
 */
public class SeleniumTodoTest extends SeamSeleniumTest
{
   
   public static final String LOGIN_URL = "/login.seam";
   public static final String TODO_URL = "/todo.seam";
   public static final String LOGIN_USERNAME = "id=login:username";
   public static final String LOGIN_SUBMIT = "id=login:submit";
   public static final String NEW_ITEM_DESCRIPTION = "id=new:description";
   public static final String NEW_ITEM_CREATE = "id=new:create";
   
   public static final String NO_ITEMS_FOUND = "id=list:noItems";
   public static final String FIRST_ITEM_DESCRIPTION = "id=list:items:0:description";
   public static final String FIRST_ITEM_PRIORITY = "id=list:items:0:priority";
   public static final String FIRST_ITEM_DONE = "id=list:items:0:done";
   public static final String NTH_ITEM_DESCRIPTION = "id=list:items:{0}:description";
   public static final String NTH_ITEM_PRIORITY = "id=list:items:{0}:priority";
   public static final String NTH_ITEM_DONE = "id=list:items:{0}:done";
   public static final String ITEMS_COUNT = "//table[@id=\"list:items\"]/tbody/tr";
   public static final String ITEMS_UPDATE = "list:update";
   
   
   public static final String DEFAULT_USERNAME = "tester";
   
   @BeforeClass
   public void prepareTestFixture() {
      String[] fixture = {"selenium test for todo example", "buy milk", "clean the bathroom"};
      setUp();
      assertTrue("Item list should be empty", browser.isElementPresent(NO_ITEMS_FOUND));
      for (String item : fixture) {
         browser.type(NEW_ITEM_DESCRIPTION, item);
         browser.clickAndWait(NEW_ITEM_CREATE);
      }
      assertEquals("Unexpected count of items.", fixture.length, browser.getXpathCount(ITEMS_COUNT));
      super.tearDown();
   }
   
   @BeforeMethod
   @Override
   public void setUp() {
      super.setUp();
      browser.open(CONTEXT_PATH + LOGIN_URL);
      browser.type(LOGIN_USERNAME, DEFAULT_USERNAME);
      browser.clickAndWait(LOGIN_SUBMIT);
      assertTrue("Navigation failure. Todo page expected.", browser.getLocation().contains(TODO_URL));
   }
   
   @Test
   public void getEntryDoneTest() {
      String description = browser.getValue(FIRST_ITEM_DESCRIPTION);
      int itemCount = browser.getXpathCount(ITEMS_COUNT).intValue();
      browser.clickAndWait(FIRST_ITEM_DONE);
      assertFalse("Item should disappear from item list when done.", browser.isTextPresent(description));
      assertEquals("Unexpected count of items.", --itemCount, browser.getXpathCount(ITEMS_COUNT));
   }

   /**
    * This test sets high priority to first item and verifies that the item is be moved to the bottom and the priority number is kept.
    */
   @Test
   public void priorityTest() {
      String description = browser.getValue(FIRST_ITEM_DESCRIPTION);
      String priority = "10";
      int itemCount = browser.getXpathCount(ITEMS_COUNT).intValue();
      int lastItemRowId = itemCount - 1;
      browser.type(FIRST_ITEM_PRIORITY, priority);
      browser.clickAndWait(ITEMS_UPDATE);
      assertEquals("Message should move to the end of item list after priority change.", description, browser.getValue(MessageFormat.format(NTH_ITEM_DESCRIPTION, lastItemRowId)));
      assertEquals("Unexpected priority.", priority, browser.getValue(MessageFormat.format(NTH_ITEM_PRIORITY, lastItemRowId)));
   }
}
