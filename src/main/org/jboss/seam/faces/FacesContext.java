//$Id$
package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Support for injecting the JSF FacesContext object
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.faces.facesContext")
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
public class FacesContext
{
   @Unwrap
   public javax.faces.context.FacesContext getContext()
   {
      return javax.faces.context.FacesContext.getCurrentInstance();
   }
}
