package org.jboss.seam.excel.ui.validation;

import org.jboss.seam.excel.ui.ExcelComponent;

public class UIListValidationItem extends ExcelComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.validation.UIListValidationItem";

   private String value;

   public String getValue()
   {
      return (String) valueOf("value", value);
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

}
