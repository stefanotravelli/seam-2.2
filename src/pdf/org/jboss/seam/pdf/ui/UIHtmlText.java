package org.jboss.seam.pdf.ui;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.faces.FacesException;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;

import org.jboss.seam.ui.util.JSF;

import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;

public class UIHtmlText extends ITextComponent implements ValueHolder
{

   private Converter converter;
   private Object localValue;

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }

   @Override
   public void encodeChildren(FacesContext context) throws IOException
   {
      ResponseWriter writer = context.getResponseWriter();

      StringWriter stringWriter = new StringWriter();
      ResponseWriter cachingResponseWriter = writer.cloneWithWriter(stringWriter);
      context.setResponseWriter(cachingResponseWriter);
      JSF.renderChildren(context, this);
      context.setResponseWriter(writer);

      String output = stringWriter.getBuffer().toString();
      addFromHtml(output);
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      Object value = getValue();
      if (value != null)
      {
         addFromHtml(convert(context, value));
      }

      super.encodeEnd(context);
   }

   private void addFromHtml(String html) throws IOException
   {
      for (Object o : HTMLWorker.parseToList(new StringReader(html), getStyle()))
      {
         addToITextParent(o);
      }

      // paragraph.addAll(HTMLWorker.parseToList(new StringReader(html),
      // getStyle()));
   }

   /**
    * XXX - this needs some work
    */
   private StyleSheet getStyle()
   {
      StyleSheet styles = new StyleSheet();
      styles.loadTagStyle("body", "leading", "16,0");
      return styles;
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

   }

   @Override
   public Object getITextObject()
   {
      return null;
   }

   @Override
   public void removeITextObject()
   {
   }

   @Override
   public void handleAdd(Object other)
   {
      throw new RuntimeException("illegal child element");
   }

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
