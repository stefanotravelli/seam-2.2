package org.jboss.seam.web;

import org.jboss.seam.ScopeType;
import org.jboss.seam.log.Logging;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides automatic addition of cache-control HTTP headers to matching resource responses.
 *
 * @author Christian Bauer
 */
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.web.cacheControlFilter")
@Install(value = false, precedence = Install.BUILT_IN)
@BypassInterceptors
@Filter(within = "org.jboss.seam.web.exceptionFilter")
public class CacheControlFilter extends AbstractFilter
{

   private static final LogProvider log = Logging.getLogProvider(CacheControlFilter.class);

   private String value;

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
         throws IOException, ServletException
   {

      HttpServletRequest httpRequest = (HttpServletRequest) request;

      if (isMappedToCurrentRequestPath(request))
      {
         log.debug("Applying Cache-Control HTTP header for resource '"
               + httpRequest.getRequestURI() + "': " + getValue());

         HttpServletResponse httpResponse = (HttpServletResponse) response;
         httpResponse.setHeader("Cache-Control", getValue());
      }

      chain.doFilter(request, response);
   }

   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }
}
