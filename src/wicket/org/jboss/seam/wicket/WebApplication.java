package org.jboss.seam.wicket;

import static org.jboss.seam.ScopeType.APPLICATION;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

@Name("org.jboss.seam.wicket.WebApplication")
@Scope(APPLICATION)
@Install(precedence=21, classDependencies="org.apache.wicket.Application")
@BypassInterceptors
public class WebApplication
{
   
   private String applicationClass;
   
   public String getApplicationClass()
   {
      return applicationClass;
   }
   
   public void setApplicationClass(String applicationClass)
   {
      this.applicationClass = applicationClass;
   }
   
   public static WebApplication instance()
   {
      if (Contexts.isApplicationContextActive())
      {
         return (WebApplication) Component.getInstance(WebApplication.class);
      }
      else
      {
         throw new IllegalStateException("Application context is not active");
      }
   }

}
