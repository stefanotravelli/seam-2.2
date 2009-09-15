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
package org.jboss.seam.example.openid.test.selenium;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class tests basic functionality of Seam OpenId example.
 * 
 * For executing this test a selenium.browser property is automaticly changed:
 * 
 * selenium.browser=*chrome   (to test Mozilla Firefox)
 * selenium.browser=*iehta    (to test Internet Explorer)
 * 
 * This parameter tells browser to increase its privileges to be able to
 * work across multiple domains.
 * 
 * 
 * @author Martin Gencur
 * 
 */
public class SeleniumOpenIdTest extends SeamSeleniumTest
{
   public static final String HOME_PAGE_TITLE = "OpenID Wall";
   public static final String LOGIN_LINK = "xpath=//input[@value='OpenID Login']";
   public static final String LOGIN_INPUT = "xpath=//input[@class='openid_input']";
   public static final String PASSWORD_INPUT = "id=password";
   public static final String SIGNIN_BUTTON = "id=signin_button";
   public static final String CONTINUE_BUTTON = "xpath=//button[contains(text(),'Continue')]";
   public static final String LOGOUT_BUTTON = "xpath=//input[@value='Logout']";
   
   public static String OPENID_ACCOUNT;
   public static String OPENID_PASSWORD;
   
   public static String ORIGINAL_BROWSER;
   
   @BeforeClass
   @Parameters( { "openid.account", "openid.password" })
   public void setCredentials(String account, String password) {
      OPENID_ACCOUNT = account;
      OPENID_PASSWORD = password;       
   }   
   
   @AfterClass
   public void returnBrowser(){
      super.setBrowser(ORIGINAL_BROWSER);
   }
   
   @BeforeMethod
   @Override
   public void setUp()
   {
      /*setting browser with enhanced security privileges for selenium*/
      setProperBrowser(SeamSeleniumTest.getBrowser());
      super.setUp();
      browser.open(CONTEXT_PATH);
   }

   /**
    * Place holder - just verifies that example deploys
    */
   @Test
   public void homePageLoadTest()
   {
      assertEquals("Unexpected page title.", HOME_PAGE_TITLE, browser.getTitle());
   }
   
   /**
    * Method verifies login and logout operations.
    */
   @Test(dependsOnMethods={"homePageLoadTest"})
   public void openIdLoginLogoutTest(){
      deleteCookies();
      browser.type(LOGIN_INPUT, OPENID_ACCOUNT);
      browser.clickAndWait(LOGIN_LINK);
      browser.type(PASSWORD_INPUT, OPENID_PASSWORD);
      browser.clickAndWait(SIGNIN_BUTTON);
      if (browser.isElementPresent(CONTINUE_BUTTON)) {
         browser.clickAndWait(CONTINUE_BUTTON);
      }
      assertTrue("Page should contain information about successfull login", browser.isTextPresent("OpenID login successful..."));
      browser.clickAndWait(LOGOUT_BUTTON);
      assertTrue("Page should contain input field which means that user is not logged in anymore", browser.isElementPresent(LOGIN_INPUT));
   }   
   
   private void deleteCookies(){
      browser.deleteCookie("session_id","");
      browser.deleteCookie("secure_session_id","");
   }   
   
   /**
    * Method for setting proper browser for selenium so that it can work 
    * across multiple domains.
    * 
    * @param origBrowser Originally intended browser
    */
   private void setProperBrowser(String origBrowser){
      ORIGINAL_BROWSER = origBrowser;      
      if (origBrowser.equals("*firefox") || origBrowser.equals("*firefoxproxy")) {
         super.setBrowser("*chrome");
      }      
      if (origBrowser.equals("*iexplore") || origBrowser.equals("*iexploreproxy")) {
         super.setBrowser("*iehta");
      }      
   }
}
