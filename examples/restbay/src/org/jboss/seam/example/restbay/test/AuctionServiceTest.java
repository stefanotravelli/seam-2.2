package org.jboss.seam.example.restbay.test;

import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;

import java.util.HashMap;
import java.util.Map;

public class AuctionServiceTest extends SeamTest
{

   ResourceRequestEnvironment requestEnv;

   @BeforeClass
   public void prepareEnv() throws Exception
   {
      requestEnv = new ResourceRequestEnvironment(this)
      {
         @Override
         public Map<String, Object> getDefaultHeaders()
         {
            return new HashMap<String, Object>()
            {{
                  put("Accept", "text/plain");
               }};
         }
      };
   }

   @Test
   public void testCategories() throws Exception
   {

      // Just verify we can do that, even if it doesn't make much sense
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/category").run();
      
      reset();

      new ResourceRequest(requestEnv, Method.GET, "/restv1/category")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            String[] lines = response.getContentAsString().split("\n");
            assert lines[0].equals("1,Antiques");
            assert lines[1].equals("2,Art");
            assert lines[2].equals("3,Books");
         }

      }.run();
      
      reset();

      new ResourceRequest(requestEnv, Method.GET, "/restv1/category/1")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("Antiques");
         }

      }.run();

   }

   @Test
   public void testAuctions() throws Exception
   {

      new ResourceRequest(requestEnv, Method.GET, "/restv1/auction")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            // TODO: Assert content
         }

      }.run();
      
      reset();

      new ResourceRequest(requestEnv, Method.GET, "/restv1/auction/19264723")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("Whistler's Mother, original painting by James McNeill Whistler");
         }

      }.run();

   }

}