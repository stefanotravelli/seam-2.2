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

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.thoughtworks.selenium.Wait;

/**
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class SeleniumBookingTest extends SeamSeleniumTest {

    private final String DEFAULT_USERNAME = "demo";
    private final String DEFAULT_PASSWORD = "demo";

    @Override
    @BeforeMethod
    public void setUp() {
        super.setUp();
        assertTrue("Login failed.", login());
    }

    @Override
    @AfterMethod
    public void tearDown() {
        logout();
        super.tearDown();
    }

    public boolean login() {
        return login(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public boolean login(String username, String password) {
        if (isLoggedIn()) {
            fail("User already logged in.");
        }
        browser.open(CONTEXT_PATH + getProperty("HOME_PAGE"));
        browser.waitForPageToLoad(TIMEOUT);
        if (!browser.getTitle().equals(getProperty("PAGE_TITLE"))) {
            return false;
        }
        browser.type(getProperty("LOGIN_USERNAME_FIELD"), username);
        browser.type(getProperty("LOGIN_PASSWORD_FIELD"), password);
        browser.click(getProperty("LOGIN_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
        return isLoggedIn();
    }

    public void logout() {
        if (isLoggedIn()) {
            browser.click(getProperty("LOGOUT"));
            browser.waitForPageToLoad(TIMEOUT);
        }
    }

    public boolean isLoggedIn() {
        return browser.isElementPresent(getProperty("LOGOUT"));
    }

    public void enterSearchQuery(String query) {
        if (getProperty("USE_AJAX_SEARCH").equalsIgnoreCase("FALSE")) {
            enterSearchQueryWithoutAJAX(query);
        } else {
            if (getProperty("USE_SEARCH_BUTTON").equalsIgnoreCase("TRUE")) {
                enterSearchQueryUsingAJAX(query, true);
            } else {
                enterSearchQueryUsingAJAX(query, false);
            }
        }
    }

    public void enterSearchQueryUsingAJAX(String query, boolean click) {
        browser.type(getProperty("SEARCH_STRING_FIELD"), "");
        browser.type(getProperty("SEARCH_STRING_FIELD"), query.substring(0, query
                .length() - 1));
        browser.typeKeys(getProperty("SEARCH_STRING_FIELD"), query.substring(query
                .length() - 1));
        if (click) {
            browser.click(getProperty("SEARCH_SUBMIT"));
        }
        // wait for javascript to show spinner
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // wait for page to get updated
        new Wait() {
            @Override
            public boolean until() {
                return !browser.isVisible(getProperty("SPINNER"));
            }
        }.wait("Spinner hasn't come out.");
        new Wait() {
            @Override
            public boolean until() {
                return (browser.isElementPresent(getProperty("SEARCH_RESULT_TABLE")) || browser
                        .isElementPresent(getProperty("NO_HOTELS_FOUND")));
            }
        }.wait("Search results not found.");
    }

    public void enterSearchQueryWithoutAJAX(String query) {
        browser.type(getProperty("SEARCH_STRING_FIELD"), "");
        browser.type(getProperty("SEARCH_STRING_FIELD"), query);
        browser.click(getProperty("SEARCH_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
    }
    
    public void waitForForm() {
        if (getProperty("USE_ICEFACES_FORMS").equalsIgnoreCase("TRUE")) {
            new Wait() {            
                @Override
                public boolean until() {
                    return !browser.isElementPresent("xpath=//*[@style='cursor: wait;']")
                        && browser.isElementPresent(getProperty("FOOTER"));
                }
            }.wait("Page was not refreshed.");
        } else {
            browser.waitForPageToLoad(TIMEOUT);
        }
    }
}
