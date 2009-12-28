package org.jboss.seam.excel.csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.excel.ExcelWorkbook;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.WorksheetItem;
import org.jboss.seam.excel.ui.UICell;
import org.jboss.seam.excel.ui.UIColumn;
import org.jboss.seam.excel.ui.UIHyperlink;
import org.jboss.seam.excel.ui.UILink;
import org.jboss.seam.excel.ui.UIWorkbook;
import org.jboss.seam.excel.ui.UIWorksheet;
import org.jboss.seam.excel.ui.command.Command;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * @author Daniel Roth (danielc.roth@gmail.com)
 */
public class CsvExcelWorkbook implements ExcelWorkbook
{
   private int currentColumn = 0;
   private int currentRow = 0;
   private int maxRow = 0;
   private int maxColumn = 0;
   private int sheetStartRow = 0;

   private final static String DEFAULT_COLUMN_DELIMITER = "\"";
   private final static String DEFAULT_COLUMN_DELIMITER_REPLACEMENT = "\"\"";
   private final static String DEFAULT_LINEBREAK = "\n";
   private final static String DEFAULT_COLUMN_SEPERATOR = ",";

   private Map<String, String> table = null;
   private String sheetName = null;

   private Log log = Logging.getLog(getClass());

   protected String getColumnDelimeterReplacement() {
	   return DEFAULT_COLUMN_DELIMITER_REPLACEMENT;
   }
  
   protected String getColumnDelimeter() {
      return DEFAULT_COLUMN_DELIMITER;
   }
   protected String getLineBreak() {
      return DEFAULT_LINEBREAK;
   }
   protected String getColumnSeparator() {
      return DEFAULT_COLUMN_SEPERATOR;
   }
   
   public void createWorkbook(UIWorkbook uiWorkbook) throws ExcelWorkbookException
   {
      table = new HashMap<String, String>();

   }

   public void createOrSelectWorksheet(UIWorksheet uiWorksheet)
   {
      createOrSelectWorksheet(uiWorksheet.getName(), uiWorksheet.getStartRow(), uiWorksheet.getStartColumn());

   }

   private void createOrSelectWorksheet(String worksheetName, Integer startRow, Integer startColumn)
   {
      if (sheetName != null && !sheetName.equals(worksheetName))
      {
         throw new RuntimeException("You cannot export multiple sheet workbooks to excel.");
      }

      sheetName = worksheetName;
      currentColumn = (startColumn == null) ? 0 : startColumn;
      currentRow = (startRow == null) ? 0 : startRow;
      sheetStartRow = currentRow;
   }

   public byte[] getBytes()
   {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i <= maxRow; i++)
      {
         for (int j = 0; j <= maxColumn; j++)
         {
            String value = table.get(hash(i, j));
            value = (value == null) ? "" : value;
            if(value.contains(getColumnDelimeter()))
            	value = value.replace(getColumnDelimeter(), getColumnDelimeterReplacement()); //JBSEAM-4187
            buffer.append(getColumnDelimeter()).append(value).append(getColumnDelimeter()).append(getColumnSeparator());
         }
         buffer.deleteCharAt(buffer.length() - 1);
         buffer.append(getLineBreak());
      }

      return buffer.toString().getBytes();
   }

   public void nextColumn()
   {
      currentColumn++;
      currentRow = sheetStartRow;
   }

   public DocumentType getDocumentType()
   {
      return new DocumentData.DocumentType("csv", "text/csv");
   }

   public void addItem(WorksheetItem item)
   {
      switch (item.getItemType())
      {
      case cell:
         addCell((UICell) item);
         break;
      case hyperlink:
         addHyperLink((UIHyperlink) item);
         break;
      case image:
         log.trace("You cannot export an image to CSV", new Object[0]);
         break;
      }
   }

   private void addCell(UICell cell)
   {
      int row = (cell.getRow() == null) ? currentRow : cell.getRow();
      int column = (cell.getColumn() == null) ? currentColumn : cell.getColumn();
      addCsvCell(column, row, String.valueOf(cell.getValue()));
      
      if (cell.getColumn() == null && cell.getRow() == null)
         currentRow++;
   }

   private void addHyperLink(UIHyperlink link)
   {
      int row = (link.getStartRow() == null) ? currentRow : link.getStartRow();
      int column = (link.getStartColumn() == null) ? currentColumn : link.getStartColumn();
      if (link.getEndColumn() != null || link.getEndRow() != null)
         log.warn("endColumn/endRow is not supported by csv exporter", new Object[0]);
      addCsvCell(column, row, String.valueOf(link.getURL()));
   }

   private void addCsvCell(int column, int row, String value) throws ExcelWorkbookException
   {
      table.put(hash(row, column), value);
      maxRow = (row > maxRow) ? row : maxRow;
      maxColumn = (column > maxColumn) ? column : maxColumn;
   }

   private String hash(int row, int column)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(row).append(getColumnSeparator()).append(column);
      return buffer.toString();
   }

   public void applyWorksheetSettings(UIWorksheet uiWorksheet)
   {
      log.trace("applyWorksheetSettings() is not supported by CSV exporter", new Object[0]);
   }

   public void applyColumnSettings(UIColumn uiColumn)
   {
      log.trace("applyColumnSettings() is not supported by CSV exporter", new Object[0]);
   }

   public void executeCommand(Command command)
   {
      log.trace("executeCommand() is not supported by CSV exporter", new Object[0]);
   }

   public void addWorksheetFooter(WorksheetItem item, int colspan)
   {
      if (colspan > 0)
         log.warn("footer colspan are not supported by CSV exporter", new Object[0]);
      addItem(item);
   }

   public void addWorksheetHeader(WorksheetItem item, int colspan)
   {
      if (colspan > 0)
         log.warn("header colspan are not supported by CSV exporter", new Object[0]);
      addItem(item);

   }

   public void setStylesheets(List<UILink> stylesheets)
   {
      log.trace("styleSheets are not supported by CSV exporter", new Object[0]);
   }

}
