package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.security.digest.DigestRequest;
import org.jboss.seam.security.digest.DigestUtils;
import org.jboss.seam.security.digest.DigestValidationException;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.util.Base64;

/**
 * Seam Servlet Filter supporting HTTP Basic and Digest authentication. Some code
 * adapted from Acegi.
 *  
 * @author Shane Bryzak
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.web.authenticationFilter")
@Install(value = false, precedence = BUILT_IN)
@BypassInterceptors
@Filter(within = "org.jboss.seam.web.exceptionFilter")
public class AuthenticationFilter extends AbstractFilter
{
   private static final String DEFAULT_REALM = "seamApp";
   
   private static final String AUTH_TYPE_BASIC = "basic";
   private static final String AUTH_TYPE_DIGEST = "digest";
   
   @Logger Log log;
   
   public enum AuthType {basic, digest}
   
   private String realm = DEFAULT_REALM;
   
   private String key;
   private int nonceValiditySeconds = 300;
   
   private String authType = AUTH_TYPE_BASIC;
   
   public void setRealm(String realm)
   {
      this.realm = realm;
   }
   
   public String getRealm()
   {
      return realm;
   }
   
   public void setAuthType(String authType)
   {
      this.authType = authType;
   }
   
   public String getAuthType()
   {
      return authType;
   }
   
   public String getKey()
   {
      return key;
   }
   
   public void setKey(String key)
   {
      this.key = key;
   }
   
   public int getNonceValiditySeconds()
   {
      return nonceValiditySeconds;
   }
   
   public void setNonceValiditySeconds(int value)
   {
      this.nonceValiditySeconds = value;
   }
   
   public void doFilter(ServletRequest request, ServletResponse response, final FilterChain chain) 
      throws IOException, ServletException
   {
      if (!(request instanceof HttpServletRequest)) 
      {
         throw new ServletException("This filter can only process HttpServletRequest requests");
      }

      final HttpServletRequest httpRequest = (HttpServletRequest) request;
      final HttpServletResponse httpResponse = (HttpServletResponse) response;

      // Force session creation
      httpRequest.getSession();
      
      new ContextualHttpServletRequest(httpRequest)
      {
         @Override
         public void process() throws ServletException, IOException, LoginException
         {      
            if (AUTH_TYPE_BASIC.equals(authType))
               processBasicAuth(httpRequest, httpResponse, chain);
            else if (AUTH_TYPE_DIGEST.equals(authType))
               processDigestAuth(httpRequest, httpResponse, chain);
            else
               throw new ServletException("Invalid authentication type");
         }
      }.run();
   }
   
   private void processBasicAuth(HttpServletRequest request, 
            HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException
   {
      Identity identity = Identity.instance();

      if (identity == null)
      {
         throw new ServletException("Identity not found - please ensure that the Identity component is created on startup.");
      }
      
      Credentials credentials = identity.getCredentials();
      
      boolean requireAuth = false;
      
      String header = request.getHeader("Authorization");
      if (header != null && header.startsWith("Basic "))
      {
         String base64Token = header.substring(6);
         String token = new String(Base64.decode(base64Token));

         String username = "";
         String password = "";
         int delim = token.indexOf(":");

         if (delim != -1) 
         {
             username = token.substring(0, delim);
             password = token.substring(delim + 1);
         }

         // Only reauthenticate if username doesn't match Identity.username and user isn't authenticated
         if (!username.equals(credentials.getUsername()) || !identity.isLoggedIn()) 
         {
            try
            {
               credentials.setPassword(password);
               authenticate( request, username );
            }         
            catch (Exception ex)
            {
               log.warn("Error authenticating: " + ex.getMessage());
               requireAuth = true;
            }  
         }
      }
      
      if (!identity.isLoggedIn() && !credentials.isSet())
      {
         requireAuth = true;
      }
      
      try
      {
         if (!requireAuth)
         {
            chain.doFilter(request, response);
            return;
         }
      }
      catch (NotLoggedInException ex) 
      {
         requireAuth = true;
      }
      
      if ((requireAuth && !identity.isLoggedIn()))
      {
         response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");         
      }               
   }

   private void processDigestAuth(HttpServletRequest request, 
            HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException
   {
      Identity identity = Identity.instance();
      
      if (identity == null)
      {
         throw new ServletException("Identity not found - please ensure that the Identity component is created on startup.");
      }      
      
      Credentials credentials = identity.getCredentials();
      
      boolean requireAuth = false;    
      boolean nonceExpired = false;
      
      String header = request.getHeader("Authorization");      
      if (header != null && header.startsWith("Digest "))
      {        
         String section212response = header.substring(7);

         String[] headerEntries = section212response.split(",");
         Map<String,String> headerMap = new HashMap<String,String>();
         for (String entry : headerEntries)
         {
            String[] vals = split(entry, "=");
            headerMap.put(vals[0].trim(), vals[1].replace("\"", "").trim());
         }

         DigestRequest digestRequest = new DigestRequest();
         digestRequest.setHttpMethod(request.getMethod());
         digestRequest.setSystemRealm(realm);
         digestRequest.setRealm(headerMap.get("realm"));         
         digestRequest.setKey(key);
         digestRequest.setNonce(headerMap.get("nonce"));
         digestRequest.setUri(headerMap.get("uri"));
         digestRequest.setClientDigest(headerMap.get("response"));
         digestRequest.setQop(headerMap.get("qop"));
         digestRequest.setNonceCount(headerMap.get("nc"));
         digestRequest.setClientNonce(headerMap.get("cnonce"));
                  
         try
         {
            digestRequest.validate();
            request.getSession().setAttribute(DigestRequest.DIGEST_REQUEST, digestRequest);
            authenticate( request, headerMap.get("username") );
         }
         catch (DigestValidationException ex)
         {
            log.warn(String.format("Digest validation failed, header [%s]: %s",
                     section212response, ex.getMessage()));
            requireAuth = true;
            
            if (ex.isNonceExpired()) nonceExpired = true;
         }            
         catch (Exception ex)
         {
            log.warn("Error authenticating: " + ex.getMessage());
            requireAuth = true;
         }
      }   

      if (!identity.isLoggedIn() && !credentials.isSet())
      {
         requireAuth = true;
      }
      
      try
      {
         if (!requireAuth)
         {
            chain.doFilter(request, response);
            return;
         }
      }
      catch (NotLoggedInException ex) 
      {
         requireAuth = true;
      }
      
      if ((requireAuth && !identity.isLoggedIn()))
      {      
         long expiryTime = System.currentTimeMillis() + (nonceValiditySeconds * 1000);
         
         String signatureValue = DigestUtils.md5Hex(expiryTime + ":" + key);
         String nonceValue = expiryTime + ":" + signatureValue;
         String nonceValueBase64 = Base64.encodeBytes(nonceValue.getBytes());

         // qop is quality of protection, as defined by RFC 2617.
         // we do not use opaque due to IE violation of RFC 2617 in not
         // representing opaque on subsequent requests in same session.
         String authenticateHeader = "Digest realm=\"" + realm + "\", " + "qop=\"auth\", nonce=\""
             + nonceValueBase64 + "\"";

         if (nonceExpired) authenticateHeader = authenticateHeader + ", stale=\"true\"";

         response.addHeader("WWW-Authenticate", authenticateHeader);
         response.sendError(HttpServletResponse.SC_UNAUTHORIZED);      
      }             
   }
   
   private void authenticate(HttpServletRequest request, final String username)
      throws ServletException, IOException, LoginException
   {
      Identity identity = Identity.instance();
      identity.getCredentials().setUsername(username);
      identity.authenticate();
   }
   
   private String[] split(String toSplit, String delimiter) 
   {
      if (delimiter.length() != 1) 
      {
          throw new IllegalArgumentException("Delimiter can only be one character in length");
      }

      int offset = toSplit.indexOf(delimiter);

      if (offset < 0) {
          return null;
      }

      String beforeDelimiter = toSplit.substring(0, offset);
      String afterDelimiter = toSplit.substring(offset + 1);

      return new String[] {beforeDelimiter, afterDelimiter};
  }   
}
