package org.jboss.seam.example.restbay.test;

import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.assertEquals;

import javax.servlet.http.Cookie;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * This is the test matrix for resources:
 * </p>
 *
 * <pre>
 *                                    | EVENT | CONVERSATION | SESSION | APPLICATION | STATELESS
 * ---------------------------------------------------------------------------------------------
 * Plain JAX-RS Resource              |  OK   |      -       |    -    |      -      |    -
 * ---------------------------------------------------------------------------------------------
 * POJO Seam Component Resource       |  OK   |      ?       |    ?    |     OK      |   OK
 * ---------------------------------------------------------------------------------------------
 * POJO interface-annotated Component |  OK   |      ?       |    ?    |     OK      |   OK
 * ---------------------------------------------------------------------------------------------
 * EJB Plain SLSB Resource            |   -   |      -       |    -    |      -      |   OK
 * ---------------------------------------------------------------------------------------------
 * EJB SLSB Seam Component Resource   |   -   |      -       |    -    |      -      |   OK
 * ---------------------------------------------------------------------------------------------
 * EJB SFSB Seam Component Resource   |   ?   |      ?       |    ?    |      ?      |    -
 * ---------------------------------------------------------------------------------------------
 * </pre>
 *
 * <p>
 * Note that all EJB resources are always @Path annotated on their interface, not the implementation class.
 * </p>
 *
 * <p>
 * This is the test matrix for providers:
 * </p>
 *
 * <pre>
 *                                    | EVENT | CONVERSATION | SESSION | APPLICATION | STATELESS
 * ---------------------------------------------------------------------------------------------
 * Plain JAX-RS Provider              |   -   |      -       |    -    |      -      |   OK
 * ---------------------------------------------------------------------------------------------
 * RESTEasy StringConverter Provider  |   -   |      -       |    -    |      -      |   OK
 * ---------------------------------------------------------------------------------------------
 * RESTEasy StringConverter Component |   ?   |      -       |    -    |      ?      |    ?
 * ---------------------------------------------------------------------------------------------
 * POJO Seam Component Provider       |   ?   |      -       |    -    |     OK      |    ?
 * ---------------------------------------------------------------------------------------------
 * POJO interface-annotated Component |   ?   |      -       |    -    |     OK      |    ?
 * ---------------------------------------------------------------------------------------------
 * EJB Plain SLSB Provider            |   -   |      -       |    -    |      -      |    ?
 * ---------------------------------------------------------------------------------------------
 * EJB SLSB Seam Component Provider   |   -   |      -       |    -    |      -      |    ?
 * ---------------------------------------------------------------------------------------------
 * EJB SFSB Seam Component Resource   |   ?   |      -       |    -    |      ?      |    -
 * ---------------------------------------------------------------------------------------------
 * </pre>
 *
 */
public class BasicServiceTest extends SeamTest
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

         @Override
         public String getServletPath()
         {
            return "/override/seam/resource/is/not/my/path/for/SeamResourceServlet";
         }

      };
   }

   @DataProvider(name = "queryPaths")
   public Object[][] getData()
   {
      return new String[][] {
            { "/restv1/plainTest" },

            { "/restv1/eventComponentTest" },
            { "/restv1/applicationComponentTest" },
            { "/restv1/statelessComponentTest" },

            { "/restv1/interfaceEventComponentTest" },
            { "/restv1/interfaceApplicationComponentTest" },
            { "/restv1/interfaceStatelessComponentTest" },

            { "/restv1/statelessEjbTest" },
            { "/restv1/statelessEjbComponentTest" }
      };
   }

   @Test(dataProvider = "queryPaths")
   public void testExeptionMapping(final String resourcePath) throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/trigger/unsupported")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 501;
            assert response.getStatusMessage().equals("The request operation is not supported: foo");
         }

      }.run();

   }

   @Test(dataProvider = "queryPaths")
   public void testEchos(final String resourcePath) throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/echouri")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().endsWith("/echouri");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/echoquery")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setQueryString("asdf=123");
            request.addQueryParameter("bar", "bbb");
            request.addQueryParameter("baz", "bzzz");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("bbb");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/echoheader")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader("bar", "baz");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("baz");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/echocookie")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addCookie(new Cookie("bar", "baz"));
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("baz");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/foo/bar/asdf")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {

            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("bar: asdf");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/echotwoparams/foo/bar")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("foobar");
         }

      }.run();

   }

   @Test(dataProvider = "queryPaths")
   public void testEncoding(final String resourcePath) throws Exception
   {
      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/echoencoded/foo bar")
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("foo%20bar");
         }

      }.run();
   }

   @Test(dataProvider = "queryPaths")
   public void testFormHandling(final String resourcePath) throws Exception
   {
      new ResourceRequest(requestEnv, Method.POST, resourcePath + "/echoformparams")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            request.addParameter("foo", new String[]{"bar", "baz"});
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("barbaz");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.POST, resourcePath + "/echoformparams2")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            request.addParameter("foo", new String[]{"bar", "baz"});
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("barbaz");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.POST, resourcePath + "/echoformparams3")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            request.addHeader("bar", "foo");
            request.addParameter("foo", new String[]{"bar", "baz"});
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("foobarbaz");
         }

      }.run();

   }

   @Test(dataProvider = "queryPaths")
   public void testStringConverter(final String resourcePath) throws Exception
   {
      final String ISO_DATE = "2007-07-10T14:54:56-0500";
      final String ISO_DATE_MILLIS = "1184097296000";

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/convertDate/" + ISO_DATE)
      {

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assertEquals(response.getContentAsString(), ISO_DATE_MILLIS);
         }

      }.run();

   }

   @Test(dataProvider = "queryPaths")
   public void testProvider(final String resourcePath) throws Exception
   {

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/commaSeparated")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader("Accept", "text/csv");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("foo,bar\r\nasdf,123\r\n");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/commaSeparatedStrings")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader("Accept", "text/plain");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("abc,foo,bar,baz");
         }

      }.run();

      new ResourceRequest(requestEnv, Method.GET, resourcePath + "/commaSeparatedIntegers")
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader("Accept", "text/plain");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assert response.getStatus() == 200;
            assert response.getContentAsString().equals("abc,1,2,3");
         }

      }.run();

   }
}
