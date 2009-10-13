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
import static org.testng.AssertJUnit.assertEquals;

import java.text.NumberFormat;
import java.text.ParseException;

import org.testng.annotations.Test;

import com.thoughtworks.selenium.Wait;

/**
 * 
 * @author Jozef Hartinger
 *
 */
public class SellTest extends SeleniumSeamBayTest
{
   private long listWaitTime = 5000;
   private NumberFormat nf = NumberFormat.getInstance();
   
   @Test(dependsOnGroups={"searchTest"})
   public void joystickSellingTest() throws NumberFormatException, ParseException
   {
      String title = "Saitek X52 Pro Flight System";
      int category = 7;
      int subcategory = 3;
      String duration = "7";
      String price = "100";
      String description = "The most fully integrated Stick and throttle flight controller: built to meet the demands of the best virtual pilots in the world!";

      sellItem(title, category, subcategory, duration, price, description);
   }

   protected void sellItem(String title, int category, int subcategory, String duration, String price, String description) throws NumberFormatException, ParseException
   {
      login();
      browser.clickAndWait(getProperty("SELL"));
      submitSell1Page(title);
      submitSell2Page(category, subcategory);
      submitSell3Page(duration, price);
      submitSell4Page();
      submitSell5Page(description);
      validatePreview(price, description);
      browser.clickAndWait(getProperty("SELL_CONFIRM"));
      assertTrue("Navigation failure, home page expected", browser.getLocation().contains(getProperty("HOME_PAGE")));
      assertEquals("Unexpected number of search results.", 1, search(title));
   }

   protected void submitSell1Page(String title) {
      assertTrue("Navigation failure, sell1 page expected", browser.getLocation().contains(getProperty("SELL_1_URL")));
      browser.type(getProperty("SELL_TITLE"), title);
      browser.clickAndWait(getProperty("SELL_NEXT"));
   }
   
   protected void submitSell2Page(int category, int subcategory) {
      assertTrue("Navigation failure, sell2 page expected", browser.getLocation().contains(getProperty("SELL_2_URL")));
      new Wait()
      {
         @Override
         public boolean until()
         {
            return browser.isElementPresent(getProperty("SELL_CATEGORY_SELECT_SECOND_OPTION"));
         }
      };
      /*this sleeping is necessary because although the previous waiting ensures the 14. option present in select component in
      java code, the web page on its own is not ready yet*/
      try
      {
         Thread.sleep(listWaitTime);
      }
      catch (InterruptedException e)
      {
      }
      
      browser.select(getProperty("SELL_CATEGORY_SELECT"), "index=" + category);
      if (subcategory != 0)
      {
         new Wait()
         {
            @Override
            public boolean until()
            {
               return browser.isElementPresent(getProperty("SELL_SUBCATEGORY_SELECT"));
            }
         };
         browser.select(getProperty("SELL_SUBCATEGORY_SELECT"), "index=" + subcategory);
      }
      browser.clickAndWait(getProperty("SELL_NEXT"));
   }
   
   protected void submitSell3Page(String duration, String price) {
      assertTrue("Navigation failure, sell3 page expected", browser.getLocation().contains(getProperty("SELL_3_URL")));
      browser.type(getProperty("SELL_DURATION"), duration);
      browser.type(getProperty("SELL_PRICE"), price);
      browser.clickAndWait(getProperty("SELL_NEXT"));
   }
   
   protected void submitSell4Page() {
      assertTrue("Navigation failure, sell4 page expected", browser.getLocation().contains(getProperty("SELL_4_URL")));
      browser.clickAndWait(getProperty("SELL_NEXT"));
   }
   
   protected void submitSell5Page(String description) {
      assertTrue("Navigation failure, sell5 page expected", browser.getLocation().contains(getProperty("SELL_5_URL")));
      browser.type(getProperty("SELL_DESCRIPTION"), description);
      browser.clickAndWait(getProperty("SELL_NEXT"));
   }
   
   protected void validatePreview(String price, String description) throws NumberFormatException, ParseException
   {
      assertTrue("Must be on preview page to validate preview.", browser.getLocation().contains(getProperty("SELL_PREVIEW_URL")));
      assertEquals("Unexpected price on preview page.", Double.valueOf(price), nf.parse(browser.getText(getProperty("SELL_PREVIEW_PRICE"))).doubleValue());
      assertEquals("Unexpected description on description page.", description, browser.getText(getProperty("SELL_PREVIEW_DESCRIPTION")));
   }
}
