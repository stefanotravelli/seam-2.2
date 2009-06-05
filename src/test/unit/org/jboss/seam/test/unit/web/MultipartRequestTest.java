package org.jboss.seam.test.unit.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.jboss.seam.mock.MockHttpServletRequest;
import org.jboss.seam.mock.MockHttpServletResponse;
import org.jboss.seam.mock.MockHttpSession;
import org.jboss.seam.mock.MockServletContext;
import org.jboss.seam.util.Resources;
import org.jboss.seam.web.MultipartFilter;
import org.jboss.seam.web.MultipartRequest;
import org.testng.annotations.Test;

/**
 * @author Pete Muir
 *
 */
public class MultipartRequestTest
{
    
    @Test
    public void testMultipartRequest() throws IOException, ServletException
    {
        MultipartFilter filter = new MultipartFilter();
        ServletContext context = new MockServletContext();
        HttpSession session = new MockHttpSession(context);
        MockHttpServletRequest request = new MockHttpServletRequest(session, "Pete", new HashSet<String>(), new Cookie[0], "post") 
        {
            
            private final InputStream is = Resources.getResourceAsStream("/META-INF/seam.properties", null);
            
            @Override
            public String getContentType()
            {
                return "multipart/test; boundary=foo";
            }
            
            @Override
            public ServletInputStream getInputStream() throws IOException
            {
                return new ServletInputStream() {

                    @Override
                    public int read() throws IOException
                    {
                        return is.read();
                    }
                    
                    @Override
                    public int read(byte[] b) throws IOException
                    {
                        return is.read(b);
                    }
                    
                };
            }
            
        };
        // Add some parameters to test passthrough
        String [] fooParams = {"bar"}; 
        request.getParameterMap().put("foo", fooParams);
        ServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new FilterChain() 
        {

            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException
            {
                assert request instanceof MultipartRequest;
                MultipartRequest multipartRequest = (MultipartRequest) request;
                assert multipartRequest.getParameterMap().containsKey("foo");
				// Test passthrough parameters
                assert multipartRequest.getParameterValues("foo").length == 1;
                assert "bar".equals(multipartRequest.getParameterValues("foo")[0]);
                
                // TODO Test a multipart request
            }
            
        };
        filter.doFilter(request, response, chain);
    }

}
