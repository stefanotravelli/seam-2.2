package org.jboss.seam.pdf;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.ElementTags;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

public class ITextUtils
{
   static Map<String, Color> colorMap = new HashMap<String, Color>();

   static
   {
      colorMap.put("white", Color.white);
      colorMap.put("gray", Color.gray);
      colorMap.put("lightgray", Color.lightGray);
      colorMap.put("darkgray", Color.darkGray);
      colorMap.put("black", Color.black);
      colorMap.put("red", Color.red);
      colorMap.put("pink", Color.pink);
      colorMap.put("yellow", Color.yellow);
      colorMap.put("green", Color.green);
      colorMap.put("magenta", Color.magenta);
      colorMap.put("cyan", Color.cyan);
      colorMap.put("blue", Color.blue);
      colorMap.put("orange", Color.orange);
   }

   /**
    * not all itext objects accept a string value as input, so we'll copy that
    * logic here.
    */
   public static int alignmentValue(String alignment)
   {
      return ElementTags.alignmentValue(alignment);
   }

   public static Rectangle pageSizeValue(String name)
   {
      return PageSize.getRectangle(name);
   }

   /**
    * return a color value from a string specification.
    */
   public static Color colorValue(String colorName)
   {
      if (colorName == null)
      {
         return null;
      }

      colorName = colorName.trim().toLowerCase();

      Color color = colorMap.get(colorName);

      if (color == null && colorName.startsWith("rgb"))
      {
         color = rgbStringToColor(colorName);
      }
      if (color == null)
      {
         color = Color.decode(colorName);
      }

      return color;
   }

   /*
    * Returns color of the form rgb(r,g,b) or rgb(r,g,b,a) r,g,b,a values can be
    * 0-255 or float values with a '%' sign
    */
   public static Color rgbStringToColor(String rgbString)
   {
      String rgb[] = rgbString.split(",");

      if (rgb.length == 3)
      {
         return new Color(
               parseSingleChanel(rgb[0]), 
               parseSingleChanel(rgb[1]), 
               parseSingleChanel(rgb[2]));
      }
      else if (rgb.length == 4)
      {
         return new Color(
               parseSingleChanel(rgb[0]), 
               parseSingleChanel(rgb[1]), 
               parseSingleChanel(rgb[2]), 
               parseSingleChanel(rgb[3]));
      }

      throw new RuntimeException("invalid rgb color specification: " + rgbString);
   }

   private static int parseSingleChanel(String chanel)
   {
      if (chanel.contains("%"))
      {
         float percent = Float.parseFloat(chanel.replaceAll("[^0-9\\.]", ""));
         return (int) (255 * (percent / 100));
      }
      return Integer.parseInt(chanel.replaceAll("[^0-9]", ""));
   }

   public static float[] stringToFloatArray(String text)
   {
      String[] parts = text.split("\\s");
      float[] values = new float[parts.length];
      for (int i = 0; i < parts.length; i++)
      {
         values[i] = Float.valueOf(parts[i]);
      }

      return values;
   }

   public static int[] stringToIntArray(String text)
   {
      String[] parts = text.split("\\s");
      int[] values = new int[parts.length];
      for (int i = 0; i < parts.length; i++)
      {
         values[i] = Integer.valueOf(parts[i]);
      }

      return values;
   }

   public static int runDirection(String direction)
   {
      if (direction == null || direction.equalsIgnoreCase("default")) {
         return PdfWriter.RUN_DIRECTION_DEFAULT;
      } else if (direction.equalsIgnoreCase("rtl")) {
         return PdfWriter.RUN_DIRECTION_RTL;
      } else if (direction.equalsIgnoreCase("ltr")) {
         return PdfWriter.RUN_DIRECTION_LTR;
      } else if (direction.equalsIgnoreCase("no-bidi")) {
         return PdfWriter.RUN_DIRECTION_NO_BIDI;
      } else {
         throw new RuntimeException("unknown run direction " + direction);
      }
   }
}
