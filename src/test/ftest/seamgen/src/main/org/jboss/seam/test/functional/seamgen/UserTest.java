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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class verifies user management.
 * @author Jozef Hartinger
 *
 */
public class UserTest extends IdentityManagementTest
{

   @Override
   @BeforeMethod
   public void beforeMethod()
   {
      super.beforeMethod();
      login();
      browser.clickAndWait(IDENTITY_MANAGEMENT);
      browser.clickAndWait(MANAGE_USERS);
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void userCreatingTest()
   {
      String username = "jharting";
      String password = "topSecret";
      String[] roles = { "admin", "student", "pilot" };
      createNewUser(username, password, password, roles, true);
      String userRow = String.format(USER_TABLE_ROW_BY_NAME, username);
      // check user list
      assertTrue(browser.isElementPresent(userRow), "User not found in userlist.");
      for (String role : roles)
      {
         assertTrue(browser.getText(userRow + USER_TABLE_ROLES).contains(role), "User not in " + role + " role.");
      }
      assertTrue(browser.isElementPresent(userRow + USER_TABLE_CHECKBOX_CHECKED), "User not enabled.");
      // check new user can login
      browser.clickAndWait(LOGOUT);
      login(username, password);
      assertTrue(isLoggedIn(), "Unable to login with new user's credentials.");
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void userEditingTest()
   {
      String username = "shadowman";
      String newPassword = "password";
      String[] newRoles = { "admin", "tester", "designer" };
      String userRow = String.format(USER_TABLE_ROW_BY_NAME, username);
      browser.clickAndWait(userRow + USER_TABLE_EDIT);
      fillUpdatableUserDetails(newPassword, newPassword, newRoles, true);
      browser.clickAndWait(USER_SAVE);
      for (String role : newRoles)
      {
         assertTrue(browser.getText(userRow + USER_TABLE_ROLES).contains(role), "User not in " + role + " role.");
      }
      logout();
      login(username, newPassword);
      assertTrue(isLoggedIn(), "Unable to login with changed password");
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void userDeletingTest()
   {
      String username = "tester";
      String userRow = String.format(USER_TABLE_ROW_BY_NAME, username);
      assertTrue(browser.isElementPresent(userRow), "User " + username + " not in user list.");
      browser.chooseOkOnNextConfirmation();
      browser.click(userRow + USER_TABLE_DELETE);
      browser.getConfirmation();
      browser.waitForPageToLoad();
      assertFalse(browser.isElementPresent(userRow), "User " + username + " exists after deletion");
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void cancelledUserDeletingTest() throws InterruptedException
   {
      String username = "demo";
      String userRow = String.format(USER_TABLE_ROW_BY_NAME, username);
      assertTrue(browser.isElementPresent(userRow), "User " + username + " not in user list.");
      browser.chooseCancelOnNextConfirmation();
      browser.click(userRow + USER_TABLE_DELETE);
      browser.getConfirmation();
      browser.refreshAndWait();
      assertTrue(browser.isElementPresent(userRow), "User " + username + " missing in user list after cancelled deletion.");
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void disablingUserAccountTest()
   {
      String username = "johndoe";
      String password = "password";
      String userRow = String.format(USER_TABLE_ROW_BY_NAME, username);
      createNewUser(username, password, password, new String[] { "admin", "student" }, false);
      assertTrue(browser.isElementPresent(userRow), "User not found in userlist.");
      assertTrue(browser.isElementPresent(userRow + USER_TABLE_CHECKBOX_UNCHECKED), "User account enabled.");
      logout();
      login(username, password);
      assertFalse(isLoggedIn(), "User logged in despite his account was disabled.");
   }

   private void createNewUser(String username, String password, String confirm, String[] roles, boolean enabled)
   {
      browser.clickAndWait(CREATE_USER_BUTTON);
      fillNewUserDetails(username, password, confirm, roles, enabled);
      browser.clickAndWait(USER_SAVE);
   }

   private void fillNewUserDetails(String username, String password, String confirm, String[] roles, boolean enabled)
   {
      browser.type(USER_NAME, username);
      fillUpdatableUserDetails(password, confirm, roles, enabled);
   }

   private void fillUpdatableUserDetails(String password, String confirm, String[] roles, boolean enabled)
   {
      browser.type(USER_PASSWORD, password);
      browser.type(USER_CONFIRM, confirm);
      for (String role : roles)
      {
         assertTrue(browser.isElementPresent(String.format(USER_ROLE_BY_NAME_CHECKBOX, role)), "Unable to add user to role: " + role);
         browser.check(String.format(USER_ROLE_BY_NAME_CHECKBOX, role));
      }
      if (enabled)
      {
         browser.check(USER_ENABLED);
      } else {
         browser.uncheck(USER_ENABLED);
      }
   }
}
