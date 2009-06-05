package org.jboss.seam.excel.ui;

public abstract class UICellBase extends ExcelComponent
{
   private String comment;
   private Integer commentWidth;
   private Integer commentHeight;

   public String getComment()
   {
      return (String) valueOf("comment", comment);
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   public Integer getCommentWidth()
   {
      return (Integer) valueOf("commentWidth", commentWidth);
   }

   public void setCommentWidth(Integer commentWidth)
   {
      this.commentWidth = commentWidth;
   }

   public Integer getCommentHeight()
   {
      return (Integer) valueOf("commentHeight", commentHeight);
   }

   public void setCommentHeight(Integer commentHeight)
   {
      this.commentHeight = commentHeight;
   }

}
