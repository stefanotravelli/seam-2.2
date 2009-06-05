package org.jboss.seam.example.restbay.test;

import static org.testng.Assert.assertEquals;

import org.jboss.seam.resteasy.testfwk.ResourceSeamTest;
import org.jboss.seam.resteasy.testfwk.MockHttpServletRequest;
import org.jboss.seam.resteasy.testfwk.MockHttpServletResponse;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * This class tests RESTEasy integration together with Seam Security.
 *
 * @author Jozef Hartinger
 */
public class SecurityTest extends ResourceSeamTest
{

   @Override
   public Map<String, Object> getDefaultHeaders()
   {
      return new HashMap<String, Object>()
      {{
            put("Accept", "text/plain");
      }};
   }

   @Test
   public void basicAuthTest() throws Exception
   {
      new ResourceRequest(Method.GET, "/restv1/secured/admin")
      {
         @Override
         protected void prepareRequest(MockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(MockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), "false", "Unexpected response.");
         }

      }.run();
   }

   @Test
   public void invalidCredentialsBasicAuthTest() throws Exception
   {
      new ResourceRequest(Method.GET, "/restv1/secured")
      {
         @Override
         protected void prepareRequest(MockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpvbWVk"); // demo:omed
         }

         @Override
         protected void onResponse(MockHttpServletResponse response)
         {
            assertEquals(
                  response.getHeader("WWW-Authenticate"),
                  "Basic realm=\"Seam RestBay Application\"",
                  "Invalid authentication header value"
            );
            assertEquals(response.getStatus(), 401, "Unexpected response code.");
         }

      }.run();
   }

   @Test
   public void adminRoleTest() throws Exception
   {
      new ResourceRequest(Method.GET, "/restv1/secured/admin")
      {
         @Override
         protected void prepareRequest(MockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic YWRtaW46YWRtaW4="); // admin:admin
         }

         @Override
         protected void onResponse(MockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), "true");
         }

      }.run();
   }

   @Test
   public void adminRoleTestWithRestriction() throws Exception
   {
      new ResourceRequest(Method.GET, "/restv1/secured/restrictedAdmin")
      {
         @Override
         protected void prepareRequest(MockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic YWRtaW46YWRtaW4="); // admin:admin
         }

         @Override
         protected void onResponse(MockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), "true");
         }

      }.run();
   }

   @Test
   public void invalidAdminAuthorization() throws Exception
   {
      new ResourceRequest(Method.GET, "/restv1/secured/restrictedAdmin")
      {
         @Override
         protected void prepareRequest(MockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(MockHttpServletResponse response)
         {
            // See AuthorizationException mapping to 403 in pages.xml!
            assertEquals(response.getStatus(), 403, "Unexpected response code.");
            assert response.getStatusMessage().startsWith("Not authorized to access resource");
         }

      }.run();
   }

}
