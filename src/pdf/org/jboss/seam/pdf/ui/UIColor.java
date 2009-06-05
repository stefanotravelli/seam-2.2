package org.jboss.seam.pdf.ui;

import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;

import javax.el.ValueExpression;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;

public class UIColor extends UIComponentBase
{
   String color;
   String color2;

   String point;
   String point2;

   public void setColor(String color)
   {
      this.color = color;
   }

   public String getColor()
   {
      return (color != null) ? color : (String) evalExpression("color");
   }

   public void setColor2(String color2)
   {
      this.color2 = color2;
   }

   public String getColor2()
   {
      return (color2 != null) ? color2 : (String) evalExpression("color2");
   }

   public void setPoint(String point)
   {
      this.point = point;
   }

   public String getPoint()
   {
      return (point != null) ? point : (String) evalExpression("point");
   }

   public void setPoint2(String point2)
   {
      this.point2 = point2;
   }

   public String getPoint2()
   {
      return (point2 != null) ? point2 : (String) evalExpression("point2");
   }

   @Override
   public String getFamily()
   {
      return ITextComponent.COMPONENT_FAMILY;
   }

   public Object evalExpression(String el)
   {
      ValueExpression expr = getValueExpression(el);
      return (expr == null) ? null : expr.getValue(FacesContext.getCurrentInstance().getELContext());
   }

   public Point2D pointValue(String string)
   {
      Point2D point = new Point2D.Float();
      float[] vals = ITextUtils.stringToFloatArray(string);
      point.setLocation(vals[0], vals[1]);
      return point;
   }

   public Paint getPaint()
   {
      String c1 = getColor();
      String c2 = getColor2();

      if (c2 == null)
      {
         return ITextUtils.colorValue(c1);
      }
      else
      {
         return new GradientPaint(pointValue(getPoint()), ITextUtils.colorValue(c1), pointValue(getPoint()), ITextUtils.colorValue(c2));
      }
   }
}
