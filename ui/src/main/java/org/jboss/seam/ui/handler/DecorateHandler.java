package org.jboss.seam.ui.handler;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;

public class DecorateHandler extends ComponentHandler
{
   private com.sun.facelets.tag.ui.DecorateHandler delegate;

   public DecorateHandler(ComponentConfig config)
   {
      super(config);
      if ( tag.getAttributes().get("template")!=null )
      {
         delegate = new com.sun.facelets.tag.ui.DecorateHandler(config);
      }
   }
   
   @Override
   protected void applyNextHandler(FaceletContext context, UIComponent component) 
      throws IOException, FacesException, ELException
   {
      if ( tag.getAttributes().get("template")!=null )
      {
         delegate.apply(context, component);
      }
      else
      {
         nextHandler.apply(context, component);
      }
   }

}
