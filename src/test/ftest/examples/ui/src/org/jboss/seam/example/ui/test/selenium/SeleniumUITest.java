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
package org.jboss.seam.example.ui.test.selenium;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests functionality of UI example
 * 
 * @author Martin Gencur
 * 
 */
public class SeleniumUITest extends SeamSeleniumTest
{
      public static final String HOME_PAGE = "/index.seam";
      public static final String HOME_PAGE_TITLE = "UI Example:";
      public static final String SELECT_ITEMS_LINK = "xpath=//a[contains(@href,\"selectItems\")]";
      public static final String FRAGMENT_LINK = "xpath=//a[contains(@href,\"fragment\")]";
      public static final String FOTMATTED_TEXT_LINK = "xpath=//a[contains(@href,\"formattedText\")]";
      public static final String BUTTON_AND_SLINK_LINK = "xpath=//a[contains(@href,\"linkAndButton\")]";
      public static final String CACHE_LINK = "xpath=//a[contains(@href,\"cache\")]";
      public static final String VALIDATE_EQUALITY_LINK = "xpath=//a[contains(@href,\"equalityValidator\")]";
      public static final String VALIDATE_EQUALITY2_LINK = "xpath=//a[contains(@href,\"equalityValidatorWConvert\")]";
      public static final String RESOURCE_DOWNLOAD_LINK = "xpath=//a[contains(@href,\"resource\")]";
               
      @BeforeMethod
      @Override
      public void setUp()
      {
         super.setUp();
         browser.open(CONTEXT_PATH + HOME_PAGE);
      }

      /**
       * Place holder - just verifies that example deploys
       */
      @Test
      public void homePageLoadTest()
      {
         assertEquals("Unexpected page title.", HOME_PAGE_TITLE, browser.getTitle());
      }        
       
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void selectItemsTest(){      
         String title = "Mr.";
         String name = "Martin Gencur";
         String continent = "Europe";
         String age = "24";
         String pet = "Dog (Needs lots of exercise)";
         String colour1 = "Green", colour2 = "Yellow";
         String book = "Pride and Prejudice by Jane Austin (British)";
         String film = "Blade Runner directed by Ridley Scott";
         browser.clickAndWait(SELECT_ITEMS_LINK);
         browser.select(getProperty("SELECT_ITEMS_TITLE"), "label="+title);
         browser.type(getProperty("SELECT_ITEMS_NAME"), name);
         browser.select(getProperty("SELECT_ITEMS_CONTINENT"), "label="+continent);
         browser.check(getProperty("SELECT_ITEMS_USER"));
         browser.check(getProperty("SELECT_ITEMS_ADMIN"));
         browser.check(getProperty("SELECT_ITEMS_MANAGER"));
         browser.check(getProperty("SELECT_ITEMS_SUPERADMIN"));
         browser.select(getProperty("SELECT_ITEMS_AGE"), "label="+age);
         browser.select(getProperty("SELECT_ITEMS_PET"), "label="+pet);
         browser.select(getProperty("SELECT_ITEMS_COLOURS"), "label="+colour1);
         browser.select(getProperty("SELECT_ITEMS_COLOURS"), "label="+colour2);         
         browser.select(getProperty("SELECT_ITEMS_BOOK"), "label="+book);
         browser.select(getProperty("SELECT_ITEMS_FILM"), "label="+film); 
         browser.clickAndWait(getProperty("SELECT_ITEMS_APPLY"));
         browser.check(getProperty("SELECT_ITEMS_COUNTRY"));
         browser.clickAndWait(getProperty("SELECT_ITEMS_APPLY"));
         assertTrue("Page should contain \"Successfully updated\"", browser.isTextPresent("Successfully updated"));
      }
      
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void fragmentTest(){ 
         browser.clickAndWait(FRAGMENT_LINK);
         assertTrue("Page should contain \"fragment is rendered\"", browser.isTextPresent("This fragment is rendered whilst"));
      }      
      
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void formattedTextTest(){ 
         browser.clickAndWait(FOTMATTED_TEXT_LINK);
         assertTrue("Page should contain information about Pete Muir working all the time on Seam", browser.isTextPresent("works on Seam, of course"));
      }
          
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void buttonAndLinkTest(){ 
         browser.clickAndWait(BUTTON_AND_SLINK_LINK);  
         assertTrue("Page should contain \"A fragment to jump to\"", browser.isTextPresent("A fragment to jump to"));
         browser.clickAndWait(getProperty("JUMP_LINK"));
         browser.clickAndWait(getProperty("JUMP_BUTTON"));
         browser.clickAndWait(getProperty("LINK_LINK"));
         browser.clickAndWait(getProperty("DO_ACTION_LINK"));
         assertTrue("Page should contain \"A simple action was performed\"", browser.isTextPresent("A simple action was performed"));
         browser.clickAndWait(getProperty("DO_ACTION_BUTTON"));
         assertTrue("Page should contain \"A simple action was performed\"", browser.isTextPresent("A simple action was performed"));
         assertTrue("Page should contain disabled link", browser.isElementPresent(getProperty("DISABLED_DO_ACTION_LINK")));
         assertTrue("Page should contain disabled button", browser.isElementPresent(getProperty("DISABLED_DO_ACTION_BUTTON")));
         browser.clickAndWait(getProperty("BEGIN_CONVERSATION_LINK"));
         browser.clickAndWait(getProperty("END_CONVERSATION_BUTTON"));
         assertTrue("Page shouldn't contain \"A simple action was performed\"", !browser.isTextPresent("A simple action was performed"));
         browser.clickAndWait(getProperty("ADD_PARAMETER_LINK"));
         browser.clickAndWait(getProperty("ADD_PARAMETER_BUTTON"));
         assertTrue("Page should contain \"Foo = bar\"", browser.isTextPresent("Foo = bar"));      
      }
      
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void cacheTest(){ 
         browser.clickAndWait(CACHE_LINK);         
         assertTrue("Page should contain some cached text", browser.isTextPresent("Some cached text"));
      }
      
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void validateEqualityTest(){ 
         String name1 = "martin";
         String name2 = "peter";   
         String age1 = "20";
         String age2 = "30";
         browser.clickAndWait(VALIDATE_EQUALITY_LINK);
         
         browser.type(getProperty("NAME_INPUT"), name1);
         browser.type(getProperty("NAME_VERIFICATION_INPUT"), name1);
         browser.clickAndWait(getProperty("CHECK_NAME_BUTTON"));
         assertTrue("Page should contain \"OK!\""+ "je tam:" + browser.getBodyText(), browser.isTextPresent("OK!"));
         
         browser.type(getProperty("NAME_INPUT"), name1);
         browser.type(getProperty("NAME_VERIFICATION_INPUT"), name2);
         browser.clickAndWait(getProperty("CHECK_NAME_BUTTON"));
         assertTrue("Page should contain \"Must be the same as name!\"", browser.isTextPresent("Must be the same as name!"));
         
         browser.type(getProperty("MINIMUM_AGE_INPUT"), age1);
         browser.type(getProperty("MAXIMUM_AGE_INPUT"), age2);
         browser.clickAndWait(getProperty("CHECK_AGES_BUTTON"));
         assertTrue("Page should contain \"OK!\"", browser.isTextPresent("OK!"));
         browser.type(getProperty("MINIMUM_AGE_INPUT"), age1);
         browser.type(getProperty("MAXIMUM_AGE_INPUT"), age1);
         browser.clickAndWait(getProperty("CHECK_AGES_BUTTON"));
         assertTrue("Page should contain \"Must be larger than minimum!\"", browser.isTextPresent("Must be larger than minimum!"));
         browser.type(getProperty("MINIMUM_AGE_INPUT"), age2);
         browser.type(getProperty("MAXIMUM_AGE_INPUT"), age1);
         browser.clickAndWait(getProperty("CHECK_AGES_BUTTON"));
         assertTrue("Page should contain \"Must be larger than minimum!\"", browser.isTextPresent("Must be larger than minimum!"));        
      }
      
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void validateEquality2Test(){ 
         String date1 = "2009-08-21";
         String date2 = "2009-08-25";
         browser.clickAndWait(VALIDATE_EQUALITY2_LINK);
         
         browser.type(getProperty("DATE_INPUT"), date1);
         browser.type(getProperty("DATE_VERIFICATION_INPUT"), date1);
         browser.clickAndWait(getProperty("CHECK_DATE_BUTTON"));
         assertTrue("Page should contain \"OK!\"", browser.isTextPresent("OK!"));
         
         browser.type(getProperty("DATE_INPUT"), date1);
         browser.type(getProperty("DATE_VERIFICATION_INPUT"), date2);
         browser.clickAndWait(getProperty("CHECK_DATE_BUTTON"));
         assertTrue("Page should contain \"Value does not equal that in 'date'\"", browser.isTextPresent("Value does not equal"));
         //assertTrue("Page should contain information about Pete Muir working all the time on Seam", browser.isTextPresent("works on Seam, of course"));
      }
      
      @Test(dependsOnMethods={"homePageLoadTest"})
      public void resourceDownloadTest(){
         String textToFind1 = "abc";
         String textToFind2 = "123";
         browser.clickAndWait(RESOURCE_DOWNLOAD_LINK);

         assertTrue( "File download failed: Restful with s:download \"Text\"", isDownloadWorking("/seam-ui/resources.seam?id=1", textToFind1));
         assertTrue( "File download failed: Restful with s:download \"Numbers\"", isDownloadWorking("/seam-ui/resources.seam?id=2", textToFind2));         
      }
      
      private boolean isDownloadWorking(String pathToFile, String textToFind)
      {
         try 
         {
            URL downloadUrl = new URL("http://localhost:8080" + pathToFile);
            BufferedReader r = new BufferedReader(new InputStreamReader(downloadUrl.openStream()));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = r.readLine()) != null)
            {
               sb.append(str);
            }
            return sb.toString().contains(textToFind);
         } 
         catch (IOException e) 
         {
            return false;
         } 
      }
}
