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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * This class is responsible for creating new project and verifying that deployment
 * and basic functionality works.
 * 
 * @author Jozef Hartinger
 * 
 */
public class NewProjectTest extends SeleniumSeamGenTest
{

   @BeforeGroups(groups = { "newProjectGroup" })
   public void setup() throws FileNotFoundException, IOException, InterruptedException
   {

      // save properties
      seamGenProperties.store(new FileOutputStream(SEAMGEN_PROPERTIES_FILE), "Created by seam-gen functional testsuite.");
      seamGen.createProject();
      seamGen.deploy();
      waitForAppToDeploy(HOME_PAGE, FOOTER);
   }

   @Test(groups = { "newProjectGroup" })
   public void validLoginTest()
   {
      login();
      // verify login
      assertTrue(browser.isElementPresent(LOGOUT), "Logout link expected.");
      assertTrue(browser.getText(SIGNED_USER).contains(DEFAULT_USERNAME), "Username not found. " + browser.getText(SIGNED_USER) + " found instead.");
      // logout
      browser.clickAndWait(LOGOUT);
      assertTrue(browser.isElementPresent(LOGIN), "Login link expected.");
      assertTrue(browser.isElementPresent(HOME), "Home link expected.");
   }

   @Test(groups = { "newProjectGroup" })
   public void invalidLoginTest()
   {
      String username = "badUser";
      String password = "password";

      login(username, password);
      // verify login
      assertTrue(browser.isElementPresent(LOGIN), "User should not be logged in.");
      assertTrue(browser.getText(MESSAGES).contains(LOGIN_FAILED_MESSAGE), LOGIN_FAILED_MESSAGE + " expected.");
   }
}
