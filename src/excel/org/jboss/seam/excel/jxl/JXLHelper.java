package org.jboss.seam.excel.jxl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIComponent;

import jxl.CellView;
import jxl.SheetSettings;
import jxl.WorkbookSettings;
import jxl.biff.DisplayFormat;
import jxl.write.DateFormat;
import jxl.write.DateFormats;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCell;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.css.CellStyle;
import org.jboss.seam.excel.css.ColumnStyle;
import org.jboss.seam.excel.css.Parser;
import org.jboss.seam.excel.ui.ExcelComponent;
import org.jboss.seam.excel.ui.UICell;
import org.jboss.seam.excel.ui.UICellBase;
import org.jboss.seam.excel.ui.UIColumn;
import org.jboss.seam.excel.ui.UIFooter;
import org.jboss.seam.excel.ui.UIHeader;
import org.jboss.seam.excel.ui.UILink;
import org.jboss.seam.excel.ui.UIPrintArea;
import org.jboss.seam.excel.ui.UIPrintTitles;
import org.jboss.seam.excel.ui.UIWorkbook;
import org.jboss.seam.excel.ui.UIWorksheet;
import org.jboss.seam.excel.ui.UICell.CellType;
import org.jboss.seam.excel.ui.validation.UIListValidation;
import org.jboss.seam.excel.ui.validation.UIListValidationItem;
import org.jboss.seam.excel.ui.validation.UINumericValidation;
import org.jboss.seam.excel.ui.validation.UIRangeValidation;
import org.jboss.seam.excel.ui.validation.Validation;
import org.jboss.seam.excel.ui.validation.UINumericValidation.ValidationCondition;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * A helper class for the JXLExcelWorkbook, caches cell info and holds CSS
 * parser
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * 
 */
public class JXLHelper
{

   private static Log log = Logging.getLog(JXLHelper.class);

   // The CSS parser
   private Parser parser = new Parser();

   // A cache of cell info
   private CellInfoCache cellInfoCache = new CellInfoCache();

   /**
    * Tries to get a general display format (number first, then date)
    *  
    * @param formatMask The format mask to attempt
    * 
    * @return The format mask (or null if not possible)
    */
   private DisplayFormat getGenericDisplayFormat(String formatMask) {
        if (formatMask == null) 
        {
            return null;
        }
        DisplayFormat displayFormat = JXLFactory.createNumberFormat(formatMask);
        if (displayFormat != null) 
        {
            return displayFormat;
        }
        displayFormat = JXLFactory.createDateFormat(formatMask);
        if (displayFormat != null) 
        {
            return displayFormat;
        }
        try 
        {
            displayFormat = new NumberFormat(formatMask);
        } 
        catch (IllegalArgumentException e) 
        {
            // no-op, it was worth a try;
        }
        if (displayFormat != null) 
        {
            return displayFormat;
        }
        try 
        {
            displayFormat = new DateFormat(formatMask);
        } 
        catch (IllegalArgumentException e) 
        {
            // no-op, it was worth a try;
        }
        return displayFormat;
    }
   
   /**
    * Creates a cell format
    * 
    * @param uiCell The cell to model
    * @return The cell format
    * @throws WriteException if the creation failed
    */
   public WritableCellFormat createCellFormat(UICell uiCell, CellType cellType) throws WriteException
   {
      WritableCellFormat cellFormat = null;
      CellStyle cellStyle = new CellStyle(parser.getCascadedStyleMap(uiCell));

//      CellType cellType = cellStyle.forceType != null ? CellType.valueOf(cellStyle.forceType) : uiCell.getDataType();
      switch (cellType)
      {
      case text:
         // Creates a basic text format
         cellFormat = new WritableCellFormat(NumberFormats.TEXT);
         break;
      case number:
         /*
          * If there is no mask, creates a default number format cell If there
          * is a mask, tries to match it against a constant name If the constant
          * can't be created, creates a custom number format from the mask
          */

         if (cellStyle.formatMask == null)
         {
            cellFormat = new WritableCellFormat(NumberFormats.DEFAULT);
         }
         else
         {
            DisplayFormat displayFormat = JXLFactory.createNumberFormat(cellStyle.formatMask);
            if (displayFormat != null)
            {
               cellFormat = new WritableCellFormat(displayFormat);
            }
            else
            {
               try
               {
                  cellFormat = new WritableCellFormat(new NumberFormat(cellStyle.formatMask));
               }
               catch (IllegalArgumentException e)
               {
                  throw new ExcelWorkbookException(Interpolator.instance().interpolate("Could not create number format for mask {0}", cellStyle.formatMask), e);
               }
            }
         }
         break;
      case date:
         /*
          * If there is no mask, creates a default date format cell If there is
          * a mask, tries to match it against a constant name If the constant
          * can't be created, creates a custom date format from the mask
          */

         if (cellStyle.formatMask == null)
         {
            cellFormat = new WritableCellFormat(DateFormats.DEFAULT);
         }
         else
         {
            DisplayFormat displayFormat = JXLFactory.createDateFormat(cellStyle.formatMask);
            if (displayFormat != null)
            {
               cellFormat = new WritableCellFormat(displayFormat);
            }
            else
            {
               try
               {
                  cellFormat = new WritableCellFormat(new DateFormat(cellStyle.formatMask));
               }
               catch (IllegalArgumentException e)
               {
                  throw new ExcelWorkbookException(Interpolator.instance().interpolate("Could not create date format for mask {0}", cellStyle.formatMask), e);
               }
            }
         }
         break;
      case formula:
         DisplayFormat displayFormat = getGenericDisplayFormat(cellStyle.formatMask); 
         cellFormat = displayFormat != null ? new WritableCellFormat(displayFormat) : new WritableCellFormat();
         break;
      case bool:
         cellFormat = new WritableCellFormat();
         break;
      default:
         cellFormat = new WritableCellFormat();
         break;
      }

      if (cellStyle.alignment != null)
      {
         cellFormat.setAlignment(JXLFactory.createAlignment(cellStyle.alignment));
      }

      if (cellStyle.indentation != null)
      {
         cellFormat.setIndentation(cellStyle.indentation);
      }

      if (cellStyle.locked != null)
      {
         cellFormat.setLocked(cellStyle.locked);
      }

      if (cellStyle.orientation != null)
      {
         cellFormat.setOrientation(JXLFactory.createOrientation(cellStyle.orientation));
      }

      if (cellStyle.shrinkToFit != null)
      {
         cellFormat.setShrinkToFit(cellStyle.shrinkToFit);
      }

      if (cellStyle.verticalAlignment != null)
      {
         cellFormat.setVerticalAlignment(JXLFactory.createVerticalAlignment(cellStyle.verticalAlignment));
      }

      if (cellStyle.wrap != null)
      {
         cellFormat.setWrap(cellStyle.wrap);
      }

      if (cellStyle.font.isUsed())
      {
         cellFormat.setFont(JXLFactory.createFont(cellStyle.font));
      }

      if (cellStyle.leftBorder.isUsed())
      {
         cellFormat.setBorder(JXLFactory.createBorder("left"), JXLFactory.createLineStyle(cellStyle.leftBorder.lineStyle), JXLFactory.createColor(cellStyle.leftBorder.color));
      }
      if (cellStyle.topBorder.isUsed())
      {
         cellFormat.setBorder(JXLFactory.createBorder("top"), JXLFactory.createLineStyle(cellStyle.topBorder.lineStyle), JXLFactory.createColor(cellStyle.topBorder.color));
      }
      if (cellStyle.rightBorder.isUsed())
      {
         cellFormat.setBorder(JXLFactory.createBorder("right"), JXLFactory.createLineStyle(cellStyle.rightBorder.lineStyle), JXLFactory.createColor(cellStyle.rightBorder.color));
      }
      if (cellStyle.bottomBorder.isUsed())
      {
         cellFormat.setBorder(JXLFactory.createBorder("bottom"), JXLFactory.createLineStyle(cellStyle.bottomBorder.lineStyle), JXLFactory.createColor(cellStyle.bottomBorder.color));
      }
      if (cellStyle.background.isUsed())
      {
         cellFormat.setBackground(JXLFactory.createColor(cellStyle.background.color), JXLFactory.createPattern(cellStyle.background.pattern));
      }
      return cellFormat;
   }

   /**
    * Sets the stylesheets for the parser
    * 
    * @param stylesheets The stylesheets to set
    * @throws MalformedURLException If the URL was bad
    * @throws IOException If the URL could not be read
    */
   public void setStylesheets(List<UILink> stylesheets) throws MalformedURLException, IOException
   {
      parser.setStylesheets(stylesheets);
   }

   /**
    * Applied worksheet settings
    * 
    * @param worksheet The worksheet to apply the settings to
    * @param uiWorksheet The settings to set
    */
   protected void applyWorksheetSettings(WritableSheet worksheet, UIWorksheet uiWorksheet)
   {
      SheetSettings settings = worksheet.getSettings();
      if (uiWorksheet.getAutomaticFormulaCalculation() != null)
      {
         settings.setAutomaticFormulaCalculation(uiWorksheet.getAutomaticFormulaCalculation());
      }
      if (uiWorksheet.getBottomMargin() != null)
      {
         settings.setBottomMargin(uiWorksheet.getBottomMargin());
      }
      if (uiWorksheet.getCopies() != null)
      {
         settings.setCopies(uiWorksheet.getCopies());
      }
      if (uiWorksheet.getDefaultColumnWidth() != null)
      {
         settings.setDefaultColumnWidth(uiWorksheet.getDefaultColumnWidth());
      }
      if (uiWorksheet.getDefaultRowHeight() != null)
      {
         settings.setDefaultRowHeight(uiWorksheet.getDefaultRowHeight());
      }
      if (uiWorksheet.getDisplayZeroValues() != null)
      {
         settings.setDisplayZeroValues(uiWorksheet.getDisplayZeroValues());
      }
      if (uiWorksheet.getFitHeight() != null)
      {
         settings.setFitHeight(uiWorksheet.getFitHeight());
      }
      if (uiWorksheet.getFitToPages() != null)
      {
         settings.setFitToPages(uiWorksheet.getFitToPages());
      }
      if (uiWorksheet.getFitWidth() != null)
      {
         settings.setFitWidth(uiWorksheet.getFitWidth());
      }
      if (uiWorksheet.getFooterMargin() != null)
      {
         settings.setFooterMargin(uiWorksheet.getFooterMargin());
      }
      if (uiWorksheet.getHeaderMargin() != null)
      {
         settings.setHeaderMargin(uiWorksheet.getHeaderMargin());
      }
      if (uiWorksheet.getHidden() != null)
      {
         settings.setHidden(uiWorksheet.getHidden());
      }
      if (uiWorksheet.getHorizontalCentre() != null)
      {
         settings.setHorizontalCentre(uiWorksheet.getHorizontalCentre());
      }
      if (uiWorksheet.getHorizontalFreeze() != null)
      {
         settings.setHorizontalFreeze(uiWorksheet.getHorizontalFreeze());
      }
      if (uiWorksheet.getHorizontalPrintResolution() != null)
      {
         settings.setHorizontalPrintResolution(uiWorksheet.getHorizontalPrintResolution());
      }
      if (uiWorksheet.getLeftMargin() != null)
      {
         settings.setLeftMargin(uiWorksheet.getLeftMargin());
      }
      if (uiWorksheet.getNormalMagnification() != null)
      {
         settings.setNormalMagnification(uiWorksheet.getNormalMagnification());
      }
      if (uiWorksheet.getOrientation() != null)
      {
         settings.setOrientation(JXLFactory.createPageOrientation(uiWorksheet.getOrientation()));
      }
      if (uiWorksheet.getPageBreakPreviewMagnification() != null)
      {
         settings.setPageBreakPreviewMagnification(uiWorksheet.getPageBreakPreviewMagnification());
      }
      if (uiWorksheet.getPageBreakPreviewMode() != null)
      {
         settings.setPageBreakPreviewMode(uiWorksheet.getPageBreakPreviewMode());
      }
      if (uiWorksheet.getPageStart() != null)
      {
         settings.setPageStart(uiWorksheet.getPageStart());
      }
      if (uiWorksheet.getPaperSize() != null)
      {
         settings.setPaperSize(JXLFactory.createPaperSize(uiWorksheet.getPaperSize()));
      }
      if (uiWorksheet.getPassword() != null)
      {
         settings.setPassword(uiWorksheet.getPassword());
      }
      if (uiWorksheet.getPasswordHash() != null)
      {
         settings.setPasswordHash(uiWorksheet.getPasswordHash());
      }
      if (uiWorksheet.getPrintGridLines() != null)
      {
         settings.setPrintGridLines(uiWorksheet.getPrintGridLines());
      }
      if (uiWorksheet.getPrintHeaders() != null)
      {
         settings.setPrintHeaders(uiWorksheet.getPrintHeaders());
      }
      if (uiWorksheet.getSheetProtected() != null)
      {
         settings.setProtected(uiWorksheet.getSheetProtected());
      }
      if (uiWorksheet.getRecalculateFormulasBeforeSave() != null)
      {
         settings.setRecalculateFormulasBeforeSave(uiWorksheet.getRecalculateFormulasBeforeSave());
      }
      if (uiWorksheet.getRightMargin() != null)
      {
         settings.setRightMargin(uiWorksheet.getRightMargin());
      }
      if (uiWorksheet.getScaleFactor() != null)
      {
         settings.setScaleFactor(uiWorksheet.getScaleFactor());
      }
      if (uiWorksheet.getSelected() != null)
      {
         settings.setSelected(uiWorksheet.getSelected());
      }
      if (uiWorksheet.getShowGridLines() != null)
      {
         settings.setShowGridLines(uiWorksheet.getShowGridLines());
      }
      if (uiWorksheet.getTopMargin() != null)
      {
         settings.setTopMargin(uiWorksheet.getTopMargin());
      }
      if (uiWorksheet.getVerticalCentre() != null)
      {
         settings.setVerticalCentre(uiWorksheet.getVerticalCentre());
      }
      if (uiWorksheet.getVerticalFreeze() != null)
      {
         settings.setVerticalFreeze(uiWorksheet.getVerticalFreeze());
      }
      if (uiWorksheet.getVerticalPrintResolution() != null)
      {
         settings.setVerticalPrintResolution(uiWorksheet.getVerticalPrintResolution());
      }
      if (uiWorksheet.getZoomFactor() != null)
      {
         settings.setZoomFactor(uiWorksheet.getZoomFactor());
      }
      // Iterates through the worksheet uiWorksheet child elements (print areas,
      // print titles and headers/footers)
      for (UIComponent child : uiWorksheet.getChildren())
      {
         if (child.getClass() == UIPrintArea.class)
         {
            UIPrintArea printArea = (UIPrintArea) child;
            settings.setPrintArea(printArea.getFirstColumn(), printArea.getFirstRow(), printArea.getLastColumn(), printArea.getLastRow());
         }
         else if (child.getClass() == UIPrintTitles.class)
         {
            UIPrintTitles printTitles = (UIPrintTitles) child;
            settings.setPrintTitles(printTitles.getFirstCol(), printTitles.getFirstRow(), printTitles.getLastCol(), printTitles.getLastRow());
         }
         else if (child.getClass() == UIHeader.class)
         {
            UIHeader uiHeader = (UIHeader) child;
            settings.setHeader(JXLFactory.createHeaderFooter(uiHeader, settings.getHeader()));
         }
         else if (child.getClass() == UIFooter.class)
         {
            UIFooter uiFooter = (UIFooter) child;
            settings.setFooter(JXLFactory.createHeaderFooter(uiFooter, settings.getFooter()));
         }
      }
   }

   /**
    * Gets cell info needed for cell creation
    * 
    * @param uiCell The cell to get info for
    * @return The cell info
    */
   protected CellInfo getCellInfo(UICell uiCell)
   {
      CellInfo cellInfo = new CellInfo();
      cellInfo.setCellFeatures(createCellFeatures(uiCell));
      cellInfo.setCellType(getCellDataType(uiCell));
      cellInfo.setCellFormat(getCellFormat(uiCell, cellInfo.getCellType()));
      return cellInfo;
   }

   /**
    * Creates cell features from a template
    * 
    * @param uiCellFormat The cell format to apply
    * @return The cell features
    */
   public WritableCellFeatures createCellFeatures(UICellBase uiCellFormat)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating cell features for #0", uiCellFormat);
      }
      WritableCellFeatures cellFeatures = new WritableCellFeatures();

      if (uiCellFormat.getComment() != null)
      {
         if (uiCellFormat.getCommentHeight() != null && uiCellFormat.getCommentWidth() != null)
         {
            cellFeatures.setComment(uiCellFormat.getComment(), uiCellFormat.getCommentWidth(), uiCellFormat.getCommentHeight());
         }
         else
         {
            cellFeatures.setComment(uiCellFormat.getComment());
         }
      }
      List<Validation> validations = ExcelComponent.getChildrenOfType(uiCellFormat.getChildren(), Validation.class);
      for (Validation validation : validations)
      {
         switch (validation.getType())
         {
         case numeric:
            addNumericValidation(cellFeatures, (UINumericValidation) validation);
            break;
         case range:
            addRangeValidation(cellFeatures, (UIRangeValidation) validation);
            break;
         case list:
            addListValidation(cellFeatures, (UIListValidation) validation);
            break;
         default:
            throw new ExcelWorkbookException(Interpolator.instance().interpolate("Unknown validation type {0}", validation.getType()));
         }
      }
      return cellFeatures;
   }

   /**
    * Gets the cell type for a cell. Tries to look it up in a cache based on the
    * component id of the cell. If it's not found, it's created and cached.
    * 
    * @param uiCell The cell to look up
    * @return The data type of a cell
    */
   private CellType getCellDataType(UICell uiCell)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Getting cell data type from cache for #0", uiCell.getId());
      }
      CellType cellDataType = cellInfoCache.getCachedCellType(uiCell.getId());
      if (cellDataType == null)
      {
         CellStyle cellStyle = new CellStyle(parser.getCascadedStyleMap(uiCell));
         cellDataType = cellStyle.forceType != null ? CellType.valueOf(cellStyle.forceType) : uiCell.getDataType();
         cellInfoCache.setCachedCellType(uiCell.getId(), cellDataType);
      }
      return cellDataType;
   }

   /**
    * Gets a cell format for a cell. Tries to look it up in a cache based on the
    * component id of the cell. If it's not found, it's created and cached.
    * 
    * @param uiCell The cell to format
    * @return The cell format
    */
   private WritableCellFormat getCellFormat(UICell uiCell, CellType cellType)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Getting cell format for #0", uiCell.getId());
      }
      WritableCellFormat cellFormat = cellInfoCache.getCachedCellFormat(uiCell.getId());
      if (cellFormat == null)
      {
         try
         {
            cellFormat = createCellFormat(uiCell, cellType);
         }
         catch (WriteException e)
         {
            throw new ExcelWorkbookException("Could not create cellformat", e);
         }
         cellInfoCache.setCachedCellFormat(uiCell.getId(), cellFormat);
      }
      return cellFormat;
   }

   /**
    * Adds list validation to a cell
    * 
    * @param cellFeatures The cell features to add validation to
    * @param validation The validation to parse
    */
   private static void addListValidation(WritableCellFeatures cellFeatures, UIListValidation validation)
   {
      List<UIListValidationItem> items = ExcelComponent.getChildrenOfType(validation.getChildren(), UIListValidationItem.class);
      if (items.isEmpty())
      {
         throw new ExcelWorkbookException("No items in validation list");
      }

      List<String> validations = new ArrayList<String>();
      for (UIListValidationItem item : items)
      {
         validations.add(item.getValue());
      }

      cellFeatures.setDataValidationList(validations);
   }

   /**
    * Adds range validation to a cell
    * 
    * @param cellFeatures The cell features to apply the validation to
    * @param validation The validation to add
    */
   private static void addRangeValidation(WritableCellFeatures cellFeatures, UIRangeValidation validation)
   {
      if (validation.getStartColumn() == null || validation.getStartRow() == null || validation.getEndColumn() == null || validation.getEndRow() == null)
      {
         throw new ExcelWorkbookException("Must set all start/end columns/rows for range validation");
      }

      cellFeatures.setDataValidationRange(validation.getStartColumn(), validation.getStartRow(), validation.getEndColumn(), validation.getEndRow());
   }

   /**
    * Adds numeric validation to a cell
    * 
    * @param cellFeatures Features to add validation to
    * @param validation Validation to add
    */
   private static void addNumericValidation(WritableCellFeatures cellFeatures, UINumericValidation validation)
   {
      if (validation.getValue() == null)
      {
         throw new ExcelWorkbookException("Must define value in validation");
      }
      if ((ValidationCondition.between.equals(validation.getCondition()) || ValidationCondition.not_between.equals(validation.getCondition())) && validation.getValue2() == null)
      {
         throw new ExcelWorkbookException("Must define both values in validation for between/not_between");
      }
      switch (validation.getCondition())
      {
      case equal:
         cellFeatures.setNumberValidation(validation.getValue(), WritableCellFeatures.EQUAL);
         break;
      case not_equal:
         cellFeatures.setNumberValidation(validation.getValue(), WritableCellFeatures.NOT_EQUAL);
         break;
      case greater_equal:
         cellFeatures.setNumberValidation(validation.getValue(), WritableCellFeatures.GREATER_EQUAL);
         break;
      case less_equal:
         cellFeatures.setNumberValidation(validation.getValue(), WritableCellFeatures.LESS_EQUAL);
         break;
      case less_than:
         cellFeatures.setNumberValidation(validation.getValue(), WritableCellFeatures.LESS_THAN);
         break;
      case between:
         cellFeatures.setNumberValidation(validation.getValue(), validation.getValue2(), WritableCellFeatures.BETWEEN);
         break;
      case not_between:
         cellFeatures.setNumberValidation(validation.getValue(), validation.getValue2(), WritableCellFeatures.NOT_BETWEEN);
         break;
      }
   }

   /**
    * Creates a JExcelAPI cell representation from the given input
    * 
    * @param column The row (0-based) to place the cell at
    * @param row The column (0-based) to place the cell at
    * @param type The type of cell
    * @param data The contents of the cell
    * @param cellFormat The cell format settings of the cell
    * @return The prepared cell representation
    * @see <a href="http://jexcelapi.sourceforge.net/resources/javadocs/2_6/docs/jxl/write/WritableCell.html">WritableCell</a>
    */
   public static WritableCell createCell(int column, int row, CellType type, Object data, WritableCellFormat cellFormat)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating cell at (#0,#1) of type #2 with data #2", column, row, type, data);
      }

      switch (type)
      {
      case text:
         return new Label(column, row, data.toString(), cellFormat);
      case number:
          try {
              return new jxl.write.Number(column, row, Double.parseDouble(data.toString()), cellFormat);
          } catch (NumberFormatException e) {
              String message = Interpolator.instance().interpolate(ResourceBundle.instance().getString("org.jboss.seam.excel.not_a_number"), data.toString());
              return new Label(column, row, message, cellFormat);
          }
      case date:
          try {
              return new DateTime(column, row, (Date) data, cellFormat);
          } catch (ClassCastException e) {
              String message = Interpolator.instance().interpolate(ResourceBundle.instance().getString("org.jboss.seam.excel.not_a_date"), data.toString());
              return new Label(column, row, message, cellFormat);
          }
      case formula:
         return new Formula(column, row, data.toString(), cellFormat);
      case bool:
         return new jxl.write.Boolean(column, row, Boolean.parseBoolean(data.toString()), cellFormat);
      default:
         return new Label(column, row, data.toString(), cellFormat);
      }
   }

   /**
    * Creates a JExcelAPI Workbook settings object from the UI counterpart.
    * Starts with an empty object and adds the setting only if it is non-null
    * 
    * @param uiWorkbook The UI element to interpret
    * @return The created workbook settings
    */
   public WorkbookSettings createWorkbookSettings(UIWorkbook uiWorkbook)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Creating workbook settings from #0", uiWorkbook);
      }
      WorkbookSettings workbookSettings = new WorkbookSettings();
      if (uiWorkbook.getArrayGrowSize() != null)
      {
         workbookSettings.setArrayGrowSize(uiWorkbook.getArrayGrowSize());
      }
      if (uiWorkbook.getAutoFilterDisabled() != null)
      {
         workbookSettings.setAutoFilterDisabled(uiWorkbook.getAutoFilterDisabled());
      }
      if (uiWorkbook.getAutoFilterDisabled() != null)
      {
         workbookSettings.setCellValidationDisabled(uiWorkbook.getAutoFilterDisabled());
      }
      if (uiWorkbook.getCharacterSet() != null)
      {
         workbookSettings.setCharacterSet(uiWorkbook.getCharacterSet());
      }
      if (uiWorkbook.getDrawingsDisabled() != null)
      {
         workbookSettings.setDrawingsDisabled(uiWorkbook.getDrawingsDisabled());
      }
      if (uiWorkbook.getEncoding() != null)
      {
         workbookSettings.setEncoding(uiWorkbook.getEncoding());
      }
      if (uiWorkbook.getExcelDisplayLanguage() != null)
      {
         workbookSettings.setExcelDisplayLanguage(uiWorkbook.getExcelDisplayLanguage());
      }
      if (uiWorkbook.getExcelRegionalSettings() != null)
      {
         workbookSettings.setExcelRegionalSettings(uiWorkbook.getExcelRegionalSettings());
      }
      if (uiWorkbook.getFormulaAdjust() != null)
      {
         workbookSettings.setFormulaAdjust(uiWorkbook.getFormulaAdjust());
      }
      if (uiWorkbook.getGcDisabled() != null)
      {
         workbookSettings.setGCDisabled(uiWorkbook.getGcDisabled());
      }
      if (uiWorkbook.getIgnoreBlanks() != null)
      {
         workbookSettings.setIgnoreBlanks(uiWorkbook.getIgnoreBlanks());
      }
      if (uiWorkbook.getLocale() != null)
      {
         workbookSettings.setLocale(new Locale(uiWorkbook.getLocale()));
      }
      if (uiWorkbook.getMergedCellCheckingDisabled() != null)
      {
         workbookSettings.setMergedCellChecking(uiWorkbook.getMergedCellCheckingDisabled());
      }
      if (uiWorkbook.getNamesDisabled() != null)
      {
         workbookSettings.setNamesDisabled(uiWorkbook.getNamesDisabled());
      }
      if (uiWorkbook.getPropertySets() != null)
      {
         workbookSettings.setPropertySets(uiWorkbook.getPropertySets());
      }
      if (uiWorkbook.getRationalization() != null)
      {
         workbookSettings.setRationalization(uiWorkbook.getRationalization());
      }
      if (uiWorkbook.getSupressWarnings() != null)
      {
         workbookSettings.setSuppressWarnings(uiWorkbook.getSupressWarnings());
      }
      if (uiWorkbook.getTemporaryFileDuringWriteDirectory() != null)
      {
         workbookSettings.setTemporaryFileDuringWriteDirectory(new File(uiWorkbook.getTemporaryFileDuringWriteDirectory()));
      }
      if (uiWorkbook.getUseTemporaryFileDuringWrite() != null)
      {
         workbookSettings.setUseTemporaryFileDuringWrite(uiWorkbook.getUseTemporaryFileDuringWrite());
      }
      return workbookSettings;
   }

   /**
    * Applies column settings to a column
    * 
    * @param uiColumn The settings to apply
    * @param worksheet The worksheet to apply the column to
    * @param columnIndex The column index to the column
    */
   public void applyColumnSettings(UIColumn uiColumn, WritableSheet worksheet, int columnIndex)
   {
      ColumnStyle columnStyle = new ColumnStyle(parser.getCascadedStyleMap(uiColumn));

      if (log.isTraceEnabled())
      {
         log.trace("Applying column settings #0 on column #1", columnStyle, columnIndex);
      }
      CellView cellView = worksheet.getColumnView(columnIndex);
      if (columnStyle.autoSize != null)
      {
         cellView.setAutosize(columnStyle.autoSize);
      }
      if (columnStyle.hidden != null)
      {
         cellView.setHidden(columnStyle.hidden);
      }
      if (columnStyle.width != null)
      {
         cellView.setSize(columnStyle.width);
      }
      worksheet.setColumnView(columnIndex, cellView);
   }

}
