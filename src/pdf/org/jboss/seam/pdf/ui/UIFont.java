package org.jboss.seam.pdf.ui;

import javax.faces.context.*;
import org.jboss.seam.pdf.ITextUtils;
import com.lowagie.text.*;

public class UIFont extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIFont";

   Font font;

   String name;
   String encoding;
   int size = Font.UNDEFINED;
   String style;
   String color;
   boolean embedded = false;

   public String getName()
   {
      return (String) valueBinding("name", name);
   }

   public void setFamily(String name)
   {
      this.name = name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getEncoding()
   {
      return (String) valueBinding("encoding", encoding);
   }

   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   public int getSize()
   {
      return (Integer) valueBinding("size", size);
   }

   public void setSize(int size)
   {
      this.size = size;
   }

   public void setStyle(String style)
   {
      this.style = style;
   }

   public String getStyle()
   {
      return (String) valueBinding("style", style);
   }

   public String getColor()
   {
      return (String) valueBinding("color", color);
   }

   public void setColor(String color)
   {
      this.color = color;
   }
      
   public boolean getEmbedded() {
      return (Boolean) valueBinding("embedded", embedded);
   }
   
   public void setEmbedded(boolean embedded) {
      this.embedded = embedded;
   }

   @Override
   public Font getFont()
   {
      return font;
   }

   @Override
   public Object getITextObject()
   {
      return null; // we don't add to this component, so skip
   }

   @Override
   public void removeITextObject()
   {
      font = null;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      if (encoding == null) {
         font = FontFactory.getFont(getName(), getSize());
      } else {
         font = FontFactory.getFont(getName(), getEncoding(), getEmbedded(), getSize());
      }
      
      if (getStyle() != null) {
         font.setStyle(getStyle());
      }
      
      if (getColor() != null) {
         font.setColor(ITextUtils.colorValue(getColor()));
      }
   }

   @Override
   public void handleAdd(Object o)
   {
      addToITextParent(o);
   }

}
