package org.jboss.seam.excel.ui;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.faces.context.FacesContext;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.WorksheetItem;

public class UICell extends UICellBase implements WorksheetItem
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UICell";

   public enum CellType
   {
      general, number, text, date, formula, bool
   }

   private Object value;
   private Integer column;
   private Integer row;

   public Integer getColumn()
   {
      return (Integer) valueOf("column", column);
   }

   public void setColumn(Integer column)
   {
      this.column = column;
   }

   public Integer getRow()
   {
      return (Integer) valueOf("row", row);
   }

   public void setRow(Integer row)
   {
      this.row = row;
   }
  
   
   public Object getValue()
   {
      Object theValue = valueOf("value", value);
      if (theValue == null) {
         try {
            theValue = cmp2String(FacesContext.getCurrentInstance(), this);
         } catch (IOException e) {
            String message = Interpolator.instance().interpolate("Could not render cell #0", getId());
            throw new ExcelWorkbookException(message, e);
         }
      }
      return theValue;
   }

   public void setValue(Object value)
   {
      this.value = value;
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   /**
    * Checks the data type of the contents to determine what kind of cell to
    * create
    * 
    * @return the data type of the cell (or forumula if this is such a subclass)
    */
   public CellType getDataType()
   {
      // FIXME: Consider if formula should be considered an item instead as a
      // subtype of formula
      if (this instanceof UIFormula)
      {
         return CellType.formula;
      }
      Object value = getValue();
      if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Short || value instanceof BigDecimal || value instanceof BigInteger || value instanceof Byte || value instanceof Float)
      {
         return CellType.number;
      }
      else if (value instanceof String || value instanceof Character)
      {
         return CellType.text;
      }
      else if (value instanceof Date || value instanceof java.sql.Date)
      {
         return CellType.date;
      }
      else if (value instanceof Boolean)
      {
         return CellType.bool;
      }
      return CellType.general;
   }

   public ItemType getItemType()
   {
      return ItemType.cell;
   }


}
