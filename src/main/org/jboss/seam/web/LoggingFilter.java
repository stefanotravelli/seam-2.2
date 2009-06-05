package org.jboss.seam.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.MDC;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.Credentials;

/**
 * This filter adds the authenticated user name to the log4j
 * mapped diagnostic context so that it can be included in
 * formatted log output if desired, by adding %X{username}
 * to the pattern.
 * 
 * @author Eric Trautman
 */
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.web.loggingFilter")
@BypassInterceptors
@Filter(within="org.jboss.seam.web.authenticationFilter")
@Install(classDependencies="org.apache.log4j.Logger", 
         dependencies="org.jboss.seam.security.identity",
         precedence=Install.BUILT_IN)
public class LoggingFilter extends AbstractFilter 
{
   
   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
         throws IOException, ServletException 
   {
   
      HttpSession session = ((HttpServletRequest) servletRequest).getSession(false);
      if (session!=null)
      {
         Object attribute = session.getAttribute("org.jboss.seam.security.identity");
         if (attribute instanceof Identity) 
         {
             Identity identity = (Identity) attribute;
             Credentials credentials = identity.getCredentials();
             String username = credentials != null ? credentials.getUsername() : null;
             if (username != null) 
             {
                 MDC.put("username", username);
             }
         }
      }
      
      filterChain.doFilter(servletRequest, servletResponse);
      
      MDC.remove("username");
      
   }
   
}
