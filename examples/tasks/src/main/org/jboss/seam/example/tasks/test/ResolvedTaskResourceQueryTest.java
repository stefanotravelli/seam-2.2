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
package org.jboss.seam.example.tasks.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.jboss.seam.mock.SeamTest;
import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test class for /user/{username}/tasks/resolved part of API.
 * @author Jozef Hartinger
 *
 */
public class ResolvedTaskResourceQueryTest extends SeamTest
{

   // We could do this BeforeClass only once but we can't do ResourceRequests there
   @BeforeMethod
   public void resolveTask() throws Exception {
      
      final String mimeType = "application/xml";
      final String representation = "<task><id>14</id></task>";
      
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.PUT, "/v1/auth/category/School/resolved/14")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
            request.addHeader("Content-Type", mimeType);
            request.setContentType(mimeType);
            request.setContent(representation.getBytes());
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            super.onResponse(response);
            assertEquals(response.getStatus(), 204, "Unexpected response code.");
         }

      }.run();
   }
   
   @DataProvider(name="data")
   public String[][] getData() {
      return new String[][] {
            new String[] {"application/xml", "<name>Get a haircut</name>"},
            new String[] {"application/json", "\"name\":\"Get a haircut\""},
            new String[] {"application/atom+xml", "<atom:title>Get a haircut</atom:title>"}
      };
   }
   
   @Test(dataProvider="data")
   public void editTaskTest(final String mimeType, final String expectedResponsePart) throws Exception
   {
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/v1/user/demo/tasks/resolved")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", mimeType);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            super.onResponse(response);
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertTrue(response.getContentAsString().contains(expectedResponsePart), "Unexpected response.");
         }

      }.run();
   }
}
