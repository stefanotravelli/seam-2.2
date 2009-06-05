package org.jboss.seam.excel.ui.command;

public interface Command
{
   public enum CommandType
   {
      merge_cells, add_row_pagebreak, group_rows, group_columns
   }

   public abstract CommandType getCommandType();
}
