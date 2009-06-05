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
package org.jboss.seam.example.nestedbooking.test.selenium;

import java.text.MessageFormat;

import org.jboss.seam.example.common.test.booking.selenium.SimpleBookingTest;

/**
 * This class alters behaviour of SimpleBookingTest to match nestedbooking example
 *
 * @author jharting
 */
public class NestedSimpleBookingTest extends SimpleBookingTest {

    @Override
    protected int bookHotel(String hotelName) {
        return bookHotel(hotelName, 0, CREDIT_CARD, CREDIT_CARD_NAME);
    }

    protected int bookHotel(String hotelName, int room, String creditCard, String creditCardName) {
        if (!isLoggedIn()) {
            return -1;
        }

        if (!browser.isElementPresent(getProperty("SEARCH_SUBMIT"))) {
            browser.open(CONTEXT_PATH + getProperty("MAIN_PAGE"));
            browser.waitForPageToLoad(TIMEOUT);
        }

        enterSearchQuery(hotelName);
        browser.click(getProperty("SEARCH_RESULT_TABLE_FIRST_ROW_LINK"));
        browser.waitForPageToLoad(TIMEOUT);
        // hotel page
        browser.click(getProperty("BOOKING_BOOK"));
        browser.waitForPageToLoad(TIMEOUT);
        // book page
        browser.click(getProperty("BOOKING_SELECT_ROOM"));
        browser.waitForPageToLoad(TIMEOUT);
        // room select page
        browser.click(MessageFormat.format(getProperty("ROOM_LINK"), room));
        browser.waitForPageToLoad(TIMEOUT);
        // payment page
        browser.type(getProperty("PAYMENT_CREDIT_CARD"), creditCard);
        browser.type(getProperty("PAYMENT_CREDIT_CARD_NAME"), creditCardName);
        browser.click(getProperty("PAYMENT_PROCEED"));
        browser.waitForPageToLoad(TIMEOUT);
        // confirm page
        browser.click(getProperty("CONFIRM_CONFIRM"));
        browser.waitForPageToLoad(TIMEOUT);
        // main page
        String message = browser.getText(getProperty("HOTEL_MESSAGE"));
        if (message.matches(MessageFormat.format(
                getProperty("BOOKING_CONFIRMATION_MESSAGE"), EXPECTED_NAME, hotelName))) {
            String[] messageParts = message.split(" ");
            int confirmationNumber = Integer.parseInt(messageParts[messageParts.length - 1]);
            return confirmationNumber;
        } else {
            return -1;
        }
    }
    
    @Override
    protected void populateBookingFields() {}
}
