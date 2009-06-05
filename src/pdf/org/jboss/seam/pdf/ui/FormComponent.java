package org.jboss.seam.pdf.ui;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

public abstract class FormComponent extends UIComponentBase
{
   protected static final String FIELDS_KEY = "acrofields";
   protected static final String STAMPER_KEY = "acrostamper";

   protected Object valueOf(String name, Object defaultValue)
   {
      Object value = defaultValue;
      if (getValueExpression(name) != null)
      {
         value = getValueExpression(name).getValue(FacesContext.getCurrentInstance().getELContext());
      }
      return value;
   }
}
