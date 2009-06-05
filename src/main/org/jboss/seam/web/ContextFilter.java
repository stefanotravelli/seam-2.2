package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.servlet.ContextualHttpServletRequest;

/**
 * Manages the Seam contexts associated with a request to any servlet.
 * 
 * @author Gavin King
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.web.contextFilter")
@Install(value=false, precedence = BUILT_IN)
@BypassInterceptors
@Filter(within="org.jboss.seam.web.ajax4jsfFilter")
public class ContextFilter extends AbstractFilter 
{
 
   public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) 
       throws IOException, ServletException 
   {
      new ContextualHttpServletRequest( (HttpServletRequest) request )
      {
         @Override
         public void process() throws ServletException, IOException
         {
            chain.doFilter(request, response);
         }
      }.run();
   }
}
