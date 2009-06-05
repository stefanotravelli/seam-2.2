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

public interface ContactlistConstants {

    public static String START_PAGE = "/";
    public static String SEARCH_CONTACT_PAGE = "id=search";
    public static String SEARCH_FIRST_NAME_FIELD = "id=searchForm:firstName";
    public static String SEARCH_LAST_NAME_FIELD = "id=searchForm:lastName";
    public static String SEARCH_SUBMIT = "id=searchForm:submit";
    public static String CREATE_CONTACT_PAGE = "id=create";
    public static String CREATE_CONTACT_SUBMIT = "id=editForm:createContact";
    public static String CREATE_CONTACT_CANCEL = "id=editForm:cancel";
    public static String FIRST_NAME_FIELD = "id=editForm:firstNameDecorate:firstName";
    public static String LAST_NAME_FIELD = "id=editForm:lastNameDecorate:lastName";
    public static String CELL_PHONE_FIELD = "id=editForm:cellPhoneDecorate:cellPhone";
    public static String HOME_PHONE_FIELD = "id=editForm:homePhoneDecorate:homePhone";
    public static String ADDRESS_FIELD = "id=editForm:addressDecorate:address";
    public static String CITY_FIELD = "id=editForm:cityDecorate:city";
    public static String STATE_FIELD = "id=editForm:stateDecorate:state";
    public static String ZIP_FIELD = "id=editForm:zipDecorate:zip";
    public static String COUNTRY_FIELD = "id=editForm:countryDecorate:country";
    public static String EDIT_CONTACT_LINK = "editContact";
    public static String UPDATE_CONTACT_SUBMIT = "editForm:updateContact";
    public static String EDIT_FORM_REMOVE_CONTACT_LINK = "id=editForm:deleteContact";
    public static String REMOVE_CONTACT_LINK = "id=deleteContact";
    public static String SEARCH_RESULT_FIRST_ROW_LINK = "id=searchResult:0:link";
    public static String SEARCH_RESULT_FIRST_ROW_CELL_PHONE = "id=searchResult:0:cellPhone";
    public static String SEARCH_RESULT_FIRST_ROW_HOME_PHONE = "id=searchResult:0:homePhone";
    public static String SEARCH_RESULT_FIRST_ROW_ADDRESS = "id=searchResult:0:address";
    public static String SEARCH_RESULT_FIRST_ROW_CITY = "id=searchResult:0:city";
    public static String SEARCH_RESULT_FIRST_ROW_STATE = "id=searchResult:0:state";
    public static String SEARCH_RESULT_FIRST_ROW_ZIP = "id=searchResult:0:zip";
    public static String SEARCH_RESULT_FIRST_ROW_COUNTRY = "id=searchResult:0:country";
    public static String COMMENT_TEXTAREA = "commentForm:text";
    public static String COMMENT_SUBMIT = "commentForm:submit";
}
