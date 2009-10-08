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

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

/**
 * This class tests CRUD on the Vehicle database table.
 * @author Jozef Hartinger
 *
 */
public class VehicleTest extends GenerateEntitiesTest
{

   public static final String SEARCH_REGISTRATION = "xpath=//input[matches(@id, 'vehicleSearch:.+:registration')]";
   public static final String SEARCH_SUBMIT = "vehicleSearch:search";
   public static final String SEARCH_RESET = "vehicleSearch:reset";

   public static final String VIEW_REGISTRATION = "xpath=id('registration')/div/span[2]";
   public static final String VIEW_STATE = "xpath=id('state')/div/span[2]";
   public static final String VIEW_MAKE = "xpath=id('make')/div/span[2]";
   public static final String VIEW_MODEL = "xpath=id('model')/div/span[2]";
   public static final String VIEW_YEAR = "xpath=id('year')/div/span[2]";
      
   protected static String REGISTRATION_TEXT = "Registration";
   protected static String ERROR_TEXT = "An Error Occurred";

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void newVehicleTest()
   {
      login();
      String registration = "44444444";
      String state = "SK";
      String make = "Honda";
      String model = "Civic";
      String year = "2008";
      createNewVehicle(registration, state, make, model, year);

      assertTrue(browser.isElementPresent(MESSAGES), "Confirmation message expected.");
      assertEquals(browser.getText(MESSAGES), "Successfully created", "Unexpected confirmation message");

      // search for the vehicle
      browser.clickAndWait(ENTITY_DONE);
      int result = search(registration);
      assertEquals(result, 1, "Unexpected count of search results after creating new vehicle");
      // verify view page
      browser.clickAndWait(String.format(VEHICLE_LIST_VIEW_BUTTON_BY_REGISTRATION, registration));
      verifyViewPage(registration, state, make, model, year);
   }

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" }, dependsOnMethods = { "newVehicleTest" })
   public void updateVehicleTest()
   {
      String registration = "11111111";
      String state = "CZ";
      String make = "Mazda";
      String model = "6";
      String year = "2005";

      login();
      browser.clickAndWait(VEHICLE_LINK);
      String editButton = String.format(VEHICLE_LIST_EDIT_BUTTON_BY_REGISTRATION, registration);
      browser.clickAndWait(editButton);
      // update the entity
      fillVehicleEditPage(registration, state, make, model, year);
      browser.clickAndWait(VEHICLE_UPDATE);
      // verify
      assertTrue(browser.isElementPresent(MESSAGES), "Confirmation message expected.");
      assertEquals(browser.getText(MESSAGES), "Successfully updated", "Unexpected confirmation message");
      verifyViewPage(registration, state, make, model, year);
   }

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void removeVehicleTest()
   {
      String registration = "22222222";

      login();
      browser.clickAndWait(VEHICLE_LINK);
      String editButton = String.format(VEHICLE_LIST_EDIT_BUTTON_BY_REGISTRATION, registration);
      browser.clickAndWait(editButton);
      // delete the person
      browser.clickAndWait(VEHICLE_DELETE);

      assertTrue(browser.isElementPresent(MESSAGES), "Confirmation message expected.");
      assertEquals(browser.getText(MESSAGES), "Successfully deleted", "Unexpected confirmation message");

      // search for the user
      int result = search(registration);
      assertEquals(result, 0, "Unexpected count of search results after removing the user");
   }

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void selectVehicleTest()
   {

      String username = "jharting";

      login();
      // create new vehicle
      String registration = "33333333";
      String state = "SK";
      String make = "Mazda";
      String model = "RX-8";
      String year = "2008";
      createNewVehicle(registration, state, make, model, year);
      // select person
      browser.clickAndWait(ENTITY_EDIT);
      browser.clickAndWait(ENTITY_SELECT_PARENT_BUTTON);
      browser.clickAndWait(String.format(PERSON_LIST_VIEW_BUTTON_BY_NAME, username));
      browser.clickAndWait(VEHICLE_UPDATE);
      // verify update is OK
      assertTrue(browser.isElementPresent(MESSAGES), "Confirmation message expected.");
      assertEquals(browser.getText(MESSAGES), "Successfully updated", "Unexpected confirmation message");
      browser.clickAndWait(ENTITY_DONE);
      // search for vehicle
      int result = search(registration);
      assertEquals(result, 1, "Unexpected count of search results after assigning a vehicle owner");
      // verify person is assigned to vehicle
      assertTrue(browser.isElementPresent(String.format(VEHICLE_LIST_ROW_BY_OWNER_NAME, registration, username)), "Person not assigned to vehicle.");
   }

   /**
    * This method verifies that JBSEAM3866 issue is already resolved
    */
   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void testForJBSEAM3866()
   {
      login();
      browser.clickAndWait(VEHICLE_LINK);
      assertTrue(browser.isTextPresent(REGISTRATION_TEXT),
            "Page should contain text Registration");
      
      boolean explode = seamGen.isExplode();
      seamGen.setExplode(true);
      seamGen.hotDeploy();
      seamGen.setExplode(explode);
      
      try
      {
         Thread.sleep(HOTDEPLOY_TIMEOUT);
      }
      catch (InterruptedException ie)
      {
         throw new RuntimeException(ie);
      }
      
      browser.refreshAndWait();
      assertTrue(browser.isTextPresent(REGISTRATION_TEXT),
            "Page should contain text Registration, which indicates that the error JBSEAM3866 is not present anymore");
      assertTrue(!browser.isTextPresent(ERROR_TEXT),
            "Page contains \"ERROR_TEXT\" which means that JBSEAM3866 error still exists");      
   }

   @Test(groups = "generate-entitiesTest", dependsOnGroups = { "newProjectGroup" })
   public void searchTest()
   {
      final String searchString = "9999999"; // should return two Audis

      login();
      browser.clickAndWait(VEHICLE_LINK);
      assertEquals(search(searchString), 2, "Unexpected number of search results for " + searchString);
   }
   
   public int search(String pattern)
   {
      browser.type(SEARCH_REGISTRATION, pattern);
      browser.clickAndWait(SEARCH_SUBMIT);
      return browser.getXpathCount(VEHICLE_LIST_RESULT_COUNT).intValue();
   }

   public void verifyViewPage(String registration, String state, String make, String model, String year)
   {
      assertEquals(browser.getText(VIEW_REGISTRATION), registration);
      assertEquals(browser.getText(VIEW_STATE), state);
      assertEquals(browser.getText(VIEW_MAKE), make);
      assertEquals(browser.getText(VIEW_MODEL), model);
      assertEquals(browser.getText(VIEW_YEAR), year);
   }
}
