//$Id$
package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

/**
 * Convenient HTTP errors
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.faces.httpError")
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
public class HttpError
{
   /**
    * Send a HTTP error as the response
    */
   public void send(int code)
   {
      try
      {
         getResponse().sendError(code);
      }
      catch (IOException ioe)
      {
         throw new IllegalStateException(ioe);
      }
      FacesContext.getCurrentInstance().responseComplete();
   }

   /**
    * Send a HTTP error as the response
    */
   public void send(int code, String message)
   {
      try
      {
         getResponse().sendError(code, message);
      }
      catch (IOException ioe)
      {
         throw new IllegalStateException(ioe);
      }
      FacesContext.getCurrentInstance().responseComplete();
   }

   private static HttpServletResponse getResponse()
   {
      return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
   }

   public static HttpError instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application scope");
      }
      return (HttpError) Component.getInstance(HttpError.class, ScopeType.APPLICATION);
   }
   
}
