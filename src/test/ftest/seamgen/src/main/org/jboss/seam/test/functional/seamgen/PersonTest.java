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
package org.jboss.seam.test.functional.seamgen;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Calendar;

import org.testng.annotations.Test;

/**
 * This test verifies CRUD functionality on the Person table.
 * @author Jozef Hartinger
 *
 */
public class PersonTest extends GenerateEntitiesTest
{

   public static final String SEARCH_USERNAME = "xpath=//input[matches(@id, 'personSearch:.+:username')]";
   public static final String SEARCH_SUBMIT = "personSearch:search";
   public static final String SEARCH_RESET = "personSearch:reset";

   public static final String VIEW_USERNAME = "xpath=id('username')/div/span[2]";
   public static final String VIEW_ADDRESS = "xpath=id('address')/div/span[2]";
   public static final String VIEW_NAME = "xpath=id('name')/div/span[2]";

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void newPersonTest()
   {
      login();
      String username = "tester";
      String address = "test address";
      String name = "John Doe";
      Calendar cal = Calendar.getInstance();
      cal.set(1991, 10, 20);
      createNewPerson(username, address, cal.getTime(), name);

      assertTrue(browser.isElementPresent(MESSAGES), "Confirmation message expected.");
      assertEquals(browser.getText(MESSAGES), "Successfully created", "Unexpected confirmation message");

      // search for the person
      browser.clickAndWait(ENTITY_DONE);
      int result = search(username);
      assertEquals(result, 1, "Unexpected count of search results after creating new user");
      // verify view page
      browser.clickAndWait(String.format(PERSON_LIST_VIEW_BUTTON_BY_NAME, username));
      verifyViewPage(username, address, name);
   }

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void updatePersonTest()
   {
      String username = "johny";
      String address = "updated address";
      String name = "Test User";

      login();
      browser.clickAndWait(PERSON_LINK);
      String editButton = String.format(PERSON_LIST_EDIT_BUTTON_BY_NAME, username);
      browser.clickAndWait(editButton);
      // update the entity
      Calendar cal = Calendar.getInstance();
      cal.set(1984, 02, 29);
      fillPersonEditPage(username, address, cal.getTime(), name);
      browser.clickAndWait(PERSON_UPDATE);
      // verify
      assertTrue(browser.isElementPresent(MESSAGES), "Confirmation message expected.");
      assertEquals(browser.getText(MESSAGES), "Successfully updated", "Unexpected confirmation message");
      verifyViewPage(username, address, name);
   }

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void removePersonTest()
   {
      String username = "jane";

      login();
      browser.clickAndWait(PERSON_LINK);
      String editButton = String.format(PERSON_LIST_EDIT_BUTTON_BY_NAME, username);
      browser.clickAndWait(editButton);
      // delete the person
      browser.clickAndWait(PERSON_DELETE);

      assertTrue(browser.isElementPresent(MESSAGES), "Confirmation message expected.");
      assertEquals(browser.getText(MESSAGES), "Successfully deleted", "Unexpected confirmation message");

      // search for the user
      int result = search(username);
      assertEquals(result, 0, "Unexpected count of search results after removing the user");

   }

   public int search(String pattern)
   {
      browser.type(SEARCH_USERNAME, pattern);
      browser.clickAndWait(SEARCH_SUBMIT);
      return browser.getXpathCount(PERSON_LIST_RESULT_COUNT).intValue();
   }

   public void verifyViewPage(String username, String address, String name)
   {
      assertEquals(browser.getText(VIEW_USERNAME), username);
      assertEquals(browser.getText(VIEW_ADDRESS), address);
      assertEquals(browser.getText(VIEW_NAME), name);
   }

}
