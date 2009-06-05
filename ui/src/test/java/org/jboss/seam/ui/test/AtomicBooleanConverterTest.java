package org.jboss.seam.ui.test;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.jboss.seam.ui.converter.AtomicBooleanConverter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Dennis Byrne
 */

public class AtomicBooleanConverterTest
{

   @Test
   public void testGetAsObject()
   {
  Converter converter = new AtomicBooleanConverter();
      assertNull(converter.getAsObject(null, null, null));
      assertNull(converter.getAsObject(null, null, ""));
      assertNull(converter.getAsObject(null, null, " "));
      assertTrue(((AtomicBoolean) converter.getAsObject(null, null, "true")).get());
      assertTrue(((AtomicBoolean) converter.getAsObject(null, null, "true ")).get());
      assertTrue(((AtomicBoolean) converter.getAsObject(null, null, " true")).get());
      assertFalse(((AtomicBoolean) converter.getAsObject(null, null, "false")).get());
      assertFalse(((AtomicBoolean) converter.getAsObject(null, null, "false ")).get());
      assertFalse(((AtomicBoolean) converter.getAsObject(null, null, " false")).get());
      assertFalse(((AtomicBoolean) converter.getAsObject(null, null, " boom ")).get());

   }

   @Test
   public void testGetAsString()
   {
      Converter converter = new AtomicBooleanConverter();
      assertEquals("", converter.getAsString(null, null, null));
      assertEquals("", converter.getAsString(null, null, ""));
      assertEquals("true", converter.getAsString(null, null, new AtomicBoolean(true)));
      assertEquals("false", converter.getAsString(null, null, new AtomicBoolean(false)));
      try
      {
         converter.getAsString(null, null, new Boolean(true));
         fail();
      }
      catch (ConverterException c) {}

   }

}