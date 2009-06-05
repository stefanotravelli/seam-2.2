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
package org.jboss.seam.example.contactlist.test.selenium;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;
import com.thoughtworks.selenium.Wait;

public class ContactCRUDTest extends SeleniumContactlistTest {

    // used for creation test
    private Person john = new Person("John", "Doe", "0123456789", "0123456789",
            "Street", "City", "State", "01234", "US");
    private Person jane = new Person("Jane", "Doe", "0123456789", "0123456789",
            "Street", "City", "State", "01234", "US");
    // used for edit test
    private Person jozef = new Person("Jozef", "Hartinger", "0123456789",
            "0123456789", "Cervinkova 99", "Brno", "Czech Republic", "01234",
            "CZ");

    @Test
    public void testCreateContact() {
        browser.open(CONTEXT_PATH + START_PAGE);
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(CREATE_CONTACT_PAGE);
            }
        }.wait("Create contact link not found.");
        browser.click(CREATE_CONTACT_PAGE);
        browser.waitForPageToLoad(TIMEOUT);
        fillCreateContactForm(john);
        browser.click(CREATE_CONTACT_SUBMIT);
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(SEARCH_CONTACT_PAGE);
            }
        }.wait("Search link not found.");
        browser.click(SEARCH_CONTACT_PAGE);
        browser.waitForPageToLoad(TIMEOUT);
        search(john);
        assertTrue("Creating new contact failed.", searchResultPresent(john));
    }

    @Test
    public void testCreationCanceling() {
        browser.open(CONTEXT_PATH + START_PAGE);
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(CREATE_CONTACT_PAGE);
            }
        }.wait("Create contact link not found.");
        browser.click(CREATE_CONTACT_PAGE);
        browser.waitForPageToLoad(TIMEOUT);
        fillCreateContactForm(jane);
        browser.click(CREATE_CONTACT_CANCEL);
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(SEARCH_CONTACT_PAGE);
            }
        }.wait("Search link not found.");
        browser.click(SEARCH_CONTACT_PAGE);
        browser.waitForPageToLoad(TIMEOUT);
        browser.type(SEARCH_FIRST_NAME_FIELD, jane.getFirstName());
        browser.type(SEARCH_LAST_NAME_FIELD, jane.getLastName());
        browser.click(SEARCH_SUBMIT);
        browser.waitForPageToLoad(TIMEOUT);
        assertFalse("New contact created despite cancel.", searchResultPresent(jane));
    }

    @Test
    public void testEditContact() {
        String firstName = "Shane";
        String lastName = "Bryzak";
        // find contact
        browser.open(CONTEXT_PATH + START_PAGE);
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(SEARCH_SUBMIT);
            }
        }.wait("Search submit link not found.");
        search(firstName, lastName);
        assertTrue("Contact not found. Application is in unexpected state.",
                searchResultPresent(firstName, lastName));
        browser.click(SEARCH_RESULT_FIRST_ROW_LINK);
        browser.waitForPageToLoad(TIMEOUT);
        browser.click(EDIT_CONTACT_LINK);
        browser.waitForPageToLoad(TIMEOUT);
        // update form fields
        fillCreateContactForm(jozef);
        browser.click(UPDATE_CONTACT_SUBMIT);
        browser.waitForPageToLoad(TIMEOUT);
        // make sure new values are present
        browser.click(SEARCH_CONTACT_PAGE);
        browser.waitForPageToLoad(TIMEOUT);
        search(jozef);
        assertTrue("Contact update failed. New values missing", searchResultPresent(jozef));
        // make sure old values are not present
        browser.click(SEARCH_CONTACT_PAGE);
        browser.waitForPageToLoad(TIMEOUT);
        search(firstName, lastName);
        assertFalse("Contact update failed. Old values still present", searchResultPresent(firstName, lastName));
    }

    @Test
    public void testRemoveContact() {
        String firstName = "Norman";
        String lastName = "Richards";
        // find contact
        browser.open(CONTEXT_PATH + START_PAGE);
        new Wait() {
            @Override
            public boolean until() {
                return browser.isElementPresent(SEARCH_SUBMIT);
            }
        }.wait("Search submit link not found.");
        search(firstName, lastName);
        assertTrue("Contact not found. Application is in unexpected state.",
                searchResultPresent(firstName, lastName));
        browser.click(SEARCH_RESULT_FIRST_ROW_LINK);
        browser.waitForPageToLoad(TIMEOUT);
        // remove contact
        browser.click(REMOVE_CONTACT_LINK);
        browser.waitForPageToLoad(TIMEOUT);
        // assert contact is removed
        browser.click(SEARCH_CONTACT_PAGE);
        browser.waitForPageToLoad(TIMEOUT);
        search(firstName, lastName);
        assertFalse("Contact present despite it should be removed.", searchResultPresent(firstName, lastName));
    }
}
