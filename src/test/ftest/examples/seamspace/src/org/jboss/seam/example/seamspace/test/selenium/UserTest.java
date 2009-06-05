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
package org.jboss.seam.example.seamspace.test.selenium;

import java.text.MessageFormat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * This class tests user management in SeamSpace application
 * @author Jozef Hartinger
 *
 */
public class UserTest extends SeleniumSeamSpaceTest
{

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.clickAndWait(SECURITY);
      browser.clickAndWait(MANAGE_USERS);
   }

   @Test(dependsOnGroups = {"loginTest"})
   public void userCreatingTest()
   {
      String username = "jharting";
      String password = "topSecret";
      String[] roles = {"admin", "user"};
      createNewUser("Jozef", "Hartinger", username, password, password, roles, true);
      String userRow = MessageFormat.format(USER_TABLE_ROW_BY_NAME, username);
      // check user list
      assertTrue("User not found in userlist.", browser.isElementPresent(userRow));
      assertTrue("User not in admin role.", browser.getText(userRow + USER_TABLE_ROLES).contains("admin"));
      assertTrue("User not in user role.", browser.getText(userRow + USER_TABLE_ROLES).contains("user"));
      assertTrue("User not enabled.", browser.isElementPresent(userRow + USER_TABLE_CHECKBOX_CHECKED));
      // check new user can login
      browser.clickAndWait(LOGOUT);
      login(username, password);
      assertTrue("Unable to login with new user's credentials.", isLoggedIn());
   }
   
   @Test(dependsOnGroups = {"loginTest"})
   public void userEditingTest() {
      String username = "shadowman";
      String password = "password";
      String[] roles = {"admin", "user"};
      String userRow = MessageFormat.format(USER_TABLE_ROW_BY_NAME, username);
      browser.clickAndWait(userRow + USER_TABLE_EDIT);
      fillUpdatableUserDetails(password, password, roles, true);
      browser.clickAndWait(USER_SAVE);
      assertTrue("User not in admin role.", browser.getText(userRow + USER_TABLE_ROLES).contains("admin"));
      assertTrue("User not in user role.", browser.getText(userRow + USER_TABLE_ROLES).contains("user"));
      browser.clickAndWait(LOGOUT);
      login(username, password);
      assertTrue("Unable to login with changed password", isLoggedIn());
   }
   
   @Test(dependsOnGroups = {"loginTest"})
   public void userDeletingTest() {
      String username = "mona";
      String userRow = MessageFormat.format(USER_TABLE_ROW_BY_NAME, username);
      assertTrue("User " + username + " not in user list.", browser.isElementPresent(userRow));
      browser.chooseOkOnNextConfirmation();
      browser.click(userRow + USER_TABLE_DELETE);
      browser.getConfirmation();
      browser.waitForPageToLoad(TIMEOUT);
      assertFalse("User " + username + " exists after deletion", browser.isElementPresent(userRow));
   }
   
   @Test(dependsOnGroups = {"loginTest"})
   public void cancelledUserDeletingTest() throws InterruptedException {
      String username = "demo";
      String userRow = MessageFormat.format(USER_TABLE_ROW_BY_NAME, username);
      assertTrue("User " + username + " not in user list.", browser.isElementPresent(userRow));
      browser.chooseCancelOnNextConfirmation();
      browser.click(userRow + USER_TABLE_DELETE);
      browser.getConfirmation();
      browser.refreshAndWait();
      assertTrue("User " + username + " missing in user list after cancelled deletion.", browser.isElementPresent(userRow));
   }
   
   @Test(dependsOnGroups = {"loginTest"}, dependsOnMethods={"userCreatingTest"})
   public void disablingUserAccountTest() {
      String username = "johny";
      String password = "password";
      String userRow = MessageFormat.format(USER_TABLE_ROW_BY_NAME, username);
      createNewUser("John", "Doe", username, password, password, new String[]{"user"}, false);
      assertTrue("User not found in userlist.", browser.isElementPresent(userRow));
      assertTrue("User account enabled.", browser.isElementPresent(userRow + USER_TABLE_CHECKBOX_UNCHECKED));
      browser.clickAndWait(LOGOUT);
      login(username, password);
      assertFalse("User logged in despite his account was disabled.", isLoggedIn());
   }
   
   private void createNewUser(String firstName, String lastName, String username, String password, String confirm, String[] roles, boolean enabled) {
      browser.clickAndWait(CREATE_USER_BUTTON);
      fillNewUserDetails(firstName, lastName, username, password, confirm, roles, enabled);
      browser.clickAndWait(USER_SAVE);
   }
   
   private void fillNewUserDetails(String firstName, String lastName, String username, String password, String confirm, String[] roles, boolean enabled)
   {
      browser.type(USER_FIRSTNAME, firstName);
      browser.type(USER_LASTNAME, lastName);
      browser.type(USER_NAME, username);
      fillUpdatableUserDetails(password, confirm, roles, enabled);
   }
   
   private void fillUpdatableUserDetails(String password, String confirm, String[] roles, boolean enabled) {
      browser.type(USER_PASSWORD, password);
      browser.type(USER_CONFIRM, confirm);
      for (String role : roles) {
         assertTrue("Unable to add user to role: " + role, browser.isElementPresent(MessageFormat.format(USER_ROLE_BY_NAME_CHECKBOX, role)));
         browser.check(MessageFormat.format(USER_ROLE_BY_NAME_CHECKBOX, role));
      }
      if (enabled)
      {
         browser.check(USER_ENABLED);
      }      
   }
}
