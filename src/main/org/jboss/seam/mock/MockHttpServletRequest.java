/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.portlet.PortletRequest;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.seam.util.IteratorEnumeration;

/**
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class MockHttpServletRequest implements HttpServletRequest
{
   
   private Map<String, String[]> parameters = new HashMap<String, String[]>();
   private Map<String, Object> attributes = new HashMap<String, Object>();
   private HttpSession session;
   private Map<String, String[]> headers = new HashMap<String, String[]>();
   private String principalName;
   private Set<String> principalRoles;
   private Cookie[] cookies;
   private String method;
   private HttpServletRequest httpServletRequest;
   private PortletRequest portletRequest;
   private String authType;
   private String pathInfo;
   private String pathTranslated;
   private String contextPath;
   private String queryString;
   private String requestedSessionId;
   private String requestURI;
   private StringBuffer requestURL;
   private String servletPath;
   private String characterEncoding;
   private int contentLength;
   private String contentType;
   private ServletInputStream inputStream;
   private String protocol;
   private String scheme;
   private String serverName;
   private int serverPort;
   private BufferedReader reader;
   private String remoteAddr;
   private String remoteHost;
   private Locale locale;
   private Enumeration locales;
   private boolean isSecure;
   private int remotePort;
   private String localName;
   private String localAddr;
   private int localPort;

   
   
   public MockHttpServletRequest(HttpSession session)
   {
      this(session, null, new HashSet<String>());
   }
   
   public MockHttpServletRequest(HttpSession session, ExternalContext externalContext) 
   {
      this(session, null, new HashSet<String>());
      Object request = externalContext.getRequest();
      if(externalContext != null && (request instanceof HttpServletRequest)) 
      {
         httpServletRequest = (HttpServletRequest)request;
         authType = httpServletRequest.getAuthType();
         pathInfo = httpServletRequest.getPathInfo();
         pathTranslated = httpServletRequest.getPathTranslated();
         contextPath = httpServletRequest.getContextPath();
         queryString = httpServletRequest.getQueryString();
         requestedSessionId = httpServletRequest.getRequestedSessionId();
         requestURI = httpServletRequest.getRequestURI();
         requestURL = httpServletRequest.getRequestURL();
         servletPath = httpServletRequest.getServletPath();
         characterEncoding = httpServletRequest.getCharacterEncoding();
         contentLength = httpServletRequest.getContentLength();
         contentType = httpServletRequest.getContentType();
         protocol = httpServletRequest.getProtocol();
         scheme = httpServletRequest.getScheme();
         serverName = httpServletRequest.getServerName();
         serverPort = httpServletRequest.getServerPort();
         remoteAddr = httpServletRequest.getRemoteAddr();
         remoteHost = httpServletRequest.getRemoteHost();
         locale = httpServletRequest.getLocale();
         locales = httpServletRequest.getLocales();
         isSecure = httpServletRequest.isSecure();
         remotePort = httpServletRequest.getRemotePort();
         localName = httpServletRequest.getLocalName();
         localAddr = httpServletRequest.getLocalAddr();
         localPort = httpServletRequest.getLocalPort();
         
      } else if(externalContext != null && (request instanceof PortletRequest)) 
      {
         portletRequest = (PortletRequest)request;
         authType = portletRequest.getAuthType();
         contextPath = portletRequest.getContextPath();
         requestedSessionId = portletRequest.getRequestedSessionId();
         scheme = portletRequest.getScheme();
         serverName = portletRequest.getServerName();
         serverPort = portletRequest.getServerPort();
         locale = portletRequest.getLocale();
         locales = portletRequest.getLocales();
         isSecure = portletRequest.isSecure();
      }
   }

   public MockHttpServletRequest(HttpSession session, String principalName, Set<String> principalRoles)
   {
      this(session, principalName, principalRoles, new Cookie[] {}, null);
   }

   public MockHttpServletRequest(HttpSession session, String principalName, Set<String> principalRoles, Cookie[] cookies, String method)
   {
      this.session = session;
      this.principalName = principalName;
      this.principalRoles = principalRoles;
      this.cookies = cookies;
      this.method = method;
      // The 1.2 RI NPEs if this header isn't present 
      headers.put("Accept", new String[0]);
      locales = new IteratorEnumeration(new ArrayList().iterator());
   }

   public Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public Map<String, Object> getAttributes()
   {
      return attributes;
   }
   
   public String getAuthType()
   {
      return authType;
   }

   public Cookie[] getCookies()
   {
      return cookies;
   }

   public long getDateHeader(String arg0)
   {
      throw new UnsupportedOperationException();
   }

   public String getHeader(String header)
   {
      String[] values = headers.get(header);
      return values==null || values.length==0 ? null : values[0];
   }

   public Enumeration getHeaders(String header)
   {      
      String[] values = headers.get(header);
      return values==null || values.length==0 ? null : new IteratorEnumeration( Arrays.asList( values ).iterator() );
   }

   public Enumeration getHeaderNames()
   {
      return new IteratorEnumeration( headers.keySet().iterator() );
   }

   public int getIntHeader(String header)
   {
      throw new UnsupportedOperationException();
   }

   public String getMethod()
   {
      return method;
   }

   public String getPathInfo()
   {
      return pathInfo;
   }

   public String getPathTranslated()
   {
      return pathTranslated;
   }

   public String getContextPath()
   {
      return (contextPath != null ? contextPath : "/project");
   }

   public String getQueryString()
   {
      return queryString;
   }

   public String getRemoteUser()
   {
      return principalName;
   }

   public boolean isUserInRole(String role)
   {
      return principalRoles.contains(role);
   }

   public Principal getUserPrincipal()
   {
      return principalName==null ? null : 
         new Principal() 
         {
            public String getName()
            {
               return principalName;
            }
         };
   }

   public String getRequestedSessionId()
   {
      return requestedSessionId;
   }

   public String getRequestURI()
   {
      return (requestURI != null ? requestURI : "http://localhost:8080/myproject/page.seam");
   }

   public StringBuffer getRequestURL()
   {
      return (requestURL != null ? requestURL : new StringBuffer(getRequestURI())); 
   }

   public String getServletPath()
   {
      return (servletPath != null ? servletPath : "/page.seam");
   }

   public HttpSession getSession(boolean create)
   {
      return session;
   }

   public HttpSession getSession()
   {
      return getSession(true);
   }

   public boolean isRequestedSessionIdValid()
   {
      return true;
   }

   public boolean isRequestedSessionIdFromCookie()
   {
      return true;
   }

   public boolean isRequestedSessionIdFromURL()
   {
      return false;
   }

   public boolean isRequestedSessionIdFromUrl()
   {
      return false;
   }

   public Object getAttribute(String att)
   {
      return attributes.get(att);
   }

   public Enumeration getAttributeNames()
   {
      return new IteratorEnumeration( attributes.keySet().iterator() );
   }

   public String getCharacterEncoding()
   {
      return characterEncoding;
   }

   public void setCharacterEncoding(String enc)
         throws UnsupportedEncodingException
   {
      //TODO

   }

   public int getContentLength()
   {
      return contentLength;
   }

   public String getContentType()
   {
      return contentType;
   }

   public ServletInputStream getInputStream() throws IOException
   {
      return inputStream;
   }

   public String getParameter(String param)
   {
      String[] values = parameters.get(param);
      return values==null || values.length==0 ? null : values[0];
   }

   public Enumeration getParameterNames()
   {
      return new IteratorEnumeration( parameters.keySet().iterator() );
   }

   public String[] getParameterValues(String param)
   {
      return parameters.get(param);
   }

   public Map getParameterMap()
   {
      return parameters;
   }

   public String getProtocol()
   {
      return protocol;
   }

   public String getScheme()
   {
      return scheme;
   }

   public String getServerName()
   {
      return serverName;
   }

   public int getServerPort()
   {
      return serverPort;
   }

   public BufferedReader getReader() throws IOException
   {
      return reader;
   }

   public String getRemoteAddr()
   {
      return remoteAddr;
   }

   public String getRemoteHost()
   {
      return remoteHost;
   }

   public void setAttribute(String att, Object value)
   {
      if (value==null)
      {
         attributes.remove(value);
      }
      else
      {
         attributes.put(att, value);
      }
   }

   public void removeAttribute(String att)
   {
      attributes.remove(att);
   }

   public Locale getLocale()
   {
      return locale;
   }

   public Enumeration getLocales()
   {
      return locales;
   }

   public boolean isSecure()
   {
      return isSecure;
   }

   public RequestDispatcher getRequestDispatcher(String path)
   {
      if(httpServletRequest != null) 
      {
         return httpServletRequest.getRequestDispatcher(path);
      }
      return null;
   }

   public String getRealPath(String path)
   {
      if(httpServletRequest != null) 
      {
         return httpServletRequest.getRealPath(path);
      }
      return null;
   }

   public int getRemotePort()
   {
      return remotePort;
   }

   public String getLocalName()
   {
      return localName;
   }

   public String getLocalAddr()
   {
      return localAddr;
   }

   public int getLocalPort()
   {
      return localPort;
   }

   public Map<String, String[]> getHeaders()
   {
      return headers;
   }
}
