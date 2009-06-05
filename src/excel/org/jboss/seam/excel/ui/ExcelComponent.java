package org.jboss.seam.excel.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;

import org.jboss.seam.excel.ExcelWorkbook;
import org.jboss.seam.excel.WorksheetItem;
import org.jboss.seam.excel.ui.command.Command;
import org.jboss.seam.ui.util.JSF;

/**
 * Common superclass for the UI components. Contains helper methods for merging
 * etc.
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * @author Daniel Roth (danielc.roth@gmail.com)
 */
public abstract class ExcelComponent extends UIComponentBase
{
   private static final String DEFAULT_CONTENT_TYPE = "text/html";
   private static final String DEFAULT_CHARACTER_ENCODING = "utf-8";

   // The CSS style class
   private String styleClass;
   
   // The CSS style
   private String style;
   
   /**
    * Helper method for rendering a component (usually on a facescontext with a caching
    * reponsewriter)
    * 
    * @param facesContext The faces context to render to
    * @param component The component to render
    * @return The textual representation of the component
    * @throws IOException If the JSF helper class can't render the component
    */
   public static String cmp2String(FacesContext facesContext, UIComponent component) throws IOException
   {
      ResponseWriter oldResponseWriter = facesContext.getResponseWriter();
      String contentType = oldResponseWriter != null ? oldResponseWriter.getContentType() : DEFAULT_CONTENT_TYPE;
      String characterEncoding = oldResponseWriter != null ? oldResponseWriter.getCharacterEncoding() : DEFAULT_CHARACTER_ENCODING;
      RenderKit renderKit = facesContext.getRenderKit();
      StringWriter cacheingWriter = new StringWriter();
      ResponseWriter newResponseWriter = renderKit.createResponseWriter(cacheingWriter, contentType, characterEncoding);
      facesContext.setResponseWriter(newResponseWriter);
      JSF.renderChild(facesContext, component);
      if (oldResponseWriter != null) {
         facesContext.setResponseWriter(oldResponseWriter);
      }
      cacheingWriter.flush();
      cacheingWriter.close();
      return cacheingWriter.toString();
   }    
   
   public ExcelComponent()
   {
      super();
   }

   /**
    * Helper class that returns all children of a certain type (implements
    * interface)
    * 
    * @param <T> The type to check for
    * @param children The list of children to search
    * @param childType The child type
    * @return The list of matching items
    */
   @SuppressWarnings("unchecked")
   public static <T> List<T> getChildrenOfType(List<UIComponent> children, Class<T> childType)
   {
      List<T> matches = new ArrayList<T>();
      for (UIComponent child : children)
      {
         if (childType.isAssignableFrom(child.getClass()))
         {
            matches.add((T) child);
         }
      }
      return matches;
   }

   /**
    * Returns all commands from a child list
    * 
    * @param children The list to search
    * @return The commands
    */
   protected static List<Command> getCommands(List<UIComponent> children)
   {
      return getChildrenOfType(children, Command.class);
   }

   /**
    * Returns all worksheet items (cells, images, hyperlinks) from a child list
    * 
    * @param children The list to search
    * @return The items
    */
   protected static List<WorksheetItem> getItems(List<UIComponent> children)
   {
      return getChildrenOfType(children, WorksheetItem.class);
   }

   /**
    * Helper method for fetching value through binding
    * 
    * @param name The field to bind to
    * @param defaultValue The default value to fall back to
    * @return The field value
    */
   protected Object valueOf(String name, Object defaultValue)
   {
      Object value = defaultValue;
      if (getValueExpression(name) != null)
      {
         value = getValueExpression(name).getValue(FacesContext.getCurrentInstance().getELContext());
      }
      return value;
   }

   /**
    * Fetches the parent workbook from a component
    * 
    * @param component The component to examine
    * @return The workbook
    */
   protected ExcelWorkbook getWorkbook(UIComponent component)
   {
      if (component == null)
         return null;
      if (component instanceof UIWorkbook)
      {
         UIWorkbook uiWorkBook = (UIWorkbook) component;
         return uiWorkBook.getExcelWorkbook();
      }
      else
      {
         return getWorkbook(component.getParent());
      }
   }

   /**
    * Gets a parent of a certain class
    * 
    * @param root The root where to start searching
    * @param searchClass The class to search for
    * @return The parent, if found.
    */
   @SuppressWarnings("unchecked")
   protected UIComponent getParentByClass(UIComponent root, Class searchClass)
   {
      if (root == null)
      {
         return null;
      }
      if (root.getClass() == searchClass)
      {
         return root;
      }
      return getParentByClass(root.getParent(), searchClass);
   }

   public String getStyleClass()
   {
      return (String) valueOf("styleClass", styleClass);
   }

   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }

   public String getStyle()
   {
      return (String) valueOf("style", style);
   }

   public void setStyle(String style)
   {
      this.style = style;
   }

}
