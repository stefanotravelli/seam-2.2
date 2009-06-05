package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Page;
import org.jboss.seam.navigation.Pages;

@Scope(APPLICATION)
@Name("org.jboss.seam.web.rewriteFilter")
@Install(precedence=Install.BUILT_IN,value=false)
@BypassInterceptors
@Filter(within={"org.jboss.seam.web.multipartFilter","org.jboss.seam.web.authenticationFilter","org.jboss.seam.web.loggingFilter","org.jboss.seam.web.exceptionFilter","org.jboss.seam.web.identityFilter"})
public class RewriteFilter 
    extends AbstractFilter
{
    private static LogProvider log = Logging.getLogProvider(RewriteFilter.class);

    private String viewMapping;
    
    
    public void doFilter(ServletRequest request, 
                         ServletResponse response, 
                         FilterChain chain) 
        throws IOException, 
               ServletException 
    {
        List<Pattern> allPatterns = getAllPatterns();
        
        boolean done = false;
        
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            response = new RewritingResponse((HttpServletRequest) request,
                    (HttpServletResponse)response,
                    allPatterns);
            
            done = process((HttpServletRequest) request, 
                           (HttpServletResponse) response,
                            allPatterns);
        }
                
       if (!done) {
            chain.doFilter(request, response);
       }
    }
    
    
    @SuppressWarnings("unchecked")
    public boolean process(HttpServletRequest request, 
                        HttpServletResponse response, List<Pattern> patterns)
        throws IOException, 
               ServletException 
    {
        String fullPath = request.getRequestURI();
        //log.debug("incoming URL is " + fullPath);
        //log.debug("known patterns are " + patterns);

        String localPath = strip(fullPath, request.getContextPath());
       
        Rewrite rewrite = matchPatterns(localPath, patterns);
        if (rewrite!=null) {
            String newPath = rewrite.rewrite();
            
            //log.debug("rewritten incoming path is " + newPath);
            
            if (!fullPath.equals(request.getContextPath() + newPath)) {
                RequestDispatcher dispatcher = request.getRequestDispatcher(newPath);
                
//                final String wrappedPath = newPath;
//                HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
//                    @Override
//                    public String getServletPath() {
//                        return wrappedPath;
//
//                    }
//                };
//                dispatcher.forward(wrapped, response);
                dispatcher.forward(request, response);
                return true;
            }
        }
        
        return false;
    }


    private Rewrite matchPatterns(String localPath, List<Pattern> patterns) {
        for (Pattern pattern: patterns) {
            Rewrite rewrite = pattern.matchIncoming(localPath);
            if (rewrite!=null && rewrite.isMatch()) {
                return rewrite;
            }
        }
        return null;
    }

    private String strip(String fullPath, String contextPath) {
        if (fullPath.startsWith(contextPath)) {
            return fullPath.substring(contextPath.length());
        } else {
            return fullPath;
        }
    }
    
    
    private List<Pattern> getAllPatterns() {
        List<Pattern> allPatterns = new ArrayList<Pattern>();
        
        Pages pages = (Pages) getServletContext().getAttribute(Seam.getComponentName(Pages.class));
        if (pages != null) {
            Collection<String> ids = pages.getKnownViewIds();

            for (String id: ids) {
                 Page page = pages.getPage(id);
                 allPatterns.addAll(page.getRewritePatterns());
            }
        } else {
            log.warn("Pages is null for incoming request!");
        }
        
        ServletMapping mapping = new ServletMapping(getViewMapping());
        for (Pattern pattern: allPatterns) {
            pattern.setViewMapping(mapping);
        }
        
        return allPatterns;
    }


   public String getViewMapping()
   {
      return viewMapping == null ? "*.seam" : viewMapping;
   }


   public void setViewMapping(String viewMapping)
   {
      this.viewMapping = viewMapping;
   }
}

