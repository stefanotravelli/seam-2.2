package org.jboss.seam.ioc.spring;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

/**
 * A FactoryBean the constructs EntityManagerFactories that proxy to a Seam
 * ManagedPersistenceContext.
 * 
 * @author Mike Youngstrom
 */
public class SeamManagedEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean
{
   private String persistenceContextName;

   @Override
   protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException
   {
      return new SeamManagedEntityManagerFactory(persistenceContextName);
   }

   @Override
   public String getPersistenceUnitName()
   {
      String persistenceUnitName = super.getPersistenceUnitName();
      if (persistenceUnitName == null || "".equals(persistenceUnitName))
      {
         return persistenceContextName;
      }
      return persistenceUnitName;
   }

   public void setPersistenceContextName(String persistenceContextName)
   {
      this.persistenceContextName = persistenceContextName;
   }
}