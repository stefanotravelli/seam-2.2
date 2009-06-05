package org.jboss.seam.excel.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.Manager;
import org.jboss.seam.document.ByteArrayDocumentData;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentStore;
import org.jboss.seam.excel.ExcelFactory;
import org.jboss.seam.excel.ExcelWorkbook;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.css.CSSNames;
import org.jboss.seam.excel.css.ColumnStyle;
import org.jboss.seam.excel.css.Parser;
import org.jboss.seam.excel.css.StyleMap;
import org.jboss.seam.excel.ui.ExcelComponent;
import org.jboss.seam.excel.ui.UICell;
import org.jboss.seam.excel.ui.UIColumn;
import org.jboss.seam.excel.ui.UIWorkbook;
import org.jboss.seam.excel.ui.UIWorksheet;
import org.jboss.seam.navigation.Pages;

/**
 * Excel export class that exports a UIData component to an Excel workbook
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * @author Daniel Roth (danielc.roth@gmail.com)
 * 
 */
@Name("org.jboss.seam.excel.exporter.excelExporter")
@Scope(ScopeType.EVENT)
@Install(precedence = Install.BUILT_IN)
@BypassInterceptors
public class ExcelExporter
{
   // The excel workbook implementation
   private ExcelWorkbook excelWorkbook = null;

   // A map of known column widths
   private Map<Integer, Integer> columnWidths = new HashMap<Integer, Integer>();

   /**
    * Helper method to call the exporter and use the default excel workbook
    * implementation
    * 
    * @param dataTableId
    */
   public void export(String dataTableId)
   {
      export(dataTableId, "");
   }

   /**
    * Exports the UIData object to Excel workbook. Looks up the component, parse
    * the templates, iterates the columns and the UIOutput elements within
    * 
    * @param dataTableId id of data table to export
    * @param type ExcelWorkbook implementation to use
    */
   @SuppressWarnings("unchecked")
   public void export(String dataTableId, String type)
   {
      excelWorkbook = ExcelFactory.instance().getExcelWorkbook(type);

      Parser parser = new Parser();

      // Gets the datatable
      UIData dataTable = (UIData) FacesContext.getCurrentInstance().getViewRoot().findComponent(dataTableId);
      if (dataTable == null)
      {
         throw new ExcelWorkbookException(Interpolator.instance().interpolate("Could not find data table with id #0", dataTableId));
      }

      // Inits the workbook and worksheet
      UIWorkbook uiWorkbook = new UIWorkbook();
      excelWorkbook.createWorkbook(uiWorkbook);
      UIWorksheet uiWorksheet = new UIWorksheet();
      uiWorkbook.getChildren().add(uiWorksheet);
      uiWorksheet.setStyle(Parser.getStyle(dataTable));
      uiWorksheet.setStyleClass(Parser.getStyleClass(dataTable));
      excelWorkbook.createOrSelectWorksheet(uiWorksheet);

      // Saves the datatable var
      String dataTableVar = dataTable.getVar();
      Object oldValue = FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(dataTableVar);

      // Processes the columns
      List<javax.faces.component.UIColumn> columns = ExcelComponent.getChildrenOfType(dataTable.getChildren(), javax.faces.component.UIColumn.class);
      columnWidths = parseColumnWidths(uiWorksheet);
      int col = 0;
      for (javax.faces.component.UIColumn column : columns)
      {
         ColumnStyle columnStyle = new ColumnStyle(parser.getCascadedStyleMap(column));
         boolean cssExport = columnStyle.export == null || columnStyle.export;
         if (column.isRendered() && cssExport)
         {
            uiWorksheet.getChildren().add(column);
            Iterator iterator = UIWorksheet.unwrapIterator(dataTable.getValue());
            processColumn(column, iterator, dataTableVar, col++);
            excelWorkbook.nextColumn();
         }
      }

      // Restores the data table var
      if (oldValue == null)
      {
         FacesContext.getCurrentInstance().getExternalContext().getRequestMap().remove(dataTableVar);
      }
      else
      {
         FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(dataTableVar, oldValue);
      }

      // Redirects to the generated document
      redirectExport();

   }

   /**
    * Parses column widths from a worksheet tag
    * 
    * @param worksheet The worksheet to get the style from
    * @return The map of column number -> column width
    */
   private Map<Integer, Integer> parseColumnWidths(UIWorksheet worksheet)
   {
      Map<Integer, Integer> columnWidths = new HashMap<Integer, Integer>();
      Parser parser = new Parser();

      StyleMap styleMap = parser.getCascadedStyleMap(worksheet);
      for (Map.Entry<String, Object> entry : styleMap.entrySet())
      {
         String key = entry.getKey();
         if (key.startsWith(CSSNames.COLUMN_WIDTHS))
         {
            String columnIndexString = key.substring(CSSNames.COLUMN_WIDTHS.length());
            int columnIndex = Integer.parseInt(columnIndexString);
            columnWidths.put(columnIndex, (Integer) entry.getValue());
         }
      }
      return columnWidths;
   }

   /**
    * Puts document in store and redirects
    */
   private void redirectExport()
   {
      String viewId = Pages.getViewId(FacesContext.getCurrentInstance());
      String baseName = Pages.getCurrentBaseName();
      DocumentData documentData = new ByteArrayDocumentData(baseName, excelWorkbook.getDocumentType(), excelWorkbook.getBytes());
      String id = DocumentStore.instance().newId();
      String url = DocumentStore.instance().preferredUrlForContent(baseName, excelWorkbook.getDocumentType().getExtension(), id);
      url = Manager.instance().encodeConversationId(url, viewId);
      DocumentStore.instance().saveData(id, documentData);
      try
      {
         FacesContext.getCurrentInstance().getExternalContext().redirect(url);
      }
      catch (IOException e)
      {
         throw new ExcelWorkbookException(Interpolator.instance().interpolate("Could not redirect to #0", url), e);
      }
   }

   /**
    * Processes a datatable column
    * 
    * @param column The column to parse
    * @param iterator The iterator to the data
    * @param var The binding var
    * @param col
    */
   @SuppressWarnings("unchecked")
   private void processColumn(javax.faces.component.UIColumn column, Iterator iterator, String var, int columnIndex)
   {
      // Process header facet
      UIComponent headerFacet = column.getFacet(UIColumn.HEADER_FACET_NAME);
      if (headerFacet != null && UIOutput.class.isAssignableFrom(headerFacet.getClass()))
      {
         List<UIOutput> headerOutputs = new ArrayList<UIOutput>();
         headerOutputs.add((UIOutput) headerFacet);
         processOutputs(column, headerOutputs);
      }

      // Process data
      while (iterator.hasNext())
      {
         FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(var, iterator.next());
         List<UIOutput> dataOutputs = ExcelComponent.getChildrenOfType(column.getChildren(), UIOutput.class);
         processOutputs(column, dataOutputs);
      }

      Integer columnWidth = columnWidths.get(columnIndex);
      if (columnWidth != null)
      {
         UIColumn uiColumn = new UIColumn();
         uiColumn.setStyle(CSSNames.COLUMN_WIDTH + ":" + columnWidth);
         excelWorkbook.applyColumnSettings(uiColumn);
      }

   }

   /**
    * Processes all output type elements (in column)
    * 
    * @param outputs The list of outputs to process
    * @param preTemplates The pre-pushed templates
    */
   private void processOutputs(javax.faces.component.UIColumn column, List<UIOutput> outputs)
   {
      for (UIOutput output : outputs)
      {
         if (!output.isRendered())
         {
            continue;
         }
         UICell cell = new UICell();
         column.getChildren().add(cell);
         cell.setId(output.getId());
         cell.setValue(output.getValue());
         cell.setStyle(Parser.getStyle(output));
         cell.setStyleClass(Parser.getStyleClass(output));

         excelWorkbook.addItem(cell);
      }
   }

}
