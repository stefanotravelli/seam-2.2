package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;

import com.lowagie.text.Rectangle;

public abstract class UIRectangle extends ITextComponent
{
   protected Integer border;
   protected String borderColor;
   protected String borderColorLeft;
   protected String borderColorRight;
   protected String borderColorTop;
   protected String borderColorBottom;
   protected String backgroundColor;
   protected Float borderWidth;
   protected Float borderWidthLeft;
   protected Float borderWidthRight;
   protected Float borderWidthTop;
   protected Float borderWidthBottom;

   public UIRectangle()
   {
      super();
   }

   // xxx - use string
   public void setBorder(Integer border)
   {
      this.border = border;
   }

   public void setBorderWidth(Float borderWidth)
   {
      this.borderWidth = borderWidth;
   }

   public void setBorderWidthBottom(Float borderWidthBottom)
   {
      this.borderWidthBottom = borderWidthBottom;
   }

   public void setBorderWidthLeft(Float borderWidthLeft)
   {
      this.borderWidthLeft = borderWidthLeft;
   }

   public void setBorderWidthRight(Float borderWidthRight)
   {
      this.borderWidthRight = borderWidthRight;
   }

   public void setBorderWidthTop(Float borderWidthTop)
   {
      this.borderWidthTop = borderWidthTop;
   }

   public void setBackgroundColor(String backgroundColor)
   {
      this.backgroundColor = backgroundColor;
   }

   public void setBorderColor(String borderColor)
   {
      this.borderColor = borderColor;
   }

   public void setBorderColorBottom(String borderColorBottom)
   {
      this.borderColorBottom = borderColorBottom;
   }

   public void setBorderColorLeft(String borderColorLeft)
   {
      this.borderColorLeft = borderColorLeft;
   }

   public void setBorderColorRight(String borderColorRight)
   {
      this.borderColorRight = borderColorRight;
   }

   public void setBorderColorTop(String borderColorTop)
   {
      this.borderColorTop = borderColorTop;
   }

   public void applyRectangleProperties(FacesContext context, Rectangle rectangle)
   {

      border = (Integer) valueBinding(context, "border", border);
      if (border != null)
      {
         rectangle.setBorder(border);
      }

      backgroundColor = (String) valueBinding(context, "backgroundColor", backgroundColor);
      if (backgroundColor != null)
      {
         rectangle.setBackgroundColor(ITextUtils.colorValue(backgroundColor));
      }

      borderColor = (String) valueBinding(context, "borderColor", borderColor);
      if (borderColor != null)
      {
         rectangle.setBorderColor(ITextUtils.colorValue(borderColor));
      }

      borderColorLeft = (String) valueBinding(context, "borderColorLeft", borderColorLeft);
      if (borderColorLeft != null)
      {
         rectangle.setBorderColorLeft(ITextUtils.colorValue(borderColorLeft));
      }

      borderColorRight = (String) valueBinding(context, "borderColorRight", borderColorRight);
      if (borderColorRight != null)
      {
         rectangle.setBorderColorRight(ITextUtils.colorValue(borderColorRight));
      }

      borderColorTop = (String) valueBinding(context, "borderColorTop", borderColorTop);
      if (borderColorTop != null)
      {
         rectangle.setBorderColorTop(ITextUtils.colorValue(borderColorTop));
      }

      borderColorBottom = (String) valueBinding(context, "borderColorBottom", borderColorBottom);
      if (borderColorBottom != null)
      {
         rectangle.setBorderColorBottom(ITextUtils.colorValue(borderColorBottom));
      }

      borderWidth = (Float) valueBinding(context, "borderWidth", borderWidth);
      if (borderWidth != null)
      {
         rectangle.setBorderWidth(borderWidth);
      }

      borderWidthLeft = (Float) valueBinding(context, "borderWidthLeft", borderWidthLeft);
      if (borderWidthLeft != null)
      {
         rectangle.setBorderWidthLeft(borderWidthLeft);
      }

      borderWidthRight = (Float) valueBinding(context, "borderWidthRight", borderWidthRight);
      if (borderWidthRight != null)
      {
         rectangle.setBorderWidthRight(borderWidthRight);
      }

      borderWidthTop = (Float) valueBinding(context, "borderWidthTop", borderWidthTop);
      if (borderWidthTop != null)
      {
         rectangle.setBorderWidthTop(borderWidthTop);
      }

      borderWidthBottom = (Float) valueBinding(context, "borderWidthBottom", borderWidthBottom);
      if (borderWidthBottom != null)
      {
         rectangle.setBorderWidthBottom(borderWidthBottom);
      }

   }

}