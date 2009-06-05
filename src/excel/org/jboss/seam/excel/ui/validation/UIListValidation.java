package org.jboss.seam.excel.ui.validation;

import org.jboss.seam.excel.ui.ExcelComponent;

public class UIListValidation extends ExcelComponent implements Validation
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.validation.UIListValidation";

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   public ValidationType getType()
   {
      return ValidationType.list;
   }
}
