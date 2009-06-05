package org.jboss.seam.framework;


/**
 * Base class for controller objects which require a persistence
 * context object.
 * 
 * @author Gavin King
 *
 * @param <T> the persistence context class (eg. Session or EntityManager)
 */
public abstract class PersistenceController<T> extends Controller
{
   private transient T persistenceContext;
   
   public T getPersistenceContext()
   {
      if (persistenceContext==null)
      {
         persistenceContext = (T) getComponentInstance( getPersistenceContextName() );
      }
      return persistenceContext;
   }

   public void setPersistenceContext(T persistenceContext)
   {
      this.persistenceContext = persistenceContext;
   }

   protected abstract String getPersistenceContextName();

}
