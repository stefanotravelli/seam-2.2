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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * This test verifies user role management
 * @author Jozef Hartinger
 *
 */
public class RoleTest extends IdentityManagementTest
{
   public static final String MANAGE_ROLES = "id=manageRoles";
   public static final String CREATE_ROLE_BUTTON = "id=newRole";
   public static final String ROLE_NAME = "id=roleForm:roleField:role";
   public static final String ROLE_ROLES = "id=role:roles";
   public static final String ROLE_MEMBER_OF_BY_NAME_CHECKBOX = "xpath=//input[matches(@id, 'roleForm:groupsField:groups:[_]{0,1}[\\d]+')][normalize-space(../label/text()) = '%s']";
   public static final String ROLE_SAVE = "id=roleForm:save";
   public static final String ROLE_CANCEL = "id=roleForm:cancel";
   public static final String ROLE_TABLE_ROW_BY_NAME = "xpath=//table[@id='roles']/tbody/tr[normalize-space(./td[1]/text()) = '%s']";
   // these locators can only be used catenated with ROLE_TABLE_ROW_BY_NAME
   public static final String ROLE_TABLE_ROLES = "/td[2]";
   public static final String ROLE_TABLE_DELETE = "//a[matches(@id, 'roles:\\d+:delete')]";
   public static final String ROLE_TABLE_EDIT = "//a[matches(@id, 'roles:\\d+:edit')]";

   @Override
   @BeforeMethod
   public void beforeMethod()
   {
      super.beforeMethod();
      login();
      browser.clickAndWait(IDENTITY_MANAGEMENT);
      browser.clickAndWait(MANAGE_ROLES);
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void roleCreatingTest()
   {
      String roleName = "golfer";
      String[] roles = { "admin", "designer" };
      createNewRole(roleName, roles);
      // check that new role is added to role list
      String roleRow = String.format(ROLE_TABLE_ROW_BY_NAME, roleName);
      assertTrue(browser.isElementPresent(roleRow), "New role not found in role list.");
      String roleTableRoles = browser.getText(roleRow + ROLE_TABLE_ROLES);
      for (String role : roles)
      {
         assertTrue(roleTableRoles.contains(role), "New role is not member of " + role + " role.");
      }
      // check that new role is available to users
      browser.clickAndWait(IDENTITY_MANAGEMENT);
      browser.clickAndWait(MANAGE_USERS);
      browser.clickAndWait(CREATE_USER_BUTTON);
      String expectedRole = String.format(USER_ROLE_BY_NAME_CHECKBOX, roleName);
      assertTrue(browser.isElementPresent(expectedRole), "New role is not available when creating new user.");
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void roleEditingTest()
   {
      String oldRoleName = "QA";
      String newRoleName = "QE";
      String[] newRoles = { "student", "admin", "pilot", "designer" };

      String oldRoleRow = String.format(ROLE_TABLE_ROW_BY_NAME, oldRoleName);
      browser.clickAndWait(oldRoleRow + ROLE_TABLE_EDIT);
      fillRoleDetails(newRoleName, newRoles);
      browser.clickAndWait(ROLE_SAVE);
      String newRoleRow = String.format(ROLE_TABLE_ROW_BY_NAME, newRoleName);
      assertFalse(browser.isElementPresent(oldRoleRow), "Old role still present.");
      assertTrue(browser.isElementPresent(newRoleRow), "Updated role not found in role table.");
      String updatedRoleTableRoles = browser.getText(newRoleRow + ROLE_TABLE_ROLES);
      for (String role : newRoles)
      {
         assertTrue(updatedRoleTableRoles.contains(role), "New role is not member of " + role + " role.");
      }
   }

   @Test(groups = { "identityManagement" }, dependsOnGroups = { "newProjectGroup", "generate-entitiesTest" }, alwaysRun = true)
   public void roleDeletingTest()
   {
      String roleName = "commiter";

      String roleRow = String.format(ROLE_TABLE_ROW_BY_NAME, roleName);
      browser.chooseOkOnNextConfirmation();
      browser.clickAndWait(roleRow + ROLE_TABLE_DELETE);
      assertFalse(browser.isElementPresent(roleRow), "Removed role still present.");
   }

   public void createNewRole(String name, String[] roles)
   {
      browser.clickAndWait(CREATE_ROLE_BUTTON);
      fillRoleDetails(name, roles);
      browser.clickAndWait(ROLE_SAVE);
   }

   public void fillRoleDetails(String name, String[] roles)
   {
      browser.type(ROLE_NAME, name);
      for (String role : roles)
      {
         assertTrue(browser.isElementPresent(String.format(ROLE_MEMBER_OF_BY_NAME_CHECKBOX, role)), "Role not available: " + role);
         browser.check(String.format(ROLE_MEMBER_OF_BY_NAME_CHECKBOX, role));
      }
   }

}
