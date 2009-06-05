package org.jboss.seam.framework;

import javax.persistence.EntityManager;
import javax.transaction.SystemException;

import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.transaction.Transaction;

public class EntityIdentifier extends Identifier<EntityManager>
{
   public EntityIdentifier(Object entity, EntityManager entityManager)
   {
      super(PersistenceProvider.instance().getBeanClass(entity), PersistenceProvider.instance().getId(entity, entityManager));
      
   }
   
   public EntityIdentifier(Class clazz, Object id)
   {
      super(clazz, id);
   }
   
   @Override
   public Object find(EntityManager entityManager)
   {
      if (entityManager == null)
      {
         throw new NullPointerException("EntityManager must not be null");
      }
      try
      {
         Transaction.instance().enlist(entityManager);
      }
      catch (SystemException se)
      {
         throw new RuntimeException("could not join transaction", se);
      }
      return entityManager.find(getClazz(), getId());
   }
   
}