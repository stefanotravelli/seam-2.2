package org.jboss.seam.wicket.web;

import static org.jboss.seam.annotations.Install.BUILT_IN;
import static org.jboss.seam.util.Resources.getRealFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.wicket.protocol.http.WicketFilter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ServletApplicationMap;
import org.jboss.seam.web.FilterConfigWrapper;
import org.jboss.seam.wicket.WebApplication;
import org.jboss.seam.wicket.ioc.WicketClassLoader;

/**
 * This component wraps the WicketFilter, encapsulates the class dependencies 
 * upon the wicket library, and propogates the configuration from seam's own
 * WicketFilter, specified with web:wicket-wilter.  It also sets up the WicketClassLoader
 * to instrument wicket components on the fly, if necessary.
 *
 */
@Name("org.jboss.seam.wicket.web.wicketFilterInstantiator")
@Install(precedence = BUILT_IN, classDependencies={"org.apache.wicket.Application"})
@BypassInterceptors
@Scope(ScopeType.STATELESS)
public class WicketFilterInstantiator
{
   
   public static String DEFAULT_WICKET_COMPONENT_DIRECTORY_PATH = "WEB-INF/wicket";
   private static LogProvider log = Logging.getLogProvider(WicketFilterInstantiator.class);
   
   @Unwrap
   public Filter unrwap()
   {
      return new WicketFilter()
      {
         private ClassLoader classLoader;
         
         @Override
         public void init(FilterConfig filterConfig) throws ServletException
         {
            try 
            { 
               // We need the Application context active in order to lookup the WebApplication component
               Lifecycle.setupApplication(new ServletApplicationMap(filterConfig.getServletContext()));
               Map<String, String> parameters = new HashMap<String, String>();
               try
               {
                  ClassLoader parent = Thread.currentThread().getContextClassLoader();
                  
                  /* if there is a directory specified for which we should instrument wicket components,
                   * create a WicketClassLoader to do that.  Otherwise, just use the contextClassLoader
                   */
                  File dir = getRealFile(filterConfig.getServletContext(), DEFAULT_WICKET_COMPONENT_DIRECTORY_PATH);
                  if (dir == null)
                  {
                     log.warn("No wicket components directory specified to give Seam super powers to");
                     this.classLoader = parent;
                  }
                  else
                  {
                     this.classLoader = new WicketClassLoader(Thread.currentThread().getContextClassLoader(), new ClassPool(), dir).instrument();
                  }
               }
               catch (NotFoundException e)
               {
                  throw new ServletException(e);
               }
               catch (CannotCompileException e)
               {
                  throw new ServletException(e);
               }
               catch (ClassNotFoundException e)
               {
                  throw new ServletException(e);
               }
               /*
                * If no applicationClassName has been specified on the filter, use the name specified with 
                * <wicket:web-application/> in components.xml
                */
               if (filterConfig.getInitParameter("applicationClassName") == null)
               {
                  String applicationClass = WebApplication.instance().getApplicationClass();
                  if (applicationClass != null)
                  {
                     parameters.put("applicationClassName", applicationClass); 
                  }
                  else
                  {
                     throw new IllegalStateException("Must set application-class using <wicket:web-application /> in components.xml");
                  }
               }
               super.init(new FilterConfigWrapper(filterConfig, parameters));
            }
            finally 
            { 
               Lifecycle.cleanupApplication();
            }
         }

         @Override
         protected ClassLoader getClassLoader()
         {
            return classLoader;
         }
         
      };
   }

}
