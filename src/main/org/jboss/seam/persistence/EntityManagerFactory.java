//$Id$
package org.jboss.seam.persistence;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Persistence;

import org.hibernate.cfg.Environment;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Naming;

/**
 * A Seam component that bootstraps an EntityManagerFactory,
 * for use of JPA outside of Java EE 5 / Embedded JBoss.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Startup
public class EntityManagerFactory
{
   private javax.persistence.EntityManagerFactory entityManagerFactory;

   private String persistenceUnitName;
   private Map<String, String> persistenceUnitProperties;
   
   @Unwrap
   public javax.persistence.EntityManagerFactory getEntityManagerFactory()
   {
      return entityManagerFactory;
   }
   
   @Create
   public void startup(Component component) throws Exception
   {
      if (persistenceUnitName==null)
      {
         persistenceUnitName = component.getName();
      }
      entityManagerFactory = createEntityManagerFactory();
   }

   @Destroy
   public void shutdown()
   {
      if (entityManagerFactory!=null)
      {
         entityManagerFactory.close();
      }
   }
   
   protected javax.persistence.EntityManagerFactory createEntityManagerFactory()
   {
      Map properties = new HashMap();
      Hashtable<String, String> jndiProperties = Naming.getInitialContextProperties();
      if ( jndiProperties!=null )
      {
         // Prefix regular JNDI properties for Hibernate
         for (Map.Entry<String, String> entry : jndiProperties.entrySet())
         {
            properties.put( Environment.JNDI_PREFIX + "." + entry.getKey(), entry.getValue() );
         }
      }
      if (persistenceUnitProperties!=null)
      {
         properties.putAll(persistenceUnitProperties);
      }

      if ( properties.isEmpty() )
      {
         return Persistence.createEntityManagerFactory(persistenceUnitName);
      }
      else
      {
         return Persistence.createEntityManagerFactory(persistenceUnitName, properties);
      }
   }
   
   /**
    * The persistence unit name
    */
   public String getPersistenceUnitName()
   {
      return persistenceUnitName;
   }

   public void setPersistenceUnitName(String persistenceUnitName)
   {
      this.persistenceUnitName = persistenceUnitName;
   }

   /**
    * Properties to pass to Persistence.createEntityManagerFactory()
    */
   public Map<String, String> getPersistenceUnitProperties()
   {
      return persistenceUnitProperties;
   }

   public void setPersistenceUnitProperties(Map<String, String> persistenceUnitProperties)
   {
      this.persistenceUnitProperties = persistenceUnitProperties;
   }

}
