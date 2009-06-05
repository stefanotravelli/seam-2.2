package org.jboss.seam.ioc.spring;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.ServletContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.el.EL;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Resolver patterned after the Spring DelegatingVariableResolver providing el access
 * to Spring beans in non Faces Requests.
 * @author youngstrommj
 *
 */
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.ioc.spring.springELResolver")
@Install(precedence = Install.FRAMEWORK, classDependencies="org.springframework.beans.factory.BeanFactory")
@Startup
@BypassInterceptors
public class SpringELResolver extends ELResolver
{
   private static final LogProvider log = Logging.getLogProvider(SpringELResolver.class);

   @Create
   public void initialize()
   {
      ELResolver resolver = EL.EL_RESOLVER;
      if (resolver == null || !(resolver instanceof CompositeELResolver))
      {
         throw new IllegalStateException("Could not add Spring ELResolver to Resolver Chain.  "
                  + "Seam resolver was not an instance of CompositeELResolver.");
      }
      ((CompositeELResolver) resolver).add(this);
   }

   @Override
   public Class<?> getCommonPropertyType(ELContext context, Object base)
   {
      return null;
   }

   @Override
   public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
   {
      return null;
   }

   @Override
   public Class<?> getType(ELContext context, Object base, Object property)
   {
      return null;
   }

   @Override
   public Object getValue(ELContext context, Object base, Object property)
   {
      if (base != null)
      {
         // We only resolve root variable.
         return null;
      }
      ServletContext servletContext = ServletLifecycle.getServletContext();
      if (servletContext == null)
      {
         log.debug("Could not locate seam stored servletContext.  Skipping.");
         return null;
      }
      BeanFactory bf = getBeanFactory(servletContext);
      if (bf == null)
      {
         log.debug("No Spring BeanFactory found.  Skipping.");
         return null;
      }
      if (!(property instanceof String))
      {
         log.debug("Property not a string.  Skipping");
         return null;
      }

      if (bf.containsBean((String) property))
      {
         if (log.isDebugEnabled())
         {
            log.debug("Successfully resolved property '" + property
                     + "' in root WebApplicationContext");
         }
         context.setPropertyResolved(true);
         return bf.getBean((String) property);
      }
      if (log.isDebugEnabled())
      {
         log.debug("Could not resolve property of name '" + property + "'");
      }
      return null;

   }

   @Override
   public boolean isReadOnly(ELContext context, Object base, Object property)
   {
      return true;
   }

   @Override
   public void setValue(ELContext context, Object base, Object property, Object value)
   {
   }

   /**
    * Obtain the BeanFactory using
    * WebApplicationContextxUtils.getWebApplicationContext
    */
   protected BeanFactory getBeanFactory(ServletContext servletContext)
   {
      return WebApplicationContextUtils.getWebApplicationContext(servletContext);
   }
}
