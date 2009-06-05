package org.jboss.seam.ui.filter;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import javax.servlet.Filter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name("org.jboss.seam.web.ajax4jsfFilterInstantiator")
@Install(precedence = FRAMEWORK, classDependencies={"org.ajax4jsf.Filter", "org.apache.commons.digester.Digester"})
@BypassInterceptors
@Scope(ScopeType.STATELESS)
public class Ajax4jsfFilterInstantiator
{
   
   @Unwrap
   public Filter create()
   {
      return new org.ajax4jsf.Filter();
   }

}
