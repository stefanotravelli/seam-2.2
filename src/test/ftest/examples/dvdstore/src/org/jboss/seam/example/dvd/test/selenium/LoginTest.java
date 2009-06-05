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
 * This class tests user authentication. Majority of other tests depends on
 * these methods.
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class LoginTest extends SeleniumDvdTest {

    @Override
    @BeforeMethod
    public void setUp() {
        browser = startBrowser();
    }

    @Test(groups = { "login.basic" })
    public void basicLoginTest() {
        String username = "user1";
        String password = "password";
        assertTrue("Login failed.", login(username, password));
    }

    @Test(groups = { "login.basic" })
    public void invalidLoginTest() {
        String username = "nonExistingUser";
        String password = "invalidPassword";
        assertFalse("Logged in despite invalid credentials.", login(username,
                password));
    }

    @Test(groups = { "login.admin" })
    public void adminLoginTest() {
        String username = "manager";
        String password = "password";
        assertTrue("Login failed.", login(username, password));
        assertTrue("Navigation failed", browser.getLocation().contains(
                getProperty("ADMIN_URL")));
    }
}
