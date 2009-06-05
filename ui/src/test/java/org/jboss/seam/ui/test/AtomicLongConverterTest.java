package org.jboss.seam.ui.test;

import java.util.concurrent.atomic.AtomicLong;

import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.jboss.seam.ui.converter.AtomicLongConverter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Dennis Byrne
 */

public class AtomicLongConverterTest
{

   @Test
   public void testGetAsObject()
   {
      Converter converter = new AtomicLongConverter();
      assertNull(converter.getAsObject(null, null, null));
      assertNull(converter.getAsObject(null, null, ""));
      assertNull(converter.getAsObject(null, null, " "));
      assertTrue(8 == ((AtomicLong) converter.getAsObject(null, null, " 8")).longValue());
      assertTrue(8 == ((AtomicLong) converter.getAsObject(null, null, "8 ")).longValue());
      assertTrue(8 == ((AtomicLong) converter.getAsObject(null, null, "8")).longValue());
      long over = Long.MAX_VALUE + 1;
      assertTrue(over == ((AtomicLong) converter.getAsObject(null, null, over + "")).longValue());
      long under = Long.MIN_VALUE - 1;
      assertTrue(under == ((AtomicLong) converter.getAsObject(null, null, under + "")).longValue());
      try
      {
         converter.getAsObject(null, null, "NaN");
         fail("should only take numbers");
      }
      catch (ConverterException c) { }

   }

   @Test
   public void testGetAsString()
   {
      Converter converter = new AtomicLongConverter();
      assertEquals("", converter.getAsString(null, null, null));
      assertEquals("", converter.getAsString(null, null, ""));
      assertEquals(" ", converter.getAsString(null, null, " "));
      assertEquals("-1", converter.getAsString(null, null, new AtomicLong(-1)));
      try
      {
         converter.getAsString(null, null, new Long(0));
         fail("should only take atomic ints");
      }
      catch (ConverterException c) { }
   }

}