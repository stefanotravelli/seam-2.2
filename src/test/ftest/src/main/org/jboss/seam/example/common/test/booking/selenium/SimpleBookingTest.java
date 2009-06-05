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
package org.jboss.seam.example.common.test.booking.selenium;

import java.text.MessageFormat;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * This class tests booking functionality of the example.
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class SimpleBookingTest extends SeleniumBookingTest {

    protected final String EXPECTED_NAME = "Demo User";
    protected final String CREDIT_CARD = "0123456789012345";
    protected final String CREDIT_CARD_NAME = "visa";

    /**
     * Tries searching for non existing hotel.
     */
    @Test(enabled = true)
    public void invalidSearchStringTest() {
        enterSearchQuery("NonExistingHotel");
        assertTrue("Search failed.", browser
                .isElementPresent(getProperty("NO_HOTELS_FOUND")));
    }

    /**
     * Simply books hotel.
     */
    @Test(enabled = true)
    public void simpleBookingTest() {
        String hotelName = "W Hotel";
        int confirmationNumber;
        confirmationNumber = bookHotel(hotelName);
        assertTrue("Booking with confirmation number " + confirmationNumber
                + " not found.", browser.isElementPresent(MessageFormat.format(
                        getProperty("BOOKING_TABLE_ITEM"), confirmationNumber, hotelName)));
    }

    /**
     * Tries booking hotel with incorrect dates.
     */
    @Test(enabled = true)
    public void invalidDatesTest() {
        String hotelName = "W Hotel";
        enterSearchQuery(hotelName);
        browser.click(getProperty("SEARCH_RESULT_TABLE_FIRST_ROW_LINK"));
        browser.waitForPageToLoad(TIMEOUT);
        // hotel page
        browser.click(getProperty("BOOKING_BOOK"));
        browser.waitForPageToLoad(TIMEOUT);
        // booking page
        String checkOut = browser.getValue(getProperty("HOTEL_CHECKOUT_DATE_FIELD"));
        populateBookingFields();
        // switch check in and check out date
        browser.type(getProperty("HOTEL_CHECKIN_DATE_FIELD"), checkOut);
        browser.click(getProperty("HOTEL_PROCEED"));
        waitForForm();
        assertTrue("Date verification #1 failed.", browser
                .isTextPresent(getProperty("BOOKING_INVALID_DATE_MESSAGE1")));
        assertTrue("Check-out date error message expected.", browser
                .isElementPresent(getProperty("HOTEL_CHECKOUT_DATE_MESSAGE")));
        // set check in to past
        browser.type(getProperty("HOTEL_CHECKIN_DATE_FIELD"), "01/01/1970");
        browser.click(getProperty("HOTEL_PROCEED"));
        waitForForm();
        assertTrue("Date verification #2 failed.", browser
                .isTextPresent(getProperty("BOOKING_INVALID_DATE_MESSAGE2")));
        assertTrue("Checkin-date error message expected.", browser
                .isElementPresent(getProperty("HOTEL_CHECKIN_DATE_MESSAGE")));
    }

    /**
     * This test verifies that user gets right confirmation number when
     * canceling order. https://jira.jboss.org/jira/browse/JBSEAM-3288
     */
    @Test(enabled = true)
    public void testJBSEAM3288() {
        String[] hotelNames = new String[] { "Doubletree", "Hotel Rouge",
                "Conrad Miami" };
        int[] confirmationNumbers = new int[3];
        // make 3 bookings
        for (int i = 0; i < 3; i++) {
            int confirmationNumber = bookHotel(hotelNames[i]);
            confirmationNumbers[i] = confirmationNumber;
        }
        // assert that there bookings are listed in hotel booking list
        for (int i = 0; i < 3; i++) {
            assertTrue("Expected booking #" + i + " not present", browser
                    .isElementPresent(MessageFormat.format(
                            getProperty("BOOKING_TABLE_ITEM"), confirmationNumbers[i],
                            hotelNames[i])));
        }
        // cancel all the reservations
        for (int i = 2; i >= 0; i--) {
            browser.click(MessageFormat.format(getProperty("BOOKING_TABLE_ITEM_LINK"),
                    confirmationNumbers[i], hotelNames[i]));
            browser.waitForPageToLoad(TIMEOUT);
            assertTrue("Booking canceling failed", browser
                    .isTextPresent(MessageFormat.format(
                            getProperty("BOOKING_CANCELLED_MESSAGE"),
                            confirmationNumbers[i])));
        }

    }

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
        String message = browser.getText(getProperty("HOTEL_MESSAGE"));
        assertTrue("Booking failed. Confirmation message does not match.", message.matches(
                MessageFormat.format(getProperty("BOOKING_CONFIRMATION_MESSAGE"), EXPECTED_NAME, hotelName)));
            String[] messageParts = message.split(" ");
            int confirmationNumber = Integer
                    .parseInt(messageParts[messageParts.length - 1]);
            return confirmationNumber;
    }

    protected int bookHotel(String hotelName) {
        return bookHotel(hotelName, 2, 0, CREDIT_CARD, CREDIT_CARD_NAME);
    }

    protected void populateBookingFields(int bed, int smoking,
            String creditCard, String creditCardName) {
        browser.select(getProperty("HOTEL_BED_FIELD"),
                getProperty("HOTEL_BED_FIELD_SELECT_CRITERIA") + bed);
        if (smoking == 1) {
            browser.check(getProperty("HOTEL_SMOKING_1"));
        } else {
            browser.check(getProperty("HOTEL_SMOKING_2"));
        }
        browser.type(getProperty("HOTEL_CREDIT_CARD"), creditCard);
        browser.type(getProperty("HOTEL_CREDIT_CARD_NAME"), creditCardName);
    }

    protected void populateBookingFields() {
        populateBookingFields(2, 0, CREDIT_CARD, CREDIT_CARD_NAME);
    }
}
