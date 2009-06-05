package org.jboss.seam.ui.test;

import java.io.UnsupportedEncodingException;

import org.jboss.seam.ui.util.UrlBuilder;
import org.testng.annotations.Test;

public class UrlBuilderTest
{
   @Test
   public void testBaseUrlAlreadyHasParams() throws UnsupportedEncodingException
   {
      UrlBuilder url = new UrlBuilder("/someurl?arg1=a", "", "UTF8");
      url.addParameter("foo", "bar");

      String encodedUrl = url.getEncodedUrl();
      
      assert "/someurl?arg1=a&foo=bar".equals(encodedUrl);
   } 
   
   @Test
   public void testParameterOrdering() throws UnsupportedEncodingException
   {
      UrlBuilder url = new UrlBuilder("/Hotel.seam", "", "UTF-8");
      url.addParameter("hotelId", "5");
      url.addParameter("cid", "10");
      url.addParameter("z", "z");
      url.addParameter("a", "a");
      String encodedUrl = url.getEncodedUrl();
      assert "/Hotel.seam?hotelId=5&cid=10&z=z&a=a".equals(encodedUrl) : "Parameters not properly ordered";
   }
}
