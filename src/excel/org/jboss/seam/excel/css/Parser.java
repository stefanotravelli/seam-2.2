package org.jboss.seam.excel.css;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.ui.UILink;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * CSS parser for the XLS-CSS
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 */
public class Parser
{
   // Where to look for the style
   private static final String STYLE_ATTRIBUTE = "style";

   // Where to look for the style class
   private static final String STYLE_CLASS_ATTRIBUTE = "styleClass";

   // What separates multiple XLS-CSS attributes in a style string
   private static final String STYLES_SEPARATOR = ";";

   // What separates the key and value in a XLS-CSS style
   private static final String STYLE_NAME_VALUE_SEPARATOR = ":";

   // What separates multiple style class references
   private static final String STYLE_SHORTHAND_SEPARATOR = " ";

   // What starts a rule block in a CSS file
   private static final String LEFT_BRACE = "{";

   // What ends a rule block in a CSS file
   private static final String RIGHT_BRACE = "}";

   // The style classes that have been read in from e:link referenced, mapped on
   // style class name
   private Map<String, StyleMap> definedStyleClasses = new HashMap<String, StyleMap>();

   // The registered property builders, mapped on attribute name
   private Map<String, PropertyBuilder> propertyBuilders = new HashMap<String, PropertyBuilder>();

   // A cache of previously parsed css, mapped on component
   private Map<UIComponent, StyleMap> cellStyleCache = new HashMap<UIComponent, StyleMap>();

   private Log log = Logging.getLog(Parser.class);

   /**
    * Constructor, initializes the property builders
    */
   public Parser()
   {
      initPropertyBuilders();
   }

   /**
    * Constructor with stylesheets
    * 
    * @param stylesheets The list of referenced stylesheets in UILink elements
    * @throws MalformedURLException If the URL was bad
    * @throws IOException If the URL could not be read
    */
   public Parser(List<UILink> stylesheets) throws MalformedURLException, IOException
   {
      initPropertyBuilders();
      loadStylesheets(stylesheets);
   }

   /**
    * Loads stylesheets (merging by class name)
    * 
    * @param stylesheets The stylesheets to read/merge
    * @throws MalformedURLException If the URL was bad
    * @throws IOException If the URL could not be read
    */
   private void loadStylesheets(List<UILink> stylesheets) throws MalformedURLException, IOException
   {
      for (UILink stylesheet : stylesheets)
      {
         definedStyleClasses.putAll(parseStylesheet(stylesheet.getURL()));
      }
   }

   /**
    * Registers the property builders
    */
   private void initPropertyBuilders()
   {
      propertyBuilders.put(CSSNames.FONT_FAMILY, new PropertyBuilders.FontFamily());
      propertyBuilders.put(CSSNames.FONT_SIZE, new PropertyBuilders.FontSize());
      propertyBuilders.put(CSSNames.FONT_COLOR, new PropertyBuilders.FontColor());
      propertyBuilders.put(CSSNames.FONT_ITALIC, new PropertyBuilders.FontItalic());
      propertyBuilders.put(CSSNames.FONT_SCRIPT_STYLE, new PropertyBuilders.FontScriptStyle());
      propertyBuilders.put(CSSNames.FONT_STRUCK_OUT, new PropertyBuilders.FontStruckOut());
      propertyBuilders.put(CSSNames.FONT_UNDERLINE_STYLE, new PropertyBuilders.FontUnderlineStyle());
      propertyBuilders.put(CSSNames.FONT_BOLD, new PropertyBuilders.FontBold());
      propertyBuilders.put(CSSNames.FONT, new PropertyBuilders.FontShorthand());
      propertyBuilders.put(CSSNames.BACKGROUND_PATTERN, new PropertyBuilders.BackgroundPattern());
      propertyBuilders.put(CSSNames.BACKGROUND_COLOR, new PropertyBuilders.BackgroundColor());
      propertyBuilders.put(CSSNames.BACKGROUND, new PropertyBuilders.BackgroundShorthand());
      propertyBuilders.put(CSSNames.BORDER_LEFT_COLOR, new PropertyBuilders.BorderLeftColor());
      propertyBuilders.put(CSSNames.BORDER_LEFT_LINE_STYLE, new PropertyBuilders.BorderLeftLineStyle());
      propertyBuilders.put(CSSNames.BORDER_LEFT, new PropertyBuilders.BorderLeftShorthand());
      propertyBuilders.put(CSSNames.BORDER_TOP_COLOR, new PropertyBuilders.BorderTopColor());
      propertyBuilders.put(CSSNames.BORDER_TOP_LINE_STYLE, new PropertyBuilders.BorderTopLineStyle());
      propertyBuilders.put(CSSNames.BORDER_TOP, new PropertyBuilders.BorderTopShorthand());
      propertyBuilders.put(CSSNames.BORDER_RIGHT_COLOR, new PropertyBuilders.BorderRightColor());
      propertyBuilders.put(CSSNames.BORDER_RIGHT_LINE_STYLE, new PropertyBuilders.BorderRightLineStyle());
      propertyBuilders.put(CSSNames.BORDER_RIGHT, new PropertyBuilders.BorderRightShorthand());
      propertyBuilders.put(CSSNames.BORDER_BOTTOM_COLOR, new PropertyBuilders.BorderBottomColor());
      propertyBuilders.put(CSSNames.BORDER_BOTTOM_LINE_STYLE, new PropertyBuilders.BorderBottomLineStyle());
      propertyBuilders.put(CSSNames.BORDER_BOTTOM, new PropertyBuilders.BorderBottomShorthand());
      propertyBuilders.put(CSSNames.BORDER, new PropertyBuilders.BorderShorthand());
      propertyBuilders.put(CSSNames.FORMAT_MASK, new PropertyBuilders.FormatMask());
      propertyBuilders.put(CSSNames.ALIGNMENT, new PropertyBuilders.Alignment());
      propertyBuilders.put(CSSNames.INDENTATION, new PropertyBuilders.Indentation());
      propertyBuilders.put(CSSNames.ORIENTATION, new PropertyBuilders.Orientation());
      propertyBuilders.put(CSSNames.LOCKED, new PropertyBuilders.Locked());
      propertyBuilders.put(CSSNames.SHRINK_TO_FIT, new PropertyBuilders.ShrinkToFit());
      propertyBuilders.put(CSSNames.WRAP, new PropertyBuilders.Wrap());
      propertyBuilders.put(CSSNames.VERICAL_ALIGNMENT, new PropertyBuilders.VericalAlignment());
      propertyBuilders.put(CSSNames.COLUMN_WIDTH, new PropertyBuilders.ColumnWidth());
      propertyBuilders.put(CSSNames.COLUMN_AUTO_SIZE, new PropertyBuilders.ColumnAutoSize());
      propertyBuilders.put(CSSNames.COLUMN_HIDDEN, new PropertyBuilders.ColumnHidden());
      propertyBuilders.put(CSSNames.COLUMN_EXPORT, new PropertyBuilders.ColumnExport());
      propertyBuilders.put(CSSNames.COLUMN_WIDTHS, new PropertyBuilders.ColumnWidths());
      propertyBuilders.put(CSSNames.FORCE_TYPE, new PropertyBuilders.ForceType());
   }

   /**
    * Reads data from an URL to a String
    * 
    * @param url The URL to read
    * @return The read data as a String
    * @throws IOException If the stream could not be read
    */
   private static String readCSS(InputStream in) throws IOException
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      StringBuffer buffer = new StringBuffer();
      String line;
      while ((line = reader.readLine()) != null)
      {
         buffer.append(line);
      }
      reader.close();
      return buffer.toString();
   }

   /**
    * Parses a style sheet. Really crude. Assumes data is nicely formatted on
    * one line per entry
    * 
    * @param urlString The URL to read
    * @return A map of style class names mapped to StyleMaps
    * @throws MalformedURLException
    * @throws IOException
    */
   private Map<String, StyleMap> parseStylesheet(String urlString) throws MalformedURLException, IOException
   {
      Map<String, StyleMap> styleClasses = new HashMap<String, StyleMap>();
      InputStream cssStream = null;
      if (urlString.indexOf("://") < 0) {
         cssStream = getClass().getResourceAsStream(urlString);
      } else {
         cssStream = new URL(urlString).openStream();
      }
      String css = readCSS(cssStream).toLowerCase();
      int firstBrace = -1;
      int secondBrace = -1;
      while (!"".equals(css))
      {
         firstBrace = css.indexOf(LEFT_BRACE);
         if (firstBrace >= 0)
         {
            secondBrace = css.indexOf(RIGHT_BRACE, firstBrace + 1);
         }
         if (firstBrace >= 0 && secondBrace >= 0 && firstBrace != secondBrace)
         {
            String styleName = css.substring(0, firstBrace).trim();
            if (styleName.startsWith(".")) {
                styleName = styleName.substring(1);
            }
            String styleString = css.substring(firstBrace + 1, secondBrace).trim();
            StyleMap styleMap = parseStyleString(styleString);
            styleClasses.put(styleName, styleMap);
            css = css.substring(secondBrace + 1);
         }
         else
         {
            css = "";
         }
      }
      return styleClasses;
   }

   /**
    * Gets style from a component
    * 
    * @param component The component to examine
    * @return null if not found, otherwise style string
    */
   public static String getStyle(UIComponent component)
   {
      return getStyleProperty(component, STYLE_ATTRIBUTE);
   }

   /**
    * Gets style class from a component
    * 
    * @param component The component to examine
    * @return null if not found, otherwise style class(es) string
    */
   public static String getStyleClass(UIComponent component)
   {
      return getStyleProperty(component, STYLE_CLASS_ATTRIBUTE);
   }

   /**
    * Reads a property from a component
    * 
    * @param component The component to examine
    * @param field The field to read
    * @return The value from the field
    */
   private static String getStyleProperty(UIComponent component, String field)
   {
      try
      {
         return (String) PropertyUtils.getProperty(component, field);
      }
      catch (NoSuchMethodException e)
      {
         // No panic, no property
         return null;
      }
      catch (Exception e)
      {
         String message = Interpolator.instance().interpolate("Could not read field #0 of bean #1", field, component.getId());
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Cascades on parents, collecting them into list
    * 
    * @param component The component to examine
    * @param styleMaps The list of collected style maps
    * @return The list of style maps
    */
   private List<StyleMap> cascadeStyleMap(UIComponent component, List<StyleMap> styleMaps)
   {
      styleMaps.add(getStyleMap(component));
      if (component.getParent() != null)
      {
         cascadeStyleMap(component.getParent(), styleMaps);
      }
      return styleMaps;
   }

   /**
    * Gets the cascaded style map for a component. Recurses on parents,
    * collecting style maps. The reverses the list and merges the styles
    * 
    * @param component The component to examine
    * @return The merged style map
    */
   public StyleMap getCascadedStyleMap(UIComponent component)
   {
      List<StyleMap> styleMaps = cascadeStyleMap(component, new ArrayList<StyleMap>());
      Collections.reverse(styleMaps);
      StyleMap cascadedStyleMap = new StyleMap();
      for (StyleMap styleMap : styleMaps)
      {
         cascadedStyleMap.putAll(styleMap);
      }
      return cascadedStyleMap;
   }

   /**
    * Gets a style map for a component (from cache if available)
    * 
    * @param component The component to examine
    * @return The style map of the component
    */
   private StyleMap getStyleMap(UIComponent component)
   {
      if (cellStyleCache.containsKey(component))
      {
         return cellStyleCache.get(component);
      }

      StyleMap styleMap = new StyleMap();

      String componentStyleClass = getStyleProperty(component, STYLE_CLASS_ATTRIBUTE);
      if (componentStyleClass != null)
      {
         String[] styleClasses = trimArray(componentStyleClass.split(STYLE_SHORTHAND_SEPARATOR));
         for (String styleClass : styleClasses)
         {
            if (!definedStyleClasses.containsKey(styleClass))
            {
               log.warn("Uknown style class #0", styleClass);
               continue;
            }
            styleMap.putAll(definedStyleClasses.get(styleClass));
         }
      }

      String componentStyle = getStyleProperty(component, STYLE_ATTRIBUTE);
      if (componentStyle != null)
      {
         styleMap.putAll(parseStyleString(componentStyle));
      }

      cellStyleCache.put(component, styleMap);
      return styleMap;
   }

   /**
    * Parses a stringle style string
    * 
    * @param styleString The string to parse
    * @return The parsed StyleMap
    */
   private StyleMap parseStyleString(String styleString)
   {
      StyleMap styleMap = new StyleMap();

      String[] styles = trimArray(styleString.split(STYLES_SEPARATOR));
      for (String style : styles)
      {
         int breakpoint = style.indexOf(STYLE_NAME_VALUE_SEPARATOR);
         if (breakpoint < 0) {
             log.warn("Style component #0 should be of form <key>#1<value>", style, STYLE_NAME_VALUE_SEPARATOR);
             continue;
         }
         String styleName = style.substring(0, breakpoint).toLowerCase().trim();
         if (!propertyBuilders.containsKey(styleName))
         {
            log.warn("No property builder (unknown style) for property #0", styleName);
            continue;
         }
         PropertyBuilder propertyBuilder = propertyBuilders.get(styleName);
         String styleValue = style.substring(breakpoint + 1);
         String[] styleValues = trimArray(styleValue.trim().split(STYLE_SHORTHAND_SEPARATOR));
         styleMap.putAll(propertyBuilder.parseProperty(styleName, styleValues));
      }

      return styleMap;
   }

   /**
    * Utility for trimming (lowercase & trim) an array of string values
    * 
    * @param array The array to trim
    * @return The trimmed array
    */
   private String[] trimArray(String[] array)
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

   /**
    * Setter for stylesheets. Loads them also.
    * 
    * @param stylesheets The stylesheets to load
    * @throws MalformedURLException If the URL is bad
    * @throws IOException If the URL cannot be read
    */
   public void setStylesheets(List<UILink> stylesheets) throws MalformedURLException, IOException
   {
      loadStylesheets(stylesheets);
   }
}
