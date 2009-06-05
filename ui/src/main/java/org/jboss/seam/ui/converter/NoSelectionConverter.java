package org.jboss.seam.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class NoSelectionConverter implements Converter
{
	public static final String NO_SELECTION_VALUE = "org.jboss.seam.ui.NoSelectionConverter.noSelectionValue";

   public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException
   {
      if (value == null) {
    	  return null;
      } else if (value.equals(NO_SELECTION_VALUE)) {
    	  return null;
      } else {
    	  return ConverterChain.CONTINUE;
      }
   }

   public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException
   {
      if (value == null) {
    	  return NO_SELECTION_VALUE;
      } else {
    	  return ConverterChain.CONTINUE;
      }
   }

}
