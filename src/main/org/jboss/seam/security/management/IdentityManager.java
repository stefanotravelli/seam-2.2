package org.jboss.seam.security.management;

import static org.jboss.seam.ScopeType.EVENT;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

/**
 * Identity Management API, deals with user name/password-based identity management.
 * 
 * @author Shane Bryzak
 */
@Scope(EVENT)
@Name("org.jboss.seam.security.identityManager")
@Install(precedence = BUILT_IN)
@BypassInterceptors
public class IdentityManager implements Serializable
{
   public static final String USER_PERMISSION_NAME = "seam.user";
   public static final String ROLE_PERMISSION_NAME = "seam.role";
   
   public static final String PERMISSION_CREATE = "create";
   public static final String PERMISSION_READ = "read";
   public static final String PERMISSION_UPDATE = "update";
   public static final String PERMISSION_DELETE = "delete";
   
   private static final LogProvider log = Logging.getLogProvider(IdentityManager.class);   
   
   private IdentityStore identityStore;
   private IdentityStore roleIdentityStore;
   
   @Create
   public void create()
   {
      initIdentityStore();
   }
   
   protected void initIdentityStore()
   {    
      // Default to JpaIdentityStore
      if (identityStore == null)
      {
         identityStore = (IdentityStore) Component.getInstance(JpaIdentityStore.class, true);
      }
      
      if (roleIdentityStore == null && identityStore != null)
      {
         roleIdentityStore = identityStore;
      }            
      
      if (identityStore == null || roleIdentityStore == null)
      {
         log.warn("no identity store available - please configure an identityStore if identity " +
               "management is required.");
      }
   }  
   
   public static IdentityManager instance()
   {
      if ( !Contexts.isEventContextActive() )
      {
         throw new IllegalStateException("No active event context");
      }

      IdentityManager instance = (IdentityManager) Component.getInstance(
            IdentityManager.class, EVENT);

      if (instance == null)
      {
         throw new IllegalStateException("No IdentityManager could be created");
      }

      return instance;
   }
   
   public boolean createUser(String name, String password)
   {
      return createUser(name, password, null, null);
   }

   public boolean createUser(String name, String password, String firstname, String lastname)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_CREATE);
      return identityStore.createUser(name, password, firstname, lastname); 
   }   
   
   public boolean deleteUser(String name)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_DELETE);
      return identityStore.deleteUser(name);
   }
   
   public boolean enableUser(String name)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_UPDATE);
      return identityStore.enableUser(name);
   }
   
   public boolean disableUser(String name)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_UPDATE);
      return identityStore.disableUser(name);
   }
   
   public boolean changePassword(String name, String password)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_UPDATE);
      return identityStore.changePassword(name, password);
   }
   
   public boolean isUserEnabled(String name)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_READ);
      return identityStore.isUserEnabled(name);
   }
   
   public boolean grantRole(String name, String role)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_UPDATE);
      return roleIdentityStore.grantRole(name, role);
   }
   
   public boolean revokeRole(String name, String role)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_UPDATE);
      return roleIdentityStore.revokeRole(name, role);
   }
   
   public boolean createRole(String role)
   {
      Identity.instance().checkPermission(ROLE_PERMISSION_NAME, PERMISSION_CREATE);
      return roleIdentityStore.createRole(role);
   }
   
   public boolean deleteRole(String role)
   {
      Identity.instance().checkPermission(ROLE_PERMISSION_NAME, PERMISSION_DELETE);
      return roleIdentityStore.deleteRole(role);
   }
   
   public boolean addRoleToGroup(String role, String group)
   {
      Identity.instance().checkPermission(ROLE_PERMISSION_NAME, PERMISSION_UPDATE);
      return roleIdentityStore.addRoleToGroup(role, group);
   }
   
   public boolean removeRoleFromGroup(String role, String group)
   {
      Identity.instance().checkPermission(ROLE_PERMISSION_NAME, PERMISSION_UPDATE);
      return roleIdentityStore.removeRoleFromGroup(role, group);      
   }
   
   public boolean userExists(String name)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_READ);
      return identityStore.userExists(name);
   }
   
   public boolean roleExists(String name)
   {
      return roleIdentityStore.roleExists(name);      
   }
   
   public List<String> listUsers()
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_READ);
      List<String> users = identityStore.listUsers();      
      
      Collections.sort(users, new Comparator<String>() {
         public int compare(String value1, String value2) {
            return value1.compareTo(value2);
         }
      });
      
      return users;
   }
   
   public List<String> listUsers(String filter)
   {
      Identity.instance().checkPermission(USER_PERMISSION_NAME, PERMISSION_READ);
      List<String> users = identityStore.listUsers(filter);
      
      Collections.sort(users, new Comparator<String>() {
         public int compare(String value1, String value2) {
            return value1.compareTo(value2);
         }
      });
      
      return users;      
   }
   
   public List<String> listRoles()
   {      
      Identity.instance().checkPermission(ROLE_PERMISSION_NAME, PERMISSION_READ);
      List<String> roles = roleIdentityStore.listRoles();
      
      Collections.sort(roles, new Comparator<String>() {
         public int compare(String value1, String value2) {
            return value1.compareTo(value2);
         }
      });
      
      return roles;      
   }
   
   public List<String> listGrantableRoles()
   {
      List<String> roles = roleIdentityStore.listGrantableRoles();
      
      Collections.sort(roles, new Comparator<String>() {
         public int compare(String value1, String value2) {
            return value1.compareTo(value2);
         }
      });
      
      return roles; 
   }
   
   /**
    * Returns a list of the roles that are explicitly granted to the specified user;
    * 
    * @param name The user for which to return a list of roles
    * @return List containing the names of the granted roles
    */
   public List<String> getGrantedRoles(String name)
   {
      return roleIdentityStore.getGrantedRoles(name);
   }
   
   /**
    * Returns a list of roles that are either explicitly or indirectly granted to the specified user. 
    * 
    * @param name The user for which to return the list of roles
    * @return List containing the names of the implied roles
    */
   public List<String> getImpliedRoles(String name)
   {
      return roleIdentityStore.getImpliedRoles(name);
   }
   
   public List<Principal> listMembers(String role)
   {
      Identity.instance().checkPermission(ROLE_PERMISSION_NAME, PERMISSION_READ);      
      return roleIdentityStore.listMembers(role);
   }
   
   public List<String> getRoleGroups(String name)
   {
      return roleIdentityStore.getRoleGroups(name);
   }
   
   public boolean authenticate(String username, String password)
   {
      if (Strings.isEmpty(username)) return false;      
      return identityStore.authenticate(username, password);
   }
   
   public IdentityStore getIdentityStore()
   {
      return identityStore;
   }
   
   public void setIdentityStore(IdentityStore identityStore)
   {
      this.identityStore = identityStore;
   }
   
   public IdentityStore getRoleIdentityStore()
   {
      return roleIdentityStore;
   }
   
   public void setRoleIdentityStore(IdentityStore roleIdentityStore)
   {
      this.roleIdentityStore = roleIdentityStore;
   }
   
   public boolean isEnabled()
   {
      return identityStore != null && roleIdentityStore != null;
   }
   
}
