package org.jboss.seam.excel;

import java.util.List;

import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.excel.ui.UIColumn;
import org.jboss.seam.excel.ui.UILink;
import org.jboss.seam.excel.ui.UIWorkbook;
import org.jboss.seam.excel.ui.UIWorksheet;
import org.jboss.seam.excel.ui.command.Command;

/**
 * General interface interacting with an Excel Workbook abstraction 
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * @author Daniel Roth (danielc.roth@gmail.com)
 */
public interface ExcelWorkbook
{

   public abstract DocumentType getDocumentType();

   /**
    * Moves the internal column pointer to the next column, called by the tag to
    * indicate that a new column has been started. If the pointer exceeds the
    * maximum allowed, throws an exception
    */
   public abstract void nextColumn();

   /**
    * Creates a new worksheet in the workbook (or selects one if it exists).
    * Will require a rework for auto-renaming when support for auto-adding of
    * new worksheets if there are more than 65k rows.
    * 
    * @param uiWorksheet Worksheet to create or select
    */
   public abstract void createOrSelectWorksheet(UIWorksheet uiWorksheet);

   /**
    * Returns the binary data from the internal representation of the workbook
    * 
    * @return the bytes
    */
   public abstract byte[] getBytes();

   /**
    * Initializes a new workbook. Must be called first
    * 
    * @param uiWorkbook the workbook UI item to create
    */
   public abstract void createWorkbook(UIWorkbook uiWorkbook);

   /**
    * Applies column settings to the current column
    * 
    * @param uiColumn The UI column to inspect for settings
    */
   public abstract void applyColumnSettings(UIColumn uiColumn);

   /**
    * Adds an item (cell, image, hyperlink) to add to the worksheet
    * 
    * @param item The item to add
    */
   public abstract void addItem(WorksheetItem item);

   /**
    * Executes a command for a worksheet
    * 
    * @param command The command to execute
    */
   public abstract void executeCommand(Command command);

   /**
    * Places an item in the worksheet header
    * 
    * @param item The item to add
    * @param colspan The number of columns to span
    */
   public abstract void addWorksheetHeader(WorksheetItem item, int colspan);
   
   /**
    * Places an item in the worksheet footer
    * 
    * @param item The item to add
    * @param colspan The number of columns to span
    */
   public abstract void addWorksheetFooter(WorksheetItem item, int colspan);

   /**
    * Sets stylesheets for the workbook
    * 
    * @param stylesheets The stylesheets to set
    */
   public abstract void setStylesheets(List<UILink> stylesheets);
}