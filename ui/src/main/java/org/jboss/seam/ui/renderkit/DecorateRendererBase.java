package org.jboss.seam.ui.renderkit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.ui.component.UIDecorate;
import org.jboss.seam.ui.util.Decoration;
import org.jboss.seam.ui.util.HTML;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class DecorateRendererBase extends RendererBase
{
   // Place the attributes you want to store away
   private Map<String, Object> originalValues = new HashMap();
   // The list of attributes in the event scope to store away
   String[] storeOriginals = new String[] {"invalid", "required"}; 
   
   @Override
   protected Class getComponentClass()
   {
      return UIDecorate.class;
   }

   /**
    * Store away the attribute from the event context (if it is set)
    * 
    * @param names The list of context keys to store away
    * @param context The context to target
    */
   private void storeOriginalValues(String[] names, Context context)
   {
      for (String name : names)
      {
         if (context.isSet(name))
         {
            originalValues.put(name, context.get(name));
         }
      }
   }
   
   /**
    * Restores the state of the event context. If the value is stored away, it is restored
    * It it was not in the map, it was not in the context in the first place so clean
    * up what we have placed there during this run.
    * 
    * @param names The list of context keys to restore
    * @param context The context to target
    */
   private void restoreOriginalValues(String[] names, Context context) {
      for (String name : names) {
         if (originalValues.containsKey(name)) {
            context.set(name, originalValues.get(name));
         } else {
            context.remove(name);
         }
      }
   }

   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIDecorate decorate = (UIDecorate) component;

      storeOriginalValues(storeOriginals, Contexts.getEventContext());

      Contexts.getEventContext().set("invalid", Decoration.hasMessage(decorate, context));
      Contexts.getEventContext().set("required", Decoration.hasRequired(component, context));

      boolean hasMessage = decorate.hasMessage();

      if (decorate.isEnclose())
      {
          writer.startElement(decorate.getElement(), decorate);
          if (decorate.getStyleClass() != null)
          {
             writer.writeAttribute(HTML.CLASS_ATTR, decorate.getStyleClass(), HTML.CLASS_ATTR);
          }
          if (decorate.getStyle() != null)
          {
             writer.writeAttribute(HTML.STYLE_ATTR, decorate.getStyle(), HTML.STYLE_ATTR);
          }
          writer.writeAttribute("id", decorate.getClientId(context), "id");
      }

      UIComponent aroundDecoration = decorate.getDecoration("aroundField");
      UIComponent aroundInvalidDecoration = decorate.getDecoration("aroundInvalidField");
      if (aroundDecoration != null && !hasMessage)
      {
         aroundDecoration.setParent(decorate);
         aroundDecoration.encodeBegin(context);
      }
      if (aroundInvalidDecoration != null && hasMessage)
      {
         aroundInvalidDecoration.setParent(decorate);
         aroundInvalidDecoration.encodeBegin(context);
      }
   }

   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIDecorate decorate = (UIDecorate) component;

      boolean hasMessage = decorate.hasMessage();
      UIComponent aroundDecoration = decorate.getDecoration("aroundField");
      UIComponent aroundInvalidDecoration = decorate.getDecoration("aroundInvalidField");
      if (aroundDecoration != null && !hasMessage)
      {
         aroundDecoration.setParent(decorate);
         aroundDecoration.encodeEnd(context);
      }
      if (aroundInvalidDecoration != null && hasMessage)
      {
         aroundInvalidDecoration.setParent(decorate);
         aroundInvalidDecoration.encodeEnd(context);
      }
      if (decorate.isEnclose())
      {
          context.getResponseWriter().endElement(decorate.getElement());
      }

      restoreOriginalValues(storeOriginals, Contexts.getEventContext());
   }

   @Override
   protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIDecorate decorate = (UIDecorate) component;

      boolean hasMessage = decorate.hasMessage();

      UIComponent beforeDecoration = decorate.getDecoration("beforeField");
      UIComponent beforeInvalidDecoration = decorate.getDecoration("beforeInvalidField");
      if (beforeDecoration != null && !hasMessage)
      {
         beforeDecoration.setParent(decorate);
         renderChild(context, beforeDecoration);
      }
      if (beforeInvalidDecoration != null && hasMessage)
      {
         beforeInvalidDecoration.setParent(decorate);
         renderChild(context, beforeInvalidDecoration);
      }

      renderChildren(context, decorate);

      UIComponent afterDecoration = decorate.getDecoration("afterField");
      UIComponent afterInvalidDecoration = decorate.getDecoration("afterInvalidField");
      if (afterDecoration != null && !hasMessage)
      {
         afterDecoration.setParent(decorate);
         renderChild(context, afterDecoration);
      }
      if (afterInvalidDecoration != null && hasMessage)
      {
         afterInvalidDecoration.setParent(decorate);
         renderChild(context, afterInvalidDecoration);
      }
   }

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }
}