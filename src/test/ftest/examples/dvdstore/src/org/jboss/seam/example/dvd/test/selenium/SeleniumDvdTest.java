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

import static org.testng.AssertJUnit.fail;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.Wait;

/**
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public abstract class SeleniumDvdTest extends SeamSeleniumTest {

    protected final String DEFAULT_USERNAME = "user1";
    protected final String DEFAULT_PASSWORD = "password";

    @Override
    @BeforeMethod
    public void setUp() {
        super.setUp();
        login(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public boolean login(String username, String password) {
        browser.open(CONTEXT_PATH + getProperty("HOME_PAGE"));
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(getProperty("LOGIN_SUBMIT"));
            }
        }.wait("Login page not loaded.", Long.valueOf(TIMEOUT));
        if (isLoggedIn(browser)) {
            fail("User already logged in.");
        }
        browser.type(getProperty("LOGIN_USERNAME"), username);
        browser.type(getProperty("LOGIN_PASSWORD"), password);
        browser.click(getProperty("LOGIN_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
        return isLoggedIn(browser);
    }

    @Override
    @AfterMethod
    public void tearDown() {
        logout(browser);
        super.tearDown();
    }

    public void logout(Selenium browser) {
        if (isLoggedIn(browser)) {
            browser.click(getProperty("LOGOUT"));
            browser.waitForPageToLoad(TIMEOUT);
        }
    }

    public boolean isLoggedIn(Selenium browser) {
        return browser.isElementPresent(getProperty("LOGOUT"));
    }

    
}
