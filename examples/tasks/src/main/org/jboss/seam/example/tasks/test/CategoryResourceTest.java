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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test class for /category part of API
 * @author Jozef Hartinger
 *
 */
public class CategoryResourceTest extends SeamTest
{
   @DataProvider(name = "query")
   public String[][] getQueryData()
   {
      return new String[][] { new String[] { "application/xml", "<category><name>School</name></category>" }, new String[] { "application/json", "{\"category\":{\"name\":\"School\"}}" } };
   }

   @Test(dataProvider = "query")
   public void getCategoryListTest(final String contentType, final String expectedResponse) throws Exception
   {
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/v1/auth/category")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", contentType);
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            super.onResponse(response);
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertTrue(response.getContentAsString().contains(expectedResponse), "Unexpected response.");
         }

      }.run();
   }

   @Test
   public void deleteCategoryTest() throws Exception
   {
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.DELETE, "/v1/auth/category/Work")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "application/xml");
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            super.onResponse(response);
            assertEquals(response.getStatus(), 204, "Unexpected response code.");
         }

      }.run();
   }

   @Test
   public void createCategoryTest() throws Exception
   {
      
      final String uri = "/v1/auth/category/Test%20Category";
      final String mimeType = "application/json";
      final String expectedResponse = "{\"category\":{\"name\":\"Test Category\"}}";
      
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.PUT, uri)
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            super.onResponse(response);
            assertEquals(response.getStatus(), 201, "Unexpected response code.");
         }

      }.run();

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, uri)
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", mimeType);
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            super.onResponse(response);
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), expectedResponse, "Unexpected response.");
         }

      }.run();
   }

   @Test
   public void noAuthorizationHeaderTest() throws Exception
   {
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/v1/auth/category")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "application/xml");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            super.onResponse(response);
            assertEquals(response.getStatus(), 401, "Unexpected response code.");
         }

      }.run();
   }

}
