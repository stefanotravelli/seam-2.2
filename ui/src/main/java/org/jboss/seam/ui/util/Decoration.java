package org.jboss.seam.ui.util;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


public class Decoration
{

   public static boolean hasMessage(UIComponent component, FacesContext context)
   {
      
      // If the component isn't to be rendered, then ignore
      if ( !component.isRendered() ) return false;
      
      
      if ( component instanceof EditableValueHolder )
      {
         
         // If the component has failed validation, then it's invalid      
         if ( ! ( (EditableValueHolder) component ).isValid() ) return true;
         
         // If the component has a faces message attached, return true.
         // TODO enhance this to only consider ERROR and WARN messages probably
         if ( context.getMessages( component.getClientId(context) ).hasNext() ) return true; //TODO: put this outside the if???
      }

      for ( Object child: component.getChildren() )
      {
         if (child instanceof UIComponent)
         {
            boolean message = hasMessage( (UIComponent) child, context );
            if (message) return true;
         }
      }
      return false;
   }

   public static boolean hasRequired(UIComponent component, FacesContext context)
   {
      if ( !component.isRendered() ) return false;
      
      if ( component instanceof EditableValueHolder )
      {
         if (  ( (EditableValueHolder) component ).isRequired() ) return true;
      }

      for (Object child: component.getChildren())
      {
         if (child instanceof UIComponent)
         {
            boolean required = hasRequired( (UIComponent) child, context );
            if (required) return true;
         }
      }
      return false;
   }
   
   /**
    * A depth-first search for an EditableValueHolder
    */
   public static UIComponent getEditableValueHolder(UIComponent component)
   {
      for (Object child: component.getChildren())
      {
         if (child instanceof EditableValueHolder)
         {
            UIComponent evh =(UIComponent) child;
            if ( evh.isRendered() )
            {
               return evh;
            }
         }
         else if (child instanceof UIComponent)
         {
            UIComponent evh = getEditableValueHolder( (UIComponent) child );
            if (evh!=null) return evh;
         }
      }
      return null;
   }
   
   public static UIComponent getDecoration(String name, UIComponent component)
   {
      UIComponent dec = component.getFacet(name);
      if (dec!=null) return dec;
      if ( component.getParent()==null ) return null;
      return getDecoration( name, component.getParent() );
   }
   
   
}
