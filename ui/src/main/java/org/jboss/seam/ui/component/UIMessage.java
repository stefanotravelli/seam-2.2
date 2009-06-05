/**
 * License Agreement.
 *
 * Ajax4jsf 1.1 - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.jboss.seam.ui.component;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlMessage;

/**
 * JSF component class
 *
 */
public abstract class UIMessage extends HtmlMessage {

   /**
    * A depth-first search for an EditableValueHolder
    */
   protected static UIComponent getEditableValueHolder(UIComponent component)
   {
      if (component instanceof EditableValueHolder)
      {
         return component.isRendered() ? component : null;
      }
      for (Object child: component.getChildren())
      {
         if (child instanceof UIComponent)
         {
            UIComponent evh = getEditableValueHolder( (UIComponent) child );
            if (evh!=null) return evh;
         }
      }
      return null;
   }

   private static String getInputId(UIComponent cmp)
   {
      String forId = cmp instanceof UIDecorate ?
               ( (UIDecorate) cmp ).getFor() : null;
      if (forId==null)
      {
         UIComponent evh = getEditableValueHolder(cmp);
         return evh==null ? null : evh.getId();
      }
      else
      {
         return forId;
      }
   }
   
   private static String getFor(UIComponent component)
   {
      
      if ( component.getParent()==null )
      {
         return null;
      }
      else if (component instanceof UIDecorate) 
      {
         return getInputId(component);
      }
      else
      {
         return getFor( component.getParent() );
      }
   }

   @Override
   public String getFor()
   {
      return getFor(this);
   }
   
}
