package org.jboss.seam.ioc.spring;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.servlet.ServletContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.ServletLifecycle;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * A seam component that loads up a spring WebApplicationContext
 * 
 * @author Mike Youngstrom
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Startup(depends="org.jboss.seam.ioc.spring.springELResolver")
@Name("org.jboss.seam.ioc.spring.contextLoader")
@Install(value = false, precedence = BUILT_IN)
public class ContextLoader
{
   private WebApplicationContext webApplicationContext;
   private String[] configLocations = { XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION };
   
   @Create 
   public void create() throws Exception
   {
      ServletContext servletContext = ServletLifecycle.getServletContext();
      try 
      {
         webApplicationContext = createContextLoader(servletContext);
         servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);
         startupContextLoader(webApplicationContext);
      } 
      catch (Exception e) 
      {
         servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, e);
         throw e;
      }
   }
   
   protected WebApplicationContext createContextLoader(ServletContext servletContext) 
   {
      XmlWebApplicationContext xmlWebApplicationContext = new XmlWebApplicationContext();
      xmlWebApplicationContext.setServletContext(servletContext);
      xmlWebApplicationContext.setConfigLocations(getConfigLocations());
      return xmlWebApplicationContext;
   }
   
   protected void startupContextLoader(WebApplicationContext webApplicationContext) 
   {
      if(webApplicationContext instanceof ConfigurableWebApplicationContext) 
      {
         ((ConfigurableWebApplicationContext) webApplicationContext).refresh();
      }
   }

   @Destroy
   public void destroy() 
   {
      if (webApplicationContext != null && webApplicationContext instanceof ConfigurableWebApplicationContext) 
      {
         ((ConfigurableWebApplicationContext) webApplicationContext).close();
      }
   }
   
   public String[] getConfigLocations()
   {
      return configLocations;
   }
   
   public void setConfigLocations(String[] configLocations)
   {
      this.configLocations = configLocations;
   }
}
