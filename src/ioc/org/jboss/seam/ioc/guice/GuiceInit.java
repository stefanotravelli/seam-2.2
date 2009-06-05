package org.jboss.seam.ioc.guice;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import com.google.inject.Injector;

/**
 * Guice support configuration component. Holds reference to the default
 * injector.
 * 
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 */
@Name("org.jboss.seam.ioc.guice.init")
@Scope(ScopeType.APPLICATION)
@Startup
@Install(precedence = FRAMEWORK, classDependencies = "com.google.inject.Injector")
@BypassInterceptors
public class GuiceInit implements Serializable
{
   private static final long serialVersionUID = -1517814449129434488L;
   
   private Injector injector;
   
   public Injector getInjector()
   {
      return injector;
   }
   
   public void setInjector(Injector injector)
   {
      this.injector = injector;
   }
   
   @Override
   public String toString()
   {
      return "org.jboss.seam.ioc.guice.init(" + injector + ")";
   }
}
