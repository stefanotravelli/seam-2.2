/**
 * 
 */
package org.jboss.seam.mail.ui.context;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;

public class MailExternalContextImpl extends ExternalContext {
   
   private ExternalContext delegate;
   private String urlBase;
   
   public MailExternalContextImpl(ExternalContext delegate) {
      this(delegate, null);
   }
   
   public MailExternalContextImpl(ExternalContext delegate, String urlBase) {
      this.delegate = delegate;
      this.urlBase = urlBase;
   }

   @Override
   public void dispatch(String path) throws IOException
   {
     delegate.dispatch(path);
      
   }

   @Override
   public String encodeActionURL(String url)
   {
      return delegate.encodeActionURL(url);
   }

   @Override
   public String encodeNamespace(String name)
   {
      return delegate.encodeNamespace(name);
   }

   @Override
   public String encodeResourceURL(String url)
   {
     return delegate.encodeResourceURL(url);
   }

   @Override
   public Map getApplicationMap()
   {
      return delegate.getApplicationMap();
   }

   @Override
   public String getAuthType()
   {
     return delegate.getAuthType();
   }

   @Override
   public Object getContext()
   {
     return delegate.getContext();
   }

   @Override
   public String getInitParameter(String name)
   {
     return delegate.getInitParameter(name);
   }

   @Override
   public Map getInitParameterMap()
   {
      return delegate.getInitParameterMap();
   }

   @Override
   public String getRemoteUser()
   {
     return delegate.getRemoteUser();
   }

   @Override
   public Object getRequest()
   {
      return delegate.getRequest();
   }

   @Override
   public String getRequestContextPath()
   {
      return urlBase;
   }

   @Override
   public Map getRequestCookieMap()
   {
      return delegate.getRequestCookieMap();
   }

   @Override
   public Map getRequestHeaderMap()
   {
      return delegate.getRequestHeaderMap();
   }

   @Override
   public Map getRequestHeaderValuesMap()
   {
      return delegate.getRequestHeaderValuesMap();
   }

   @Override
   public Locale getRequestLocale()
   {
     return delegate.getRequestLocale();
   }

   @Override
   public Iterator getRequestLocales()
   {
      return delegate.getRequestLocales();
   }

   @Override
   public Map getRequestMap()
   {
     return delegate.getRequestMap();
   }

   @Override
   public Map getRequestParameterMap()
   {
      return delegate.getRequestParameterMap();
   }

   @Override
   public Iterator getRequestParameterNames()
   {
      return delegate.getRequestParameterNames();
   }

   @Override
   public Map getRequestParameterValuesMap()
   {
      return delegate.getRequestHeaderValuesMap();
   }

   @Override
   public String getRequestPathInfo()
   {
      return delegate.getRequestPathInfo();
   }

   @Override
   public String getRequestServletPath()
   {
      return delegate.getRequestServletPath();
   }

   @Override
   public URL getResource(String path) throws MalformedURLException
   {
      return delegate.getResource(path);
   }

   @Override
   public InputStream getResourceAsStream(String path)
   {
      return delegate.getResourceAsStream(path);
   }

   @Override
   public Set getResourcePaths(String path)
   {
      return delegate.getResourcePaths(path);
   }

   @Override
   public Object getResponse()
   {
      return delegate.getResponse();
   }

   @Override
   public Object getSession(boolean create)
   {
      return delegate.getSession(create);
   }

   @Override
   public Map getSessionMap()
   {
      return delegate.getSessionMap();
   }

   @Override
   public Principal getUserPrincipal()
   {
      return delegate.getUserPrincipal();
   }

   @Override
   public boolean isUserInRole(String role)
   {
      return delegate.isUserInRole(role);
   }

   @Override
   public void log(String message)
   {
      delegate.log(message);
   }

   @Override
   public void log(String message, Throwable exception)
   {
      delegate.log(message, exception);
      
   }

   @Override
   public void redirect(String url) throws IOException
   {
      delegate.redirect(url);
   }
   
}