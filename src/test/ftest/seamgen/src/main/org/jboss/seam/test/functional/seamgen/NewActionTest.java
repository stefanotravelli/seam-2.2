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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class verifies functionality of "new-action command".
 * @author Jozef Hartinger
 *
 */
public class NewActionTest extends SeleniumSeamGenTest
{

   protected ComponentHolder newComponent;
   
   @BeforeClass
   public void createNewAction() throws InterruptedException {
      prepareData();
      generateNewComponent();
      deployNewComponent();
      waitForAppToDeploy(getComponentPath(), FOOTER);
   }


   @Test(groups = { "newActionGroup" }, dependsOnGroups = { "newProjectGroup" })
   public void testNewComponent()
   {
      String form = "id=" + newComponent.name + "Form";
      String button = form + ":" + newComponent.actionMethod;
      
      browser.open(getComponentPath());
      browser.waitForPageToLoad();

      assertTrue(browser.isElementPresent(FOOTER), "Footer not found.");
      assertTrue(browser.isElementPresent(form), form + " not found.");
      assertTrue(browser.isElementPresent(button), button + " not found.");

      browser.clickAndWait(button);

      assertTrue(browser.isElementPresent(MESSAGES));
      assertTrue(browser.getText(MESSAGES).contains(newComponent.actionMethod));
   }

   public void generateNewComponent()
   {
      seamGen.newAction(newComponent.asArray());
   }
   
   protected void prepareData() {
      // war version
      if(SeamGenTest.WAR)
         newComponent = new ComponentHolder("ping", null, "Ping", "ping", "pingPage");
      // ear version
      else
         newComponent = new ComponentHolder("ping", "PingLocal", "Ping", "ping", "pingPage");
   }
   
   public String getComponentPath() {
      return "/" + APP_NAME + "/" + newComponent.pageName + ".seam";
   }
   
   protected void deployNewComponent() {
      seamGen.restart();
   }   
}

/**
 * Holds component input for seam-gen
 * @author kpiwko
 *
 */
class ComponentHolder {
   String name;
   String localInterface;
   String beanClass;
   String actionMethod;
   String pageName;
   
   /**
    * Constructs new component holder
    * @param name Name of component
    * @param localInterface Name of local interface
    * @param beanClass Name of bean class
    * @param actionMethod Name of action method
    * @param pageName Name of Seam page
    */
   public ComponentHolder(String name, String localInterface, String beanClass, String actionMethod, String pageName) {
      this.name = name;
      this.localInterface = localInterface;
      this.beanClass = beanClass;
      this.actionMethod = actionMethod;
      this.pageName = pageName;
   }
   
   /**
    * Return properties set in holder as array of strings
    * @return Constructed array
    */
   public String[] asArray() {
      List<String> list = new ArrayList<String>();
      if(name!=null) list.add(name);
      if(localInterface!=null) list.add(localInterface);
      if(beanClass!=null) list.add(beanClass);
      if(actionMethod!=null) list.add(actionMethod);
      if(pageName!=null) list.add(pageName);
      
      return list.toArray(new String[] {});
   }
}
