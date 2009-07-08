package org.jboss.seam.mock;

import org.jboss.seam.servlet.SeamResourceServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import java.security.Principal;
import java.io.IOException;

/**
 * Executes (through local calls, not TCP sockets) an HTTP request in a unit test, passing it through
 * the Seam resource handlers and filters.
 *
 * <p>
 * This class is supposed to be used <b>within</b> a <tt>SeamTest</tt>, in fact, you need
 * to pass an instance of <tt>SeamTest</tt> into its constructor. This prepares the environment
 * for the resource request processing. You can either share an instance of the environment between
 * all your test methods (prepare it in <tt>&#064;BeforeClass</tt>) or you can create a new instance
 * for each <tt>ResourceRequest</tt>:
 * </p>
 *
 * <pre>
 * import org.jboss.seam.mock.ResourceRequestEnvironment;
 * import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
 * import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
 * import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
 * import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;
 *
 * public class MyTest extends SeamTest {
 *
 *    ResourceRequestEnvironment sharedEnvironment;
 *
 *    &#064;BeforeClass
 *    public void prepareSharedEnvironment() throws Exception {
 *        sharedEnvironment = new ResourceRequestEnvironment(this) {
 *             &#064;Override
 *             public Map<String, Object> getDefaultHeaders() {
 *                return new HashMap<String, Object>() {{
 *                    put("Accept", "text/plain");
 *                }};
 *             }
 *          };
 *    }
 *
 *    &#064;Test
 *    public void test() throws Exception
 *    {
 *       //Not shared: new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/my/relative/uri)
 *
 *       new ResourceRequest(sharedEnvironment, Method.GET, "/my/relative/uri)
 *       {
 *
 *          &#064;Override
 *          protected void prepareRequest(EnhancedMockHttpServletRequest request)
 *          {
 *             request.addQueryParameter("foo", "123");
 *             request.addHeader("Accept-Language", "en_US, de");
 *          }
 *
 *          &#064;Override
 *          protected void onResponse(EnhancedMockHttpServletResponse response)
 *          {
 *             assert response.getStatus() == 200;
 *             assert response.getContentAsString().equals("foobar");
 *          }
 *
 *       }.run();
 *    }
 *
 * }
 * </pre>
 * <p>
 * Note that in a <tt>SeamTest</tt> the (mock) HTTP session is always shared between all requests in a particular test
 * method. Each test method however executes with a new (mock) HTTP session. Design your tests accordingly, this is not
 * configurable.
 * </p>
 * <p>
 * <b>IMPORTANT: A <tt>ResourceRequest</tt> has to be executed in a <tt>@Test</tt> method or in a
 * <tt>@BeforeMethod</tt> callback. You can not execute it in any other callback, such * as <tt>@BeforeClass</tt>.
 * </p>
 *
 * @author Christian Bauer
 */
public class ResourceRequestEnvironment
{

   public enum Method
   {
      GET, PUT, POST, DELETE, HEAD, OPTIONS
   }

   final protected AbstractSeamTest seamTest;
   final protected SeamResourceServlet resourceServlet;

   public ResourceRequestEnvironment(AbstractSeamTest seamTest)
   {
      this.seamTest = seamTest;
      resourceServlet = new SeamResourceServlet();
      try {
         resourceServlet.init(
               new ServletConfig()
               {
                  public String getServletName()
                  {
                     return "Seam Resource Servlet";
                  }

                  public ServletContext getServletContext()
                  {
                     return ResourceRequestEnvironment.this.seamTest.servletContext;
                  }

                  public String getInitParameter(String s)
                  {
                     return null;
                  }

                  public Enumeration getInitParameterNames()
                  {
                     return null;
                  }
               }
         );
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   public static class ResourceRequest
   {

      final private ResourceRequestEnvironment environment;
      private Method httpMethod;
      private String requestPath;
      private EnhancedMockHttpServletRequest request;
      private EnhancedMockHttpServletResponse response;

      public ResourceRequest(ResourceRequestEnvironment environment, Method httpMethod, String requestPath)
      {
         this.environment = environment;
         this.httpMethod = httpMethod;
         this.requestPath = environment.getServletPath() + (requestPath.startsWith("/") ? requestPath : "/" + requestPath);
      }

      public void run() throws Exception
      {
         init();
         prepareRequest(request);
         environment.seamTest.seamFilter.doFilter(request, response, new FilterChain()
         {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
            {
               environment.resourceServlet.service(request, response);
            }
         });
         environment.seamTest.seamFilter.destroy();
         onResponse(getResponse());
      }

      protected void init()
      {
         request = createRequest();
         response = createResponse();

         request.setMethod(httpMethod.toString());
         request.setRequestURI(requestPath);

         request.setServletPath(environment.getServletPath());

         request.setCookies(getCookies().toArray(new Cookie[getCookies().size()]));

         for (Map.Entry<String, Object> entry : environment.getDefaultHeaders().entrySet())
         {
            request.addHeader(entry.getKey(), entry.getValue());
         }

         request.setUserPrincipal(
               new Principal()
               {
                  public String getName()
                  {
                     return getPrincipalName();
                  }
               }
         );
         for (String role : getPrincipalRoles())
         {
            request.addUserRole(role);
         }

         // Use the (mock) HttpSession that Seam uses, see AbstractSeamTest
         request.setSession(environment.seamTest.session);

      }

      protected EnhancedMockHttpServletRequest createRequest()
      {
         return new EnhancedMockHttpServletRequest();
      }

      protected EnhancedMockHttpServletResponse createResponse()
      {
         return new EnhancedMockHttpServletResponse();
      }

      protected Map<String, String> getRequestQueryParameters()
      {
         return Collections.EMPTY_MAP;
      }

      protected List<Cookie> getCookies()
      {
         return Collections.EMPTY_LIST;
      }

      protected String getPrincipalName()
      {
         return null;
      }

      protected Set<String> getPrincipalRoles()
      {
         return Collections.EMPTY_SET;
      }

      protected void prepareRequest(EnhancedMockHttpServletRequest request)
      {
      }

      protected void onResponse(EnhancedMockHttpServletResponse response)
      {
      }

      public HttpServletRequest getRequest()
      {
         return request;
      }

      public EnhancedMockHttpServletResponse getResponse()
      {
         return response;
      }

   }

   public String getServletPath()
   {
      return "/seam/resource";
   }

   public Map<String, Object> getDefaultHeaders()
   {
      return Collections.EMPTY_MAP;
   }

}
