package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;

/**
 * A Seam filter component wrapper for the Ajax4JSF.
 * This class exists to allow the Ajax4JSF filter to
 * be configured in the web: namespace. The subclass
 * does the actual work.
 * 
 * @see org.jboss.seam.ui.filter.Ajax4jsfFilterInstantiator
 * @author Pete Muir
 * 
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.web.ajax4jsfFilter")
@Install(precedence = BUILT_IN, dependencies="org.jboss.seam.web.ajax4jsfFilterInstantiator")
@BypassInterceptors
@Filter
public class Ajax4jsfFilter extends AbstractFilter
{
   
   private javax.servlet.Filter delegate;
   
   private String forceParser;
   private String enableCache;
   private String log4jInitFile;
   
   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
   {
      if (delegate==null)
      {
         chain.doFilter(servletRequest, servletResponse);
      }
      else
      {
         delegate.doFilter(servletRequest, servletResponse, chain);
      }
   }
   
   @Override
   public void init(FilterConfig filterConfig) throws ServletException
   {  
      super.init(filterConfig);
      
      delegate = (javax.servlet.Filter) Component.getInstance("org.jboss.seam.web.ajax4jsfFilterInstantiator", ScopeType.STATELESS);
      if (delegate!=null)
      {
         Map<String, String> parameters = new HashMap<String, String>();
         if ( getForceParser() != null )
         {
            parameters.put( "forceparser", getForceParser() );
         }
         if ( getEnableCache() != null )
         {
            parameters.put( "enable-cache", getEnableCache() );
         }
         if ( getLog4jInitFile() != null )
         {
            parameters.put( "log4j-init-file", getLog4jInitFile() );
         }
      
         delegate.init( new FilterConfigWrapper(filterConfig, parameters) );
      }
   }
   
   public String getEnableCache()
   {
      return enableCache;
   }

   public void setEnableCache(String enableCache)
   {
      this.enableCache = enableCache;
   }

   public String getForceParser()
   {
      return forceParser;
   }

   public void setForceParser(String forceParser)
   {
      this.forceParser = forceParser;
   }
   
   public String getForceparser()
   {
      return forceParser;
   }
   
   public void setForceparser(String forceParser)
   {
      this.forceParser = forceParser;
   }

   public String getLog4jInitFile()
   {
      return log4jInitFile;
   }

   public void setLog4jInitFile(String log4jInitFile)
   {
      this.log4jInitFile = log4jInitFile;
   }
   
}
