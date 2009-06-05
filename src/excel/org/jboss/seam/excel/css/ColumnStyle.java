package org.jboss.seam.excel.css;

import java.util.Map;

/**
 * Helper class that collects column info into one package from stylesheet data
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 */
public class ColumnStyle
{
   public Boolean autoSize;
   public Boolean hidden;
   public Integer width;
   public Boolean export;
   
   public ColumnStyle(Map<String, Object> styleMap) {
      autoSize = (Boolean) styleMap.get(CSSNames.COLUMN_AUTO_SIZE);
      hidden = (Boolean) styleMap.get(CSSNames.COLUMN_HIDDEN);
      width = (Integer) styleMap.get(CSSNames.COLUMN_WIDTH);
      export = (Boolean) styleMap.get(CSSNames.COLUMN_EXPORT);
   }
}
