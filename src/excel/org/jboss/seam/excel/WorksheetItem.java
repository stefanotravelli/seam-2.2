package org.jboss.seam.excel;

public interface WorksheetItem
{

   public enum ItemType
   {
      cell, image, hyperlink
   }

   public abstract ItemType getItemType();
   
   public abstract Object getValue();
   
   public Integer getColumn();
   
   public Integer getRow();

}
