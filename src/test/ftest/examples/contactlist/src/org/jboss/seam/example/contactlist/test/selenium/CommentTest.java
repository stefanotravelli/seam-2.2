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

public class CommentTest extends SeleniumContactlistTest {

	@Test
	public void testComment() {
		String firstName = "Gavin";
		String lastName = "King";
		String message = "founder of the Hibernate open source object/relational mapping project";
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
		// submit comment
		browser.type(COMMENT_TEXTAREA, message);
		browser.click(COMMENT_SUBMIT);
		browser.waitForPageToLoad(TIMEOUT);
		// assert comment is stored
		browser.click(SEARCH_CONTACT_PAGE);
		browser.waitForPageToLoad(TIMEOUT);
		search(firstName, lastName);
		browser.click(SEARCH_RESULT_FIRST_ROW_LINK);
		browser.waitForPageToLoad(TIMEOUT);
		assertTrue("Comment is not stored.", browser
				.isTextPresent(message));
	}
}
