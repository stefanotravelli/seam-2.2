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
package org.jboss.seam.example.wicket.test.selenium;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.text.MessageFormat;

import org.jboss.seam.example.common.test.booking.selenium.SimpleBookingTest;

/**
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class WicketSimpleBookingTest extends SimpleBookingTest {

    @Override
    protected void populateBookingFields(int bed, int smoking,
            String creditCard, String creditCardName) {
        super.populateBookingFields(bed, smoking, creditCard, creditCardName);
        browser.select(getProperty("HOTEL_CREDIT_CARD_EXPIRY_MONTH"),
                "index=1");
        browser.select(getProperty("HOTEL_CREDIT_CARD_EXPIRY_YEAR"), "index=1");
    }
    
    @Override
    protected int bookHotel(String hotelName, int bed, int smoking,
          String creditCard, String creditCardName) {
      if (!isLoggedIn())
          fail();
      if (!browser.isElementPresent(getProperty("SEARCH_SUBMIT"))) {
          browser.open(CONTEXT_PATH + getProperty("MAIN_PAGE"));
          browser.waitForPageToLoad(TIMEOUT);
      }
      enterSearchQuery(hotelName);
      browser.click(getProperty("SEARCH_RESULT_TABLE_FIRST_ROW_LINK"));
      browser.waitForPageToLoad(TIMEOUT);
      // booking page
      browser.click(getProperty("BOOKING_BOOK"));
      browser.waitForPageToLoad(TIMEOUT);
      // hotel page
      populateBookingFields(bed, smoking, creditCard, creditCardName);
      browser.click(getProperty("HOTEL_PROCEED"));
      browser.waitForPageToLoad(TIMEOUT);
      // confirm page
      browser.click(getProperty("HOTEL_CONFIRM"));
      browser.waitForPageToLoad(TIMEOUT);
      // main page
      String message = browser.getText(MessageFormat.format(getProperty("ORDER_CONFIRMATION_NUMBER"), hotelName));
      
      int confirmationNumber = Integer.parseInt(message);
      return confirmationNumber;
  }
    
}
