package org.jboss.seam.pdf.ui;

import java.awt.BasicStroke;
import java.awt.Stroke;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;

public class UIStroke extends UIComponentBase
{
   Float width;
   String cap; // CAP_BUTT, CAP_ROUND, CAP_SQUARE
   String join; // JOIN_MITER, JOIN_ROUND, JOIN_BEVEL
   Float miterLimit = 1f;
   String dashString;
   Float dashPhase = 0f;

   public String getCap()
   {
      return cap;
   }

   public void setCap(String cap)
   {
      this.cap = cap;
   }

   public String getDash()
   {
      return dashString;
   }

   public void setDash(String dash)
   {
      this.dashString = dash;
   }

   public float getDashPhase()
   {
      return dashPhase;
   }

   public void setDashPhase(float dashPhase)
   {
      this.dashPhase = dashPhase;
   }

   public String getJoin()
   {
      return join;
   }

   public void setJoin(String join)
   {
      this.join = join;
   }

   public float getMiterlimit()
   {
      return miterLimit;
   }

   public void setMiterLimit(float miterLimit)
   {
      this.miterLimit = miterLimit;
   }

   public float getWidth()
   {
      return width;
   }

   public void setWidth(float width)
   {
      this.width = width;
   }

   public int capValue(String cap)
   {
      if (cap == null || cap.equalsIgnoreCase("butt"))
      {
         return BasicStroke.CAP_BUTT;
      }
      else if (cap.equalsIgnoreCase("round"))
      {
         return BasicStroke.CAP_ROUND;
      }
      else if (cap.equalsIgnoreCase("square"))
      {
         return BasicStroke.CAP_SQUARE;
      }
      throw new RuntimeException("invalid cap value: " + cap);
   }

   public int joinValue(String join)
   {
      if (join == null || join.equalsIgnoreCase("miter"))
      {
         return BasicStroke.JOIN_MITER;
      }
      else if (join.equalsIgnoreCase("round"))
      {
         return BasicStroke.JOIN_ROUND;
      }
      else if (join.equalsIgnoreCase("bevel"))
      {
         return BasicStroke.JOIN_BEVEL;
      }
      throw new RuntimeException("invalid join value: " + join);
   }

   @Override
   public String getFamily()
   {
      return ITextComponent.COMPONENT_FAMILY;
   }

   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object[] values = (Object[]) state;
      super.restoreState(context, values[0]);

      width = (Float) values[1];
      cap = (String) values[2];
      join = (String) values[3];
      miterLimit = (Float) values[4];
      dashString = (String) values[5];
      dashPhase = (Float) values[6];
   }

   @Override
   public Object saveState(FacesContext context)
   {
      Object[] values = new Object[7];

      values[0] = super.saveState(context);
      values[1] = width;
      values[2] = cap;
      values[3] = join;
      values[4] = miterLimit;
      values[5] = dashString;
      values[6] = dashPhase;

      return values;
   }

   public Stroke getStroke()
   {
      if (width == null)
      {
         return new BasicStroke();
      }
      else if (cap == null)
      {
         return new BasicStroke(getWidth());
      }
      else if (dashString == null)
      {
         if (miterLimit == null)
         {
            return new BasicStroke(getWidth(), capValue(getCap()), joinValue(getJoin()));
         }
         else
         {
            return new BasicStroke(getWidth(), capValue(getCap()), joinValue(getJoin()), miterLimit);
         }
      }
      else
      {
         return new BasicStroke(getWidth(), capValue(getCap()), joinValue(getJoin()), getMiterlimit(), ITextUtils.stringToFloatArray(getDash()), getDashPhase());
      }
   }

}
