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

import java.io.InputStream;
import java.util.Date;

import org.testng.annotations.BeforeGroups;

import com.thoughtworks.selenium.Wait;

/**
 * This class and its subclasses test seam-gen's "generate-entities" feature.
 * Every test method should be part of "generate-entitiesTest" in order to get
 * entities generated before its execution.
 * 
 * @author Jozef Hartinger
 * 
 */
public class GenerateEntitiesTest extends DatabaseTest
{

   public static final String PERSON_LINK = "id=PersonId";
   public static final String PERSON_USERNAME = "id=person:usernameField:username";
   public static final String PERSON_ADDRESS = "id=person:addressField:address";
   public static final String PERSON_BIRTHDAY = "id=person:birthdateField:birthdate";
   public static final String PERSON_NAME = "id=person:nameField:name";
   public static final String PERSON_SAVE = "id=person:save";
   public static final String PERSON_CANCEL = "id=person:cancel";
   public static final String PERSON_DELETE = "id=person:delete";
   public static final String PERSON_UPDATE = "id=person:update";
   public static final int PERSON_IDENTIFIER_SIZE = 10;

   public static final String PERSON_LIST_EDIT_BUTTON_BY_NAME = "xpath=//table[@id='personList']/tbody/tr[normalize-space(td[1]/text()) = '%s']//a[matches(@id, 'personList:\\d+:personEdit')]";
   public static final String PERSON_LIST_VIEW_BUTTON_BY_NAME = "xpath=//table[@id='personList']/tbody/tr[normalize-space(td[1]/text()) = '%s']//a[matches(@id, 'personList:\\d+:person')]";
   public static final String PERSON_LIST_RESULT_COUNT = "//table[@id='personList']/tbody/tr";

   public static final String VEHICLE_REGISTRATION = "id=vehicle:registrationField:registration";
   public static final String VEHICLE_STATE = "id=vehicle:stateField:state";
   public static final String VEHICLE_MAKE = "id=vehicle:makeField:make";
   public static final String VEHICLE_MODEL = "id=vehicle:modelField:model";
   public static final String VEHICLE_YEAR = "id=vehicle:yearField:year";
   public static final String VEHICLE_LINK = "id=VehicleId";
   public static final String VEHICLE_SAVE = "id=vehicle:save";
   public static final String VEHICLE_CANCEL = "id=vehicle:cancel";
   public static final String VEHICLE_DELETE = "id=vehicle:delete";
   public static final String VEHICLE_UPDATE = "id=vehicle:update";
   public static final int VEHICLE_IDENTIFIER_SIZE = 8;

   public static final String ENTITY_CREATE_BUTTON = "id=create";
   public static final String ENTITY_EDIT = "id=edit";
   public static final String ENTITY_DONE = "id=done";
   public static final String ENTITY_SELECT_PARENT_BUTTON = "xpath=//*[contains(@id, 'selectParent')]";
   public static final String VEHICLE_LIST_EDIT_BUTTON_BY_REGISTRATION = "xpath=//table[@id='vehicleList']/tbody/tr[normalize-space(td[1]/text()) = \"%s\"]//a[matches(@id, 'vehicleList:\\d+:vehicleEdit')]";
   public static final String VEHICLE_LIST_VIEW_BUTTON_BY_REGISTRATION = "xpath=//table[@id='vehicleList']/tbody/tr[normalize-space(td[1]/text()) = \"%s\"]//a[matches(@id, 'vehicleList:\\d+:vehicle')]";
   public static final String VEHICLE_LIST_RESULT_COUNT = "//table[@id='vehicleList']/tbody/tr";
   public static final String VEHICLE_LIST_ROW_BY_OWNER_NAME = "xpath=//table[@id='vehicleList']/tbody/tr[normalize-space(td[1]/text()) = \"%s\"]/td[normalize-space(text()) = \"%s\"]";

   /**
    * Execute generate-entities.sql script, run ./seam generate-entities, deploy
    * the application and wait for it to load.
    * 
    */
   @BeforeGroups(groups = "generate-entitiesTest")
   public void generateEntitiesTest()
   {
      // open and execute import script
      InputStream importScript = getClass().getResourceAsStream("/org/jboss/seam/test/functional/seamgen/generate-entities.sql");
      executeImportScript(importScript);

      seamGen.generateEntities();
      seamGen.restart();
      waitForAppToDeploy(HOME_PAGE, PERSON_LINK);
   }

   /**
    * Submit new person.
    */
   public void createNewPerson(String username, String address, Date birthday, String name)
   {
      browser.clickAndWait(PERSON_LINK);
      browser.clickAndWait(ENTITY_CREATE_BUTTON);
      fillPersonEditPage(username, address, birthday, name);
      browser.clickAndWait(PERSON_SAVE);
   }

   /**
    * Fill user details. Browser must be navigated to user's detail page before
    * executing this method. This method does not submit the form.
    */
   public void fillPersonEditPage(String username, String address, Date birthday, String name)
   {
      browser.type(PERSON_USERNAME, username);
      browser.type(PERSON_ADDRESS, address);
      selectDate(birthday);
      browser.type(PERSON_NAME, name);
   }

   /**
    * Select a date using icefaces or richfaces calendar component. Selecting
    * hardcoded values is only implemented yet.
    */
   public void selectDate(Date date)
   {
      // TODO
      final String richFaces = "id=person:birthdateField:birthdateDayCell24";
      final String iceFaces = "xpath=id('person:birthdateField')//table/tbody/tr[4]/td[4]/a";
      final String icefacesCalendarButton = "id=person:birthdateField:birthdate_cb";

      if (browser.isElementPresent(richFaces))
      {
         browser.click(richFaces);
      }
      else if (browser.isElementPresent(icefacesCalendarButton))
      {
         browser.click(icefacesCalendarButton);
         new Wait()
         {

            @Override
            public boolean until()
            {
               return browser.isElementPresent(iceFaces);
            }
         }.wait("Calendar did not appear.", Long.valueOf(SELENIUM_TIMEOUT));
         browser.click(iceFaces);
      }
      else
      {
         throw new RuntimeException("Unable to select date." + browser.getHtmlSource());
      }
   }

   /**
    * Submit new vehicle
    */
   public void createNewVehicle(String registration, String state, String make, String model, String year)
   {
      browser.clickAndWait(VEHICLE_LINK);
      browser.clickAndWait(ENTITY_CREATE_BUTTON);
      fillVehicleEditPage(registration, state, make, model, year);
      browser.clickAndWait(VEHICLE_SAVE);
   }

   /**
    * Fill vehicle details. Browser must be navigated to vehicle's detail page before
    * executing this method. This method does not submit the form.
    */
   public void fillVehicleEditPage(String registration, String state, String make, String model, String year)
   {
      browser.type(VEHICLE_REGISTRATION, registration);
      browser.type(VEHICLE_STATE, state);
      browser.type(VEHICLE_MAKE, make);
      browser.type(VEHICLE_MODEL, model);
      browser.type(VEHICLE_YEAR, year);
   }
}
