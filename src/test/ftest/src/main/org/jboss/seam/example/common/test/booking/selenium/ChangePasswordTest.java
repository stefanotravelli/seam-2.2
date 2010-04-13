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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * This class tests change password funcionality.
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class ChangePasswordTest extends SeleniumBookingTest {

    private final static String LONG_TEXT = "testertestertest";
    private final static String SHORT_TEXT = "tt";
    // overriding default values
    private final String USERNAME = "gavin";
    private final String PASSWORD = "foobar";

    @Override
    @BeforeMethod
    public void setUp() {
        browser = startBrowser();
        assertTrue("Login failed.", login(USERNAME, PASSWORD));
    }

    /**
     * Verifies that changing password works well. If clean-up part of this
     * method fails it may affect other methods.
     */
    @Test
    public void changePasswordTest() {
        changePassword("password");
        assertTrue("Password change failed.", browser
                .isTextPresent(getProperty("PASSWORD_UPDATED_MESSAGE")));
        logout();
        assertTrue("Login failed.", login(USERNAME, "password"));
        // cleanup - set default password
        changePassword(PASSWORD);
        try
        {
           Thread.sleep(7000);
        }
        catch (InterruptedException e)
        {
        }
        assertTrue("Password change failed.", browser
                .isTextPresent(getProperty("PASSWORD_UPDATED_MESSAGE")));
        logout();
        assertTrue("Login failed.", login(USERNAME, PASSWORD));
    }

    @Test
    public void usingDifferentPasswordsTest() {
        changePassword("password", "password1");
        assertTrue("Password verification failed", browser
                .isTextPresent(getProperty("PASSWORD_REENTER_MESSAGE")));
    }

    @Test
    public void usingEmptyPasswordsTest() {
        changePassword("", "");
        assertEquals("Password validation failed", 2, browser
                .getXpathCount(getProperty("PASSWORD_VALUE_REQUIRED_MESSAGE")));
    }

    @Test
    public void usingLongPasswordTest() {
        changePassword(LONG_TEXT, LONG_TEXT);
        assertTrue("Password validation failed", browser
                .isTextPresent(getProperty("PASSWORD_LENGTH_MESSAGE")));
    }

    @Test
    public void usingShortPasswordTest() {
        changePassword(SHORT_TEXT, SHORT_TEXT);
        assertTrue("Password validation failed", browser
                .isTextPresent(getProperty("PASSWORD_LENGTH_MESSAGE")));
    }

    public void changePassword(String newPassword) {
        changePassword(newPassword, newPassword);
    }

    public void changePassword(String newPassword, String verify) {
        browser.click(getProperty("SETTINGS"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.type(getProperty("PASSWORD_PASSWORD"), newPassword);
        browser.type(getProperty("PASSWORD_VERIFY"), verify);
        browser.click(getProperty("PASSWORD_SUBMIT"));
        waitForForm();
    }
}
