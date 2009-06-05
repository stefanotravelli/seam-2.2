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

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests artists management in SeamDiscs application
 * @author Ondrej Skutka
 *
 */
public class ArtistsTest extends SeleniumSeamDiscsTest
{

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.clickAndWait(MANAGE_ARTISTS);
   }

   @Test(dependsOnMethods = {"createWithDiscsTest"})
   public void paginationTest() 
   {
      findAndClickArtist("Fairport Convention"); // should be on second page
      checkDisc(1, "Liege and Lief", "", "The first folk-rock album ever made in the UK, this was the only studio recording of the classic line up of Sandy Denny, Richard Thompson, Dave Swarbick, Ashley Hutchings and Simon Nicol");
   }

   @Test(dependsOnMethods = {"createWithDiscsTest"})
   public void filterTest() 
   {
      browser.type(ARTIST_FILTER, "Fairport");
      browser.keyUp(ARTIST_FILTER, "t"); // to trigger ajax search
      sleep(3000);
      browser.clickAndWait(ARTISTS_FIRST_ARTIST_LINK);
      checkDisc(1, "Liege and Lief", "", "The first folk-rock album ever made in the UK, this was the only studio recording of the classic line up of Sandy Denny, Richard Thompson, Dave Swarbick, Ashley Hutchings and Simon Nicol");
   }
   
   @Test(dependsOnGroups = {"loginTest"})
   public void createWithDiscsTest() 
   {
      browser.clickAndWait(CREATE_ARTIST_BUTTON);
      browser.type(ARTIST_NAME, "Lou Reed");
      browser.type(ARTIST_DESCRIPTION, "First came to prominence as the guitarist and principal singer-songwriter of The Velvet Underground. Than began a long and eclectic solo career.");
      createDisc("Metal Machine Music", "1975");
      createDisc("Sally Can't Dance", "1974");
      createDisc("Rock and Roll Heart", "1977");
      browser.clickAndWait(ARTIST_PERSIST);
      assertTrue("Cannot create artist with discs", browser.isTextPresent("Successfully created"));
  
      findAndClickArtist("Lou Reed");
      
      // check whether it is the Lou Reed
      assertTrue("This artist is not Lou Reed!", browser.getAttribute(ARTIST_NAME + "@value").equals("Lou Reed"));

      // check discs (they should be sorted by release date)
      checkDisc(1, "Sally Can't Dance", "1974", EMPTY_DISC_DESCRIPTION);
      checkDisc(2, "Metal Machine Music", "1975", EMPTY_DISC_DESCRIPTION);
      checkDisc(3, "Rock and Roll Heart", "1977", EMPTY_DISC_DESCRIPTION);
   }

// TODO check creating empty disc
   
   
   @Test(dependsOnMethods = {"createWithDiscsTest"})
   public void editDiscsTest() 
   {
      // correct Rock and Roll Heart releas date
      findAndClickArtist("Lou Reed");
      browser.type(MessageFormat.format(ARTIST_NTH_DISC_YEAR, "last()"), "1976");
      
      browser.clickAndWait(ARTIST_UPDATE);
      findAndClickArtist("Lou Reed");
      
      checkDisc(1, "Sally Can't Dance", "1974", EMPTY_DISC_DESCRIPTION);
      checkDisc(2, "Metal Machine Music", "1975", EMPTY_DISC_DESCRIPTION);
      checkDisc(3, "Rock and Roll Heart", "1976", EMPTY_DISC_DESCRIPTION); // this was altered 
   }
   
   @Test(dependsOnGroups = {"loginTest"})
   public void addBandTest() 
   {
      browser.clickAndWait(CREATE_BAND_BUTTON);
      browser.type(ARTIST_NAME, "The Velvet Underground");
      browser.type(ARTIST_DESCRIPTION, "An underground band.");
      createDisc("White Light/White Heat", "1968");
      createDisc("The Velvet Underground and Nico", "1967");
      
      addBandMember("Lou Reed");
      addBandMember("Sterling Morrison");
      addBandMember("John Cale");
      addBandMember("Maureen Tucker");
      addBandMember("Nico");

      browser.clickAndWait(ARTIST_PERSIST);
      assertTrue("Cannot create artist with discs", browser.isTextPresent("Successfully created"));

      findAndClickArtist("The Velvet Underground");

      // check whether it is the Lou Reed
      assertTrue("This artist is not the Velvet Underground!", browser.getAttribute(ARTIST_NAME + "@value").equals("The Velvet Underground"));

      // check discs (they should be sorted by release date)
      checkDisc(1, "The Velvet Underground and Nico", "1967", EMPTY_DISC_DESCRIPTION);
      checkDisc(2, "White Light/White Heat", "1968", EMPTY_DISC_DESCRIPTION);
      
      checkBandMember(1, "Lou Reed");
      checkBandMember(2, "Sterling Morrison");
      checkBandMember(3, "John Cale");
      checkBandMember(4, "Maureen Tucker");
      checkBandMember(5, "Nico");
   }


}
