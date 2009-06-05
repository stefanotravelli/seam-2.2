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

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.navigation.Page;
import org.jboss.seam.navigation.Pages;


/**
 * JSF component class
 *
 */
public abstract class UIConversationId extends UIParameter {
	
	private static final String COMPONENT_TYPE = "org.jboss.seam.ui.ConversationId";   
   
   @Override
   public String getName()
   {
      Conversation conversation = Conversation.instance();
      if (getViewId()!=null && ( !conversation.isNested() || conversation.isLongRunning() ) )
      {
         return Pages.instance().getPage(getViewId())
                     .getConversationIdParameter()
                     .getParameterName();
      }
      else
      {
         return Manager.instance().getConversationIdParameter();
      }
   }
   
   @Override
   public Object getValue()
   {
      Conversation conversation = Conversation.instance();
      if ( !conversation.isNested() || conversation.isLongRunning() )
      {
         if (getViewId()!=null)
         {
            Page page = Pages.instance().getPage(getViewId());
            return page.getConversationIdParameter().getParameterValue();
         }
         else
         {
            return conversation.getId();
         }
      }
      else
      {
         return conversation.getParentId();
      }
   }

   public abstract String getViewId();

   public abstract void setViewId(String viewId);
   
   public static UIConversationId newInstance() {
      return (UIConversationId) FacesContext.getCurrentInstance().getApplication().createComponent(COMPONENT_TYPE);
   }
}
