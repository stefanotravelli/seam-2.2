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
package org.jboss.seam.example.dvd.test.selenium;

import static org.testng.AssertJUnit.*;

import java.text.MessageFormat;
import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.selenium.Wait;

/**
 * This class tests the registration functionality of dvdstore example
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class RegistrationTest extends SeleniumDvdTest {

    // suffix is needed to allow test to be run repeatedly
    private String suffix = Long.toString(new Date().getTime() % 10000000);

    @Override
    @BeforeMethod
    public void setUp() {
        browser = startBrowser();
        browser.open(CONTEXT_PATH + getProperty("HOME_PAGE"));
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(getProperty("REGISTRATION"));
            }
        }.wait("Unable to load home page.");
        browser.click(getProperty("REGISTRATION"));
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("Unable to load registration page.", browser.getLocation()
                .contains(getProperty("REGISTRATION_FIRST_PAGE_URL")));
    }

    /**
     * Tries to register user
     */
    @Test
    public void basicRegistrationTest() {
        Person person = new Person("Street 123", "012-3456-7890",
                "Visa", "City", "john@example.com", "John", "Doe",
                "password", "0123456789", "US", "john" + suffix, "password",
                "01234");

        fillFirstPage(person);
        assertTrue("Unable to load account page.", browser.getLocation()
                .contains(getProperty("REGISTRATION_SECOND_PAGE_URL")));
        fillSecondPage(person);
        assertTrue("Unable to load card page.", browser.getLocation().contains(
                getProperty("REGISTRATION_THIRD_PAGE_URL")));
        fillThirdPage(person);
        assertTrue("Unable to load confirmation page.", browser.getLocation()
                .contains(getProperty("REGISTRATION_CONFIRMATION_PAGE_URL")));
        assertTrue("Registration failed.", browser.isTextPresent(MessageFormat
                .format(getProperty("REGISTRATION_CONFIRMATION_MESSAGE"),
                        person.getUsername())));
        assertTrue("User should be logged in after succesful registration.",
                isLoggedIn(browser));
    }

    /**
     * Tests whether validation of input fields works fine.
     */
    @Test
    public void firstPageInvalidValuesTest() {
        Person person = new Person("t", "t", "t");
        fillFirstPage(person);
        assertTrue("Navigation failed.", browser.getLocation().contains(
                getProperty("REGISTRATION_FIRST_PAGE_URL")));
        assertEquals("Error messages expected.", 2, browser
                .getXpathCount(getProperty("REGISTRATION_LENGTH_MESSAGE")));
    }

    /**
     * Tests password verification.
     */
    @Test
    public void verifyPasswordTest() {
        Person person = new Person("tester", "password", "password1");
        fillFirstPage(person);
        assertTrue("Navigation failed.", browser.getLocation().contains(
                getProperty("REGISTRATION_FIRST_PAGE_URL")));
        assertTrue("Password verify message expected.", browser
                .isElementPresent(getProperty("REGISTRATION_VERIFY_MESSAGE")));
    }

    /**
     * Tries to register user that already exists. Test assumes that user1 is
     * already registered.
     */
    @Test
    public void duplicateUserTest() {
        Person person = new Person("user1", "password", "password");
        fillFirstPage(person);
        assertTrue("Navigation failed.", browser.getLocation().contains(
                getProperty("REGISTRATION_FIRST_PAGE_URL")));
        assertTrue(
                "Duplicate user error message expected.",
                browser
                        .isElementPresent(getProperty("REGISTRATION_DUPLICATE_USER_MESSAGE")));
    }

    private void fillFirstPage(Person person) {
        browser
                .type(getProperty("REGISTRATION_USERNAME"), person
                        .getUsername());
        browser
                .type(getProperty("REGISTRATION_PASSWORD"), person
                        .getPassword());
        browser.type(getProperty("REGISTRATION_VERIFY"), person.getVerify());
        browser.click(getProperty("REGISTRATION_FIRST_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
    }

    private void fillSecondPage(Person person) {
        browser.type(getProperty("REGISTRATION_FIRST_NAME"), person
                .getUsername());
        browser.type(getProperty("REGISTRATION_LAST_NAME"), person
                .getLastName());
        browser.type(getProperty("REGISTRATION_ADDRESS"), person.getAddress());
        browser
                .type(getProperty("REGISTRATION_ADDRESS2"), person
                        .getAddress2());
        browser.type(getProperty("REGISTRATION_CITY"), person.getCity());
        browser.type(getProperty("REGISTRATION_STATE"), person.getState());
        browser.type(getProperty("REGISTRATION_ZIP"), person.getZip());
        browser.type(getProperty("REGISTRATION_EMAIL"), person.getEmail());
        browser.type(getProperty("REGISTRATION_PHONE"), person.getPhone());
        browser.click(getProperty("REGISTRATION_SECOND_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
    }

    private void fillThirdPage(Person person) {
        browser.select(getProperty("REGISTRATION_CARD_TYPE_SELECT"), person
                .getCardType());
        browser.type(getProperty("REGISTRATION_CARD_NUMBER"), person
                .getCardNumber());
        fillThirdPage();
    }

    private void fillThirdPage() {
        browser.click(getProperty("REGISTRATION_THIRD_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
    }
}
