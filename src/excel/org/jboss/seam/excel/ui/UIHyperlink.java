package org.jboss.seam.excel.ui;

import org.jboss.seam.excel.WorksheetItem;

public class UIHyperlink extends UICellBase implements WorksheetItem
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIHyperlink";

   private String description;
   private String URL;
   private Integer startColumn;
   private Integer startRow;
   private Integer endColumn;
   private Integer endRow;

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   public String getDescription()
   {
      return (String) valueOf("description", description);
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getURL()
   {
      return (String) valueOf("URL", URL);
   }

   public void setURL(String url)
   {
      URL = url;
   }

   public Integer getStartColumn()
   {
      return (Integer) valueOf("startColumn", startColumn);
   }

   public void setStartColumn(Integer startColumn)
   {
      this.startColumn = startColumn;
   }

   public Integer getStartRow()
   {
      return (Integer) valueOf("startRow", startRow);
   }

   public void setStartRow(Integer startRow)
   {
      this.startRow = startRow;
   }

   public Integer getEndColumn()
   {
      return (Integer) valueOf("endColumn", endColumn);
   }

   public void setEndColumn(Integer endColumn)
   {
      this.endColumn = endColumn;
   }

   public Integer getEndRow()
   {
      return (Integer) valueOf("endRow", endRow);
   }

   public void setEndRow(Integer endRow)
   {
      this.endRow = endRow;
   }

   public ItemType getItemType()
   {
      return ItemType.hyperlink;
   }

    public Integer getColumn() {
        return getStartColumn();
    }

    public Integer getRow() {
        return getStartRow();
    }

    public Object getValue() {
        return getURL();
    }

}
