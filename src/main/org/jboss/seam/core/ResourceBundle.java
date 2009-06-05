package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Manager component for the Seam resource bundle
 * 
 * @see SeamResourceBundle
 * 
 * @author Gavin King
 */
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Name("org.jboss.seam.core.resourceBundle")
@Install(precedence=BUILT_IN)
public class ResourceBundle 
{
   @Unwrap
   public java.util.ResourceBundle getResourceBundle()
   {
      return SeamResourceBundle.getBundle();
   }
   
   /**
    * @return the ResourceBundle instance
    */
   public static java.util.ResourceBundle instance()
   {
      return (java.util.ResourceBundle) Component.getInstance(ResourceBundle.class);
   }
   
}
