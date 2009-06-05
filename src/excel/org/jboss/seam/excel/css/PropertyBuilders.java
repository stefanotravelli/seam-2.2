package org.jboss.seam.excel.css;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.jxl.JXLFactory;

/**
 * Property builder implementations for parsing css style arrays.
 * @author karlsnic
 *
 */
public class PropertyBuilders
{

   // Wildcard symbol for column widths
   private static final String COLUMN_WIDTH_WILDCARD = "*";

   public static class ForceType extends StringPropertyBuilder {}

   public static class ColumnWidths implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         for (int i = 0; i < values.length; i++) {
            String value = values[i];
            String indexedKey = CSSNames.COLUMN_WIDTHS + i;
            if (COLUMN_WIDTH_WILDCARD.equals(value)) {
               // Skip it, just moving along
            } else if (isNumeric(value)) {
               styleMap.put(indexedKey, Integer.parseInt(value));
            } else {
               String message = Interpolator.instance().interpolate("Column widths must be numerical or *, not #0", value);
               throw new ExcelWorkbookException(message);
            }
         }
         return styleMap;
      }
   }

   public static class ColumnWidth extends IntegerPropertyBuilder
   {
   }

   public static class ColumnAutoSize extends BooleanPropertyBuilder
   {
   }

   public static class ColumnHidden extends BooleanPropertyBuilder
   {
   }
   
   public static class ColumnExport extends BooleanPropertyBuilder
   {
   }   

   public static class BorderBottomLineStyle extends StringPropertyBuilder
   {
   }

   public static class BorderBottomColor extends StringPropertyBuilder
   {
   }
   
   public static class BorderBottomShorthand implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         for (int i = 0; i < values.length; i++)
         {
            String value = values[i];
            if (JXLFactory.isColor(values[i]))
            {
               styleMap.put(CSSNames.BORDER_BOTTOM_COLOR, value);
            }
            else if (JXLFactory.isBorderLineStyle(values[i]))
            {
               styleMap.put(CSSNames.BORDER_BOTTOM_LINE_STYLE, value);
            }
            else
            {
               throw new ExcelWorkbookException("Border bottom shorthand can only handle line style and color");
            }
         }
         return styleMap;
      }
   }

   public static class BorderRightLineStyle extends StringPropertyBuilder
   {
   }

   public static class BorderRightColor extends StringPropertyBuilder
   {
   }

   public static class BorderRightShorthand implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         for (int i = 0; i < values.length; i++)
         {
            String value = values[i];
            if (JXLFactory.isColor(values[i]))
            {
               styleMap.put(CSSNames.BORDER_RIGHT_COLOR, value);
            }
            else if (JXLFactory.isBorderLineStyle(values[i]))
            {
               styleMap.put(CSSNames.BORDER_RIGHT_LINE_STYLE, value);
            }
            else
            {
               throw new ExcelWorkbookException("Border right shorthand can only handle line style and color");
            }
         }
         return styleMap;
      }
   }

   public static class BorderTopLineStyle extends StringPropertyBuilder
   {
   }

   public static class BorderTopColor extends StringPropertyBuilder
   {
   }

   public static class BorderTopShorthand implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         for (int i = 0; i < values.length; i++)
         {
            String value = values[i];
            if (JXLFactory.isColor(values[i]))
            {
               styleMap.put(CSSNames.BORDER_TOP_COLOR, value);
            }
            else if (JXLFactory.isBorderLineStyle(values[i]))
            {
               styleMap.put(CSSNames.BORDER_TOP_LINE_STYLE, value);
            }
            else
            {
               throw new ExcelWorkbookException("Border top shorthand can only handle line style and color");
            }
         }
         return styleMap;
      }
   }

   public static class BorderLeftLineStyle extends StringPropertyBuilder
   {
   }

   public static class BorderLeftColor extends StringPropertyBuilder
   {
   }

   public static class BorderLeftShorthand implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         for (int i = 0; i < values.length; i++)
         {
            String value = values[i];
            if (JXLFactory.isColor(values[i]))
            {
               styleMap.put(CSSNames.BORDER_LEFT_COLOR, value);
            }
            else if (JXLFactory.isBorderLineStyle(values[i]))
            {
               styleMap.put(CSSNames.BORDER_LEFT_LINE_STYLE, value);
            }
            else
            {
               throw new ExcelWorkbookException("Border left shorthand can only handle line style and color");
            }
         }
         return styleMap;
      }
   }

   public static class BorderShorthand implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         for (int i = 0; i < values.length; i++)
         {
            String value = values[i];
            if (JXLFactory.isColor(value))
            {
               styleMap.put(CSSNames.BORDER_LEFT_COLOR, values[1]);
               styleMap.put(CSSNames.BORDER_RIGHT_COLOR, values[1]);
               styleMap.put(CSSNames.BORDER_TOP_COLOR, values[1]);
               styleMap.put(CSSNames.BORDER_BOTTOM_COLOR, values[1]);
            }
            else if (JXLFactory.isBorderLineStyle(value))
            {
               styleMap.put(CSSNames.BORDER_LEFT_LINE_STYLE, values[0]);
               styleMap.put(CSSNames.BORDER_RIGHT_LINE_STYLE, values[0]);
               styleMap.put(CSSNames.BORDER_TOP_LINE_STYLE, values[0]);
               styleMap.put(CSSNames.BORDER_BOTTOM_LINE_STYLE, values[0]);
            }
            else
            {
               throw new ExcelWorkbookException("Border shorthand can only handle line style and color");
            }
         }
         return styleMap;
      }
   }

   public static class BackgroundColor extends StringPropertyBuilder
   {
   }

   public static class BackgroundPattern extends StringPropertyBuilder
   {
   }

   public static class BackgroundShorthand implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         for (int i = 0; i < values.length; i++)
         {
            String value = values[i];
            if (JXLFactory.isPattern(values[i]))
            {
               styleMap.put(CSSNames.BACKGROUND_PATTERN, value);
            }
            else if (JXLFactory.isColor(values[i]))
            {
               styleMap.put(CSSNames.BACKGROUND_COLOR, value);
            }
            else
            {
               throw new ExcelWorkbookException("Background shorthand can only handle color and pattern");
            }
         }
         return styleMap;
      }
   }

   public static class FontFamily extends MergingStringPropertyBuilder
   {
   }
   
   private static String collectString(String[] values, String delimiter) {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < values.length; i++) {
         if (i > 0) {
            buffer.append(delimiter);
         }
         buffer.append(values[i]);
      }
      return buffer.toString();
   }
   
   private static class MergingStringPropertyBuilder extends StringPropertyBuilder {
      @Override
      public StyleMap parseProperty(String key, String[] values)
      {
         String concatValues = collectString(values, " ");
         return super.parseProperty(key, new String[] {concatValues});
      }
   }

   public static class FontSize extends IntegerPropertyBuilder
   {
   }

   public static class FontColor extends StringPropertyBuilder
   {
   }

   public static class FontItalic extends BooleanPropertyBuilder
   {
   }

   public static class FontScriptStyle extends StringPropertyBuilder
   {
   }

   public static class FontUnderlineStyle extends StringPropertyBuilder
   {
   }

   public static class FontBold extends BooleanPropertyBuilder
   {
   }

   public static class FontStruckOut extends BooleanPropertyBuilder
   {
   }

   // "12 'Times New Roman'"
   public static class FontShorthand implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();

         String valueString = collectString(values, " ");
         int firstQuote = valueString.indexOf("'");
         int lastQuote = valueString.lastIndexOf("'");
         if (firstQuote > 0 && lastQuote > 0 && firstQuote != lastQuote) {
            String fontName = valueString.substring(firstQuote, lastQuote);
            styleMap.put(CSSNames.FONT_FAMILY, fontName);
            String pre = valueString.substring(0, firstQuote - 1).trim();
            String post = valueString.substring(lastQuote + 1).trim();
            valueString = pre + post;
            values = valueString.split(" ");
         }
         
         for (int i = 0; i < values.length; i++)
         {
            String value = values[i];
            if (JXLFactory.isScriptStyle(value))
            {
               styleMap.put(CSSNames.FONT_SCRIPT_STYLE, value);
            }
            else if (JXLFactory.isColor(value))
            {
               styleMap.put(CSSNames.FONT_COLOR, value);
            }
            else if (JXLFactory.isUnderlineStyle(value))
            {
               styleMap.put(CSSNames.FONT_UNDERLINE_STYLE, value);
            }
            else if ("italic".equals(value.toLowerCase()))
            {
               styleMap.put(CSSNames.FONT_ITALIC, Boolean.TRUE);
            }
            else if ("bold".equals(value.toLowerCase()))
            {
               styleMap.put(CSSNames.FONT_BOLD, Boolean.TRUE);
            }
            else if ("struck_out".equals(value.toLowerCase()))
            {
               styleMap.put(CSSNames.FONT_STRUCK_OUT, Boolean.TRUE);
            }
            else if (isNumeric(value))
            {
               styleMap.put(CSSNames.FONT_SIZE, Integer.parseInt(value));
            }
            else
            {
               styleMap.put(CSSNames.FONT_FAMILY, value);
            }
         }
         
         return styleMap;
      }
   }

   private static boolean isNumeric(String value)
   {
      try
      {
         Integer.parseInt(value);
      }
      catch (NumberFormatException e)
      {
         return false;
      }
      return true;
   }

   private static boolean isBoolean(String value)
   {
      return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
   }
   
   public static class VericalAlignment extends StringPropertyBuilder
   {
   }

   public static class Wrap extends BooleanPropertyBuilder
   {
   }

   public static class ShrinkToFit extends BooleanPropertyBuilder
   {
   }

   public static class Locked extends BooleanPropertyBuilder
   {
   }

   public static class Orientation extends StringPropertyBuilder
   {
   }

   public static class Indentation extends IntegerPropertyBuilder
   {
   }

   public static class Alignment extends StringPropertyBuilder
   {
   }

   public static class FormatMask extends MergingStringPropertyBuilder
   {
   }

   private static class StringPropertyBuilder implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         StyleMap styleMap = new StyleMap();
         styleMap.put(key, values[0]);
         return styleMap;
      }
   }

   private static class IntegerPropertyBuilder implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         String value = values[0];
         if (!isNumeric(value)) {
            String message = Interpolator.instance().interpolate("#0 is not a number in #1", value, key);
            throw new ExcelWorkbookException(message);
         }
         StyleMap styleMap = new StyleMap();
         styleMap.put(key, Integer.parseInt(value));
         return styleMap;
      }
   }
   
   private static class BooleanPropertyBuilder implements PropertyBuilder
   {
      public StyleMap parseProperty(String key, String[] values)
      {
         String value = values[0];
         if (!isBoolean(value)) {
            String message = Interpolator.instance().interpolate("#0 is not a boolean in #1", value, key);
            throw new ExcelWorkbookException(message);
         }
         StyleMap styleMap = new StyleMap();
         styleMap.put(key, Boolean.parseBoolean(value));
         return styleMap;
      }
   }
}
