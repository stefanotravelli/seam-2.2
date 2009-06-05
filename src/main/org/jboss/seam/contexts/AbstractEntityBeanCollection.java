package org.jboss.seam.contexts;

abstract class AbstractEntityBeanCollection implements Wrapper
{
   private transient boolean initialized;
   
   protected AbstractEntityBeanCollection()
   {
      initialized = true;
   }
   
   public final void activate()
   {
      if ( isPassivatedEntitiesInitialized() && isAnyVersioned() )
      {
         activateAll();
         initialized = true;
      }
      else
      {
         initialized = false;
      }
   }

   public final Object getInstance()
   {
      if ( !initialized && isPassivatedEntitiesInitialized() )
      {
         activateAll();
      }
      initialized = true;
      return getEntityCollection();
   }
   
   public final boolean passivate()
   {
      if ( PassivatedEntity.isTransactionRolledBackOrMarkedRollback() )
      {
         clearPassivatedEntities();
      }
      else
      {
         passivateAll();
      }
      return true;
   }

   private boolean isAnyVersioned()
   {
      for ( PassivatedEntity passivatedEntity: getPassivatedEntities() )
      {
         if ( passivatedEntity!=null && passivatedEntity.isVersioned() ) return true;
      }
      return false;
   }
   
   protected abstract void activateAll();
   protected abstract void passivateAll();
   protected abstract Iterable<PassivatedEntity> getPassivatedEntities();
   protected abstract void clearPassivatedEntities();
   protected abstract boolean isPassivatedEntitiesInitialized();
   protected abstract Object getEntityCollection();

}
