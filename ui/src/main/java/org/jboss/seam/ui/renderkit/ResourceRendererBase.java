package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.jboss.seam.ui.DocumentStoreUtils;
import org.jboss.seam.ui.component.UIResource;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class ResourceRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIResource.class;
   }

   @Override
   protected void doEncodeBegin(javax.faces.context.ResponseWriter writer, FacesContext ctx, UIComponent component) throws IOException
   {

      UIResource resource = (UIResource) component;

      String url = DocumentStoreUtils.addResourceToDataStore(ctx, resource);

      ctx.getExternalContext().redirect(url);

   }



}
