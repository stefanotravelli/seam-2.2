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

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;

public class SeleniumContactlistTest extends SeamSeleniumTest implements ContactlistConstants {

    public boolean searchResultPresent(String firstName, String lastName) {
        return browser.isElementPresent(SEARCH_RESULT_FIRST_ROW_LINK) && 
            browser.getText(SEARCH_RESULT_FIRST_ROW_LINK).equals(firstName + " " + lastName);
    }
    
    public boolean searchResultPresent(Person person) {
        return searchResultPresent(person.getFirstName(), person.getLastName()) &&
            browser.getText(SEARCH_RESULT_FIRST_ROW_CELL_PHONE).equals(person.getCellPhone()) &&
            browser.getText(SEARCH_RESULT_FIRST_ROW_HOME_PHONE).equals(person.getHomePhone()) &&
            browser.getText(SEARCH_RESULT_FIRST_ROW_ADDRESS).equals(person.getAddress()) &&
            browser.getText(SEARCH_RESULT_FIRST_ROW_CITY).equals(person.getCity()) &&
            browser.getText(SEARCH_RESULT_FIRST_ROW_STATE).equals(person.getState()) &&
            browser.getText(SEARCH_RESULT_FIRST_ROW_ZIP).equals(person.getZip()) &&
            browser.getText(SEARCH_RESULT_FIRST_ROW_COUNTRY).equals(person.getCountry());
    }
    
    public void fillCreateContactForm(Person person) {
        browser.type(FIRST_NAME_FIELD, person.getFirstName());
        browser.type(LAST_NAME_FIELD, person.getLastName());
        browser.type(CELL_PHONE_FIELD, person.getCellPhone());
        browser.type(HOME_PHONE_FIELD, person.getHomePhone());
        browser.type(ADDRESS_FIELD, person.getAddress());
        browser.type(CITY_FIELD, person.getCity());
        browser.type(STATE_FIELD, person.getState());
        browser.type(ZIP_FIELD, person.getZip());
        browser.type(COUNTRY_FIELD, person.getCountry());
    }
    
    public void search(String firstName, String lastName) {
        browser.type(SEARCH_FIRST_NAME_FIELD, firstName);
        browser.type(SEARCH_LAST_NAME_FIELD, lastName);
        browser.click(SEARCH_SUBMIT);
        browser.waitForPageToLoad(TIMEOUT);
    }
    
    public void search(Person person) {
        search(person.getFirstName(), person.getLastName());
    }
}
