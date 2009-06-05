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

import org.jboss.seam.navigation.Pages;
import org.jboss.seam.navigation.SafeActions;

/*
 * This is a support component and is not processed by the CDK (doesn't appear in faces-config)
 *
 */
public class UIAction extends UIParameter 
{
	
	private static final String COMPONENT_FAMILY = "org.jboss.seam.ui.Action";
   
   @Override
   public String getFamily()
   {
     return COMPONENT_FAMILY;
   }
   
   private String action;
   
   public String getAction() 
   {
      return action;
   }
   
   public void setAction(String action)
   {
      this.action = action;
   }
   
   private boolean isMethodBinding()
   {
      return getAction().startsWith("#{");
   }

   @Override
   public String getName()
   {
      return isMethodBinding() ? "actionMethod" : "actionOutcome";
   }
   
   @Override
   public Object getValue()
   {
      String viewId = Pages.getCurrentViewId();
      if ( isMethodBinding() )
      {
         String actionId = SafeActions.toActionId( viewId, getAction() );
         SafeActions.instance().addSafeAction(actionId);
         return actionId;
      }
      else
      {
         return getAction();
      }
   }
   
   @Override
   public void restoreState(FacesContext context, Object state) {
      Object[] values = (Object[]) state;
      super.restoreState(context, values[0]);
      action = (String) values[1];
   }

   @Override
   public Object saveState(FacesContext context) {
      Object[] values = new Object[2];
      values[0] = super.saveState(context);
      values[1] = action;
      return values;
   }
	
}
