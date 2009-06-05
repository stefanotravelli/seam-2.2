package org.jboss.seam.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;

/**
 * A filter that provides integration between Servlet Security and the Seam
 * identity component. This integration is accomplished by wrapping the
 * HttpServletRequest with an HttpServletRequestWrapper implementation that
 * delegates security-related calls to the Seam identity component.
 *
 * @author Dan Allen
 */
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.web.identityFilter")
@Install(precedence = Install.BUILT_IN, dependencies = "org.jboss.seam.security.identity")
@BypassInterceptors
@Filter(within = {"org.jboss.seam.web.multipartFilter"})
public class IdentityFilter extends AbstractFilter {

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
      if (!(request instanceof HttpServletRequest)) {
         throw new ServletException("This filter can only process HttpServletRequest requests");
      }

      HttpServletRequest httpRequest = (HttpServletRequest) request;
      chain.doFilter(new IdentityRequestWrapper(httpRequest), response);
   }
}
