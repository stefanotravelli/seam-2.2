package org.jboss.seam.pdf.ui;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfStamper;

public class UIField extends FormComponent
{
   public static final String COMPONENT_FAMILY = "org.jboss.seam.pdf.UIField";
   private static Log log = Logging.getLog(FormComponent.class);

   private String name;
   private Object value;
   private Boolean readOnly = Boolean.TRUE;

   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      AcroFields fields = (AcroFields) Contexts.getEventContext().get(FIELDS_KEY);
      String theName = getName();
      Object theValue = getValue();
      if (theValue == null) {
          return;
      }
      Boolean readOnly = getReadOnly();
      try
      {
         log.debug("Setting field '#0' to value '#1'", theName, theValue);
         boolean success = false;
         if (theValue instanceof String) {
             success = fields.setField(theName, (String) theValue);
         } else if (theValue instanceof String[]){
	     String[] stringValue = (String[])theValue;
	     if (stringValue.length>0) {
                 success = fields.setField(theName, stringValue[0]);
	     } else {
		 success = true;
	     }
         } else {
             String message = Interpolator.instance().interpolate("Field #0 expected String or String[] but got #0", getName(), theValue.getClass().getName());
             throw new IllegalArgumentException(message);
         }
         if (!success)
         {
            warnNotFound(fields, theName, theValue);
         }
         else
         {
            if (readOnly.booleanValue())
            {
               PdfStamper stamper = (PdfStamper) Contexts.getEventContext().get(STAMPER_KEY);
               stamper.partialFormFlattening(theName);
            }
         }
      }
      catch (DocumentException e)
      {
         String message = Interpolator.instance().interpolate("Could not set field '#0' to '#1'", theName, theValue);
         throw new FacesException(message, e);
      }
   }

   private void warnNotFound(AcroFields fields, String theName, Object theValue)
   {
      log.warn("Could not set field '#0' to '#1'", theName, theValue);
      Map fieldMap = fields.getFields();
      if (!fieldMap.containsKey(theName))
      {
         log.warn("Could not find field '#0'. Found fields are", theName);
         for (Iterator i = fieldMap.keySet().iterator(); i.hasNext();)
         {
            log.warn(i.next());
         }
         return;
      }
      String[] options = fields.getListOptionExport(theName);
      String[] values = fields.getListOptionDisplay(theName);
      log.warn("Valid values for #0 are", theName);
      for (int i = 0; i < options.length; i++)
      {
         log.warn("'#0' : '#1'", options[i], values[i]);
      }
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   public String getName()
   {
      return (String) valueOf("name", name);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public Object getValue()
   {
      return valueOf("value", value);
   }

   public void setValue(Object value)
   {
      this.value = value;
   }

   public Boolean getReadOnly()
   {
      return (Boolean) valueOf("readOnly", readOnly);
   }

   public void setReadOnly(Boolean readOnly)
   {
      this.readOnly = readOnly;
   }
   

}
