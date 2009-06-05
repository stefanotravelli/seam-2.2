package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

/**
 * Manager component for the current locale. This base
 * implementation simply returns the server default 
 * locale.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.STATELESS)
@Name("org.jboss.seam.core.locale")
@Install(precedence=BUILT_IN)
@BypassInterceptors
public class Locale 
{

   @Unwrap
   public java.util.Locale getLocale()
   {
      return java.util.Locale.getDefault();
   }
     
   public static java.util.Locale instance()
   {
       if ( Contexts.isApplicationContextActive() ) 
       {
          return (java.util.Locale) Component.getInstance(Locale.class, ScopeType.STATELESS);
       } 
       else 
       {
          return java.util.Locale.getDefault(); //for unit tests
       }
   }
}