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
import java.util.Date;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * This class tests registration
 * 
 * @author jbalunas
 * @author jharting
 */
public class RegistrationTest extends SeleniumBookingTest {

    private final String LONG_TEXT = "testertestertest";
    private final String SHORT_TEXT = "tes";
    // private final static int USER_COUNT = 3;
    private String suffix;

    public RegistrationTest() {
        Date date = new Date();
        // suffix is needed to allow tests to be run repeatedly
        suffix = Long.toString(date.getTime() % 10000000);
    }

    @Override
    @BeforeMethod
    public void setUp() {
        browser = startBrowser();
    }

    @Override
    @AfterMethod
    public void tearDown() {
        stopBrowser();
    }

    @Test
    public void testVerify() {
        register("tester", "tester", "password", "password1");
        // assertTrue("Error message expected.",
        // browser.isElementPresent(get("REGISTRATION_VERIFY_MESSAGE")));
        assertTrue("Password verification failed.", browser
                .isTextPresent(getProperty("REGISTRATION_REENTER_MESSAGE")));
    }

    @Test
    public void testLongText() {
        register(LONG_TEXT, "tester", "password", "password");
        assertTrue("Username validation failed.", browser
                .isTextPresent(getProperty("REGISTRATION_LENGTH_MESSAGE")));
    }

    @Test
    public void testShortText() {
        register(SHORT_TEXT, "tester", "password", "password");
        assertTrue("Username validation failed.", browser
                .isTextPresent(getProperty("REGISTRATION_LENGTH_MESSAGE")));
    }

    @Test
    public void testDuplicateUser() {
        String username = "tester" + suffix;
        register(username, "tester", "password", "password");
        assertTrue("Navigation after succesful registration failed.", browser
                .getLocation().contains(getProperty("HOME_PAGE")));
        // assertTrue("Registration failed.",
        // browser.isTextPresent(MessageFormat.format(get("REGISTRATION_SUCCESSFUL_MESSAGE"),
        // username)));
        register(username, "tester", "password", "password");
        assertTrue("Registered 2 users with the same username.", browser
                .isTextPresent(MessageFormat.format(
                        getProperty("REGISTRATION_USER_EXISTS_MESSAGE"), username)));
    }

    @Test
    public void standardRegistrationTest() {
        String username = "john" + suffix;
        String name = "John Doe";
        String password = "password";
        register(username, name, password, password);
        assertTrue("Navigation after succesful registration failed.", browser
                .getLocation().contains(getProperty("HOME_PAGE")));
        // assertTrue("Registration failed.",
        // browser.isTextPresent(MessageFormat.format(get("REGISTRATION_SUCCESSFUL_MESSAGE"),
        // username)));
        // try logging in to verify registration
        assertTrue("Login failed.", login(username, password));
    }

    private void register(String username, String name, String password,
            String verify) {
        browser.open(CONTEXT_PATH + getProperty("HOME_PAGE"));
        browser.waitForPageToLoad(TIMEOUT);
        assertEquals("Unable to load home page.", getProperty("PAGE_TITLE"), browser
                .getTitle());
        browser.click(getProperty("REGISTRATION"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.type(getProperty("REGISTRATION_USERNAME"), username);
        browser.type(getProperty("REGISTRATION_NAME"), name);
        browser.type(getProperty("REGISTRATION_PASSWORD"), password);
        browser.type(getProperty("REGISTRATION_VERIFY"), verify);
        browser.click(getProperty("REGISTRATION_SUBMIT"));
        waitForForm();
    }

}
