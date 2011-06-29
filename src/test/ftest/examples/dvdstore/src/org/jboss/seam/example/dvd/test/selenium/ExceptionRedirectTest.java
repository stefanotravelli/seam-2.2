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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests messages specified in exception redirects in pages.xml
 * 
 * @author maschmid
 * 
 */
public class ExceptionRedirectTest extends SeleniumDvdTest {

    @Override
    @BeforeMethod
    public void setUp() {
        browser = startBrowser();
    }

    @Test
    public void notLoggedInExceptionRedirectTest() {
        browser.open(CONTEXT_PATH + "/" + getProperty("ADMIN_URL"));

        assertFalse("On the admin page despite not logged in", 
                browser.getLocation().contains(getProperty("ADMIN_URL")));
        assertTrue("Not redirected to home", 
                browser.getLocation().contains(getProperty("HOME_URL")));
        
        assertFalse("The word `Exception' appeared on the page", 
                browser.isTextPresent("Exception"));
        assertTrue("Expected message didn't appear",
                browser.isTextPresent(getProperty(
                        "REDIRECT_NOT_LOGGED_IN_EXCEPTION_MESSAGE")));
    }
    
    @Test
    public void authorizationExceptionRedirectTest() {
        login(DEFAULT_USERNAME, DEFAULT_PASSWORD);
        browser.open(CONTEXT_PATH + "/" + getProperty("ADMIN_URL"));

        assertFalse("On the admin page despite logged in as an ordinary user",
                browser.getLocation().contains(getProperty("ADMIN_URL")));
        assertTrue("Not redirected to home", 
                browser.getLocation().contains(getProperty("HOME_URL")));
        
        assertFalse("The word `Exception' appeared on the page", 
                browser.isTextPresent("Exception"));
        assertTrue("Expected message didn't appear",
                browser.isTextPresent(getProperty(
                        "REDIRECT_AUTHORIZATION_EXCEPTION_MESSAGE")));
    }
}
