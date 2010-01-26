package org.jboss.seam.remoting;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.web.AbstractResource;

/**
 * Serves JavaScript implementation of Seam Remoting
 * 
 * @author Shane Bryzak
 *
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.remoting.remoting")
@Install(precedence = BUILT_IN)
@BypassInterceptors
public class Remoting extends AbstractResource
{   
   public static final int DEFAULT_POLL_TIMEOUT = 10; // 10 seconds
   public static final int DEFAULT_POLL_INTERVAL = 1; // 1 second

   private int pollTimeout = DEFAULT_POLL_TIMEOUT;
   
   private int pollInterval = DEFAULT_POLL_INTERVAL;
   
   private boolean debug = false;   
   
   /**
    * We use a Map for this because a Servlet can serve requests for more than
    * one context path.
    */
   private Map<String, byte[]> cachedConfig = new HashMap<String, byte[]>();
   
   private static final LogProvider log = Logging.getLogProvider(Remoting.class);

   private static final Pattern pathPattern = Pattern.compile("/(.*?)/([^/]+)");

   private static final String REMOTING_RESOURCE_PATH = "resource";   
   
   @Override
   public String getResourcePath()
   {
      return "/remoting";
   }
   
   private synchronized void initConfig(String contextPath, HttpServletRequest request)
   {
      if (!cachedConfig.containsKey(contextPath))
      {
         try
         {
            ServletLifecycle.beginRequest(request,getServletContext());

            StringBuilder sb = new StringBuilder();
            sb.append("\nSeam.Remoting.resourcePath = \"");
            sb.append(contextPath);
            sb.append(request.getServletPath());
            sb.append(getResourcePath());
            sb.append("\";");
            sb.append("\nSeam.Remoting.debug = ");
            sb.append(getDebug() ? "true" : "false");
            sb.append(";");
            sb.append("\nSeam.Remoting.pollInterval = ");
            sb.append(getPollInterval());
            sb.append(";");
            sb.append("\nSeam.Remoting.pollTimeout = ");
            sb.append(getPollTimeout());
            sb.append(";");

            cachedConfig.put(contextPath, sb.toString().getBytes());
         }
         finally
         {
            ServletLifecycle.endRequest(request);
         }
      }
   }   
   
   @Override
   public void getResource(HttpServletRequest request, HttpServletResponse response)
       throws IOException
   {
      try
      {         
         String pathInfo = request.getPathInfo().substring(getResourcePath().length());      
         
         RequestHandler handler = RequestHandlerFactory.getInstance()
               .getRequestHandler(pathInfo);
         if (handler != null)
         {
            handler.setServletContext(getServletContext());
            handler.handle(request, response);
         }
         else
         {
            Matcher m = pathPattern.matcher(pathInfo);
            if (m.matches())
            {
               String path = m.group(1);
               String resource = m.group(2);

               if (REMOTING_RESOURCE_PATH.equals(path))
               {
                  writeResource(resource, response);
                  if ("remote.js".equals(resource))
                  {
                     appendConfig(response.getOutputStream(), request
                           .getContextPath(), request);
                  }
               }
               response.getOutputStream().flush();               
            }
         }
      }
      catch (Exception ex)
      {
         log.error("Error", ex);
      }      
   }

   /**
    * Appends various configuration options to the remoting javascript client
    * api.
    * 
    * @param out OutputStream
    */
   private void appendConfig(OutputStream out, String contextPath,
         HttpServletRequest request) throws IOException
   {
      if (!cachedConfig.containsKey(contextPath))
      {
         initConfig(contextPath, request);
      }

      out.write(cachedConfig.get(contextPath));
   }   

   /**
    * 
    * @param resourceName String
    * @param out OutputStream
    */
   private void writeResource(String resourceName, HttpServletResponse response)
         throws IOException
   {
      // Only allow resource requests for .js files
      if (resourceName.endsWith(".js"))
      {                  
         InputStream in = this.getClass().getClassLoader().getResourceAsStream(
               "org/jboss/seam/remoting/" + resourceName);
         try
         {
            if (in != null)
            {
               response.setContentType("text/javascript");
               
               byte[] buffer = new byte[1024];
               int read = in.read(buffer);
               while (read != -1)
               {
                  response.getOutputStream().write(buffer, 0, read);
                  read = in.read(buffer);
               }
            }
            else
            {
               log.error(String.format("Resource [%s] not found.", resourceName));
            }
         }
         finally
         {
            if (in != null) in.close();
         }
      }
   }   
   
   public int getPollTimeout()
   {
     return pollTimeout;
   }

   public void setPollTimeout(int pollTimeout)
   {
     this.pollTimeout = pollTimeout;
   }

   public int getPollInterval()
   {
     return pollInterval;
   }

   public void setPollInterval(int pollInterval)
   {
     this.pollInterval = pollInterval;
   }

   public boolean getDebug()
   {
     return debug;
   }

   public void setDebug(boolean debug)
   {
     this.debug = debug;
   }   
}
