package org.jboss.seam.ui.handler;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;

import static org.jboss.seam.ui.util.cdk.RendererBase.getUtils;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;

/**
 * If user didn't specify id on button, do it for them (we need one rendered)
 * 
 * @author Pete Muir
 *
 */
public class CommandButtonParameterComponentHandler extends ComponentHandler
{
   
   public CommandButtonParameterComponentHandler(ComponentConfig config)
   {
      super(config);
   }
   
   @Override
   protected void onComponentCreated(FaceletContext ctx, UIComponent c,
         UIComponent parent)
   {
      if (getUtils().isCommandButton(parent) && parent.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
      {
         parent.setId("seam" + parent.getId());
      }
   }

}
