package org.jboss.seam.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.MethodExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.permission.PermissionMapper;
import org.jboss.seam.web.Session;

/**
 * API for authorization and authentication via Seam security. This base 
 * implementation supports role-based authorization only. Subclasses may add 
 * more sophisticated permissioning mechanisms.
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = BUILT_IN)
@BypassInterceptors
@Startup
public class Identity implements Serializable
{  
   private static final long serialVersionUID = 3751659008033189259L;
   
   // Event keys
   public static final String EVENT_LOGIN_SUCCESSFUL = "org.jboss.seam.security.loginSuccessful";
   public static final String EVENT_LOGIN_FAILED = "org.jboss.seam.security.loginFailed";
   public static final String EVENT_NOT_LOGGED_IN = "org.jboss.seam.security.notLoggedIn";
   public static final String EVENT_NOT_AUTHORIZED = "org.jboss.seam.security.notAuthorized";
   public static final String EVENT_PRE_AUTHENTICATE = "org.jboss.seam.security.preAuthenticate";
   public static final String EVENT_POST_AUTHENTICATE = "org.jboss.seam.security.postAuthenticate";
   public static final String EVENT_LOGGED_OUT = "org.jboss.seam.security.loggedOut";
   public static final String EVENT_ALREADY_LOGGED_IN = "org.jboss.seam.security.alreadyLoggedIn";
   public static final String EVENT_QUIET_LOGIN = "org.jboss.seam.security.quietLogin";
   
   protected static boolean securityEnabled = true;
   
   public static final String ROLES_GROUP = "Roles";
   
   // Context variables
   private static final String LOGIN_TRIED = "org.jboss.seam.security.loginTried";
   private static final String SILENT_LOGIN = "org.jboss.seam.security.silentLogin";
   
   private static final LogProvider log = Logging.getLogProvider(Identity.class);
   
   private Credentials credentials;
   
   private MethodExpression authenticateMethod;

   private Principal principal;   
   private Subject subject;
   
   private RememberMe rememberMe;
   
   private transient ThreadLocal<Boolean> systemOp;
   
   private String jaasConfigName = null;
   
   private List<String> preAuthenticationRoles = new ArrayList<String>();
   
   private PermissionMapper permissionMapper;
   
   /**
    * Flag that indicates we are in the process of authenticating
    */
   private boolean authenticating = false;
         
   @Create
   public void create()
   {     
      subject = new Subject();
      
      if (Contexts.isApplicationContextActive())
      {
         permissionMapper = (PermissionMapper) Component.getInstance(PermissionMapper.class);                 
      }    
      
      if (Contexts.isSessionContextActive())
      {
         rememberMe = (RememberMe) Component.getInstance(RememberMe.class, true);      
         credentials = (Credentials) Component.getInstance(Credentials.class);         
      }
      
      if (credentials == null)
      {
         // Must have credentials for unit tests
         credentials = new Credentials();
      }
   }
   
   public static boolean isSecurityEnabled()
   {
      return securityEnabled;
   }
   
   public static void setSecurityEnabled(boolean enabled)
   {
      securityEnabled = enabled;
   }

   public static Identity instance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No active session context");
      }

      Identity instance = (Identity) Component.getInstance(Identity.class, ScopeType.SESSION);

      if (instance == null)
      {
         throw new IllegalStateException("No Identity could be created");
      }

      return instance;
   }
   
   /**
    * Simple check that returns true if the user is logged in, without attempting to authenticate
    * 
    * @return true if the user is logged in
    */
   public boolean isLoggedIn()
   {           
      // If there is a principal set, then the user is logged in.
      return getPrincipal() != null;
   }
   
   /**
    * Will attempt to authenticate quietly if the user's credentials are set and they haven't
    * authenticated already.  A quiet authentication doesn't throw any exceptions if authentication
    * fails.
    * 
    * @return true if the user is logged in, false otherwise
    */
   public boolean tryLogin()
   {
      if (!authenticating && getPrincipal() == null && credentials.isSet() &&
            Contexts.isEventContextActive() &&
            !Contexts.getEventContext().isSet(LOGIN_TRIED))
        {
           Contexts.getEventContext().set(LOGIN_TRIED, true);
           quietLogin();
        }     
        
        return isLoggedIn();      
   }
   
   @Deprecated
   public boolean isLoggedIn(boolean attemptLogin)
   {
      return attemptLogin ? tryLogin() : isLoggedIn();
   }


    public void acceptExternallyAuthenticatedPrincipal(Principal principal) {
        getSubject().getPrincipals().add(principal);
        this.principal = principal;
    }

   public Principal getPrincipal()
   {
      return principal;
   }
   
   public Subject getSubject()
   {
      return subject;
   }
      
   /**
    * Performs an authorization check, based on the specified security expression.
    * 
    * @param expr The security expression to evaluate
    * @throws NotLoggedInException Thrown if the authorization check fails and 
    * the user is not authenticated
    * @throws AuthorizationException Thrown if the authorization check fails and
    * the user is authenticated
    */
   public void checkRestriction(String expr)
   {      
      if (!securityEnabled) return;
      
      if ( !evaluateExpression(expr) )
      {
         if ( !isLoggedIn() )
         {           
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_LOGGED_IN);
            log.debug(String.format(
               "Error evaluating expression [%s] - User not logged in", expr));
            throw new NotLoggedInException();
         }
         else
         {
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_AUTHORIZED);
            throw new AuthorizationException(String.format(
               "Authorization check failed for expression [%s]", expr));
         }
      }
   }

   /**
    * Attempts to authenticate the user.  This method is distinct to the 
    * authenticate() method in that it raises events in response to whether
    * authentication is successful or not.  The following events may be raised
    * by calling login():
    * 
    * org.jboss.seam.security.loginSuccessful - raised when authentication is successful
    * org.jboss.seam.security.loginFailed - raised when authentication fails
    * org.jboss.seam.security.alreadyLoggedIn - raised if the user is already authenticated
    * 
    * @return String returns "loggedIn" if user is authenticated, or null if not.
    */
   public String login()
   {
      try
      {            
         if (isLoggedIn())
         {
            // If authentication has already occurred during this request via a silent login,
            // and login() is explicitly called then we still want to raise the LOGIN_SUCCESSFUL event,
            // and then return.
            if (Contexts.isEventContextActive() && Contexts.getEventContext().isSet(SILENT_LOGIN))
            {
               if (Events.exists()) Events.instance().raiseEvent(EVENT_LOGIN_SUCCESSFUL);
               return "loggedIn";            
            }            
            
            if (Events.exists()) Events.instance().raiseEvent(EVENT_ALREADY_LOGGED_IN);
            return "loggedIn";           
         }
         
         authenticate();
         
         if (!isLoggedIn())
         {
            throw new LoginException();
         }
         
         if ( log.isDebugEnabled() )
         {
            log.debug("Login successful for: " + getCredentials().getUsername());
         }

         if (Events.exists()) Events.instance().raiseEvent(EVENT_LOGIN_SUCCESSFUL);
         return "loggedIn";
      }
      catch (LoginException ex)
      {
         credentials.invalidate();
         
         if ( log.isDebugEnabled() )
         {
             log.debug("Login failed for: " + getCredentials().getUsername(), ex);
         }
         if (Events.exists()) Events.instance().raiseEvent(EVENT_LOGIN_FAILED, ex);
      }
      
      return null;      
   }
   
   /**
    * Attempts a quiet login, suppressing any login exceptions and not creating
    * any faces messages. This method is intended to be used primarily as an 
    * internal API call, however has been made public for convenience.
    */
   public void quietLogin()
   {
      try
      {
         if (Events.exists()) Events.instance().raiseEvent(EVENT_QUIET_LOGIN);         
          
         // Ensure that we haven't been authenticated as a result of the EVENT_QUIET_LOGIN event
         if (!isLoggedIn())
         {
            if (credentials.isSet()) 
            {
               authenticate();
               if (isLoggedIn() && Contexts.isEventContextActive())
               {
                  Contexts.getEventContext().set(SILENT_LOGIN, true);
               }
            }
         }
      }
      catch (LoginException ex) 
      { 
         credentials.invalidate();
      }
   }
   


   /**
    * 
    * @throws LoginException
    */
   public synchronized void authenticate() 
      throws LoginException
   {
      // If we're already authenticated, then don't authenticate again
      if (!isLoggedIn() && !credentials.isInvalid())
      {
         principal = null;
         subject = new Subject();
         authenticate( getLoginContext() );
      }      
   }

    
   protected void authenticate(LoginContext loginContext) 
      throws LoginException
   {
      try
      {
         authenticating = true;
         preAuthenticate();
         loginContext.login();
         postAuthenticate();
      }
      finally
      {
         // Set password to null whether authentication is successful or not
         credentials.setPassword(null);    
         authenticating = false;
      }
   }
   
   /**
    * Clears any roles added by calling addRole() while not authenticated.  
    * This method may be overridden by a subclass if different 
    * pre-authentication logic should occur.
    */
   protected void preAuthenticate()
   {     
      preAuthenticationRoles.clear();      
      if (Events.exists()) Events.instance().raiseEvent(EVENT_PRE_AUTHENTICATE);
   }   
   
   /**
    * Extracts the principal from the subject, and populates the roles of the
    * authenticated user.  This method may be overridden by a subclass if
    * different post-authentication logic should occur.
    */
   protected void postAuthenticate()
   {
      // Populate the working memory with the user's principals
      for ( Principal p : getSubject().getPrincipals() )
      {         
         if ( !(p instanceof Group))
         {
            if (principal == null) 
            {
               principal = p;
               break;
            }            
         }         
      }      
      
      if (!preAuthenticationRoles.isEmpty() && isLoggedIn())
      {
         for (String role : preAuthenticationRoles)
         {
            addRole(role);
         }
         preAuthenticationRoles.clear();
      }

      credentials.setPassword(null);
      
      if (Events.exists()) Events.instance().raiseEvent(EVENT_POST_AUTHENTICATE, this);      
   }
   
   /**
    * Resets all security state and credentials
    */
   public void unAuthenticate()
   {      
      principal = null;
      subject = new Subject();
      
      credentials.clear();
   }

   protected LoginContext getLoginContext() throws LoginException
   {
      if (getJaasConfigName() != null)
      {
         return new LoginContext(getJaasConfigName(), getSubject(), 
                  credentials.createCallbackHandler());
      }
      
      return new LoginContext(Configuration.DEFAULT_JAAS_CONFIG_NAME, getSubject(), 
            credentials.createCallbackHandler(), Configuration.instance());
   }
   
   public void logout()
   {
      if (isLoggedIn())
      {
         unAuthenticate();
         Session.instance().invalidate();
         if (Events.exists()) Events.instance().raiseEvent(EVENT_LOGGED_OUT);
      }
   }

   /**
    * Checks if the authenticated user is a member of the specified role.
    * 
    * @param role String The name of the role to check
    * @return boolean True if the user is a member of the specified role
    */
   public boolean hasRole(String role)
   {
      if (!securityEnabled) return true;
      if (systemOp != null && Boolean.TRUE.equals(systemOp.get())) return true;
      
      tryLogin();
      
      for ( Group sg : getSubject().getPrincipals(Group.class) )      
      {
         if ( ROLES_GROUP.equals( sg.getName() ) )
         {
            return sg.isMember( new Role(role) );
         }
      }
      return false;
   }
   
   /**
    * Adds a role to the authenticated user.  If the user is not logged in,
    * the role will be added to a list of roles that will be granted to the
    * user upon successful authentication, but only during the authentication
    * process.
    * 
    * @param role The name of the role to add
    */
   public boolean addRole(String role)
   {
      if (role == null || "".equals(role)) return false;
      
      if (!isLoggedIn())
      {
         preAuthenticationRoles.add(role);
         return false;
      }
      else
      {
         for ( Group sg : getSubject().getPrincipals(Group.class) )      
         {
            if ( ROLES_GROUP.equals( sg.getName() ) )
            {
               return sg.addMember(new Role(role));
            }
         }
                  
         SimpleGroup roleGroup = new SimpleGroup(ROLES_GROUP);
         roleGroup.addMember(new Role(role));
         getSubject().getPrincipals().add(roleGroup);
         return true;
      }
   }

   /**
    * Removes a role from the authenticated user
    * 
    * @param role The name of the role to remove
    */
   public void removeRole(String role)
   {     
      for ( Group sg : getSubject().getPrincipals(Group.class) )      
      {
         if ( ROLES_GROUP.equals( sg.getName() ) )
         {
            Enumeration e = sg.members();
            while (e.hasMoreElements())
            {
               Principal member = (Principal) e.nextElement();
               if (member.getName().equals(role))
               {
                  sg.removeMember(member);
                  break;
               }
            }

         }
      }      
   }   
   
   /**
    * Checks that the current authenticated user is a member of
    * the specified role.
    * 
    * @param role String The name of the role to check
    * @throws AuthorizationException if the authenticated user is not a member of the role
    */
   public void checkRole(String role)
   {
      tryLogin();
      
      if ( !hasRole(role) )
      {
         if ( !isLoggedIn() )
         {           
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_LOGGED_IN);
            throw new NotLoggedInException();
         }
         else
         {
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_AUTHORIZED);
            throw new AuthorizationException(String.format(
                  "Authorization check failed for role [%s]", role));
         }
      }
   }

   /**
    * Checks that the current authenticated user has permission for
    * the specified name and action
    * 
    * @param name String The permission name
    * @param action String The permission action
    * @param arg Object Optional object parameter used to make a permission decision
    * @throws AuthorizationException if the user does not have the specified permission
    */
   public void checkPermission(String name, String action, Object...arg)
   {
      if (systemOp != null && Boolean.TRUE.equals(systemOp.get())) return; 
      
      tryLogin();
      
      if ( !hasPermission(name, action, arg) )
      {
         if ( !isLoggedIn() )
         {
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_LOGGED_IN);
            throw new NotLoggedInException();
         }
         else
         {
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_AUTHORIZED);
            throw new AuthorizationException(String.format(
                  "Authorization check failed for permission [%s,%s]", name, action));
         }
      }
   }
   
   public void checkPermission(Object target, String action)
   {
      if (systemOp != null && Boolean.TRUE.equals(systemOp.get())) return;
      
      tryLogin();
      
      if ( !hasPermission(target, action) )
      {
         if ( !isLoggedIn() )
         {
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_LOGGED_IN);
            throw new NotLoggedInException();            
         }
         else
         {
            if (Events.exists()) Events.instance().raiseEvent(EVENT_NOT_AUTHORIZED);
            throw new AuthorizationException(String.format(
                  "Authorization check failed for permission[%s,%s]", target, action));
         }
      }
   }

   /**
    * Performs a permission check for the specified name and action
    * 
    * @param name String The permission name
    * @param action String The permission action
    * @param arg Object Optional object parameter used to make a permission decision
    * @return boolean True if the user has the specified permission
    */
   public boolean hasPermission(String name, String action, Object...arg)
   {      
      if (!securityEnabled) return true;
      if (systemOp != null && Boolean.TRUE.equals(systemOp.get())) return true;   
      if (permissionMapper == null) return false;
         
      if (arg != null)
      {
         return permissionMapper.resolvePermission(arg[0], action);
      }
      else
      {
         return permissionMapper.resolvePermission(name, action);
      }
   }   
   
   public void filterByPermission(Collection collection, String action)
   {
      permissionMapper.filterByPermission(collection, action);  
   }
   
   public boolean hasPermission(Object target, String action)
   {
      if (!securityEnabled) return true;
      if (systemOp != null && Boolean.TRUE.equals(systemOp.get())) return true;     
      if (permissionMapper == null) return false;
      if (target == null) return false;
      
      return permissionMapper.resolvePermission(target, action);
   }
   
   /**
    * Evaluates the specified security expression, which must return a boolean
    * value.
    * 
    * @param expr String The expression to evaluate
    * @return boolean The result of the expression evaluation
    */
   protected boolean evaluateExpression(String expr) 
   {         
      return Expressions.instance().createValueExpression(expr, Boolean.class).getValue();
   }   
   
   /**
    * @see org.jboss.seam.security.Credentials#getUsername()
    */
   @Deprecated
   public String getUsername()
   {
      return credentials.getUsername();
   }

   /**
    * @see org.jboss.seam.security.Credentials#setUsername(String)
    */
   @Deprecated
   public void setUsername(String username)
   {  
      credentials.setUsername(username);
   }

   /**
    * @see org.jboss.seam.security.Credentials#getPassword()
    */
   @Deprecated
   public String getPassword()
   {
      return credentials.getPassword();
   }
   
   /**
    * @see org.jboss.seam.security.Credentials#setPassword(String)
    */   
   @Deprecated
   public void setPassword(String password)
   {
      credentials.setPassword(password);
   }   
   
   /**
    * @see org.jboss.seam.security.RememberMe#isEnabled()
    */
   @Deprecated
   public boolean isRememberMe()
   {
      return rememberMe != null ? rememberMe.isEnabled() : false;
   }
   
   /**
    * @see org.jboss.seam.security.RememberMe#setEnabled(boolean)
    */
   @Deprecated
   public void setRememberMe(boolean remember)
   {
      if (rememberMe != null) rememberMe.setEnabled(remember);
   }   
   
   public Credentials getCredentials()
   {
      return credentials;
   }   
   
   public MethodExpression getAuthenticateMethod()
   {
      return authenticateMethod;
   }
   
   public void setAuthenticateMethod(MethodExpression authMethod)
   {
      this.authenticateMethod = authMethod;
   }
   
   public String getJaasConfigName()
   {
      return jaasConfigName;
   }
   
   public void setJaasConfigName(String jaasConfigName)
   {
      this.jaasConfigName = jaasConfigName;
   }
   
   synchronized void runAs(RunAsOperation operation)
   {
      Principal savedPrincipal = getPrincipal();
      Subject savedSubject = getSubject();
      
      try
      {
         principal = operation.getPrincipal();
         subject = operation.getSubject();
         
         if (systemOp == null)
         {
            systemOp = new ThreadLocal<Boolean>();
         }
         
         systemOp.set(operation.isSystemOperation());
         
         operation.execute();
      }
      finally
      {
         systemOp.set(false);
         principal = savedPrincipal;
         subject = savedSubject;
      }
   } 
}
