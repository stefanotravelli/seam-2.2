package org.jboss.seam.ui.converter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * @author Dennis Byrne
 */

public class AtomicBooleanConverter implements Converter
{

   public Object getAsObject(FacesContext ctx, UIComponent ui, String string)
   {
      return string != null && string.trim().length() > 0 ? new AtomicBoolean(Boolean.parseBoolean(string.trim())) : null;
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
         else if (object instanceof AtomicBoolean)
         {
            string = ((AtomicBoolean) object).toString();
         }
         else
         {
            throw new ConverterException("Received an instance of " + object.getClass().getName() + ", but was expecting an instance of " + AtomicInteger.class.getName());
         }
      }
      return string;
   }

}
