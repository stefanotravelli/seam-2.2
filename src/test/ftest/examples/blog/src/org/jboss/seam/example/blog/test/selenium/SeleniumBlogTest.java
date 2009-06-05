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
package org.jboss.seam.example.blog.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;

/**
 * This class holds basic methods for interacting with blog example.
 * @author Jozef Hartinger
 */
public class SeleniumBlogTest extends SeamSeleniumTest
{

   protected String password = "tokyo";

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH);
      browser.waitForPageToLoad(TIMEOUT);
   }

   protected void enterNewEntry(String id, String title, String excerpt, String body)
   {
      browser.click(getProperty("NEW_POST"));
      browser.waitForPageToLoad(TIMEOUT);
      if (browser.getLocation().contains(getProperty("LOGIN_URL")))
      {
         login();
      }
      fillNewEntryForm(id, title, excerpt, body);
   }

   protected void fillNewEntryForm(String id, String title, String excerpt, String body)
   {
      browser.type(getProperty("NEW_ENTRY_ID"), id);
      browser.type(getProperty("NEW_ENTRY_TITLE"), title);
      browser.type(getProperty("NEW_ENTRY_EXCERPT"), excerpt);
      browser.type(getProperty("NEW_ENTRY_BODY"), body);
      browser.click(getProperty("NEW_ENTRY_SUBMIT"));
      browser.waitForPageToLoad(TIMEOUT);
   }

   protected void login()
   {
      browser.type(getProperty("LOGIN_PASSWORD"), password);
      browser.click(getProperty("LOGIN_SUBMIT"));
      browser.waitForPageToLoad(TIMEOUT);
   }

}
