package org.jboss.seam.example.restbay.test;

import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.jboss.seam.mock.DBUnitSeamTest;

import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;

import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;

import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class CategoryServiceDBUnitTest extends DBUnitSeamTest
{

   protected void prepareDBUnitOperations() {
       beforeTestOperations.add(
               new DataSetOperation("org/jboss/seam/example/restbay/test/dbunitdata.xml", DatabaseOperation.CLEAN_INSERT)
       );
   }

   // Or, if you don't want shared headers between test methods, just use
   // it directly in your test method:

   // new ResourceRequest(new ResourceRequestTest(this), Method.GET, ...).run();

   ResourceRequestEnvironment sharedEnvironment;
   @BeforeClass
   public void prepareSharedEnvironment() throws Exception
   {
      sharedEnvironment = new ResourceRequestEnvironment(this)
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
      // new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/category")
      // or:
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/category")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            // Or set it as default in environment
            request.addHeader("Accept", "text/plain");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            String[] lines = response.getContentAsString().split("\n");
            assert lines[0].equals("16,foo");
            assert lines[1].equals("17,bar");
            assert lines[2].equals("18,baz");
         }

      }.run();

   }


}
