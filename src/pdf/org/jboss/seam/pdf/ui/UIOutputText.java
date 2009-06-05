package org.jboss.seam.pdf.ui;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.lowagie.text.Chunk;

public class UIOutputText extends ITextComponent implements ValueHolder

{
   Chunk chunk;

   Converter converter;
   Object value;
   Object localValue;

   // -- ITextComponent methods
   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      chunk.append(convert(context, getValue()));

      super.encodeEnd(context);
   }

   protected String convert(FacesContext context, Object value)
   {
      Converter myConverter = converterForValue(context, value);
      if (myConverter != null)
      {
         return myConverter.getAsString(context, this, value);
      }
      else if (value != null)
      {
         return value.toString();
      }
      else
      {
         return "";
      }
   }

   protected Converter converterForValue(FacesContext ctx, Object value)
   {
      if (converter != null)
      {
         return converter;
      }

      if (value != null)
      {
         try
         {
            return ctx.getApplication().createConverter(value.getClass());
         }
         catch (FacesException e)
         {
            // no converter defined - no problem
         }
      }

      return null;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      com.lowagie.text.Font font = getFont();

      if (font == null)
      {
         chunk = new Chunk("");
      }
      else
      {
         chunk = new Chunk("", getFont());
      }
   }

   @Override
   public Object getITextObject()
   {
      return chunk;
   }

   @Override
   public void handleAdd(Object other)
   {
      throw new RuntimeException("illegal child element");
   }

   @Override
   public void removeITextObject()
   {
      chunk = null;
   }

   // -- ValueHolder methods

   public Converter getConverter()
   {
      return converter;
   }

   public void setConverter(Converter converter)
   {
      this.converter = converter;
   }

   public Object getValue()
   {
      return valueBinding(FacesContext.getCurrentInstance(), "value", localValue);
   }

   public void setValue(Object value)
   {
      this.localValue = value;
   }

   public Object getLocalValue()
   {
      return localValue;
   }

}
