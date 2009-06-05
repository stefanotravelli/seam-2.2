package org.jboss.seam.debug.jsf;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesManager;

@Name("org.jboss.seam.debug.jsf.debugRedirect")
@BypassInterceptors
@Install(debug = true, precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
public class DebugRedirect
{
   private String viewId;

   public String getViewId()
   {
      return viewId;
   }

   public void setViewId(String viewId)
   {
      this.viewId = viewId;
   }

   public void execute()
   {
      FacesManager.instance().redirect(viewId, null, false);
   }
}
