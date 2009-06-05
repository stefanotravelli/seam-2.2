package org.jboss.seam.excel.ui;

public class UIFooter extends ExcelComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIFooter";

   public static final String LEFT_FACET = "left";
   public static final String CENTER_FACET = "center";
   public static final String RIGHT_FACET = "right";

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

}
