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

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;


/**
 * Mangled version of Ajax4Jsf's RendererBase without supporting classes
 * 
 * Base Renderer for all chameleon Skin's and components.
 * At most, make all common procedures and realise concrete work in "template" methods. 
 * @author asmirnov@exadel.com (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 */
public abstract class RendererBase extends Renderer {
	
	/**
	 * logger for common cases. 
	 */
	protected static final String JAVASCRIPT_NAMESPACE = "JBossSeam";
   private static final RendererUtils utils = new RendererUtils();

	@Override
   public void decode(FacesContext context, UIComponent component) {
		// Test for correct parameters.
        if (context == null) throw new NullPointerException("Context must not be null");
        if (component == null) throw new NullPointerException("Component must not be null");
        if (! getComponentClass().isInstance(component)) throw new IllegalArgumentException("Component must be of type " + getComponentClass().getName());	
		preDecode(context,component);
        // TODO - create set od common decoders ( UIInput, ActionSource etc. ) for process decoding.
        if (component.isRendered()) {
			doDecode(context, component);
		}
	}

	protected void preDecode(FacesContext context, UIComponent component)   {
	}

	protected void preEncodeBegin(FacesContext context, UIComponent component) throws IOException  {
		
	}

   @Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		// Test for correct parameters.
      if (context == null) throw new NullPointerException("Context must not be null");
      if (component == null) throw new NullPointerException("Component must not be null");
      if (! getComponentClass().isInstance(component)) throw new IllegalArgumentException("Component must be of type " + getComponentClass().getName());
		preEncodeBegin(context,component);
        if (component.isRendered()) {
			ResponseWriter writer = context.getResponseWriter();
			doEncodeBegin(writer, context, component);
		}
	}

   @Override
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		// Test for correct parameters.
      if (context == null) throw new NullPointerException("Context must not be null");
      if (component == null) throw new NullPointerException("Component must not be null");
      if (! getComponentClass().isInstance(component)) throw new IllegalArgumentException("Component must be of type " + getComponentClass().getName());
		preEncodeBegin(context,component);
        if (component.isRendered()) {
			ResponseWriter writer = context.getResponseWriter();
			doEncodeChildren(writer, context, component);
		}
	}

   @Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		// Test for correct parameters.
      if (context == null) throw new NullPointerException("Context must not be null");
      if (component == null) throw new NullPointerException("Component must not be null");
      if (! getComponentClass().isInstance(component)) throw new IllegalArgumentException("Component must be of type " + getComponentClass().getName());
        if (component.isRendered()) {
			ResponseWriter writer = context.getResponseWriter();
			doEncodeEnd(writer, context, component);
		}
	}

	/**
	 * Get base component slass , targetted for this renderer. Used for check arguments in decode/encode.
	 */
	protected abstract Class getComponentClass();


	/**
	 * Template method for custom decoding of concrete renderer.
	 * All parameters checking if performed in original decode() method.
	 * @param context
	 * @param component
	 */
	protected void doDecode(FacesContext context, UIComponent component) {
		
	}

	/**
	 * Template method for custom start encoding of concrete renderer.
	 * All parameters checking and writer is performed in original encodeBegin() method.
	 * @param writer
	 * @param context
	 * @param component
	 */
	protected void doEncodeBegin(ResponseWriter writer,FacesContext context, UIComponent component) throws IOException {
		
	}
	/**
	 * @param writer
	 * @param context
	 * @param component
	 */
	protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException  {
		// Hook method, must be overriden in renderers with special children processing
	}

	/**
	 * Template method for custom finish encoding of concrete renderer.
	 * All parameters checking and writer is performed in original encodeEnd() method.
	 * @param writer
	 * @param context
	 * @param component
	 * @throws IOException 
	 */
	protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
		
	}

    /**
     * Render all children for given component.
     * @param facesContext
     * @param component
     * @throws IOException
     */
    public void renderChildren(FacesContext facesContext,
			UIComponent component) throws IOException {
		if (component.getChildCount() > 0) {
			for (Iterator it = component.getChildren().iterator(); it.hasNext();) {
				UIComponent child = (UIComponent) it.next();
				renderChild(facesContext, child);
			}
		}
	}


	/**
	 * Render one component and it childrens
	 * @param facesContext
	 * @param child
	 * @throws IOException
	 */
	public void renderChild(FacesContext facesContext, UIComponent child)
			throws IOException {
		if (!child.isRendered()) {
			return;
		}

		child.encodeBegin(facesContext);
		if (child.getRendersChildren()) {
			child.encodeChildren(facesContext);
		} else {
			renderChildren(facesContext, child);
		}
		child.encodeEnd(facesContext);
	}
   
   public static RendererUtils getUtils()
   {
      return utils;
   }

}
