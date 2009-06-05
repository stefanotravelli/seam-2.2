package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;
import static org.jboss.seam.core.Manager.REDIRECT_FROM_MANAGER;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.navigation.Pages;

/**
 * Propagates the conversation context and page parameters across any 
 * browser redirect initiated from a JSF navigation rule defined in 
 * faces-config.xml. Note that this is no longer needed if all
 * navigation rules are defined in pages.xml.
 * 
 * @author Gavin King
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.web.redirectFilter")
@Install(precedence = BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@BypassInterceptors
@Filter(within="org.jboss.seam.web.ajax4jsfFilter")
public class RedirectFilter extends AbstractFilter 
{
   public void doFilter(ServletRequest request, ServletResponse response,
         FilterChain chain) throws IOException, ServletException 
   {
      chain.doFilter( request, wrapResponse( (HttpServletResponse) response ) );
   }
   
   private static ServletResponse wrapResponse(HttpServletResponse response) 
   {
      return new HttpServletResponseWrapper(response)
      {
         @Override
         public void sendRedirect(String url) throws IOException
         {
            if ( FacesContext.getCurrentInstance() != null 
                  && Contexts.isEventContextActive() 
                  && !Contexts.getEventContext().isSet(REDIRECT_FROM_MANAGER) )
            {
               if ( !url.startsWith("http:") && !url.startsWith("https:") ) //yew!
               {
                  String viewId = getViewId(url);
                  if (viewId!=null)
                  {
                     url = Pages.instance().encodePageParameters( FacesContext.getCurrentInstance(), url, viewId );
                  }
                  if ( Contexts.isConversationContextActive() )
                  {
                     url = FacesManager.instance().appendConversationIdFromRedirectFilter(url, viewId);
                  }
               }
            }
            super.sendRedirect(url);
         }
      };
   }
   
   public static String getViewId(String url)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext==null)
      {
         return null;
      }
      else
      {
         ExternalContext externalContext = facesContext.getExternalContext();
         String pathInfo = externalContext.getRequestPathInfo();
         String servletPath = externalContext.getRequestServletPath();
         String contextPath = externalContext.getRequestContextPath();
         return getViewId(url, pathInfo, servletPath, contextPath);
      }
   }
   
   protected static String getViewId(String url, String pathInfo, String servletPath, String contextPath)
   {
      if (pathInfo!=null)
      {
         //for /seam/* style servlet mappings
         return url.substring( contextPath.length() + servletPath.length(), getParamLoc(url) );
      }
      else if ( url.startsWith(contextPath) )
      {
         //for *.seam style servlet mappings
         String extension = servletPath.substring( servletPath.lastIndexOf('.') );
         if ( url.endsWith(extension) || url.contains(extension + '?') )
         {
            String suffix = Pages.getSuffix();
            return url.substring( contextPath.length(), getParamLoc(url) - extension.length() ) + suffix;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }
   
   private static int getParamLoc(String url)
   {
      int loc = url.indexOf('?');
      if (loc<0) loc = url.length();
      return loc;
   }
}
