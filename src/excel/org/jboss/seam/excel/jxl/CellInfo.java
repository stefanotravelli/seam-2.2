package org.jboss.seam.excel.jxl;

import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;

import org.jboss.seam.excel.ui.UICell.CellType;

/**
 * Cell info that collects features and formats
 * 
 * @author karlsnic
 */
public class CellInfo
{
   // Cell format of the cell
   private WritableCellFormat cellFormat;

   // Cell features of the cell
   private WritableCellFeatures cellFeatures;

   // Cell contents type of the cell
   private CellType cellType;

   public CellType getCellType()
   {
      return cellType;
   }

   public void setCellType(CellType cellType)
   {
      this.cellType = cellType;
   }

   public WritableCellFormat getCellFormat()
   {
      return cellFormat;
   }

   public WritableCellFeatures getCellFeatures()
   {
      return cellFeatures;
   }

   public void setCellFormat(WritableCellFormat cellFormat)
   {
      this.cellFormat = cellFormat;
   }

   public void setCellFeatures(WritableCellFeatures cellFeatures)
   {
      this.cellFeatures = cellFeatures;
   }
}

