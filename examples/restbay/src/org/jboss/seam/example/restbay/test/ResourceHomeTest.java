package org.jboss.seam.example.restbay.test;

import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * 
 * @author Jozef Hartinger
 * 
 */
public class ResourceHomeTest extends SeamTest
{

   @DataProvider(name = "queryPaths")
   public Object[][] getData()
   {
      return new String[][]{ { "/configuredCategory" }, { "/extendedCategory" } };
   }

   @Test(dataProvider = "queryPaths")
   public void testResourceHomeRead(final String resourcePath) throws Exception
   {
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><category><categoryId>1</categoryId><name>Antiques</name></category>";
      final String path = "/restv1" + resourcePath + "/1";

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, path)
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
            assertEquals(response.getContentAsString(), expectedResponse, "Unexpected response.");
         }

      }.run();
   }

   @Test(dataProvider = "queryPaths")
   public void testResourceHomeCreate(final String resourcePath) throws Exception
   {
      final String name = "Airplanes";
      final String body = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><category><name>" + name + "</name></category>";
      final String mediaType = "application/xml";
      final String path = "/restv1" + resourcePath;

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, path)
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            // TODO for some reason content type must be set using both these
            // methods
            request.addHeader("Content-Type", mediaType);
            request.setContentType(mediaType);
            request.setContent(body.getBytes());
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 201, "Unexpected response code.");
         }

      }.run();
   }

   @Test(dataProvider = "queryPaths")
   public void testResourceHomeUpdate(String resourcePath) throws Exception
   {
      final String body = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><category><categoryId>5</categoryId><name>" + resourcePath.hashCode() + "</name></category>";
      final String mediaType = "application/xml";
      final String path = "/restv1" + resourcePath + "/5";

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.PUT, path)
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.setContentType(mediaType);
            request.addHeader("Content-Type", mediaType);
            request.setContent(body.getBytes());
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 204, "Unexpected response code.");
         }

      }.run();

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, path)
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", mediaType);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), body, "Unexpected response.");
         }

      }.run();

   }

   @Test
   public void testResourceHomeDelete() throws Exception
   {

      final String path = "/restv1/configuredCategory/15004";

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.DELETE, path)
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 204, "Unexpected response code.");
         }

      }.run();

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, path)
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
            assertEquals(response.getStatus(), 404, "Unexpected response code.");
         }

      }.run();
   }
}
