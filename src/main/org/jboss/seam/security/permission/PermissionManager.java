package org.jboss.seam.security.permission;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;

/**
 * Permission management component, used to grant or revoke permissions on specific objects or of
 * specific permission types to particular users or roles.
 * 
 * @author Shane Bryzak
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.security.permissionManager")
@Install(precedence = BUILT_IN)
public class PermissionManager implements Serializable
{
   public static final String PERMISSION_STORE_COMPONENT_NAME = "org.jboss.seam.security.jpaPermissionStore";
   
   public static final String PERMISSION_PERMISSION_NAME = "seam.permission";
   
   public static final String PERMISSION_READ = "seam.read-permissions";
   public static final String PERMISSION_GRANT = "seam.grant-permission";
   public static final String PERMISSION_REVOKE = "seam.revoke-permission";   
   
   private static final LogProvider log = Logging.getLogProvider(PermissionManager.class);
   
   private PermissionStore permissionStore;
   
   @Create
   public void create()
   {
      if (permissionStore == null)
      {
         permissionStore = (PermissionStore) Component.getInstance(PERMISSION_STORE_COMPONENT_NAME, true);
      }         
      
      if (permissionStore == null)
      {
         log.warn("no permission store available - please install a PermissionStore with the name '" +
               PERMISSION_STORE_COMPONENT_NAME + "' if permission management is required.");
      }
   } 
   
   public static PermissionManager instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application context");
      }

      PermissionManager instance = (PermissionManager) Component.getInstance(
            PermissionManager.class, ScopeType.APPLICATION);

      if (instance == null)
      {
         throw new IllegalStateException("No PermissionManager could be created");
      }

      return instance;
   }
   
   public PermissionStore getPermissionStore()
   {
      return permissionStore;
   }
   
   public void setPermissionStore(PermissionStore permissionStore)
   {
      this.permissionStore = permissionStore;
   }
   
   public List<Permission> listPermissions(Object target, String action)
   {
      if (target == null) return null;      
      Identity.instance().checkPermission(target, PERMISSION_READ);
      return permissionStore.listPermissions(target, action);
   }
   
   public List<Permission> listPermissions(Object target)
   {
      if (target == null) return null;
      Identity.instance().checkPermission(target, PERMISSION_READ);
      return permissionStore.listPermissions(target);
   }
   
   public boolean grantPermission(Permission permission)
   {
      Identity.instance().checkPermission(permission.getTarget(), PERMISSION_GRANT);
      return permissionStore.grantPermission(permission);
   }
   
   public boolean grantPermissions(List<Permission> permissions)
   {
      for (Permission permission : permissions)
      {
         Identity.instance().checkPermission(permission.getTarget(), PERMISSION_GRANT);
      }
      return permissionStore.grantPermissions(permissions);
   }
   
   public boolean revokePermission(Permission permission)
   {
      Identity.instance().checkPermission(permission.getTarget(), PERMISSION_REVOKE);
      return permissionStore.revokePermission(permission);
   }
   
   public boolean revokePermissions(List<Permission> permissions)
   {
      for (Permission permission : permissions)
      {
         Identity.instance().checkPermission(permission.getTarget(), PERMISSION_REVOKE);
      }
      return permissionStore.revokePermissions(permissions);
   }
   
   public List<String> listAvailableActions(Object target)
   {
      return permissionStore.listAvailableActions(target);
   }
   
   public void clearPermissions(Object target)
   {
      if (permissionStore != null)
      {
         permissionStore.clearPermissions(target);
      }
   }
}
