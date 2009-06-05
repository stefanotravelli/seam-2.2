package org.jboss.seam.ui.util.cdk;

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

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;

/**
 * Simple method binding for constant action outcome.
 * @author asmirnov@exadel.com (latest modification by $Author$)
 *
 */
@Deprecated
public class SimpleActionMethodBinding extends MethodBinding implements StateHolder {
	// private static final Log log =
	// LogFactory.getLog(SimpleActionMethodBinding.class);

	private String _outcome;

	public SimpleActionMethodBinding(String outcome) {
		_outcome = outcome;
	}

   @Override
	public Object invoke(FacesContext facescontext, Object aobj[])
			throws EvaluationException, MethodNotFoundException {
		return _outcome;
	}
   
   @Override
	public Class getType(FacesContext facescontext)
			throws MethodNotFoundException {
		return String.class;
	}

	// ~ StateHolder support
	// ----------------------------------------------------------------------------

	private boolean _transient = false;

	/**
	 * Empty constructor, so that new instances can be created when restoring
	 * state.
	 */
	public SimpleActionMethodBinding() {
		_outcome = null;
	}

	public Object saveState(FacesContext facescontext) {
		return _outcome;
	}

	public void restoreState(FacesContext facescontext, Object obj) {
		_outcome = (String) obj;
	}

	public boolean isTransient() {
		return _transient;
	}

	public void setTransient(boolean flag) {
		_transient = flag;
	}

	@Override
   public String toString() {
		return _outcome;
	}
   
   @Override
   public String getExpressionString()
   {
      return _outcome;
   }
}

