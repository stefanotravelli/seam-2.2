package org.jboss.seam.example.restbay.resteasy;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;

/**
 * Plain JAX RS root resource, no Seam components/lifecycle.
 * 
 * @author Christian Bauer
 */
public class TestResource
{

   @Context
   protected UriInfo uriInfo;

   @Context
   protected HttpHeaders headers;

   public void setUriInfo(UriInfo uriInfo)
   {
      this.uriInfo = uriInfo;
   }

   public void setHeaders(HttpHeaders headers)
   {
      this.headers = headers;
   }

   public String echoUri()
   {
      return uriInfo.getPath();
   }

   public String echoQueryParam(String bar)
   {
      return bar;
   }

   public String echoHeaderParam(String bar)
   {
      return bar;
   }

   public String echoCookieParam(String bar)
   {
      return bar;
   }

   public String echoTwoParams(String one, String two)
   {
      return one+two;
   }

   public String echoEncoded(String val)
   {
      return val;
   }

   public String echoFormParams(MultivaluedMap<String, String> formMap)
   {
      String result = "";
      for (String s : formMap.get("foo"))
      {
         result = result + s;
      }
      return result;
   }

   public String echoFormParams2(String[] foo)
   {
      String result = "";
      for (String s : foo)
      {
         result = result + s;
      }
      return result;
   }

   public String echoFormParams3(TestForm form)
   {
      return form.toString();
   }

   public SubResource getBar(String baz)
   {
      return new SubResource(baz);
   }

   public long convertPathParam(GregorianCalendar isoDate)
   {
      return isoDate.getTime().getTime();
   }

   public String throwException()
   {
      throw new UnsupportedOperationException("foo");
   }

   public List<String[]> getCommaSeparated() {
      assert headers.getAcceptableMediaTypes().size() == 2;
      assert headers.getAcceptableMediaTypes().get(0).toString().equals("text/plain");
      assert headers.getAcceptableMediaTypes().get(1).toString().equals("text/csv");
      return new TestComponent().getCommaSeparated();
   }

   public String[] getCommaSeparatedStrings() {
      return new String[] {"foo", "bar", "baz"};
   }

   public Integer[] getCommaSeparatedIntegers() {
      return new Integer[] {1, 2, 3};
   }

}
