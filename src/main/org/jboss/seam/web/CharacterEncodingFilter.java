package org.jboss.seam.web;
import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
/**
 * A servlet filter that lets you set the character encoding of 
 * submitted data. There are two init parameters: "encoding" and
 * "overrideClient".
 * 
 * @author Gavin King
 * 
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.servlet.characterEncodingFilter")
@Install(value=false, precedence = BUILT_IN)
@BypassInterceptors
@Filter(within="org.jboss.seam.web.ajax4jsfFilter")
public class CharacterEncodingFilter extends AbstractFilter
{
   private String encoding;
   private boolean overrideClient;
   
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
         throws ServletException, IOException
   {
      if ( encoding!=null && ( overrideClient || request.getCharacterEncoding() == null ) )
      {
         request.setCharacterEncoding(encoding);
      }
      filterChain.doFilter(request, response);
   }
   
   public String getEncoding()
   {
      return encoding;
   }
   
   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }
   
   public boolean getOverrideClient()
   {
      return overrideClient;
   }
   
   public void setOverrideClient(boolean overrideClient)
   {
      this.overrideClient = overrideClient;
   }
   
}