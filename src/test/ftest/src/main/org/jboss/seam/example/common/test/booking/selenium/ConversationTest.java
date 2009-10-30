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
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class ConversationTest extends SeleniumBookingTest {

    private final String hotel1 = "Hotel Rouge";
    private final String hotel2 = "Doubletree";

    /**
     * Opens two windows and tries switching over workspaces to make sure
     * conversations work properly.
     */
    @Test
    public void testConversations() {
        // Start booking in window 1
        browser.openWindow(CONTEXT_PATH + getProperty("MAIN_PAGE"), "1");
        browser.openWindow(CONTEXT_PATH + getProperty("MAIN_PAGE"), "2");
        browser.selectWindow("1");
        if (!isLoggedIn()) {
            login();
        }
        enterSearchQuery(hotel1);
        browser.click(getProperty("SEARCH_RESULT_TABLE_FIRST_ROW_LINK"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.click(getProperty("BOOKING_BOOK"));
        // Find hotel in window 2
        browser.selectWindow("2");
        if (!isLoggedIn()) {
            login();
        }
        enterSearchQuery(hotel2);
        browser.click(getProperty("SEARCH_RESULT_TABLE_FIRST_ROW_LINK"));
        browser.waitForPageToLoad(TIMEOUT);
        // Reload window 1 to check whether both workspaces are displayed
        browser.selectWindow("1");
        assertEquals("#1 workspace not present in workspace table",
                MessageFormat.format(getProperty("WORKSPACE_BOOKING_TEXT"),
                        hotel1), browser.getText(MessageFormat.format(
                        getProperty("WORKSPACE_TABLE_LINK_BY_ID"), 0)));
        assertEquals("#2 workspace not present in workspace table",
                MessageFormat
                        .format(getProperty("WORKSPACE_VIEW_TEXT"), hotel2),
                browser.getText(MessageFormat.format(
                        getProperty("WORKSPACE_TABLE_LINK_BY_ID"), 1)));
        // Switch window 1 to second workspace
        browser.click(MessageFormat.format(
                getProperty("WORKSPACE_TABLE_LINK_BY_ID"), 1));
        browser.waitForPageToLoad(TIMEOUT);
        // Switch window 1 back to first workspace
        browser.click(MessageFormat.format(
                getProperty("WORKSPACE_TABLE_LINK_BY_ID"), 1));
        browser.waitForPageToLoad(TIMEOUT);
        // End conversation in window 2
        browser.selectWindow("2");
        browser.click(getProperty("BOOKING_CANCEL"));
        browser.waitForPageToLoad(TIMEOUT);
        // Second workspace should disappear
        browser.selectWindow("1");
        assertEquals("Workspace failure.", 1, browser
                .getXpathCount(getProperty("WORKSPACE_TABLE_ROW_COUNT")));
    }
}
