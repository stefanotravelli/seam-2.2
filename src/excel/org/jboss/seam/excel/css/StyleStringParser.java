package org.jboss.seam.excel.css;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

public class StyleStringParser
{
   private static final char STYLE_SEPARATOR = ';';
   private static final String KEY_VALUE_SEPARATOR = ":";
   private static final String STYLE_SHORTHAND_SEPARATOR = " ";
   private static final char ESCAPE_CHAR = '\'';
   
   private Log log = Logging.getLog(StyleStringParser.class);
   
   private String styleString;
   private Map<String, PropertyBuilder> propertyBuilderMap;
   private StyleMap styleMap = new StyleMap();
   private boolean escaping = false;
   
   public static StyleStringParser of(String styleString, Map<String, PropertyBuilder> propertyBuilderMap)
   {
      return new StyleStringParser(styleString, propertyBuilderMap);
   }
   
   protected StyleStringParser(String styleString, Map<String, PropertyBuilder> propertyBuilderMap)
   {
      this.styleString = styleString;
      this.propertyBuilderMap = propertyBuilderMap;
   }
   
   private void addStyle(StringBuilder styleBuilder)
   {
      String styleString = styleBuilder.toString();
      int keyValueBreakpointIndex = styleString.indexOf(KEY_VALUE_SEPARATOR);
      if (keyValueBreakpointIndex < 0)
      {
         log.warn("Key-value separator character #0 not found in style #1, dropping", KEY_VALUE_SEPARATOR + styleBuilder.toString());
         return;
      }
      String styleName = styleString.substring(0, keyValueBreakpointIndex).toLowerCase().trim();
      if (!propertyBuilderMap.containsKey(styleName))
      {
         log.warn("No property builder (unknown style) for property #0", styleName);
         return;
      }
      PropertyBuilder propertyBuilder = propertyBuilderMap.get(styleName);
      String styleValue = styleString.substring(keyValueBreakpointIndex + 1);
      log.trace("Parsed style #0 to #1 => #2", styleString, styleName, styleValue);
      String[] styleValues = trimArray(styleValue.trim().split(STYLE_SHORTHAND_SEPARATOR));
      styleMap.putAll(propertyBuilder.parseProperty(styleName, styleValues));
   }
   
   public static String[] trimArray(String[] array)
   {
      List<String> validValues = new ArrayList<String>();
      for (int i = 0; i < array.length; i++)
      {
         if (!"".equals(array[i]) && !" ".equals(array[i]))
         {
            validValues.add(array[i].toLowerCase().trim());
         }
      }
      return validValues.toArray(new String[validValues.size()]);
   }
   
   public StyleMap parse()
   {
      CharacterIterator iterator = new StringCharacterIterator(styleString);
      StringBuilder styleBuilder = new StringBuilder();
      for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next())
      {
         if (ESCAPE_CHAR == c)
         {
            escaping = !escaping;
         }
         else if (STYLE_SEPARATOR == c && !escaping)
         {
            addStyle(styleBuilder);
            styleBuilder = new StringBuilder();
         }
         else
         {
            styleBuilder.append(c);
         }
      }
      if (styleBuilder.length() > 0)
      {
         addStyle(styleBuilder);
      }
      
      if (escaping)
      {
         log.warn("Unbalanced escape characters #0 in style #1", ESCAPE_CHAR, styleBuilder.toString());
      }
      return styleMap;
   }
}
