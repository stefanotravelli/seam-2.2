package org.jboss.seam.excel.ui;

public class UIHeader extends ExcelComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIHeader";

   public static final String LEFT_FACET = "left";
   public static final String CENTER_FACET = "center";
   public static final String RIGHT_FACET = "right";

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

}
