package org.jboss.seam.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.Selector;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.util.Base64;

/**
 * Remember-me functionality is provided by this class, in two different flavours.  The first mode
 * provides username-only persistence, and is considered to be secure as the user (or their browser)
 * is still required to provide a password.  The second mode provides an auto-login feature, however
 * is NOT considered to be secure and is vulnerable to XSS attacks compromising the user's account.
 * 
 * Use the auto-login mode with caution!
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.rememberMe")
@Scope(SESSION)
@Install(precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
@BypassInterceptors
public class RememberMe implements Serializable
{
   class UsernameSelector extends Selector
   {
      @Override
      public String getCookieName()
      {
         return "org.jboss.seam.security.username";
      }       
      
      @Override
      public void setDirty()
      {
         super.setDirty();
      }
      
      @Override
      public String getCookieValue()
      {
         return super.getCookieValue();
      }
      
      @Override
      public void clearCookieValue()
      {
         super.clearCookieValue();
      }
      
      @Override
      public void setCookieValueIfEnabled(String value)
      {
         super.setCookieValueIfEnabled(value);
      }
   }
   
   class TokenSelector extends UsernameSelector
   {
      @Override
      public String getCookieName()
      {
         return "org.jboss.seam.security.authtoken";
      }
   }
   
   private class DecodedToken
   {
      private String username;
      private String value;
      
      public DecodedToken(String cookieValue)
      {
         if (cookieValue != null)
         {
            try
            {
               String decoded = new String(Base64.decode(cookieValue));         
               username = decoded.substring(0, decoded.indexOf(':'));
               value = decoded.substring(decoded.indexOf(':') + 1);
            }
            catch (Exception ex)
            {
               // swallow
            }
         }
      }
      
      public String getUsername()
      {
         return username;
      }
      
      public String getValue()
      {
         return value;
      }
   }
      
   private UsernameSelector usernameSelector;
   
   private TokenSelector tokenSelector;   
   private TokenStore tokenStore;
      
   private boolean enabled;

   private int cookieMaxAge = Selector.DEFAULT_MAX_AGE;
   
   private boolean autoLoggedIn;
   
   private Random random = new Random(System.currentTimeMillis());
   
   public enum Mode { disabled, usernameOnly, autoLogin}
   
   private Mode mode = Mode.usernameOnly;
   
   public Mode getMode()
   {
      return mode;
   }
   
   public void setMode(Mode mode)
   {
      this.mode = mode;
   }
   
   public boolean isEnabled()
   {
      return enabled;
   }
   
   public void setEnabled(boolean enabled)
   {
      if (this.enabled != enabled)
      {
         this.enabled = enabled;
         // selector is null during component initialization (setup handled in @Create method)
         if (usernameSelector != null && mode.equals(Mode.usernameOnly))
         {
            usernameSelector.setCookieEnabled(enabled);
            usernameSelector.setDirty();
         }
         // selector is null during component initialization (setup handled in @Create method)
         else if (tokenSelector != null && mode.equals(Mode.autoLogin))
         {
            tokenSelector.setCookieEnabled(enabled);
            tokenSelector.setDirty();
         }
      }      
   }

   public int getCookieMaxAge() {
       return cookieMaxAge;
   }

   public void setCookieMaxAge(int cookieMaxAge) {
       this.cookieMaxAge = cookieMaxAge;
   }
   
   public TokenStore getTokenStore()
   {
      return tokenStore;
   }
   
   public void setTokenStore(TokenStore tokenStore)
   {
      this.tokenStore = tokenStore;
   }
   
   @Create
   public void create()
   {
      if (mode.equals(Mode.usernameOnly))
      {      
         usernameSelector = new UsernameSelector();
         usernameSelector.setCookieEnabled(enabled);
      }
      else if (mode.equals(Mode.autoLogin))
      {
         tokenSelector = new TokenSelector();
         tokenSelector.setCookieEnabled(enabled);

         // Default to JpaTokenStore
         if (tokenStore == null)
         {
            tokenStore = (TokenStore) Component.getInstance(JpaTokenStore.class, true);
         }         
      }
   }
   
   protected String generateTokenValue()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(new UID().toString());
      sb.append(":");
      sb.append(random.nextLong());
      return sb.toString();
   }
   
   protected String encodeToken(String username, String value)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(username);
      sb.append(":");
      sb.append(value);
      return Base64.encodeBytes(sb.toString().getBytes());      
   }
   
   public String getCookiePath()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      return ctx != null ? ctx.getExternalContext().getRequestContextPath() : null;
   }
   
   @Observer(Credentials.EVENT_INIT_CREDENTIALS)
   public void initCredentials(Credentials credentials)
   {     
      String cookiePath = getCookiePath();
      
      if (mode.equals(Mode.usernameOnly))
      {         
         if (cookiePath != null)
         {
            usernameSelector.setCookiePath(cookiePath);
         }
         
         String username = usernameSelector.getCookieValue();
         if (username!=null)
         {
            setEnabled(true);
            credentials.setUsername(username);
         }
               
         usernameSelector.setDirty();
      }
      else if (mode.equals(Mode.autoLogin))
      {
         if (cookiePath != null)
         {
            tokenSelector.setCookiePath(cookiePath);
         }
         
         String token = tokenSelector.getCookieValue();
         if (token != null)
         {
            setEnabled(true);
            
            DecodedToken decoded = new DecodedToken(token);

            if (tokenStore.validateToken(decoded.getUsername(), decoded.getValue()))
            {
               credentials.setUsername(decoded.getUsername());
               credentials.setPassword(decoded.getValue());               
            }
            else
            {
               // Have we been compromised? Just in case, invalidate all authentication tokens
               tokenStore.invalidateAll(decoded.getUsername());
            }
         }
      }
   }
   
   /**
    * I hate these hacks... 
    */
   private class BoolWrapper 
   {
      boolean value;
   }
   
   @Observer(Identity.EVENT_QUIET_LOGIN)
   public void quietLogin()
   {
      final Identity identity = Identity.instance();
      
      if (mode.equals(Mode.autoLogin) && isEnabled())
      {
         final String username = identity.getCredentials().getUsername();    
         final BoolWrapper userEnabled = new BoolWrapper();
         final List<String> roles = new ArrayList<String>();
         
         // Double check our credentials again
         if (tokenStore.validateToken(username, identity.getCredentials().getPassword()))
         {            
            new RunAsOperation(true) {
               @Override
               public void execute()
               {        
                  if (IdentityManager.instance().isUserEnabled(username))
                  {
                     userEnabled.value = true;

                     for (String role : IdentityManager.instance().getImpliedRoles(username))
                     {
                        roles.add(role);
                     }
                  }
               }
            }.run();
            
            if (userEnabled.value)
            {
               identity.unAuthenticate();
               identity.preAuthenticate();
               
               // populate the roles
               for (String role : roles)
               {
                  identity.addRole(role);
               }
   
               // Set the principal
               identity.getSubject().getPrincipals().add(new SimplePrincipal(username));
               identity.postAuthenticate();
            
               autoLoggedIn = true;
            }
         }            
      }
   }
   
   @Observer(Identity.EVENT_LOGGED_OUT)
   public void loggedOut()
   {
      if (mode.equals(Mode.autoLogin))
      {
         tokenSelector.clearCookieValue();
      }
   }
   
   @Observer(Identity.EVENT_POST_AUTHENTICATE)
   public void postAuthenticate(Identity identity)
   {
      if (mode.equals(Mode.usernameOnly))
      {
         // Password is set to null during authentication, so we set dirty
         usernameSelector.setDirty();
               
         if ( !enabled )
         {
            usernameSelector.clearCookieValue();
         }
         else
         {
            usernameSelector.setCookieMaxAge(cookieMaxAge);
            usernameSelector.setCookieValueIfEnabled( Identity.instance().getCredentials().getUsername() );
         }
      }
      else if (mode.equals(Mode.autoLogin))
      {
         tokenSelector.setDirty();
         
         DecodedToken decoded = new DecodedToken(tokenSelector.getCookieValue());
         
         // Invalidate the current token (if it exists) whether enabled or not
         if (decoded.getUsername() != null)
         {
            tokenStore.invalidateToken(decoded.getUsername(), decoded.getValue());
         }
         
         if ( !enabled ) 
         {
            tokenSelector.clearCookieValue();         
         }
         else
         {
            String value = generateTokenValue();
            tokenStore.createToken(identity.getPrincipal().getName(), value);
            tokenSelector.setCookieEnabled(enabled);
            tokenSelector.setCookieMaxAge(cookieMaxAge);
            tokenSelector.setCookieValueIfEnabled(encodeToken(identity.getPrincipal().getName(), value));            
         }
      }
   }        
   
   @Observer(Credentials.EVENT_CREDENTIALS_UPDATED)
   public void credentialsUpdated()
   {
      if (mode.equals(Mode.usernameOnly)) 
      {
         usernameSelector.setDirty();
      }      
   }      
   
   /**
    * A flag that an application can use to protect sensitive operations if the user has been
    * auto-authenticated. 
    */
   public boolean isAutoLoggedIn()
   {
      return autoLoggedIn;
   }
}
