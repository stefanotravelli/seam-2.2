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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

/**
 * This test verifies hot deployment of static resources. It modifies home.xhtml
 * page and verifies that changes are reflected within the running app. All is
 * done within user session to detect if the whole application was restarted or
 * not.
 * 
 * @author Jozef Hartinger
 * 
 */
public class HotDeploymentStaticTest extends SeleniumSeamGenTest
{
   
   private String newFeature;
   
   public void modifyHomePage() throws InterruptedException
   {
      String homePageLocation = WORKSPACE + "/" + APP_NAME + "/view/home.xhtml";
      newFeature = "Works flawlessly as it is tested by Selenium";
      
      BufferedReader reader = null;
      StringBuilder homePageContentBuilder = new StringBuilder();
      try
      {
         reader = new BufferedReader(new InputStreamReader(new FileInputStream(homePageLocation)));
         // load file content into String
         String line = reader.readLine();
         while (line != null)
         {
            homePageContentBuilder.append(line);
            line = reader.readLine();
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to read home page " + homePageLocation);
      }
      finally
      {
         try
         {
            reader.close();
         }
         catch (IOException e)
         {
            throw new RuntimeException("Unable to close home page reader.");
         }
      }
      
      String homePageContent = homePageContentBuilder.toString();
      
      // add new item into the feature list
      homePageContent = homePageContent.replaceAll("<li>Internationalization support</li>", "<li>Internationalization support</li>\n<li id=\"newFeature\">" + newFeature + "</li>");
      
      // write new content
      Writer writer = null;
      try
      {
         writer = new OutputStreamWriter(new FileOutputStream(homePageLocation));
         writer.write(homePageContent);
         writer.flush();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable write modified home page " + homePageLocation);
      }
      finally
      {
         try
         {
            writer.close();
         }
         catch (IOException e)
         {
            throw new RuntimeException("Unable to close home page reader.");
         }
      }
      
      seamGen.deploy();
   }
   
   @Test(dependsOnGroups = { "newProjectGroup" })
   public void hotDeploymentOfFaceletTemplateTest() throws InterruptedException
   {
      
      login();
      
      modifyHomePage();
      
      waitForAppToDeploy(HOME_PAGE, "id=newFeature");
      
      browser.open(HOME_PAGE);
      assertTrue(browser.isElementPresent("id=newFeature"), "New feature not found. Hot deployment failure.");
      assertTrue(isLoggedIn(), "Session lost. Hot deployment failure.");
   }
}
