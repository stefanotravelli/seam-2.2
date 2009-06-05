package org.jboss.seam.web;

import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Abstract superclass for Seam components that act as servlet filters. Note
 * that since a filter is potentially called outside of a set of Seam contexts,
 * it is not a true Seam component. 
 * 
 * However, we are able to reuse the functionality for component scanning, 
 * installation and configuration for filters. All filters must specify
 * the @Filter annotation to be included by Seam's master filter.
 * 
 * @see org.jboss.seam.annotations.web.Filter
 * @author Shane Bryzak
 *
 */
public abstract class AbstractFilter implements Filter
{
   private ServletContext servletContext;

   private String urlPattern;
   
   private String regexUrlPattern;
   
   private Pattern pattern;
   
   private boolean disabled;

   public void init(FilterConfig filterConfig) throws ServletException
   {
      servletContext = filterConfig.getServletContext();
   }
   
   protected ServletContext getServletContext()
   {
      return servletContext;
   }
   
   public String getUrlPattern()
   {
      return urlPattern;
   }
   
   public void setUrlPattern(String urlPattern)
   {
      this.urlPattern = urlPattern;
   }
   
   public String getRegexUrlPattern()
   {
      return this.regexUrlPattern;
   }
   
   public void setRegexUrlPattern(String regexUrlPattern)
   {
      this.regexUrlPattern = regexUrlPattern;
      pattern = null;
   }
   
   private Pattern getPattern()
   {
      if (pattern == null && getRegexUrlPattern() != null)
      {
         pattern = Pattern.compile(getRegexUrlPattern());
      }
      return pattern;
   }
   
   public boolean isDisabled() 
   {
      return disabled;
   }
   
   public void setDisabled(boolean disabled) 
   {
      this.disabled = disabled;
   }
   
   /**
    * Pattern matching code, adapted from Tomcat. This method checks to see if
    * the specified path matches the specified pattern.
    * 
    * @param request ServletRequest The request containing the path
    * @return boolean True if the path matches the pattern, false otherwise
    */
   public boolean isMappedToCurrentRequestPath(ServletRequest request)
   {
      if (!(request instanceof HttpServletRequest))
      {
         return true;
      }

      HttpServletRequest httpRequest = (HttpServletRequest)request;
      String path = httpRequest.getRequestURI().replaceFirst(httpRequest.getContextPath(), "");      
      String urlPattern = getUrlPattern();
      Pattern regexPattern = getPattern();
      if (urlPattern != null)
      {
         return matchesTomcatPattern(path, urlPattern);
      }
      else if (regexPattern != null)
      {
         return matchesRegexPattern(path, regexPattern);
      }
      else
      {
         return true;
      }
   }
   
   private static boolean matchesRegexPattern(String path, Pattern pattern)
   {
      return pattern.matcher(path).matches();
   }

   private static boolean matchesTomcatPattern(String path, String pattern)
   {
      
      if (pattern==null) return true;

      if (path == null || "".equals(path)) path = "/";
      if (pattern == null || "".equals(pattern)) pattern = "/";

      // Check for an exact match
      if (path.equals(pattern)) return true;

      // Check for path prefix matching
      if (pattern.startsWith("/") && pattern.endsWith("/*"))
      {
         pattern = pattern.substring(0, pattern.length() - 2);
         if (pattern.length() == 0) return true;

         if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

         while (true)
         {
            if (pattern.equals(path)) return true;
            int slash = path.lastIndexOf('/');
            if (slash <= 0) break;
            path = path.substring(0, slash);
         }
         return false;
      }

      // Check for suffix matching
      if (pattern.startsWith("*."))
      {
         int slash = path.lastIndexOf('/');
         int period = path.lastIndexOf('.');
         if ((slash >= 0) && (period > slash) && path.endsWith(pattern.substring(1)))
         {
            return true;
         }
         return false;
      }

      // Check for universal mapping
      if (pattern.equals("/")) return true;

      return false;
   }
   
   public void destroy() {}
   
}
