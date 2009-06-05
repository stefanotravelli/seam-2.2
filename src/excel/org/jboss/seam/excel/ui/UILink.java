package org.jboss.seam.excel.ui;

public class UILink extends ExcelComponent
{
   private static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UILink";
   
   private String URL;
   
   public String getURL()
   {
      return (String) valueOf("URL", URL);
   }

   public void setURL(String url)
   {
      URL = url;
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

}
