package org.jboss.seam.ui.facelet;

import java.io.IOException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.Renderer;

/**
 * 
 * Implementation of Renderer using Facelets
 * 
 * Especially useful for sending email using Seam Mail
 * 
 * @author Pete Muir
 * @author Norman Richards
 *
 */

@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Name("org.jboss.seam.faces.renderer")
@AutoCreate
@Install(value = true, precedence = Install.BUILT_IN, classDependencies="com.sun.facelets.Facelet")
public class FaceletsRenderer extends Renderer
{
   
   
   /**
    * Render the viewId, anything written to the JSF ResponseWriter is
    * returned 
    */
   @Override
   public String render(final String viewId) 
   {
      RendererRequest rendererRequest = new RendererRequest(viewId);
      try
      {
         rendererRequest.run();
      }
      catch (IOException e)
      {
         throw new RuntimeException("error rendering " + viewId, e);
      }
      return rendererRequest.getOutput();
   }
   
}
