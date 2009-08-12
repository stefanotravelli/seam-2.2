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

package org.jboss.seam.example.guice.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;

 /*
 * @author Martin Gencur
 * 
 */
public class GuiceTest extends SeamSeleniumTest
{
   protected static final String BAR_URL = "/bar.seam";
   protected static final String JUICE_OF_THE_DAY = "Apple Juice* - 10 cents"; 
   protected static final String ANOTHER_JUICE = "Orange Juice - 12 cents";
   protected static final String GUICE_TITLE = "Juice Bar";    
   
   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + BAR_URL);
   }

   @Test
   public void simplePageContentTest()
   {
      assertTrue("Home page of Guice Example expected", browser.getLocation().contains(BAR_URL));      
      assertTrue("Different page title expected",browser.getTitle().contains(GUICE_TITLE));      
      assertTrue("Juice of the day should contain its name and price", browser.isTextPresent(JUICE_OF_THE_DAY));
      assertTrue("Another juice should contain its name and price", browser.isTextPresent(ANOTHER_JUICE));      
   }
   
}
