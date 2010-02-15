package org.jboss.seam.example.restbay.test;

import static org.testng.Assert.assertEquals;

import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import java.util.HashMap;
import java.util.Map;

/**
 * This class tests RESTEasy integration together with Seam Security.
 *
 * @author Jozef Hartinger
 */
public class SecurityTest extends SeamTest
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
   public void basicAuthTest() throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, "/restv1/secured/admin")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), "false", "Unexpected response.");
         }

      }.run();
   }

   @Test
   public void invalidCredentialsBasicAuthTest() throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, "/restv1/secured")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpvbWVk"); // demo:omed
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
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
      new ResourceRequest(requestEnv, Method.GET, "/restv1/secured/admin")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic YWRtaW46YWRtaW4="); // admin:admin
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), "true");
         }

      }.run();
   }

   @Test
   public void adminRoleTestWithRestriction() throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, "/restv1/secured/restrictedAdmin")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic YWRtaW46YWRtaW4="); // admin:admin
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), "true");
         }

      }.run();
   }

   @Test
   public void invalidAdminAuthorization() throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, "/restv1/secured/restrictedAdmin")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            // See AuthorizationException mapping to 403 in pages.xml!
            assertEquals(response.getStatus(), 403, "Unexpected response code.");
            assert response.getStatusMessage().startsWith("Not authorized to access resource");
         }

      }.run();
   }
   
   @Test
   // JBPAPP-3713
   public void ejbLookup() throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, "/restv1/secured/ejbLookup")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assert response.getContentAsString().equals("true");
         }
         
      }.run();
   }
   
   @Test
   // JBPAPP-3713
   public void synchronizationsLookup() throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, "/restv1/secured/synchronizationsLookup")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
            request.addHeader("Authorization", "Basic ZGVtbzpkZW1v"); // demo:demo
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assert response.getContentAsString().equals("true");
         }
         
      }.run();
   }
}
