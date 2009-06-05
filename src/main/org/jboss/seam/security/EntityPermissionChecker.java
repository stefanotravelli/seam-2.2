package org.jboss.seam.security;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.util.Strings;

/**
 * Entity permission checks
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.entityPermissionChecker")
@Scope(APPLICATION)
@Install(precedence = BUILT_IN)
@BypassInterceptors
@Startup
public class EntityPermissionChecker
{
   private String entityManagerName = "entityManager";
   
   private EntityManager getEntityManager()
   {
      return (EntityManager) Component.getInstance(entityManagerName);
   }
   
   public String getEntityManagerName()
   {
      return entityManagerName;
   }
   
   public void setEntityManagerName(String name)
   {
      this.entityManagerName = name;
   } 
   
   public static EntityPermissionChecker instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application context");
      }

      EntityPermissionChecker instance = (EntityPermissionChecker) Component.getInstance(
            EntityPermissionChecker.class, ScopeType.APPLICATION);

      if (instance == null)
      {
         throw new IllegalStateException("No EntityPermissionChecker could be created");
      }

      return instance;      
   }
   
   public void checkEntityPermission(Object entity, EntityAction action)
   {      
      if (!Identity.isSecurityEnabled()) return;
      
      if (!Contexts.isSessionContextActive()) return;
      
      Identity identity = Identity.instance();
      
      identity.tryLogin();
      
      PersistenceProvider provider = PersistenceProvider.instance(); 
      Class beanClass = provider.getBeanClass(entity);
      
      if (beanClass != null)
      {        
         Method m = null;
         switch (action)
         {
            case READ:
               m = provider.getPostLoadMethod(entity, getEntityManager());
               break;
            case INSERT:
               m = provider.getPrePersistMethod(entity, getEntityManager());
               break;
            case UPDATE:
               m = provider.getPreUpdateMethod(entity, getEntityManager());
               break;
            case DELETE:
               m = provider.getPreRemoveMethod(entity, getEntityManager());
         }
         
         Restrict restrict = null;
         
         if (m != null && m.isAnnotationPresent(Restrict.class))
         {
            restrict = m.getAnnotation(Restrict.class);
         }
         else if (entity.getClass().isAnnotationPresent(Restrict.class))
         {
            restrict = entity.getClass().getAnnotation(Restrict.class);
         }

         if (restrict != null)
         {
            if (Strings.isEmpty(restrict.value()))
            {
               identity.checkPermission(entity, action.toString());
            }
            else
            {
               identity.checkRestriction(restrict.value());
            }
         }
      }
   }  
}
