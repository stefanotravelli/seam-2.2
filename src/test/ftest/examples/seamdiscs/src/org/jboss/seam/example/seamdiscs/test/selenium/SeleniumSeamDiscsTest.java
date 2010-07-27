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
package org.jboss.seam.example.seamdiscs.test.selenium;

import java.text.MessageFormat;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import com.thoughtworks.selenium.SeleniumException;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * 
 * @author Ondrej Skutka
 *
 */
public abstract class SeleniumSeamDiscsTest extends SeamSeleniumTest
{
   // TODO final
   public static String LOGIN = "id=loginlink";
   public static String LOGOUT = "id=logout";
   public static String LOGIN_USERNAME = "id=login:username";
   public static String LOGIN_PASSWORD = "id=login:password";
   public static String LOGIN_LOGIN = "id=login:loginbutton";
   public static String HOME_URL = "/home.seam";
//   public static String SECURITY = "id=security";
//   
   public static String MANAGE_DISCS = "id=manageDiscs";
   public static String CREATE_ARTIST_BUTTON = "id=addArtist";
   public static String CREATE_BAND_BUTTON = "id=addBand";
   public static String ARTIST_FILTER = "//form[1]//input[@type='text']";
   public static String ARTIST_NAME = "id=artist:name";
   public static String ARTIST_CREATE_DISC_BUTTON = "id=artist:addDisc";
   public static String ARTIST_DESCRIPTION = "id=artist:description";
   public static String ARTIST_PERSIST = "id=artist:persist";
   public static String ARTIST_UPDATE = "id=artist:update";
   public static String ARTIST_CANCEL = "id=artist:cancel";
   public static String ARTIST_ADD_BAND_MEMBER = "id=artist:addBandMember";
   public static String ARTIST_NTH_BAND_MEMBER = "xpath=id(\"artist\")//tr[1]//tr[3]/td[2]//ul/li[{0}]//input[@type=\"text\"]";
   public static String ARTIST_LAST_BAND_MEMBER = MessageFormat.format(ARTIST_NTH_BAND_MEMBER, "last()");
   
   
   public static String ARTIST_NTH_DISC = "xpath=id(\"artist:discs\")/table/tbody/tr/td/table/tbody/tr[{0}]";
   public static String ARTIST_NTH_DISC_SHOW_DETAILS = ARTIST_NTH_DISC + "/td[1]/div/a[2]";
   public static String ARTIST_NTH_DISC_NAME = ARTIST_NTH_DISC + "/td[2]/input";
   public static String ARTIST_NTH_DISC_YEAR = ARTIST_NTH_DISC + "/td[3]//input";
   public static String ARTIST_NTH_DISC_DETAIL = ARTIST_NTH_DISC + "/td";
   public static String ARTISTS_FIRST_ARTIST_LINK = "xpath=id(\"artists\")//table/tbody/tr/td/table/tbody/tr[2]/td[2]/a";
   public static String ARTISTS_NEXT_PAGE_LINK = "xpath=id(\"artists\")//td[1]//td[2]//td[5]/a";
   public static String ARTIST_TABLE_ROW_BY_NAME = "xpath=id(\"artists\")//tr[normalize-space(td/a/text())=\"{0}\"]";
   // these locators can only be used catenated with ARTIST_TABLE_ROW_BY_NAME
   public static String ARTIST_TABLE_ROW_LINK = ARTIST_TABLE_ROW_BY_NAME + "/td[2]/a";

   
   public static String MANAGE_ARTISTS = "id=manageArtists";
   public static String CREATE_DISC_BUTTON = "id=addDisc";
   public static String DISC_DETAIL_TITLE = "xpath=id(\"disc\")//tr//tr[2]/td[2]/input";
   public static String DISC_DETAIL_RELEASE_DATE = "xpath=id(\"disc\")//tr//tr[3]/td[2]//input";
   public static String DISC_DETAIL_ARTIST = "xpath=id(\"disc\")//tr//tr[4]//select";
   public static String DISC_DETAIL_DESCRIPTION = "id=description";
   public static String DISC_DETAIL_UPDATE = "id=update";
   public static String DISC_DETAIL_PERSIST = "id=persist";
   public static String DISC_DETAIL_REMOVE = "id=remove";
   public static String DISC_DETAIL_CANCEL = "id=cancel";
   public static String DISCS_NEXT_PAGE_LINK = "xpath=id(\"discs\")//td[1]//td[2]//td[5]/a";
   public static String DISC_TABLE_ROW_BY_NAME = "xpath=id(\"discs\")//tr[normalize-space(td/a/text())=\"{0}\"]";
   // these locators can only be used catenated with DISC_TABLE_ROW_BY_NAME
   public static String DISC_TABLE_ROW_LINK = DISC_TABLE_ROW_BY_NAME + "/td[2]/a";
   
   public static String EMPTY_DISC_DESCRIPTION = "None known";
   
   public static String DEFAULT_USERNAME = "administrator";
   public static String DEFAULT_PASSWORD = "administrator";
   
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

   /**
    * Checks whether specified disc contains expected data.
    */
   protected void checkDisc(int tableRow, String expectedDiscTitle, String expectedReleaseDate, String expectedDescription) {
      tableRow++; // first row is header
      String actualDiscTitle = browser.getAttribute(MessageFormat.format(ARTIST_NTH_DISC_NAME, tableRow) + "@value");
      assertTrue("This is not expected (" + expectedDiscTitle + ") album (" + actualDiscTitle + ")!", actualDiscTitle.equals(expectedDiscTitle));
      
      String actualReleaseDate = "";
      if (browser.isElementPresent(MessageFormat.format(ARTIST_NTH_DISC_YEAR, tableRow))) {
         try {
            actualReleaseDate = browser.getAttribute(MessageFormat.format(ARTIST_NTH_DISC_YEAR, tableRow) + "@value");
         } catch (SeleniumException ex) {
            // intentianally left blank
         }
         assertTrue("This is not expected (" + expectedReleaseDate + ") release date (" + actualReleaseDate + ")!", actualReleaseDate.equals(expectedReleaseDate));
      }

      browser.clickAndWait(MessageFormat.format(ARTIST_NTH_DISC_SHOW_DETAILS, tableRow));
      String actualDescription = browser.getText(MessageFormat.format(ARTIST_NTH_DISC_DETAIL, tableRow + 1));
      assertTrue("This is not expected (" + expectedDescription + ") description (" + actualDescription + ")!", actualDescription.equals(expectedDescription));
      browser.clickAndWait(MessageFormat.format(ARTIST_NTH_DISC_SHOW_DETAILS, tableRow));
   }

   /**
    * Checks whether specified disc contains expected data.
    */
   protected void checkDiscDetail(String expectedDiscTitle, String expectedReleaseDate, String expectedDescription, String expectedArtist) {
      String actualDiscTitle = browser.getAttribute(DISC_DETAIL_TITLE + "@value");
      assertTrue("This is not expected (" + expectedDiscTitle + ") album: (" + actualDiscTitle + ")!", actualDiscTitle.equals(expectedDiscTitle));
      
      String actualReleaseDate = "";
      if (browser.isElementPresent(DISC_DETAIL_RELEASE_DATE + "/attribute::value")) {
         actualReleaseDate = browser.getAttribute(DISC_DETAIL_RELEASE_DATE + "@value");
         assertTrue("This is not expected (" + expectedReleaseDate + ") release date: (" + actualReleaseDate + ")!", actualReleaseDate.equals(expectedReleaseDate));
      }

      String actualDescription = browser.getText(DISC_DETAIL_DESCRIPTION);
      assertTrue("This is not expected (" + expectedDescription + ") description: (" + actualDescription + ")!", actualDescription.equals(expectedDescription));
      
      String actualArtist = browser.getSelectedLabel(DISC_DETAIL_ARTIST);
      assertTrue("This is not expected (" + expectedArtist + ") artist: (" + actualArtist + ")!", actualArtist.equals(expectedArtist));
   }

   /**
    * Creates new disc.
    * Expected to be on artist's edit page.
    * 
    */
   protected void createDisc(String title, String year) {
      browser.clickAndWait(ARTIST_CREATE_DISC_BUTTON);
      browser.type(MessageFormat.format(ARTIST_NTH_DISC_NAME, "last()"), title);
      browser.type(MessageFormat.format(ARTIST_NTH_DISC_YEAR, "last()"), year);
   }

   /**
    * Finds the specified artist in paginated artists page and clicks it.
    * Expected to be on artists page.
    * 
    */
   protected void findAndClickArtist(String artistName) {
      // find the artist's page (it's paginated) and click it
      String expectedArtist = MessageFormat.format(ARTIST_TABLE_ROW_BY_NAME, artistName);
      while (!browser.isElementPresent(expectedArtist)) { // click through pages
         assertTrue("Artist " + artistName + " not found.", browser.isElementPresent(ARTISTS_NEXT_PAGE_LINK));
         browser.click(ARTISTS_NEXT_PAGE_LINK); // ajax
         sleep(3000);
      }
      
      browser.clickAndWait(MessageFormat.format(ARTIST_TABLE_ROW_LINK, artistName)); // click artist link
   }

   /**
    * Finds the specified disc in paginated discs page and clicks it.
    * Expected to be on discs page.
    * 
    */
   protected void findAndClickDisc(String discName) {
      // find the disc page (it's paginated) and click it
      String expectedDisc = MessageFormat.format(DISC_TABLE_ROW_BY_NAME, discName);
      while (!browser.isElementPresent(expectedDisc)) { // click through pages
         assertTrue("Disc " + discName + " not found.", browser.isElementPresent(DISCS_NEXT_PAGE_LINK));
         browser.click(DISCS_NEXT_PAGE_LINK); // ajax
         sleep(3000);
      }
      
      browser.clickAndWait(MessageFormat.format(DISC_TABLE_ROW_LINK, discName)); // click disc link
   }

   /**
    * @param in milliseconds
    * 
    */
   protected void sleep(int milliseconds) {
      try {
         Thread.sleep(milliseconds); // TODO how to do this properly?
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }

   }
   
   protected void addBandMember(String artistName) {
       browser.clickAndWait(ARTIST_ADD_BAND_MEMBER);
       browser.type(ARTIST_LAST_BAND_MEMBER, artistName);
   }
   
   /**
    * Checks whether specified disc contains expected data.
    */
   protected void checkBandMember(int tableRow, String expectedBandMember) {
      String actualMemberName = browser.getAttribute(MessageFormat.format(ARTIST_NTH_BAND_MEMBER, tableRow) + "@value");
      assertTrue("This is not expected (" + expectedBandMember + ") album (" + actualMemberName + ")!", actualMemberName.equals(expectedBandMember));
   }
}
