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
import org.testng.annotations.Test;

/**
 * This test verifies hot deployment of JavaBean components. This class should
 * only be used to test WAR packaged Seam applications.
 * 
 * @author Jozef Hartinger
 * 
 */
public class HotDeploymentNewFormTest extends NewFormTest
{

   @Override
   protected void prepareData()
   {
      newComponentProperties = new String[] { "hi", "HiLocal", "Hi", "hi", "hiPage" };
   }

   @Override
   protected void deployNewComponent()
   {
      seamGen.hotDeploy();
   }

   @Override
   @Test(groups = { "newFormGroup" }, dependsOnGroups = { "newProjectGroup" })
   public void testNewComponent()
   {
      String username = "admin";
      String password = "password";

      login(username, password);

      super.testNewComponent();

      assertTrue(isLoggedIn(), "User should be logged in by now. Hot deployment failure.");
   }

}
