package org.jboss.seam.security.permission;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.Role;
import org.jboss.seam.security.SimplePrincipal;

/**
 * Resolves dynamically-assigned permissions, mapped to a user or a role, and kept in persistent 
 * storage, such as a relational database.
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.persistentPermissionResolver")
@Scope(APPLICATION)
@BypassInterceptors
@Install(precedence=BUILT_IN)
@Startup
public class PersistentPermissionResolver implements PermissionResolver, Serializable
{      
   private PermissionStore permissionStore;
   
   private static final LogProvider log = Logging.getLogProvider(PersistentPermissionResolver.class);   
   
   @Create
   public void create()
   {
      initPermissionStore();
   }
   
   protected void initPermissionStore()
   {
      if (permissionStore == null)
      {
         permissionStore = (PermissionStore) Component.getInstance(JpaPermissionStore.class, true);
      }           
      
      if (permissionStore == null)
      {
         log.warn("no permission store available - please install a PermissionStore with the name '" +
               Seam.getComponentName(JpaPermissionStore.class) + "' if persistent permissions are required.");
      }
   }     
   
   public PermissionStore getPermissionStore()
   {
      return permissionStore;
   }
   
   public void setPermissionStore(PermissionStore permissionStore)
   {
      this.permissionStore = permissionStore;
   }
   
   public boolean hasPermission(Object target, String action)
   {      
      if (permissionStore == null) return false;
      
      Identity identity = Identity.instance();
      
      if (!identity.isLoggedIn()) return false;      
      
      List<Permission> permissions = permissionStore.listPermissions(target, action);
      
      String username = identity.getPrincipal().getName();
      
      for (Permission permission : permissions)
      {
         if (permission.getRecipient() instanceof SimplePrincipal &&
               username.equals(permission.getRecipient().getName()))
         {
            return true;
         }
         
         if (permission.getRecipient() instanceof Role)
         {
            Role role = (Role) permission.getRecipient();
            
            if (role.isConditional())
            {
               RuleBasedPermissionResolver resolver = RuleBasedPermissionResolver.instance();
               if (resolver.checkConditionalRole(role.getName(), target, action)) return true;               
            }
            else if (identity.hasRole(role.getName()))
            {
               return true;
            }
         }
      }      
      
      return false;
   }
   
   public void filterSetByAction(Set<Object> targets, String action)
   {
      if (permissionStore == null) return;
      
      Identity identity = Identity.instance();
      if (!identity.isLoggedIn()) return;
      
      List<Permission> permissions = permissionStore.listPermissions(targets, action);
      
      String username = identity.getPrincipal().getName();
      
      Iterator iter = targets.iterator();
      while (iter.hasNext())
      {
         Object target = iter.next();
         
         for (Permission permission : permissions)
         {
            if (permission.getTarget().equals(target))
            {
               if (permission.getRecipient() instanceof SimplePrincipal &&
                     username.equals(permission.getRecipient().getName()))
               {
                  iter.remove();
                  break;
               }
               
               if (permission.getRecipient() instanceof Role)
               {
                  Role role = (Role) permission.getRecipient();
                  
                  if (role.isConditional())
                  {
                     RuleBasedPermissionResolver resolver = RuleBasedPermissionResolver.instance();
                     if (resolver.checkConditionalRole(role.getName(), target, action))
                     {
                        iter.remove();
                        break;
                     }
                  }
                  else if (identity.hasRole(role.getName()))
                  {
                     iter.remove();
                     break;
                  }
               }               
            }
         }
      }
   }
}
