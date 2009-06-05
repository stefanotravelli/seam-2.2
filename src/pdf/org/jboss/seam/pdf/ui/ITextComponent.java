package org.jboss.seam.pdf.ui;

import java.io.IOException;
import java.io.StringWriter;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.util.JSF;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.xml.simpleparser.EntitiesToUnicode;

public abstract class ITextComponent extends UIComponentBase
{
   public static final String COMPONENT_FAMILY = "org.jboss.seam.pdf";

   protected String inFacet;
   protected Object currentFacet;

   /**
    * get the current Itext object
    */
   abstract public Object getITextObject();

   /**
    * signal that the component should create it's managed object
    * 
    * @throws IOException
    * @throws DocumentException
    */
   abstract public void createITextObject(FacesContext context) throws IOException, DocumentException;

   /**
    * remove the itext objext
    */
   abstract public void removeITextObject();

   /**
    * subcomponents should implement this to add child components to themselves
    */
   abstract public void handleAdd(Object other);

   final public void add(Object other)
   {
      if (inFacet != null)
      {
         handleFacet(inFacet, other);
      }
      else
      {
         handleAdd(other);
      }
   }

   public void handleFacet(String facetName, Object obj)
   {
      currentFacet = obj;
      // facets.put(facetName,obj);
   }

   /**
    * look up the tree for an itext font
    */
   public Font getFont()
   {
      UIFont fontComponent = (UIFont) findITextParent(this, UIFont.class);
      return fontComponent == null ? null : fontComponent.getFont();
   }

   /**
    * look up the tree for the itext document
    */
   public Document findDocument()
   {
      ITextComponent parent = findITextParent(this, UIDocument.class);
      if (parent != null)
      {
         return (Document) parent.getITextObject();
      }
      else
      {
         return null;
      }
   }

   /**
    * find the first parent that is an itext component);
    */
   public ITextComponent findITextParent(UIComponent parent)
   {
      return findITextParent(parent, null);
   }

   /**
    * find the first parent that is an itext component of a given type
    */
   public ITextComponent findITextParent(UIComponent parent, Class<?> c)
   {
      if (parent == null)
      {
         return null;
      }

      if (parent instanceof ITextComponent)
      {
         if (c == null || c.isAssignableFrom(parent.getClass()))
         {
            return (ITextComponent) parent;
         }
      }

      return findITextParent(parent.getParent(), c);
   }

   /**
    * add a component (usually the current itext object) to the itext parent's
    * itext object
    */
   public void addToITextParent(Object obj)
   {
      ITextComponent parent = findITextParent(getParent());
      if (parent != null)
      {
         parent.add(obj);
      }
      else
      {
         noITextParentFound();
      }
   }

   public void noITextParentFound()
   {
      throw new RuntimeException(
            "Couldn't find ITextComponent parent for component " + this.getClass().getName());
   }

   public Object processFacet(String facetName)
   {
      if (inFacet != null && inFacet.equals(facetName))
      {
         return null;
      }

      UIComponent facet = this.getFacet(facetName);
      Object result = null;
      if (facet != null)
      {
         currentFacet = null;
         inFacet = facetName;
         try
         {
            encode(FacesContext.getCurrentInstance(), facet);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         finally
         {
            inFacet = null;
            result = currentFacet;
            currentFacet = null;
         }
      }
      return result;
   }

   public Object valueBinding(FacesContext context, String property, Object defaultValue)
   {
      Object value = defaultValue;
      ValueExpression expression = getValueExpression(property);

      if (expression != null)
      {
         value = expression.getValue(context.getELContext());
      }
      return value;
   }

   public Object valueBinding(String property, Object defaultValue)
   {
      return valueBinding(FacesContext.getCurrentInstance(), property, defaultValue);
   }

   // ------------------------------------------------------

   @Override
   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }

   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      try
      {
         createITextObject(context);
      }
      catch (DocumentException e)
      {
         throw new FacesException(e);
      }
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      Object obj = getITextObject();
      if (obj != null)
      {
         addToITextParent(getITextObject());
      }
      removeITextObject();
   }

   @SuppressWarnings("unchecked")
   @Override
   public void encodeChildren(FacesContext context) throws IOException
   {
      for (UIComponent child : this.getChildren())
      {
         // ugly hack to be able to capture facelets text
         if (child.getFamily().equals("facelets.LiteralText"))
         {
            String text = EntitiesToUnicode.decodeString(extractText(context, child));
            Font font = getFont();
            Chunk chunk = null;
            if (font == null)
            {
               chunk = new Chunk(text);
            }
            else
            {
               chunk = new Chunk(text, getFont());
            }
            add(chunk);
         }
         else
         {
            encode(context, child);
         }
      }
   }

   public String extractText(FacesContext context, UIComponent child) throws IOException
   {
      ResponseWriter response = context.getResponseWriter();
      StringWriter stringWriter = new StringWriter();
      ResponseWriter cachingResponseWriter = response.cloneWithWriter(stringWriter);
      context.setResponseWriter(cachingResponseWriter);

      JSF.renderChild(context, child);

      context.setResponseWriter(response);

      return stringWriter.getBuffer().toString();
   }

   @SuppressWarnings("unchecked")
   public void encode(FacesContext context, UIComponent component) throws IOException
   {
      if (!component.isRendered())
      {
         return;
      }

      component.encodeBegin(context);

      if (component.getChildCount() > 0)
      {
         if (component.getRendersChildren())
         {
            component.encodeChildren(context);
         }
         else
         {
            for (UIComponent child : component.getChildren())
            {
               encode(context, child);
            }
         }
      }

      component.encodeEnd(context);
   }
}
