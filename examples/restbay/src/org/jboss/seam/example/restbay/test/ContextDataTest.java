package org.jboss.seam.example.restbay.test;

import static org.testng.Assert.assertEquals;

import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author Jozef Hartinger
 */
public class ContextDataTest extends SeamTest
{
   @DataProvider(name = "contextDataTypes")
   public Object[][] getContextDataTypePaths()
   {
      return new String[][]{ { "/providers" }, { "/registry" }, { "/dispatcher" } };
   }
   
   @Test(dataProvider = "contextDataTypes")
   public void testContextData(String pathSegment) throws Exception
   {
      final String path = "/restv1/contextData" + pathSegment;

      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, path)
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "text/plain");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertEquals(response.getStatus(), 200, "Unexpected response code.");
            assertEquals(response.getContentAsString(), "true", "Unexpected response.");
         }

      }.run();
   }
}
