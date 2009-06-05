package org.jboss.seam.ui.converter;

import java.util.concurrent.atomic.AtomicLong;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * @author Dennis Byrne
 */

public class AtomicLongConverter implements Converter
{

   public Object getAsObject(FacesContext ctx, UIComponent ui, String value)
   {
      Object object = null;
      if (value != null && value.trim().length() > 0)
      {
         try
         {
            object = new AtomicLong(Long.parseLong(value.trim()));
         }
         catch (NumberFormatException nfe)
         {
            throw new ConverterException(nfe);
         }
      }
      return object;
   }

   public String getAsString(FacesContext ctx, UIComponent ui, Object object)
   {
      String string = "";
      if (object != null)
      {
         if (object instanceof String)
         {
            string = (String) object;
         }
         else if (object instanceof AtomicLong)
         {
            string = ((AtomicLong) object).toString();
         }
         else
         {
            throw new ConverterException("Received an instance of " + object.getClass().getName() + ", but was expecting an instance of " + AtomicLong.class.getName());
         }
      }
      return string;
   }
}
