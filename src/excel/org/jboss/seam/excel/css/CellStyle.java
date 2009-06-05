package org.jboss.seam.excel.css;

import java.util.Map;

/**
 * Helper class that collects font, border and background info with cell info into one package 
 * from stylesheet data
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 */
public class CellStyle
{

   public class Font
   {
      public String family;
      public Integer pointSize;
      public String color;
      public Boolean bold;
      public Boolean italic;
      public String scriptStyle;
      public String underlineStyle;
      public Boolean struckOut;

      public boolean isUsed()
      {
         return family != null || pointSize != null || color != null || bold != null || italic != null || 
         scriptStyle != null || underlineStyle != null || struckOut != null;
      }
   }

   public class Background
   {
      public String color;
      public String pattern;

      public boolean isUsed()
      {
         return color != null || pattern != null;
      }
   }

   public class Border
   {
      public String color;
      public String lineStyle;

      public boolean isUsed()
      {
         return color != null || lineStyle != null;
      }

   }

   public Font font = new Font();
   public Background background = new Background();
   public Border leftBorder = new Border();
   public Border topBorder = new Border();
   public Border bottomBorder = new Border();
   public Border rightBorder = new Border();
   public String alignment;
   public Integer indentation;
   public Boolean locked;
   public String orientation;
   public Boolean shrinkToFit;
   public Boolean wrap;
   public String verticalAlignment;
   public String formatMask;
   public String forceType;

   public CellStyle(Map<String, Object> styleMap)
   {
      font.family = (String) styleMap.get(CSSNames.FONT_FAMILY);
      font.color = (String) styleMap.get(CSSNames.FONT_COLOR);
      font.pointSize = (Integer) styleMap.get(CSSNames.FONT_SIZE);
      font.italic = (Boolean) styleMap.get(CSSNames.FONT_ITALIC);
      font.scriptStyle = (String) styleMap.get(CSSNames.FONT_SCRIPT_STYLE);
      font.struckOut = (Boolean) styleMap.get(CSSNames.FONT_STRUCK_OUT);
      font.underlineStyle = (String) styleMap.get(CSSNames.FONT_UNDERLINE_STYLE);
      font.bold = (Boolean) styleMap.get(CSSNames.FONT_BOLD);
      background.color = (String) styleMap.get(CSSNames.BACKGROUND_COLOR);
      background.pattern = (String) styleMap.get(CSSNames.BACKGROUND_PATTERN);
      leftBorder.color = (String) styleMap.get(CSSNames.BORDER_LEFT_COLOR);
      leftBorder.lineStyle = (String) styleMap.get(CSSNames.BORDER_LEFT_LINE_STYLE);
      topBorder.color = (String) styleMap.get(CSSNames.BORDER_TOP_COLOR);
      topBorder.lineStyle = (String) styleMap.get(CSSNames.BORDER_TOP_LINE_STYLE);
      rightBorder.color = (String) styleMap.get(CSSNames.BORDER_RIGHT_COLOR);
      rightBorder.lineStyle = (String) styleMap.get(CSSNames.BORDER_RIGHT_LINE_STYLE);
      bottomBorder.color = (String) styleMap.get(CSSNames.BORDER_BOTTOM_COLOR);
      bottomBorder.lineStyle = (String) styleMap.get(CSSNames.BORDER_BOTTOM_LINE_STYLE);
      alignment = (String) styleMap.get(CSSNames.ALIGNMENT);
      indentation = (Integer) styleMap.get(CSSNames.INDENTATION);
      locked = (Boolean) styleMap.get(CSSNames.LOCKED);
      orientation = (String) styleMap.get(CSSNames.ORIENTATION);
      shrinkToFit = (Boolean) styleMap.get(CSSNames.SHRINK_TO_FIT);
      wrap = (Boolean) styleMap.get(CSSNames.WRAP);
      verticalAlignment = (String) styleMap.get(CSSNames.VERICAL_ALIGNMENT);
      formatMask = (String) styleMap.get(CSSNames.FORMAT_MASK);
      forceType = (String) styleMap.get(CSSNames.FORCE_TYPE);
   }

}
