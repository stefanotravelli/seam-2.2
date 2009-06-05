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

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import static org.testng.AssertJUnit.fail;

/**
 * 
 * @author Jozef Hartinger
 *
 */
public abstract class SeleniumSeamSpaceTest extends SeamSeleniumTest
{
   
   public static final String LOGIN = "id=login";
   public static final String LOGOUT = "id=logout";
   public static final String LOGIN_USERNAME = "id=loginForm:name";
   public static final String LOGIN_PASSWORD = "id=loginForm:password";
   public static final String LOGIN_LOGIN = "id=loginForm:login";
   public static final String HOME_URL = "/home.seam";
   public static final String SECURITY = "id=security";
   
   public static final String MANAGE_USERS = "id=manageUsers";
   public static final String CREATE_USER_BUTTON = "id=newUser";
   public static final String USER_FIRSTNAME = "id=user:firstname";
   public static final String USER_LASTNAME = "id=user:lastname";
   public static final String USER_NAME = "id=user:username";
   public static final String USER_PASSWORD = "id=user:password";
   public static final String USER_CONFIRM = "id=user:confirm";
   public static final String USER_ROLES = "id=user:roles";
   public static final String USER_ROLE_BY_NAME_CHECKBOX = "xpath=//input[contains(@id, \"user:roles:\")][normalize-space(../label/text()) = \"{0}\"]";
   public static final String USER_ENABLED = "id=user:enabled";
   public static final String USER_SAVE = "id=user:save";
   public static final String USER_CANCEL = "id=user:cancel";
   public static final String USER_TABLE_ROW_BY_NAME = "xpath=//table[@id=\"threads\"]/tbody/tr[normalize-space(./td[1]/text()) = \"{0}\"]";
   // these locators can only be used catenated with USER_TABLE_ROW_BY_NAME
   public static final String USER_TABLE_ROLES = "/td[2]";
   public static final String USER_TABLE_CHECKBOX_CHECKED = "/td[3]/div[@class=\"checkmark\"]";
   public static final String USER_TABLE_CHECKBOX_UNCHECKED = "/td[3]/div[@class=\"cross\"]";
   public static final String USER_TABLE_DELETE = "//a[contains(@id, \"delete\")]";
   public static final String USER_TABLE_EDIT = "//a[contains(@id, \"edit\")]";
   
   public static final String MANAGE_ROLES = "id=manageRoles";
   public static final String CREATE_ROLE_BUTTON = "id=newRole";
   public static final String ROLE_NAME = "id=role:name";
   public static final String ROLE_ROLES = "id=role:roles";
   public static final String ROLE_MEMBER_OF_BY_NAME_CHECKBOX = "xpath=//input[contains(@id, \"role:roles:\")][normalize-space(../label/text()) = \"{0}\"]";
   public static final String ROLE_SAVE = "id=role:save";
   public static final String ROLE_CANCEL = "id=role:cancel";
   public static final String ROLE_TABLE_ROW_BY_NAME = "xpath=//table[@id=\"threads\"]/tbody/tr[normalize-space(./td[1]/text()) = \"{0}\"]";
   // these locators can only be used catenated with ROLE_TABLE_ROW_BY_NAME
   public static final String ROLE_TABLE_ROLES = "/td[2]";
   public static final String ROLE_TABLE_DELETE = "//a[contains(@id, \"delete\")]";
   public static final String ROLE_TABLE_EDIT = "//a[contains(@id, \"edit\")]";
   
   public static final String DEFAULT_USERNAME = "demo";
   public static final String DEFAULT_PASSWORD = "demo";
   
   @Override
   @BeforeMethod
   public void setUp() {
      super.setUp();
      browser.open(CONTEXT_PATH + HOME_URL);
      login();
   }

   public void login() {
      login(DEFAULT_USERNAME, DEFAULT_PASSWORD);
   }
   
   public void login(String username, String password) {
      if (isLoggedIn()) {
         fail("User already logged in.");
      }
      browser.clickAndWait(LOGIN);
      browser.type(LOGIN_USERNAME, username);
      browser.type(LOGIN_PASSWORD, password);
      browser.clickAndWait(LOGIN_LOGIN);
   }

   protected boolean isLoggedIn()
   {
      return !browser.isElementPresent(LOGIN) && browser.isElementPresent(LOGOUT);
   }
}
