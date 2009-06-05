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

package org.jboss.seam.ui.util.cdk;

import static org.jboss.seam.util.Reflections.isInstanceOf;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.html.HtmlCommandButton;


public class RendererUtils 
{
   
   private static final String TRINIDAD_FORM_FAMILY = "org.apache.myfaces.trinidad.Form";
   private static final String TRINIDAD_COMMANDBUTTON_CLASS = "org.apache.myfaces.trinidad.component.core.nav.CoreCommandButton";
   private static final String RICHFACES_COMMANDBUTTON_CLASS = "org.ajax4jsf.component.UIAjaxCommandButton";

   /**
    * Since Trinidad, and possibly other JSF implementations don't always subclass
    * from {@link javax.faces.component.UIForm} we can't cast to UIForm.
    */
   public UIComponent getForm(UIComponent component) 
   {
       while (component != null) 
       {
          if (isForm(component)) 
          {
             break;
          }
          component = component.getParent();
       }
       return component;
   }
   
   public boolean isCommandButton(UIComponent cmp)
   {
      if ( cmp instanceof HtmlCommandButton || isInstanceOf(cmp.getClass(), TRINIDAD_COMMANDBUTTON_CLASS) || isInstanceOf(cmp.getClass(), RICHFACES_COMMANDBUTTON_CLASS) )
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
   public boolean isForm(UIComponent cmp)
   {
      if ( cmp instanceof UIForm || TRINIDAD_FORM_FAMILY.equals(cmp.getFamily()) ) 
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
}