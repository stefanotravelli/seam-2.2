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
package org.jboss.seam.test.functional.seamgen;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * This class verifies functionality of "new-action command".
 * @author Jozef Hartinger
 *
 */
public class NewFormTest extends NewActionTest
{
   
   @Override
   @Test(groups = { "newFormGroup" }, dependsOnGroups = { "newProjectGroup" })
   public void testNewComponent()
   {
      
      String form = "id=" + newComponent.name + "Form";
      String button = form + ":" + newComponent.actionMethod;
      String field = form + ":" + "valueField:value";
      String value = "world";
      
      browser.open(getComponentPath());

      assertTrue(browser.isElementPresent(FOOTER), "Footer not found.");
      assertTrue(browser.isElementPresent(form), form + " not found.");
      assertTrue(browser.isElementPresent(field), field + " not found.");
      assertTrue(browser.isElementPresent(button), button + " not found.");

      browser.type(field, value);
      browser.clickAndWait(button);

      assertTrue(browser.isElementPresent(MESSAGES), "Message not found.");
      assertEquals(browser.getText(MESSAGES), newComponent.actionMethod + " " + value, "Unexpected form output.");
   }

   @Override
   protected void prepareData() {
      // war version
      if (SeamGenTest.WAR)
         newComponent = new ComponentHolder("hello", null, "Hello", "hello", "helloPage");
      // ear version
      else
         newComponent = new ComponentHolder("hello", "HelloLocal", "Hello", "hello", "helloPage");
   }
 
   @Override
   public void generateNewComponent()
   {
      seamGen.newForm(newComponent.asArray());
   }
}
