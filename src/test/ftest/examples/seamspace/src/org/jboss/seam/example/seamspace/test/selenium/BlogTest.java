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
package org.jboss.seam.example.seamspace.test.selenium;

import java.text.MessageFormat;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class tests blog functionality of SeamSpace application
 * @author Jozef Hartinger
 *
 */
public class BlogTest extends SeleniumSeamSpaceTest
{

   public static final String CREATE_BLOG_ENTRY = "id=createBlog";
   public static final String VIEW_BLOG_ENTRY = "id=viewBlog";
   public static final String BLOG_ENTRY_COUNT = "//div[@class=\"blogEntry\"]";
   public static final String NEW_BLOG_TITLE = "id=newBlog:title";
   public static final String NEW_BLOG_TEXT = "id=newBlog:text";
   public static final String NEW_BLOG_SUBMIT = "id=newBlog:submit";
   public static final String NEW_BLOG_PREVIEW = "id=newBlog:preview";
   public static final String BLOG_ENTRY_BY_TITLE = "xpath=//div[@class=\"blogEntry\"][.//div[@class=\"blogTitle\"]/text() = \"{0}\"]";
   public static final String BLOG_ENTRY_TEXT = "//div[@class=\"blogText\"]";
   
   @Test
   public void createBlogTest() {
      String title = "What is Seam?";
      String text = "Seam is a powerful open source development platform for building rich Internet applications in Java. Seam integrates technologies such as Asynchronous JavaScript and XML (AJAX), JavaServer Faces (JSF), Java Persistence (JPA), Enterprise Java Beans (EJB 3.0) and Business Process Management (BPM) into a unified full-stack solution, complete with sophisticated tooling.";
      int blogCount = 0;
      
      browser.clickAndWait(VIEW_BLOG_ENTRY);
      blogCount = browser.getXpathCount(BLOG_ENTRY_COUNT).intValue();
      browser.goBackAndWait();
      browser.clickAndWait(CREATE_BLOG_ENTRY);
      browser.type(NEW_BLOG_TITLE, title);
      browser.type(NEW_BLOG_TEXT, text);
      browser.clickAndWait(NEW_BLOG_SUBMIT);
      assertEquals("Unexpected number of blog entries.", ++blogCount, browser.getXpathCount(BLOG_ENTRY_COUNT).intValue());
      String blogEntry = MessageFormat.format(BLOG_ENTRY_BY_TITLE, title);
      assertTrue("Blog entry not found. " + blogEntry, browser.isElementPresent(blogEntry));
      assertEquals("Blog entry text has been modified.", text, browser.getText(blogEntry + BLOG_ENTRY_TEXT));
   }
   
}
