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

import javax.el.ValueExpression;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * JSF component class
 *
 */
public abstract class UITaskId extends UIParameter {
	
	private static final String COMPONENT_TYPE = "org.jboss.seam.ui.TaskId";
	
   @Override
   public String getName()
   {
      return "taskId";
   }
   
   @Override
   public Object getValue()
   {
      ValueExpression valueExpression = getValueExpression("taskInstance");
      if (valueExpression==null) valueExpression = getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(), "#{task}", TaskInstance.class);
      TaskInstance taskInstance = (TaskInstance) valueExpression.getValue( getFacesContext().getELContext() );
      return taskInstance==null ? null : taskInstance.getId();
   }
   
   public static UITaskId newInstance() {
      return (UITaskId) FacesContext.getCurrentInstance().getApplication().createComponent(COMPONENT_TYPE);
   }
}
