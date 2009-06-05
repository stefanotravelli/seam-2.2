package org.jboss.seam.excel.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;

import org.jboss.seam.excel.ExcelWorkbook;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.WorksheetItem;
import org.jboss.seam.excel.ui.command.Command;

public class UIColumn extends ExcelComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIColumn";

   public static final String HEADER_FACET_NAME = "header";
   private static final String FOOTER_FACET_NAME = "footer";

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      // Get workbook and worksheet
      ExcelWorkbook excelWorkbook = getWorkbook(getParent());

      if (excelWorkbook == null)
      {
         throw new ExcelWorkbookException("Could not find excel workbook");
      }

      // Column width etc.
      excelWorkbook.applyColumnSettings(this);

      UIWorksheet sheet = (UIWorksheet) getParentByClass(getParent(), UIWorksheet.class);
      if (sheet == null)
      {
         throw new ExcelWorkbookException("Could not find worksheet");
      }

      // Add header items (if any)
      WorksheetItem headerItem = (WorksheetItem) getFacet(HEADER_FACET_NAME);
      if (headerItem != null)
      {
         excelWorkbook.addItem(headerItem);
      }

      // Execute commands (if any)
      List<Command> commands = getCommands(getChildren());
      for (Command command : commands)
      {
         excelWorkbook.executeCommand(command);
      }

       // Get UiCell template this column's data cells and iterate over sheet data
      for (WorksheetItem item : getItems(getChildren()))
      {
         Object oldValue = null;
         Iterator iterator = null;
         // Store away the old value for the sheet binding var (if there is one)
         if (sheet.getVar() != null) {
            oldValue = FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(sheet.getVar());
            iterator = sheet.getDataIterator();
         } else {
            // No var, no iteration...
            iterator = new ArrayList().iterator();
         }
         while (iterator.hasNext())
         {
            // Store the bound data in the request map and add the cell
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(sheet.getVar(), iterator.next());
            excelWorkbook.addItem(item);
         }

         //  No iteration, nothing to restore
         if (sheet.getVar() == null) {
            continue;
         }
         // Restore the previously modified request map (if there was a var)
         if (oldValue == null)
         {
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap().remove(sheet.getVar());
         }
         else
         {
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(sheet.getVar(), oldValue);
         }
      }

      // Add footer items (if any)
      WorksheetItem footerItem = (WorksheetItem) getFacet(FOOTER_FACET_NAME);
      if (footerItem != null)
      {
         excelWorkbook.addItem(footerItem);
      }
      
      
      // Move column pointer to next column
      excelWorkbook.nextColumn();

   }

}
