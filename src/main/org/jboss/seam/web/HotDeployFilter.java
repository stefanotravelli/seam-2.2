package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.core.Init;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

@Name("org.jboss.seam.web.hotDeployFilter")
@Install(debug=true, precedence=BUILT_IN)
@BypassInterceptors
@Scope(APPLICATION)
@Filter
public class HotDeployFilter extends AbstractFilter
{

   private static LogProvider log = Logging.getLogProvider(HotDeployFilter.class);
   
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
   {
      if (request instanceof HttpServletRequest)
      {
         Init init = (Init) getServletContext().getAttribute(Seam.getComponentName(Init.class));
         if (init != null)
         {
            try
            {
               new Initialization(getServletContext()).redeploy((HttpServletRequest) request, init);
            }
            catch (InterruptedException e)
            {
               log.warn("Unable to redeploy, please try again");
            }
         }
      }
      chain.doFilter(request, response);
   }



}
