package org.jboss.seam.test.mail.unit;

import org.jboss.seam.mail.ui.Header;
import org.testng.annotations.Test;

public class HeaderTest
{
   @Test
   public void testHeader()
   {
      String name = "foo";
      String value = "bar";
      
      Header header = new Header(name, value);
      
      assert header.getSanitizedName().equals(name);
      assert header.getSanitizedValue().equals(value);
   }
   
   @Test
   public void testHeaderWithLineFeed()
   {
      String name = "foo\nnewline";
      String value = "bar\nnewline";
      
      Header header = new Header(name, value);
      
      assert !header.getSanitizedName().equals(name);
      assert !header.getSanitizedValue().equals(value);
      
      assert "foo".equals(header.getSanitizedName());
      assert "bar".equals(header.getSanitizedValue());
   }
   
   @Test
   public void testHeaderWithCarrigeReturnLineBreak()
   {
      String name = "foo\r\nnewline";
      String value = "bar\r\nnewline";
      
      Header header = new Header(name, value);
      
      assert !header.getSanitizedName().equals(name);
      assert !header.getSanitizedValue().equals(value);
      
      assert "foo".equals(header.getSanitizedName());
      assert "bar".equals(header.getSanitizedValue());
   }
   
   @Test
   public void testHeaderWithCarriageReturn()
   {
      String name = "foo\rnewline";
      String value = "bar\rnewline";
      
      Header header = new Header(name, value);
      
      assert !header.getSanitizedName().equals(name);
      assert !header.getSanitizedValue().equals(value);
      
      assert "foo".equals(header.getSanitizedName());
      assert "bar".equals(header.getSanitizedValue());
   }
   
   @Test
   public void testHeaderWithFolding1()
   {
      String name = "header";
      String value = "\"Joe & J. Harvey\" <ddd @ Org>,\n  JJV @ BBN";
      String sanitizedValue = "\"Joe & J. Harvey\" <ddd @ Org>, JJV @ BBN";
      
      Header header = new Header(name, value);
      
      assert header.getSanitizedName().equals(name);
      assert header.getSanitizedValue().equals(sanitizedValue);
      
   }
   
   @Test
   public void testHeaderWithFolding2()
   {
      String name = "header";
      String value = "\"Joe & J. Harvey\"\n <ddd @ Org>, JJV\n @ BBN";
      String sanitizedValue = "\"Joe & J. Harvey\" <ddd @ Org>, JJV @ BBN";
      
      Header header = new Header(name, value);
      
      assert header.getSanitizedName().equals(name);
      assert header.getSanitizedValue().equals(sanitizedValue);
      
   }
   
   @Test
   public void testHeaderWithFolding3()
   {
      String name = "header";
      String value = "\"Joe &\n  J. Harvey\" <ddd @ Org>, JJV @ BBN";
      String sanitizedValue = "\"Joe & J. Harvey\" <ddd @ Org>, JJV @ BBN";
      Header header = new Header(name, value);
      
      assert header.getSanitizedName().equals(name);
      assert header.getSanitizedValue().equals(sanitizedValue);
      
   }
}
