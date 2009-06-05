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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.testng.annotations.BeforeGroups;

/**
 * Base class for Identity Management tests. Every test method of this class or
 * its subclasses should be member of "identityManagement" group and should
 * depend on "newProjectGroup" and "generate-entitiesTest" groups to be executed
 * in appropriate order. generate-entities.sql script is executed prior to
 * "identityManagement" group to feed the database with test values.
 * 
 * @author Jozef Hartinger
 * 
 */
public class IdentityManagementTest extends DatabaseTest
{

   public static final String IDENTITY_MANAGEMENT = "id=identityManagement";
   public static final String MANAGE_USERS = "id=manageUsers";
   public static final String CREATE_USER_BUTTON = "id=newUser";
   public static final String USER_NAME = "id=userForm:usernameField:username";
   public static final String USER_PASSWORD = "id=userForm:passwordField:password";
   public static final String USER_CONFIRM = "id=userForm:confirmField:confirm";
   public static final String USER_ROLES = "id=userForm:rolesField:roles";
   public static final String USER_ROLE_BY_NAME_CHECKBOX = "xpath=//input[matches(@id, 'userForm:rolesField:roles:[_]{0,1}[\\d]+')][normalize-space(../label/text()) = '%s']";
   public static final String USER_ENABLED = "id=userForm:enabledField:enabled";
   public static final String USER_SAVE = "id=userForm:save";
   public static final String USER_CANCEL = "id=userForm:cancel";
   public static final String USER_TABLE_ROW_BY_NAME = "xpath=//table[@id='users' or @id='usersCmdForm:users']/tbody/tr[normalize-space(./td[1]/text()) = '%s']";
   public static final String USER_TABLE_ROLES = "/td[2]";
   public static final String USER_TABLE_CHECKBOX_CHECKED = "/td[3]/div[@class='status-true']";
   public static final String USER_TABLE_CHECKBOX_UNCHECKED = "/td[3]/div[@class='status-false']";
   public static final String USER_TABLE_DELETE = "//a[matches(@id, 'users:\\d+:delete') or matches(@id, 'usersCmdForm:users:\\d+:delete')]";
   public static final String USER_TABLE_EDIT = "//a[matches(@id, 'users:\\d+:edit') or matches(@id, 'usersCmdForm:users:\\d+:edit')]";

   @BeforeGroups(groups = "identityManagement")
   public void addIdentityManagement() throws FileNotFoundException
   {
      seamGen.addIdentityManagement();
      seamGen.restart();
      waitForAppToDeploy(HOME_PAGE, FOOTER);

      // execute testing import script
      InputStream is = getClass().getResourceAsStream("/org/jboss/seam/test/functional/seamgen/identity-management.sql");
      executeImportScript(is);
      // execute default identity management import script
      String scriptPath = WORKSPACE + "/" + APP_NAME + "/resources/import-dev.sql";
      executeImportScript(new FileInputStream(scriptPath));
   }

}
