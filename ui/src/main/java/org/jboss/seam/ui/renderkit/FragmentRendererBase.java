package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UIFragment;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class FragmentRendererBase extends RendererBase
{
   @Override
   protected Class getComponentClass()
   {
      return UIFragment.class;
   }
   
   @Override
   public boolean getRendersChildren()
   {
      return true;
   }
   
   @Override
   protected void doEncodeChildren(ResponseWriter writer, FacesContext facesContext, UIComponent component) throws IOException
   {
      renderChildren(facesContext, component);
   }

}
