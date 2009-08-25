package org.jboss.seam.test.unit.web;

import org.testng.annotations.Test;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.jboss.seam.mock.MockHttpSession;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.web.ConditionalAbstractResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;

/**
 * @author Christian Bauer
 *
 */
public class ConditionalRequestTest
{
   @Test
   public void testNotModifiedOnlyETag() throws Exception
   {

      HttpSession session = new MockHttpSession();
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest(session);
      EnhancedMockHttpServletResponse response = new EnhancedMockHttpServletResponse();

      request.addHeader(ConditionalAbstractResource.HEADER_IF_NONE_MATCH, "\"1234\", \"5678\"");

      ConditionalAbstractResource resource = new ConditionalAbstractResource()
      {
         public void getResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
         {
            if (!sendConditional(request, response, "\"5678\"", null))
            {
               response.sendError(HttpServletResponse.SC_OK);
            }
         }

         public String getResourcePath()
         {
            return null;
         }
      };

      resource.getResource(request, response);

      assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_MODIFIED);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_ETAG), "\"5678\"");

   }

   @Test
   public void testModifiedOnlyETag() throws Exception
   {

      HttpSession session = new MockHttpSession();
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest(session);
      EnhancedMockHttpServletResponse response = new EnhancedMockHttpServletResponse();

      request.addHeader(ConditionalAbstractResource.HEADER_IF_NONE_MATCH, "\"123\", \"456\"");

      ConditionalAbstractResource resource = new ConditionalAbstractResource()
      {
         public void getResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
         {
            if (!sendConditional(request, response, "\"5678\"", null))
            {
               response.sendError(HttpServletResponse.SC_OK);
            }
         }

         public String getResourcePath()
         {
            return null;
         }
      };

      resource.getResource(request, response);

      assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_ETAG), "\"5678\"");
   }

   @Test
   public void testNotModifiedOnlyLastModified() throws Exception
   {

      HttpSession session = new MockHttpSession();
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest(session);
      EnhancedMockHttpServletResponse response = new EnhancedMockHttpServletResponse();

      final Long currentTime = new Date().getTime();
      request.addHeader(ConditionalAbstractResource.HEADER_IF_MODIFIED_SINCE, currentTime);

      ConditionalAbstractResource resource = new ConditionalAbstractResource()
      {
         public void getResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
         {
            if (!sendConditional(request, response, null, currentTime))
            {
               response.sendError(HttpServletResponse.SC_OK);
            }
         }

         public String getResourcePath()
         {
            return null;
         }
      };

      resource.getResource(request, response);

      assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_MODIFIED);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_LAST_MODIFIED), currentTime);

   }

   @Test
   public void testModifiedOnlyLastModified() throws Exception
   {

      HttpSession session = new MockHttpSession();
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest(session);
      EnhancedMockHttpServletResponse response = new EnhancedMockHttpServletResponse();

      final Long currentTime = new Date().getTime();
      request.addHeader(ConditionalAbstractResource.HEADER_IF_MODIFIED_SINCE, currentTime);

      ConditionalAbstractResource resource = new ConditionalAbstractResource()
      {
         public void getResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
         {
            if (!sendConditional(request, response, null, currentTime + 5000))
            {
               response.sendError(HttpServletResponse.SC_OK);
            }
         }

         public String getResourcePath()
         {
            return null;
         }
      };

      resource.getResource(request, response);

      assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_LAST_MODIFIED), currentTime + 5000);

   }

   @Test
   public void testNotModifiedETagLastModified() throws Exception
   {

      HttpSession session = new MockHttpSession();
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest(session);
      EnhancedMockHttpServletResponse response = new EnhancedMockHttpServletResponse();

      final Long currentTime = new Date().getTime();
      request.addHeader(ConditionalAbstractResource.HEADER_IF_MODIFIED_SINCE, currentTime);
      request.addHeader(ConditionalAbstractResource.HEADER_IF_NONE_MATCH, "\"1234\", \"5678\"");

      ConditionalAbstractResource resource = new ConditionalAbstractResource()
      {
         public void getResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
         {
            if (!sendConditional(request, response, "\"5678\"", currentTime))
            {
               response.sendError(HttpServletResponse.SC_OK);
            }
         }

         public String getResourcePath()
         {
            return null;
         }
      };

      resource.getResource(request, response);

      assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_MODIFIED);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_LAST_MODIFIED), currentTime);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_ETAG), "\"5678\"");

   }

   @Test
   public void testModifiedETagLastModified() throws Exception
   {

      HttpSession session = new MockHttpSession();
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest(session);
      EnhancedMockHttpServletResponse response = new EnhancedMockHttpServletResponse();

      final Long currentTime = new Date().getTime();
      request.addHeader(ConditionalAbstractResource.HEADER_IF_MODIFIED_SINCE, currentTime);
      request.addHeader(ConditionalAbstractResource.HEADER_IF_NONE_MATCH, "\"1234\", \"5678\"");

      ConditionalAbstractResource resource = new ConditionalAbstractResource()
      {
         public void getResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
         {
            if (!sendConditional(request, response, "\"5678\"", currentTime + 5000))
            {
               response.sendError(HttpServletResponse.SC_OK);
            }
         }

         public String getResourcePath()
         {
            return null;
         }
      };

      resource.getResource(request, response);

      assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_LAST_MODIFIED), currentTime + 5000);
      assertEquals(response.getHeader(ConditionalAbstractResource.HEADER_ETAG), "\"5678\"");

   }
}
