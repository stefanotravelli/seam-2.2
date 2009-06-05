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

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * This tests verifies that the example can cleanly handle backbuttoning in
 * various situations
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class BackButtonTest extends SeleniumBookingTest {

    String hotelName = "Mar";

    /**
     * Tries whether backbuttoning after logout is cleanly handled.
     */
    @Test
    public void backbuttoningAfterLogoutTest() {
        enterSearchQuery(hotelName);
        logout();
        // goBack() does not work with konqueror
        browser.goBack();
        browser.waitForPageToLoad(TIMEOUT);
        if (isLoggedIn()) {
            browser.click(getProperty("SETTINGS"));
            browser.waitForPageToLoad(TIMEOUT);
            assertEquals("Backbuttoning failed.", getProperty("PAGE_TITLE"),
                    browser.getTitle());
            assertFalse("Backbuttoning handled bad way.", isLoggedIn());
        }
    }

    /**
     * Tries whether backbuttoning after logout is cleanly handled. Using ajax
     * functionality after logout.
     */
    @Test
    public void backbuttoningAfterLogoutWithAjaxTest() {
        enterSearchQuery(hotelName);
        logout();
        // goBack() does not work with konqueror
        // browser.refresh();
        // browser.waitForPageToLoad(TIMEOUT);
        browser.goBack();
        browser.waitForPageToLoad(TIMEOUT);
        if (isLoggedIn()) {
            browser.click(getProperty("SEARCH_SUBMIT"));
            browser.waitForPageToLoad(TIMEOUT);
            assertEquals("Backbuttoning failed.", getProperty("PAGE_TITLE"),
                    browser.getTitle());
            assertFalse("User should not be logged in by now.", isLoggedIn());
        }
    }

    /**
     * Verifies that backbuttoning after ending conversation is handled cleanly.
     */
    @Test
    public void backbuttoningAfterConversationEndTest() {
        // start booking
        enterSearchQuery(hotelName);
        browser.click(getProperty("SEARCH_RESULT_TABLE_FIRST_ROW_LINK"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.click(getProperty("BOOKING_BOOK"));
        browser.waitForPageToLoad(TIMEOUT);
        // cancel booking
        browser.click(getProperty("HOTEL_CANCEL"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.goBack();
        browser.waitForPageToLoad(TIMEOUT);
        browser.refresh();
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("Conversation failure.", browser
                .isTextPresent(getProperty("CONVERSATION_TIMEOUT_MESSAGE")));
    }
}
