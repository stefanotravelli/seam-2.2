package org.jboss.seam.web;

import java.util.Collection;

import java.net.*;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

public class RewritingResponse 
    extends HttpServletResponseWrapper
{
    private static LogProvider log = Logging.getLogProvider(RewritingResponse.class);

    private HttpServletRequest request;
    private Collection<Pattern> patterns;

    public RewritingResponse(HttpServletRequest request, 
            HttpServletResponse response, 
            Collection<Pattern> patterns) 
    {
        super(response);

        this.request  = request;
        this.patterns = patterns;   
    }   

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    
    @Override
    public String encodeRedirectURL(String url) {
        String result = rewriteURL(url);
        log.debug("encodeRedirectURL " + url + " -> " + result);
        return wrappedEncodeRedirectURL(result);
    }

    private String wrappedEncodeRedirectURL(String result) {
        ServletResponse response = getResponse();
        if (response instanceof HttpServletResponse) {
            return ((HttpServletResponse)response).encodeRedirectURL(result);            
        }
        return result;
    }

    @Override
    public String encodeURL(String url) {        
        String result = super.encodeUrl(rewriteURL(url));
        log.debug("encodeURL " + url + " -> " + result);
        return wrappedEncodeURL(result);
    }
    
    private String wrappedEncodeURL(String result) {
        ServletResponse response = getResponse();
        if (response instanceof HttpServletResponse) {
            return ((HttpServletResponse)response).encodeRedirectURL(result);            
        }
        return result;
    }

    public boolean isLocalURL(URL url) {
        return url.getHost().equals(request.getServerName());
    }
    
    public String rewritePath(String originalPath) {
        String contextPath = request.getContextPath();

        String path = originalPath.startsWith(contextPath) ? 
                      originalPath.substring(contextPath.length()) : originalPath;
                      
        for (Pattern pattern: patterns) {
            Rewrite rewrite = pattern.matchOutgoing(path);
            if (rewrite != null) {
                return request.getContextPath() + rewrite.rewrite();
            }
        }

        return originalPath;
    }

    public String rewriteURL(String originalUrl) {        
        if (originalUrl.startsWith("http://") || originalUrl.startsWith("https://")) {
            try {
                URL url = new URL(originalUrl);

                if (isLocalURL(url)) {
                    URL newUrl = new URL(url, rewritePath(url.getFile()));
                    return newUrl.toExternalForm(); 
                }
            } catch (MalformedURLException e) {
                // ignore - we simply don't care.  we could log this at info/debug level.
            }
        }

        return rewritePath(originalUrl);
    }

}
