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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests role management in SeamSpace application
 * @author Jozef Hartinger
 *
 */
public class RoleTest extends SeleniumSeamSpaceTest
{

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.clickAndWait(SECURITY);
      browser.clickAndWait(MANAGE_ROLES);
   }

   @Test(dependsOnGroups = {"loginTest"})
   public void roleCreatingTest()
   {
      String roleName = "tester";
      String[] roles = { "user", "admin" };
      createNewRole(roleName, roles);
      // check that new role is added to role list
      String roleRow = MessageFormat.format(ROLE_TABLE_ROW_BY_NAME, roleName);
      assertTrue("New role not found in role list.", browser.isElementPresent(roleRow));
      String roleTableRoles = browser.getText(roleRow + ROLE_TABLE_ROLES);
      assertTrue("New role is not member of " + roles[0] + " role.", roleTableRoles.contains(roles[0]));
      assertTrue("New role is not member of " + roles[1] + " role.", roleTableRoles.contains(roles[1]));
      // check that new role is available to users
      browser.clickAndWait(SECURITY);
      browser.clickAndWait(MANAGE_USERS);
      browser.clickAndWait(CREATE_USER_BUTTON);
      String expectedRole = MessageFormat.format(USER_ROLE_BY_NAME_CHECKBOX, roleName);
      assertTrue("New role is not available when creating new user.", browser.isElementPresent(expectedRole));
   }
   
   @Test(dependsOnMethods = {"roleCreatingTest"})
   public void roleEditingTest() {
      String oldRoleName = "QA";
      String[] oldRoles = {"user"};
      String newRoleName = "QE";
      String[] newRoles = {"user", "admin"};
      
      createNewRole(oldRoleName, oldRoles);
      String oldRoleRow = MessageFormat.format(ROLE_TABLE_ROW_BY_NAME, oldRoleName);
      assertTrue("New role not found.", browser.isElementPresent(oldRoleRow));
      assertFalse("New role should not be member of admin role.", browser.getText(oldRoleRow + ROLE_TABLE_ROLES).contains("admin"));
      browser.clickAndWait(oldRoleRow + ROLE_TABLE_EDIT);
      fillRoleDetails(newRoleName, newRoles);
      browser.clickAndWait(ROLE_SAVE);
      String newRoleRow = MessageFormat.format(ROLE_TABLE_ROW_BY_NAME, newRoleName);
      assertFalse("Old role still present.", browser.isElementPresent(oldRoleRow));
      assertTrue("Updated role not found in role table.", browser.isElementPresent(newRoleRow));
      String updatedRoleTableRoles = browser.getText(newRoleRow + ROLE_TABLE_ROLES);
      assertTrue("New role is not member of " + newRoles[0] + " role.", updatedRoleTableRoles.contains(newRoles[0]));
      assertTrue("New role is not member of " + newRoles[1] + " role.", updatedRoleTableRoles.contains(newRoles[1]));
   }

   @Test(dependsOnMethods = {"roleCreatingTest"})
   public void roleDeletingTest() {
      String roleName = "commiter";
      String[] roles = {"user"};
      
      createNewRole(roleName, roles);
      String roleRow = MessageFormat.format(ROLE_TABLE_ROW_BY_NAME, roleName);
      assertTrue("New role not found.", browser.isElementPresent(roleRow));
      browser.chooseOkOnNextConfirmation();
      browser.clickAndWait(roleRow + ROLE_TABLE_DELETE);
      assertFalse("Removed role still present.", browser.isElementPresent(roleRow));
   }
   
   public void createNewRole(String name, String[] roles) {
      browser.clickAndWait(CREATE_ROLE_BUTTON);
      fillRoleDetails(name, roles);
      browser.clickAndWait(ROLE_SAVE);
   }
   
   public void fillRoleDetails(String name, String[] roles)
   {
      browser.type(ROLE_NAME, name);
      for (String role : roles)
      {
         assertTrue("Role not available: " + role, browser.isElementPresent(MessageFormat.format(ROLE_MEMBER_OF_BY_NAME_CHECKBOX, role)));
         browser.check(MessageFormat.format(ROLE_MEMBER_OF_BY_NAME_CHECKBOX, role));
      }
   }
}
