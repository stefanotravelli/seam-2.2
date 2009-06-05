package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;

/**
 * Allows the application to determine whether the JSF validation
 * phase completed successfully, or if a validation failure
 * occurred.
 * 
 * @author Gavin king
 *
 */
@Name("org.jboss.seam.faces.validation")
@BypassInterceptors
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
public class Validation
{

   private boolean succeeded;
   private boolean failed;

   public static Validation instance()
   {
      if ( !Contexts.isEventContextActive() )
      {
         throw new IllegalStateException("No active event scope");
      }
      return (Validation) Component.getInstance(Validation.class, ScopeType.EVENT);
   }
   
   public void afterProcessValidations(FacesContext facesContext)
   {
      failed = facesContext.getRenderResponse();
      if (failed)
      {
         Events.instance().raiseEvent("org.jboss.seam.validationFailed");
      }
      succeeded = !failed;
   }

   public boolean isSucceeded()
   {
      return succeeded;
   }

   public boolean isFailed()
   {
      return failed;
   }

   public void fail()
   {
      failed = true;
      succeeded = false;
   }

}
