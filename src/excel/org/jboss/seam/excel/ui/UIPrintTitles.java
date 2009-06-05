package org.jboss.seam.excel.ui;

public class UIPrintTitles extends ExcelComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIPrintTitles";

   private Integer firstCol;
   private Integer firstRow;
   private Integer lastCol;
   private Integer lastRow;

   public Integer getFirstCol()
   {
      return (Integer) valueOf("firstCol", firstCol);

   }

   public void setFirstCol(Integer firstCol)
   {
      this.firstCol = firstCol;
   }

   public Integer getFirstRow()
   {
      return (Integer) valueOf("firstRow", firstRow);
   }

   public void setFirstRow(Integer firstRow)
   {
      this.firstRow = firstRow;
   }

   public Integer getLastCol()
   {
      return (Integer) valueOf("lastCol", lastCol);
   }

   public void setLastCol(Integer lastCol)
   {
      this.lastCol = lastCol;
   }

   public Integer getLastRow()
   {
      return (Integer) valueOf("lastRow", lastRow);
   }

   public void setLastRow(Integer lastRow)
   {
      this.lastRow = lastRow;
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

}
