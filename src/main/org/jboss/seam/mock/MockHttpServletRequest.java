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
   private Enumeration locales;
   
   public MockHttpServletRequest(HttpSession session)
   {
      this(session, null, new HashSet<String>());
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
      //TODO
      return null;
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
      return new IteratorEnumeration( Arrays.asList( headers.get(header) ).iterator() );
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
      //TODO
      return null;
   }

   public String getPathTranslated()
   {
      //TODO
      return null;
   }

   public String getContextPath()
   {
      return "/project";
   }

   public String getQueryString()
   {
      //TODO
      return null;
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
      //TODO
      return null;
   }

   public String getRequestURI()
   {
      return "http://localhost:8080/myproject/page.seam";
   }

   public StringBuffer getRequestURL()
   {
      return new StringBuffer( getRequestURI() );
   }

   public String getServletPath()
   {
      return "/page.seam";
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
      //TODO
      return null;
   }

   public void setCharacterEncoding(String enc)
         throws UnsupportedEncodingException
   {
      //TODO

   }

   public int getContentLength()
   {
      //TODO
      return 0;
   }

   public String getContentType()
   {
      //TODO
      return null;
   }

   public ServletInputStream getInputStream() throws IOException
   {
      //TODO
      return null;
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
      //TODO
      return null;
   }

   public String getScheme()
   {
      //TODO
      return null;
   }

   public String getServerName()
   {
      //TODO
      return null;
   }

   public int getServerPort()
   {
      //TODO
      return 0;
   }

   public BufferedReader getReader() throws IOException
   {
      //TODO
      return null;
   }

   public String getRemoteAddr()
   {
      //TODO
      return null;
   }

   public String getRemoteHost()
   {
      //TODO
      return null;
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
      //TODO
      return null;
   }

   public Enumeration getLocales()
   {
      return locales;
   }

   public boolean isSecure()
   {
      //TODO
      return false;
   }

   public RequestDispatcher getRequestDispatcher(String path)
   {
      //TODO
      return null;
   }

   public String getRealPath(String path)
   {
      //TODO
      return null;
   }

   public int getRemotePort()
   {
      //TODO
      return 0;
   }

   public String getLocalName()
   {
      //TODO
      return null;
   }

   public String getLocalAddr()
   {
      //TODO
      return null;
   }

   public int getLocalPort()
   {
      //TODO
      return 0;
   }

   public Map<String, String[]> getHeaders()
   {
      return headers;
   }
}
