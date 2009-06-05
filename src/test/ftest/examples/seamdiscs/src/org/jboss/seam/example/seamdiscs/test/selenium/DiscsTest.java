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

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests artists management in SeamDiscs application
 * @author Ondrej Skutka
 *
 */
public class DiscsTest extends SeleniumSeamDiscsTest
{

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.clickAndWait(MANAGE_DISCS);
   }

   @Test(dependsOnMethods = {"editDiscTest"})
   public void discPaginationTest() 
   {
      findAndClickDisc("Rock and Roll Heart"); // should not be on the first page
      checkDiscDetail("Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.", "Lou Reed");
   }


   @Test(dependsOnMethods = {"editDiscsTest"})
   public void editDiscTest() 
   {
      // correct Rock and Roll Heart description
      findAndClickDisc("Rock and Roll Heart"); // should not be on the first page
      checkDiscDetail("Rock and Roll Heart", "1976", "", "Lou Reed");
      
      browser.type(DISC_DETAIL_DESCRIPTION, "A sensitive and revealing look into the prince of darkness.");
      
      browser.clickAndWait(DISC_DETAIL_UPDATE);
      
      findAndClickDisc("Rock and Roll Heart");
      checkDiscDetail("Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.", "Lou Reed");
      
      // check whether it's ok from the artists' perspective
      browser.clickAndWait(MANAGE_ARTISTS);

      findAndClickArtist("Lou Reed");
      
      // check whether it is the Lou Reed
      assertTrue("This artist is not Lou Reed!", browser.getAttribute(ARTIST_NAME + "@value").equals("Lou Reed"));

      // check discs (they should be sorted by release date)
      checkDisc(1, "Sally Can't Dance", "1974", EMPTY_DISC_DESCRIPTION);
      checkDisc(2, "Metal Machine Music", "1975", EMPTY_DISC_DESCRIPTION);
      checkDisc(3, "Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.");
   }
   
   @Test(dependsOnMethods = {"editDiscTest"})
   public void addDiscTest()
   {
      browser.clickAndWait(CREATE_DISC_BUTTON);
      browser.type(DISC_DETAIL_TITLE, "Berlin");
      browser.select(DISC_DETAIL_ARTIST, "Lou Reed");
      browser.type(DISC_DETAIL_RELEASE_DATE, "1973");
      browser.type(DISC_DETAIL_DESCRIPTION, "A tragic rock opera about a doomed couple that addresses themes of drug use and depression.");
      browser.clickAndWait(DISC_DETAIL_PERSIST);
      assertTrue("Cannot create disc", browser.isTextPresent("Successfully created"));

      // check whether it's ok from the artists' perspective
      browser.clickAndWait(MANAGE_ARTISTS);

      findAndClickArtist("Lou Reed");
      
      // check whether it is the Lou Reed
      assertTrue("This artist is not Lou Reed!", browser.getAttribute(ARTIST_NAME + "@value").equals("Lou Reed"));

      // check discs (they should be sorted by release date)
      checkDisc(1, "Berlin", "1973", "A tragic rock opera about a doomed couple that addresses themes of drug use and depression.");
      checkDisc(2, "Sally Can't Dance", "1974", EMPTY_DISC_DESCRIPTION);
      checkDisc(3, "Metal Machine Music", "1975", EMPTY_DISC_DESCRIPTION);
      checkDisc(4, "Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.");
   }


   @Test(dependsOnMethods = {"addDiscTest"})
   public void removeDiscTest() 
   {
      // correct Rock and Roll Heart description
      findAndClickDisc("Berlin");
      checkDiscDetail("Berlin", "1973", "A tragic rock opera about a doomed couple that addresses themes of drug use and depression.", "Lou Reed");
      
      browser.clickAndWait(DISC_DETAIL_REMOVE);
      
      // check whether it's ok from the artists' perspective
      browser.clickAndWait(MANAGE_ARTISTS);

      findAndClickArtist("Lou Reed");
      
      // check whether it is the Lou Reed
      assertTrue("This artist is not Lou Reed!", browser.getAttribute(ARTIST_NAME + "@value").equals("Lou Reed"));

      // check discs (they should be sorted by release date)
      checkDisc(1, "Sally Can't Dance", "1974", EMPTY_DISC_DESCRIPTION);
      checkDisc(2, "Metal Machine Music", "1975", EMPTY_DISC_DESCRIPTION);
      checkDisc(3, "Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.");
   }

   @Test(dependsOnMethods = {"removeDiscTest"})
   public void cancelDiscTest() 
   {
      // correct Rock and Roll Heart description
      findAndClickDisc("Rock and Roll Heart"); // should not be on the first page
      checkDiscDetail("Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.", "Lou Reed");
      
      browser.type(DISC_DETAIL_DESCRIPTION, "Pretty lame album.");
      
      browser.clickAndWait(DISC_DETAIL_CANCEL);
      
      findAndClickDisc("Rock and Roll Heart");
      checkDiscDetail("Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.", "Lou Reed");
      
      // check whether it's ok from the artists' perspective
      browser.clickAndWait(MANAGE_ARTISTS);

      findAndClickArtist("Lou Reed");
      
      // check whether it is the Lou Reed
      assertTrue("This artist is not Lou Reed!", browser.getAttribute(ARTIST_NAME + "@value").equals("Lou Reed"));

      // check discs (they should be sorted by release date)
      checkDisc(1, "Sally Can't Dance", "1974", EMPTY_DISC_DESCRIPTION);
      checkDisc(2, "Metal Machine Music", "1975", EMPTY_DISC_DESCRIPTION);
      checkDisc(3, "Rock and Roll Heart", "1976", "A sensitive and revealing look into the prince of darkness.");
   }
   
}
