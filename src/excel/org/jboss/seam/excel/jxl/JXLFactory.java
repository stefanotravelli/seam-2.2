package org.jboss.seam.excel.jxl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import jxl.HeaderFooter;
import jxl.HeaderFooter.Contents;
import jxl.biff.DisplayFormat;
import jxl.biff.FontRecord;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.Orientation;
import jxl.format.PageOrientation;
import jxl.format.PaperSize;
import jxl.format.Pattern;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.WritableFont;
import jxl.write.WriteException;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.css.CellStyle;
import org.jboss.seam.excel.ui.ExcelComponent;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * Factory for creating JExcelAPI objects
 * 
 * @author karlsnic
 */
public class JXLFactory
{
   // Class names for JExcelAPI objects
   private static final String DATEFORMATS_CLASSNAME = "jxl.write.DateFormats";
   private static final String NUMBERFORMATS_CLASSNAME = "jxl.write.NumberFormats";
   private static final String ALIGNMENT_CLASS_NAME = "jxl.format.Alignment";
   private static final String ORIENTATION_CLASS_NAME = "jxl.format.Orientation";
   private static final String VERTICAL_ALIGNMENT_CLASS_NAME = "jxl.format.VerticalAlignment";
   private static final String COLOR_CLASS_NAME = "jxl.format.Colour";
   private static final String BORDER_CLASS_NAME = "jxl.format.Border";
   private static final String BORDER_LINE_STYLE_CLASS_NAME = "jxl.format.BorderLineStyle";
   private static final String PATTERN_CLASS_NAME = "jxl.format.Pattern";
   private static final String PAGE_ORIENTATION_CLASS_NAME = "jxl.format.PageOrientation";
   private static final String PAPER_SIZE_CLASS_NAME = "jxl.format.PaperSize";
   private static final String SCRIPT_STYLE_CLASS_NAME = "jxl.format.ScriptStyle";
   private static final String UNDERLINE_STYLE_CLASS_NAME = "jxl.format.UnderlineStyle";

   private static final String HEADERFOOTER_COMMAND_MARKER = "#";
   private static final String HEADERFOOTER_PAIR_DELIMITER = "=";

   private static final String HF_CMD_FONT_SIZE = "font_size";
   private static final String HF_CMD_FONT_NAME = "font_name";
   private static final String HF_CMD_UNDERLINE = "underline";
   private static final String HF_CMD_SUPERSCRIPT = "superscript";
   private static final String HF_CMD_SUBSCRIPT = "subscript";
   private static final String HF_CMD_STRIKETHROUGH = "strikethrough";
   private static final String HF_CMD_SHADOW = "shadow";
   private static final String HF_CMD_OUTLINE = "outline";
   private static final String HF_CMD_ITALICS = "italics";
   private static final String HF_CMD_DOUBLE_UNDERLINE = "double_underline";
   private static final String HF_CMD_BOLD = "bold";
   private static final String HF_CMD_WORKSHEET_NAME = "worksheet_name";
   private static final String HF_CMD_WORKBOOK_NAME = "workbook_name";
   private static final String HF_CMD_TOTAL_PAGES = "total_pages";
   private static final String HF_CMD_TIME = "time";
   private static final String HF_CMD_PAGE_NUMBER = "page_number";

   private static final Log log = Logging.getLog(JXLFactory.class);

   /**
    * Checks if text is a border line style
    * 
    * @param text The text to check
    * @return True if border line style, false otherwise
    */
   public static boolean isBorderLineStyle(String text)
   {
      return getValidContants(BORDER_LINE_STYLE_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Checks if text is a pattern
    * 
    * @param text The text to check
    * @return True if pattern, false otherwise
    */
   public static boolean isPattern(String text)
   {
      return getValidContants(PATTERN_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Checks if text is a color
    * 
    * @param text The text to check
    * @return True if color, false otherwise
    */
   public static boolean isColor(String text)
   {
      return getValidContants(COLOR_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Checks if text is an alignment
    * 
    * @param text The text to check
    * @return True if alignment, false otherwise
    */
   public static boolean isAlignment(String text)
   {
      return getValidContants(ALIGNMENT_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Checks if text is an orientation
    * 
    * @param text The text to check
    * @return True if orientation, false otherwise
    */
   public static boolean isOrientation(String text)
   {
      return getValidContants(ORIENTATION_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Checks if text is an vertical alignment
    * 
    * @param text The text to check
    * @return True if vertical alignment, false otherwise
    */
   public static boolean isVerticalAlignment(String text)
   {
      return getValidContants(VERTICAL_ALIGNMENT_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Checks if text is an underline style
    * 
    * @param text The text to check
    * @return True if underline style, false otherwise
    */
   public static boolean isUnderlineStyle(String text)
   {
      return getValidContants(UNDERLINE_STYLE_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Checks if text is a script style
    * 
    * @param text The text to check
    * @return True if script style, false otherwise
    */
   public static boolean isScriptStyle(String text)
   {
      return getValidContants(SCRIPT_STYLE_CLASS_NAME).contains(text.toLowerCase());
   }

   /**
    * Gets a list of constants from a class
    * 
    * @param className The class to examine
    * @return A list of constants
    */
   private static List<String> getValidContants(String className)
   {
      List<String> constants = new ArrayList<String>();

      if (log.isTraceEnabled())
      {
         log.trace("Getting valid constants from #0", className);
      }
      Class clazz = null;
      try
      {
         clazz = Class.forName(className);
      }
      catch (ClassNotFoundException e)
      {
         throw new ExcelWorkbookException("Could not find class while getting valid constants", e);
      }
      // Loop through the fields
      for (Field field : clazz.getFields())
      {
         int modifiers = field.getModifiers();
         // Append to list if it's public and static (as most our constants are)
         if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
         {
            constants.add(field.getName().toLowerCase());
         }
      }
      return constants;
   }

   /**
    * Gets a suggestion string of available constants from a class.
    * 
    * @param className The class to examine
    * @return The suggestion string
    */
   public static String getValidConstantsSuggestion(String className)
   {
      List<String> constants = getValidContants(className);
      StringBuffer buffer = new StringBuffer();
      int i = 0;
      // Loop through the fields
      for (String field : constants)
      {
         buffer.append(i++ == 0 ? field : ", " + field);
      }
      return Interpolator.instance().interpolate("[#0]", buffer.toString());
   }

   /**
    * Gets a constant from a class
    * 
    * @param className The class name to examine
    * @param fieldName The field to read
    * @return The constant
    * @throws NoSuchFieldException If the field is not available
    */
   private static Object getConstant(String className, String fieldName) throws NoSuchFieldException
   {
      if (log.isTraceEnabled())
      {
         log.trace("Looking for constant #0 in class #1", fieldName.toUpperCase(), className);
      }
      try
      {
         return Class.forName(className).getField(fieldName.toUpperCase()).get(null);
      }
      catch (NoSuchFieldException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new ExcelWorkbookException(Interpolator.instance().interpolate("Could not read field #0 from class #1", fieldName, className), e);
      }
   }

   /**
    * Creates a JExcelAPI representation of an alignment
    * 
    * @param alignment The requested alignment
    * @return The alignment representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/Alignment.html">Alignment</a>
    */
   public static Alignment createAlignment(String alignment)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating alignment for #0", alignment);
      }
      try
      {
         return alignment == null ? Alignment.LEFT : (Alignment) getConstant(ALIGNMENT_CLASS_NAME, alignment.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Alignment {0} not supported, try {1}", alignment, getValidConstantsSuggestion(ALIGNMENT_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of an script style
    * 
    * @param mask The requested script style
    * @return The script style representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/ScriptStyle.html">ScriptStyle</a>
    */
   private static ScriptStyle createScriptStyle(String scriptStyle)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating script style for #0", scriptStyle);
      }
      try
      {
         return scriptStyle == null ? ScriptStyle.NORMAL_SCRIPT : (ScriptStyle) getConstant(SCRIPT_STYLE_CLASS_NAME, scriptStyle.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Script style {0} not supported, try {1}", scriptStyle, getValidConstantsSuggestion(SCRIPT_STYLE_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of an underline style
    * 
    * @param mask The requested underline style
    * @return The underline style representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/UnderlineStyle.html">UnderlineStyle</a>
    */
   private static UnderlineStyle createUnderlineStyle(String underlineStyle)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating underline style for #0", underlineStyle);
      }
      try
      {
         return underlineStyle == null ? UnderlineStyle.NO_UNDERLINE : (UnderlineStyle) getConstant(UNDERLINE_STYLE_CLASS_NAME, underlineStyle.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Underline style {0} not supported, try {1}", underlineStyle, getValidConstantsSuggestion(UNDERLINE_STYLE_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of a font
    * 
    * @param fontspecs The font specifications
    * @return The font
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/write/WritableFont.html">WritableFont</a>
    */
   public static FontRecord createFont(CellStyle.Font fontspecs) throws WriteException
   {
      WritableFont font = null;
      if (fontspecs.family != null)
      {
         font = new WritableFont(WritableFont.createFont(fontspecs.family));
      }
      else
      {
         font = new WritableFont(WritableFont.ARIAL);
      }
      if (fontspecs.pointSize != null)
      {
         font.setPointSize(fontspecs.pointSize);
      }
      if (fontspecs.color != null)
      {
         font.setColour(createColor(fontspecs.color));
      }
      if (fontspecs.bold != null)
      {
         font.setBoldStyle(fontspecs.bold ? WritableFont.BOLD : WritableFont.NO_BOLD);
      }
      if (fontspecs.italic != null)
      {
         font.setItalic(fontspecs.italic);
      }
      if (fontspecs.struckOut != null)
      {
         font.setStruckout(fontspecs.struckOut);
      }
      if (fontspecs.scriptStyle != null)
      {
         font.setScriptStyle(createScriptStyle(fontspecs.scriptStyle));
      }
      if (fontspecs.underlineStyle != null)
      {
         font.setUnderlineStyle(createUnderlineStyle(fontspecs.underlineStyle));
      }
      return font;
   }

   /**
    * Creates a JExcelAPI number display format
    * 
    * @param formatMask The format mask
    * @return The display format
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/write/NumberFormat.html">NumberFormat</a>
    */
   public static DisplayFormat createNumberFormat(String formatMask)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating number format for mask #0", formatMask);
      }
      try
      {
         return (DisplayFormat) getConstant(NUMBERFORMATS_CLASSNAME, formatMask);
      }
      catch (NoSuchFieldException e)
      {
         // Look! An empty catch block! But this one is documented. We are using
         // this to see if there is a constant
         // defines for this in the class
         return null;
      }
   }

   /**
    * Creates a JExcelAPI date display format
    * 
    * @param mask The format mask
    * @return The display format
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/write/DateFormat.html">DateFormat</a>
    */
   public static DisplayFormat createDateFormat(String mask)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating date format for mask #0", mask);
      }
      try
      {
         return (DisplayFormat) getConstant(DATEFORMATS_CLASSNAME, mask.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         // Look! An empty catch block! But this one is documented. We are using
         // this to see if there is a constant
         // defines for this in the class
         return null;
      }
   }

   /**
    * Creates a JExcelAPI representation of an color
    * 
    * @param color The requested color
    * @return The color representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/Color.html">Color</a>
    */
   public static Colour createColor(String color)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating color for #0", color);
      }
      // Workaround for the feature that black is... well not always black in
      // Excel (ref: Andy Khan on yahoo groups)
      if (color.equalsIgnoreCase("black"))
      {
         color = "palette_black";
      }
      try
      {
         return color == null ? Colour.AUTOMATIC : (Colour) getConstant(COLOR_CLASS_NAME, color.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Color {0} not supported, try {1}", color, getValidConstantsSuggestion(COLOR_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of an orientation
    * 
    * @param orientation The requested orientation
    * @return The orientation representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/Orientation.html">Orientation</a>
    */
   public static Orientation createOrientation(String orientation)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating orientation for #0", orientation);
      }
      try
      {
         return orientation == null ? Orientation.HORIZONTAL : (Orientation) getConstant(ORIENTATION_CLASS_NAME, orientation.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Orientation {0} not supported, try {1}", orientation, getValidConstantsSuggestion(ORIENTATION_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of a vertical alignment
    * 
    * @param verticalAlignment The requested alignment
    * @return The alignment representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/VerticalAlignment.html">VerticalAlignment</a>
    */
   public static VerticalAlignment createVerticalAlignment(String verticalAlignment)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating verical alignment for #0", verticalAlignment);
      }
      try
      {
         return verticalAlignment == null ? VerticalAlignment.BOTTOM : (VerticalAlignment) getConstant(VERTICAL_ALIGNMENT_CLASS_NAME, verticalAlignment.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Verical alignment {0} not supported, try {1}", verticalAlignment, getValidConstantsSuggestion(VERTICAL_ALIGNMENT_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of a border
    * 
    * @param border The requested border
    * @return border representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/Border.html">Border</a>
    */
   public static Border createBorder(String border)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating border for #0", border);
      }
      try
      {
         return border == null ? Border.ALL : (Border) getConstant(BORDER_CLASS_NAME, border.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Border {0} not supported, try {1}", border, getValidConstantsSuggestion(BORDER_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of a border line style
    * 
    * @param borderLineStyle The requested border line style
    * @return The border line style representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/BorderLineStyle.html">BorderLineStyle</a>
    */
   public static BorderLineStyle createLineStyle(String borderLineStyle)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating border line style for #0", borderLineStyle);
      }
      try
      {
         return borderLineStyle == null ? BorderLineStyle.NONE : (BorderLineStyle) getConstant(BORDER_LINE_STYLE_CLASS_NAME, borderLineStyle.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Border line style {0} not supported, try {1}", borderLineStyle, getValidConstantsSuggestion(BORDER_LINE_STYLE_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of a pattern
    * 
    * @param pattern The requested pattern
    * @return The pattern representation
    * @see <a
    *      href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/format/Pattern.html">Pattern</a>
    */
   public static Pattern createPattern(String pattern)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating pattern for #0", pattern);
      }
      try
      {
         return pattern == null ? Pattern.SOLID : (Pattern) getConstant(PATTERN_CLASS_NAME, pattern.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Pattern {0} not supported, try {1}", pattern, getValidConstantsSuggestion(PATTERN_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of a page orientation
    * 
    * @param orientation The type of orientation to create
    * @return The page orientation representation
    */
   public static PageOrientation createPageOrientation(String orientation)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating page orientation for #0", orientation);
      }
      try
      {
         return orientation == null ? PageOrientation.LANDSCAPE : (PageOrientation) getConstant(PAGE_ORIENTATION_CLASS_NAME, orientation.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Page orientation {0} not supported, try {1}", orientation, getValidConstantsSuggestion(PAGE_ORIENTATION_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI representation of a paper size
    * 
    * @param paperSize The type of paper size to create
    * @return The paper size representation
    */
   public static PaperSize createPaperSize(String paperSize)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating paper size for #0", paperSize);
      }
      try
      {
         return paperSize == null ? PaperSize.A4 : (PaperSize) getConstant(PAPER_SIZE_CLASS_NAME, paperSize.toUpperCase());
      }
      catch (NoSuchFieldException e)
      {
         String message = Interpolator.instance().interpolate("Page size {0} not supported, try {1}", paperSize, getValidConstantsSuggestion(PAPER_SIZE_CLASS_NAME));
         throw new ExcelWorkbookException(message, e);
      }
   }

   /**
    * Creates a JExcelAPI header or footer representation. Processes the left,
    * center and right facets using a helper method
    * 
    * @param uiHeaderFooter The UI header or footer to interpret
    * @param headerFooter The JExcelAPI header or footer representation to add
    *           to
    * @return The JExcelAPI header or footer representation
    */
   public static HeaderFooter createHeaderFooter(UIComponent uiHeaderFooter, HeaderFooter headerFooter)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Processing header/footer #0", uiHeaderFooter);
      }
      processHeaderFooterFacet(headerFooter.getLeft(), uiHeaderFooter.getFacet("left"));
      processHeaderFooterFacet(headerFooter.getCentre(), uiHeaderFooter.getFacet("center"));
      processHeaderFooterFacet(headerFooter.getRight(), uiHeaderFooter.getFacet("right"));
      return headerFooter;
   }

   /**
    * Processes a header or footer facet. A header or footer facet in JExcelAPI
    * is split into three parts, left, center and right and the UI
    * representation has facets with the same namings. Gets the requested facet
    * from the UI component and calls helper methods for processing the header
    * commands in sequence
    * 
    * @param headerFooter The JExcelAPI header or footer facet to process
    * @param facetName The name of the facet to process (left, center, right)
    * @param uiHeaderFooter The UI representation to interpret
    */
   private static void processHeaderFooterFacet(HeaderFooter.Contents contents, UIComponent facet)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Processing facet #0 of header/footer #1", facet, contents);
      }
      // No facet found
      if (facet == null)
      {
         return;
      }
      String facetContent = null;
      try
      {
         facetContent = ExcelComponent.cmp2String(FacesContext.getCurrentInstance(), facet);
      }
      catch (IOException e)
      {
         throw new ExcelWorkbookException("Could not get content from header facet", e);
      }
      if (facetContent == null)
      {
         return;
      }
      facetContent = facetContent.trim();
      int firstHash;
      int secondHash;
      String command;
      String pre;
      while (!"".equals(facetContent))
      {
         firstHash = -1;
         secondHash = -1;
         firstHash = facetContent.indexOf(HEADERFOOTER_COMMAND_MARKER);
         if (firstHash >= 0)
         {
            secondHash = facetContent.indexOf(HEADERFOOTER_COMMAND_MARKER, firstHash + 1);
         }
         if (firstHash >= 0 && secondHash >= 0 && firstHash != secondHash)
         {
            pre = facetContent.substring(0, firstHash);
            if (!"".equals(pre))
            {
               contents.append(pre);
            }
            command = facetContent.substring(firstHash + 1, secondHash);
            processCommand(contents, command);
            facetContent = facetContent.substring(secondHash + 1);
         }
         else
         {
            contents.append(facetContent);
            facetContent = "";
         }
      }
   }

   /**
    * Processes a header or footer command, adding itself to the contents
    * 
    * @param contents The target contents
    * @param command The command to execute
    */
   private static void processCommand(Contents contents, String command)
   {
      command = command.toLowerCase();
      if (command.startsWith("date"))
      {
         contents.appendDate();
      }
      else if (command.startsWith(HF_CMD_PAGE_NUMBER))
      {
         contents.appendPageNumber();
      }
      else if (command.startsWith(HF_CMD_TIME))
      {
         contents.appendTime();
      }
      else if (command.startsWith(HF_CMD_TOTAL_PAGES))
      {
         contents.appendTotalPages();
      }
      else if (command.startsWith(HF_CMD_WORKBOOK_NAME))
      {
         contents.appendWorkbookName();
      }
      else if (command.startsWith(HF_CMD_WORKSHEET_NAME))
      {
         contents.appendWorkSheetName();
      }
      else if (command.startsWith(HF_CMD_BOLD))
      {
         contents.toggleBold();
      }
      else if (command.startsWith(HF_CMD_DOUBLE_UNDERLINE))
      {
         contents.toggleDoubleUnderline();
      }
      else if (command.startsWith(HF_CMD_ITALICS))
      {
         contents.toggleItalics();
      }
      else if (command.startsWith(HF_CMD_OUTLINE))
      {
         contents.toggleOutline();
      }
      else if (command.startsWith(HF_CMD_SHADOW))
      {
         contents.toggleShadow();
      }
      else if (command.startsWith(HF_CMD_STRIKETHROUGH))
      {
         contents.toggleStrikethrough();
      }
      else if (command.startsWith(HF_CMD_SUBSCRIPT))
      {
         contents.toggleSubScript();
      }
      else if (command.startsWith(HF_CMD_SUPERSCRIPT))
      {
         contents.toggleSuperScript();
      }
      else if (command.startsWith(HF_CMD_UNDERLINE))
      {
         contents.toggleUnderline();
      }
      else if (command.startsWith(HF_CMD_FONT_NAME))
      {
         String[] parts = command.split(HEADERFOOTER_PAIR_DELIMITER);
         if (parts.length != 2)
         {
            log.warn("Header/Footer font name error in #0", command);
         }
         contents.setFontName(parts[1].trim());
      }
      else if (command.startsWith(HF_CMD_FONT_SIZE))
      {
         String[] parts = command.split(HEADERFOOTER_PAIR_DELIMITER);
         if (parts.length != 2)
         {
            log.warn("Header/Footer font size error in #0", command);
         }
         contents.setFontSize(Integer.parseInt(parts[1].trim()));
      }
   }

}
