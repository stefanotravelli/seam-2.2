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
package org.jboss.seam.example.seambay.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * Main class for SeamBay example tests
 * 
 * @author Jozef Hartinger
 * 
 */
public class SeleniumSeamBayTest extends SeamSeleniumTest
{

   protected String defaultLogin = "demo";
   protected String defaultPassword = "demo";

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + getProperty("HOME_PAGE"));
      browser.waitForPageToLoad(TIMEOUT);
   }

   public void login(String username, String password)
   {
      if (isLoggedIn())
      {
         fail("User already logged in.");
      }
      browser.clickAndWait(getProperty("LOGIN"));
      submitLoginForm(username, password);
   }

   public void login()
   {
      login(defaultLogin, defaultPassword);
   }

   public boolean isLoggedIn()
   {
      return browser.isElementPresent(getProperty("LOGOUT"));
   }

   public void submitRegistrationForm(String username, String password, String verify, String location)
   {
      assertTrue("Registration page expected.", browser.getLocation().contains(getProperty("REGISTRATION_URL")));
      browser.type(getProperty("REGISTRATION_USERNAME"), username);
      browser.type(getProperty("REGISTRATION_PASSWORD"), password);
      browser.type(getProperty("REGISTRATION_VERIFY"), verify);
      browser.type(getProperty("REGISTRATION_LOCATION"), location);
      browser.clickAndWait(getProperty("REGISTRATION_SUBMIT"));
   }

   public void submitLoginForm(String username, String password)
   {
      browser.type(getProperty("LOGIN_USERNAME"), username);
      browser.type(getProperty("LOGIN_PASSWORD"), password);
      browser.clickAndWait(getProperty("LOGIN_SUBMIT"));
   }

   public int search(String keyword)
   {
      browser.type(getProperty("SEARCH_FIELD"), keyword);
      browser.clickAndWait(getProperty("SEARCH_SUBMIT"));
      return browser.getXpathCount(getProperty("SEARCH_RESULTS_COUNT")).intValue();
   }

}
