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

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.jboss.seam.example.common.test.selenium.SeamSelenium;
import org.testng.annotations.Test;

/**
 * 
 * @author Jozef Hartinger
 *
 */
public class BidTest extends SeleniumSeamBayTest
{
   @Test(dependsOnGroups = { "searchTest"})
   public void simpleBidTest()
   {
      String title = "Lost Tales Volume 1 by J.R.R. Tolkien";
      String price = "10";
      int bidCount;

      login();
      search(title);
      browser.clickAndWait(getProperty("SEARCH_RESULTS_FIRST_ROW_LINK"));
      browser.clickAndWait(getProperty("ITEM_BID_HISTORY"));
      if (browser.isElementPresent(getProperty("BID_HISTORY_COUNT_EMPTY")))
      {
         bidCount = 0;
      }
      else
      {         
         bidCount = browser.getXpathCount(getProperty("BID_HISTORY_COUNT")).intValue();
      }      
      browser.goBackAndWait();
      placeBid(price);
      assertTrue("Auction page expected.", browser.getLocation().contains(getProperty("AUCTION_URL")));
      browser.clickAndWait(getProperty("ITEM_BID_HISTORY"));
      assertEquals("Unexpected count of bids.", ++bidCount, browser.getXpathCount(getProperty("BID_HISTORY_COUNT")));
   }

   @Test(dependsOnGroups = { "searchTest", "registrationTest" }, dependsOnMethods = { "simpleBidTest" })
   public void complexBidTest()
   {
      String firstBidderName = "honestjoe";
      String secondBidderName = "bidTester";
      String title = "Nikon D80 Digital Camera";

      SeamSelenium firstBrowser = browser;
      SeamSelenium secondBrowser = startBrowser();
      try
      {
         // register new user in first browser
         browser.clickAndWait(getProperty("REGISTRATION"));
         submitRegistrationForm(secondBidderName, "password", "password", "Slovakia");
         assertTrue("Creating new user failed.", isLoggedIn());
         // place a bid for a camera
         search(title);
         browser.clickAndWait(getProperty("SEARCH_RESULTS_FIRST_ROW_LINK"));
         placeBid("2000");
         // switch to second browser and place several bids
         browser = secondBrowser;
         browser.open(CONTEXT_PATH + getProperty("HOME_PAGE"));
         login();
         search(title);
         browser.clickAndWait(getProperty("SEARCH_RESULTS_FIRST_ROW_LINK"));
         for (int i = 1100; i < 2000; i += 200)
         {
            placeBid(String.valueOf(i));
            assertTrue("'You have been outbid' page expected.", browser.isElementPresent(getProperty("BID_OUTBID")));
         }
         placeBid("2200");
         assertFalse("Outbid unexpectedly", browser.isElementPresent(getProperty("BID_OUTBID")));
         assertEquals("High bidder not recognized.", firstBidderName, browser.getText(getProperty("BID_HIGH_BIDDER")));
         // switch to first browser again and place the highest bid again
         browser = firstBrowser;
         placeBid("2100");
         assertTrue("'You have been outbid' page expected.", browser.isElementPresent(getProperty("BID_OUTBID")));
         placeBid("2500");
         assertEquals("High bidder not recognized.", secondBidderName, browser.getText(getProperty("BID_HIGH_BIDDER")));
      }
      finally
      {
         browser = firstBrowser;
         secondBrowser.stop();
      }
   }

   public void placeBid(String price)
   {
      if (browser.isElementPresent(getProperty("ITEM_NEW_BID_FIELD")))
      {
         browser.type(getProperty("ITEM_NEW_BID_FIELD"), price);
         browser.clickAndWait(getProperty("ITEM_NEW_BID_SUBMIT"));
      }
      else if (browser.isElementPresent(getProperty("BID_INCREASE_FIELD")))
      {
         browser.type(getProperty("BID_INCREASE_FIELD"), price);
         browser.clickAndWait(getProperty("BID_INCREASE_SUBMIT"));
      }
      else
      {
         fail("Unable to place a bid.");
      }
      browser.clickAndWait(getProperty("BID_CONFIRM"));
   }
}
