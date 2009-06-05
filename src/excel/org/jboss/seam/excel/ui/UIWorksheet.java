package org.jboss.seam.excel.ui;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import org.jboss.seam.excel.ExcelWorkbook;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.WorksheetItem;
import org.jboss.seam.excel.ui.command.Command;
import org.jboss.seam.framework.Query;

public class UIWorksheet extends ExcelComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIWorksheet";

   private static final String HEADER_FACET_NAME = "header";
   private static final String FOOTER_FACET_NAME = "footer";

   private String name;
   private String var;
   private Object value;
   private Integer startRow;
   private Integer startColumn;
   private Boolean automaticFormulaCalculation;
   private Double bottomMargin;
   private Integer copies;
   private Integer defaultColumnWidth;
   private Integer defaultRowHeight;
   private Boolean displayZeroValues;
   private Integer fitHeight;
   private Boolean fitToPages;
   private Integer fitWidth;
   private Double footerMargin;
   private Double headerMargin;
   private Boolean hidden;
   private Boolean horizontalCentre;
   private Integer horizontalFreeze;
   private Integer horizontalPrintResolution;
   private Double leftMargin;
   private Integer normalMagnification;
   private String orientation;
   private Integer pageBreakPreviewMagnification;
   private Boolean pageBreakPreviewMode;
   private Integer pageStart;
   private String paperSize;
   private String password;
   private Integer passwordHash;
   private Boolean printGridLines;
   private Boolean printHeaders;
   private Boolean sheetProtected;
   private Boolean recalculateFormulasBeforeSave;
   private Double rightMargin;
   private Integer scaleFactor;
   private Boolean selected;
   private Boolean showGridLines;
   private Double topMargin;
   private Boolean verticalCentre;
   private Integer verticalFreeze;
   private Integer verticalPrintResolution;
   private Integer zoomFactor;

   public Boolean getAutomaticFormulaCalculation()
   {
      return (Boolean) valueOf("automaticFormulaCalculation", automaticFormulaCalculation);
   }

   public void setAutomaticFormulaCalculation(Boolean automaticFormulaCalculation)
   {
      this.automaticFormulaCalculation = automaticFormulaCalculation;
   }

   public Double getBottomMargin()
   {
      return (Double) valueOf("bottomMargin", bottomMargin);
   }

   public void setBottomMargin(Double bottomMargin)
   {
      this.bottomMargin = bottomMargin;
   }

   public Integer getCopies()
   {
      return (Integer) valueOf("copies", copies);
   }

   public void setCopies(Integer copies)
   {
      this.copies = copies;
   }

   public Integer getDefaultColumnWidth()
   {
      return (Integer) valueOf("defaultColumnWidth", defaultColumnWidth);
   }

   public void setDefaultColumnWidth(Integer defaultColumnWidth)
   {
      this.defaultColumnWidth = defaultColumnWidth;
   }

   public Integer getDefaultRowHeight()
   {
      return (Integer) valueOf("defaultRowHeight", defaultRowHeight);
   }

   public void setDefaultRowHeight(Integer defaultRowHeight)
   {
      this.defaultRowHeight = defaultRowHeight;
   }

   public Boolean getDisplayZeroValues()
   {
      return (Boolean) valueOf("displayZeroValues", displayZeroValues);
   }

   public void setDisplayZeroValues(Boolean displayZeroValues)
   {
      this.displayZeroValues = displayZeroValues;
   }

   public Integer getFitHeight()
   {
      return (Integer) valueOf("fitHeight", fitHeight);
   }

   public void setFitHeight(Integer fitHeight)
   {
      this.fitHeight = fitHeight;
   }

   public Boolean getFitToPages()
   {
      return (Boolean) valueOf("fitToPages", fitToPages);
   }

   public void setFitToPages(Boolean fitToPages)
   {
      this.fitToPages = fitToPages;
   }

   public Integer getFitWidth()
   {
      return (Integer) valueOf("fitWidth", fitWidth);
   }

   public void setFitWidth(Integer fitWidth)
   {
      this.fitWidth = fitWidth;
   }

   public Double getFooterMargin()
   {
      return (Double) valueOf("footerMargin", footerMargin);
   }

   public void setFooterMargin(Double footerMargin)
   {
      this.footerMargin = footerMargin;
   }

   public Double getHeaderMargin()
   {
      return (Double) valueOf("headerMargin", headerMargin);
   }

   public void setHeaderMargin(Double headerMargin)
   {
      this.headerMargin = headerMargin;
   }

   public Boolean getHidden()
   {
      return (Boolean) valueOf("hidden", hidden);
   }

   public void setHidden(Boolean hidden)
   {
      this.hidden = hidden;
   }

   public Boolean getHorizontalCentre()
   {
      return (Boolean) valueOf("horizontalCentre", horizontalCentre);
   }

   public void setHorizontalCentre(Boolean horizontalCentre)
   {
      this.horizontalCentre = horizontalCentre;
   }

   public Integer getHorizontalFreeze()
   {
      return (Integer) valueOf("horizontalFreeze", horizontalFreeze);
   }

   public void setHorizontalFreeze(Integer horizontalFreeze)
   {
      this.horizontalFreeze = horizontalFreeze;
   }

   public Integer getHorizontalPrintResolution()
   {
      return (Integer) valueOf("horizontalPrintResolution", horizontalPrintResolution);
   }

   public void setHorizontalPrintResolution(Integer horizontalPrintResolution)
   {
      this.horizontalPrintResolution = horizontalPrintResolution;
   }

   public Double getLeftMargin()
   {
      return (Double) valueOf("leftMargin", leftMargin);
   }

   public void setLeftMargin(Double leftMargin)
   {
      this.leftMargin = leftMargin;
   }

   public Integer getNormalMagnification()
   {
      return (Integer) valueOf("normalMagnification", normalMagnification);
   }

   public void setNormalMagnification(Integer normalMagnification)
   {
      this.normalMagnification = normalMagnification;
   }

   public String getOrientation()
   {
      return (String) valueOf("orientation", orientation);
   }

   public void setOrientation(String orientation)
   {
      this.orientation = orientation;
   }

   public Integer getPageBreakPreviewMagnification()
   {
      return (Integer) valueOf("pageBreakPreviewMagnification", pageBreakPreviewMagnification);
   }

   public void setPageBreakPreviewMagnification(Integer pageBreakPreviewMagnification)
   {
      this.pageBreakPreviewMagnification = pageBreakPreviewMagnification;
   }

   public Integer getPageStart()
   {
      return (Integer) valueOf("pageStart", pageStart);
   }

   public void setPageStart(Integer pageStart)
   {
      this.pageStart = pageStart;
   }

   public String getPaperSize()
   {
      return (String) valueOf("paperSize", paperSize);
   }

   public void setPaperSize(String paperSize)
   {
      this.paperSize = paperSize;
   }

   public String getPassword()
   {
      return (String) valueOf("password", password);
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public Integer getPasswordHash()
   {
      return (Integer) valueOf("passwordHash", passwordHash);
   }

   public void setPasswordHash(Integer passwordHash)
   {
      this.passwordHash = passwordHash;
   }

   public Boolean getPrintGridLines()
   {
      return (Boolean) valueOf("printGridLines", printGridLines);
   }

   public void setPrintGridLines(Boolean printGridLines)
   {
      this.printGridLines = printGridLines;
   }

   public Boolean getPrintHeaders()
   {
      return (Boolean) valueOf("printHeaders", printHeaders);
   }

   public void setPrintHeaders(Boolean printHeaders)
   {
      this.printHeaders = printHeaders;
   }

   public Boolean getSheetProtected()
   {
      return (Boolean) valueOf("sheetProtected", sheetProtected);
   }

   public void setSheetProtected(Boolean sheetProtected)
   {
      this.sheetProtected = sheetProtected;
   }

   public Boolean getRecalculateFormulasBeforeSave()
   {
      return (Boolean) valueOf("recalculateFormulasBeforeSave", recalculateFormulasBeforeSave);
   }

   public void setRecalculateFormulasBeforeSave(Boolean recalculateFormulasBeforeSave)
   {
      this.recalculateFormulasBeforeSave = recalculateFormulasBeforeSave;
   }

   public Double getRightMargin()
   {
      return (Double) valueOf("rightMargin", rightMargin);
   }

   public void setRightMargin(Double rightMargin)
   {
      this.rightMargin = rightMargin;
   }

   public Boolean getSelected()
   {
      return (Boolean) valueOf("selected", selected);
   }

   public void setSelected(Boolean selected)
   {
      this.selected = selected;
   }

   public Boolean getShowGridLines()
   {
      return (Boolean) valueOf("showGridLines", showGridLines);
   }

   public void setShowGridLines(Boolean showGridLines)
   {
      this.showGridLines = showGridLines;
   }

   public Double getTopMargin()
   {
      return (Double) valueOf("topMargin", topMargin);
   }

   public void setTopMargin(Double topMargin)
   {
      this.topMargin = topMargin;
   }

   public Boolean getVerticalCentre()
   {
      return (Boolean) valueOf("verticalCentre", verticalCentre);
   }

   public void setVerticalCentre(Boolean verticalCentre)
   {
      this.verticalCentre = verticalCentre;
   }

   public Integer getVerticalFreeze()
   {
      return (Integer) valueOf("verticalFreeze", verticalFreeze);
   }

   public void setVerticalFreeze(Integer verticalFreeze)
   {
      this.verticalFreeze = verticalFreeze;
   }

   public Integer getVerticalPrintResolution()
   {
      return (Integer) valueOf("verticalPrintResolution", verticalPrintResolution);
   }

   public void setVerticalPrintResolution(Integer verticalPrintResolution)
   {
      this.verticalPrintResolution = verticalPrintResolution;
   }

   public Integer getZoomFactor()
   {
      return (Integer) valueOf("zoomFactor", zoomFactor);
   }

   public void setZoomFactor(Integer zoomFactor)
   {
      this.zoomFactor = zoomFactor;
   }

   public Boolean getPageBreakPreviewMode()
   {
      return (Boolean) valueOf("pageBreakPreviewMode", pageBreakPreviewMode);
   }

   public void setPageBreakPreviewMode(Boolean pageBreakPreviewMode)
   {
      this.pageBreakPreviewMode = pageBreakPreviewMode;
   }

   public Integer getScaleFactor()
   {
      return (Integer) valueOf("scaleFactor", scaleFactor);
   }

   public void setScaleFactor(Integer scaleFactor)
   {
      this.scaleFactor = scaleFactor;
   }
   

   public Integer getStartRow()
   {
      return (Integer) valueOf("startRow", startRow);
   }

   public void setStartRow(Integer startRow)
   {
      this.startRow = startRow;
   }

   public Integer getStartColumn()
   {
      return (Integer) valueOf("startColumn", startColumn);
   }

   public void setStartColumn(Integer startColumn)
   {
      this.startColumn = startColumn;
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   public String getName()
   {
      return (String) valueOf("name", name);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getVar()
   {
      return (String) valueOf("var", var);
   }

   public void setVar(String var)
   {
      this.var = var;
   }

   public Object getValue()
   {
      return valueOf("value", value);
   }

   public void setValue(Object value)
   {
      this.value = value;
   }

   @Override
   public void encodeBegin(javax.faces.context.FacesContext facesContext) throws java.io.IOException
   {
      // Get workbook
      ExcelWorkbook excelWorkbook = getWorkbook(getParent());

      if (excelWorkbook == null)
      {
         throw new ExcelWorkbookException("Could not find excel workbook");
      }

      // Create new worksheet (or select an existing one) and apply settings (if
      // any)
      excelWorkbook.createOrSelectWorksheet(this);

      WorksheetItem headerItem = (WorksheetItem) getFacet(HEADER_FACET_NAME);
      if (headerItem != null)
      {
         int colspan = getChildrenOfType(getChildren(), UIColumn.class).size();
         excelWorkbook.addWorksheetHeader(headerItem, colspan);
      }

      // Add worksheet level items
      List<WorksheetItem> items = getItems(getChildren());
      for (WorksheetItem item : items)
      {
         excelWorkbook.addItem(item);
      }

      // Execute worksheet level commands
      List<Command> commands = getCommands(getChildren());
      for (Command command : commands)
      {
         excelWorkbook.executeCommand(command);
      }
   }

   @Override
   public void encodeEnd(javax.faces.context.FacesContext facesContext) throws java.io.IOException
   {
      ExcelWorkbook excelWorkbook = getWorkbook(getParent());
      if (excelWorkbook == null)
      {
         throw new ExcelWorkbookException("Could not find excel workbook");
      }

      WorksheetItem footerItem = (WorksheetItem) getFacet(FOOTER_FACET_NAME);
      if (footerItem != null)
      {
         int colspan = getChildrenOfType(getChildren(), UIColumn.class).size();
         excelWorkbook.addWorksheetFooter(footerItem, colspan);
      }
   }

   @SuppressWarnings("unchecked")
   public static Iterator unwrapIterator(Object value)
   {
      if (value instanceof Iterable)
      {
         return ((Iterable) value).iterator();
      }
      else if (value instanceof DataModel && ((DataModel) value).getWrappedData() instanceof Iterable)
      {
         return ((Iterable) ((DataModel) value).getWrappedData()).iterator();
      }
      else if (value instanceof Map) {
          return ((Map) value).entrySet().iterator();
      }
      else if (value instanceof Query)
      {
         return (((Query) value).getResultList()).iterator();
      }
      else if (value != null && value.getClass().isArray())
      {
         return arrayAsList(value).iterator();
      }
      else
      {
         throw new ExcelWorkbookException("A worksheet's value must be an Iterable, DataModel or Query");
      }
   }

   /**
    * Returns an iterator over objects passed to the worksheet
    * 
    * @return Iterator for values passed to the sheet
    */
   @SuppressWarnings("unchecked")
   public Iterator getDataIterator()
   {
      return unwrapIterator(getValue());
   }

   @SuppressWarnings("unchecked")
   private static List arrayAsList(Object array)
   {
      if (array.getClass().getComponentType().isPrimitive())
      {
         List list = new ArrayList();
         for (int i = 0; i < Array.getLength(array); i++)
         {
            list.add(Array.get(array, i));
         }
         return list;
      }
      else
      {
         return Arrays.asList((Object[]) array);
      }
   }

}
