package org.jboss.seam.framework;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.TransientObjectException;
import org.jboss.seam.annotations.Transactional;

/**
 * Base class for Home objects for Hibernate entities.
 * 
 * @author Gavin King
 *
 */
public class HibernateEntityHome<E> extends Home<Session, E>
{
   
   private static final long serialVersionUID = 6071072408602519385L;
   
   @Override
   public void create()
   {
      super.create();
      if ( getSession()==null )
      {
         throw new IllegalStateException("hibernateSession is null");
      }
   }
   
   @Transactional
   public boolean isManaged()
   {
      return getInstance()!=null && 
            getSession().contains( getInstance() );
   }
   
   @Transactional
   public String update()
   {
      getSession().flush();
      updatedMessage();
      raiseAfterTransactionSuccessEvent();
      return "updated";
   }
   
   @Transactional
   public String persist()
   {
      getSession().persist( getInstance() );
      getSession().flush();
      assignId( getSession().getIdentifier( getInstance() ) );
      createdMessage();
      raiseAfterTransactionSuccessEvent();
      return "persisted";
   }
   
   @Transactional
   public String remove()
   {
      getSession().delete( getInstance() );
      getSession().flush();
      deletedMessage();
      raiseAfterTransactionSuccessEvent();
      return "removed";
   }
    
    @Transactional
    @Override
    public E find()
    {
        if (getSession().isOpen()) {
            E result = loadInstance();
            if (result==null) {
                result = handleNotFound();
            }
            return result;
        } else {
            return null;
        }
    }

    protected E loadInstance() 
    {
        return (E) getSession().get(getEntityClass(), (Serializable) getId());   
    }

   @Override
   protected void joinTransaction()
   {
      getSession().isOpen();
   }
   
   public Session getSession()
   {
      return getPersistenceContext();
   }
   
   public void setSession(Session session)
   {
      setPersistenceContext(session);
   }
   
   @Override
   protected String getPersistenceContextName()
   {
      return "hibernateSession";
   }
   
   @Override
   protected String getEntityName()
   {
      try
      {
         return getSession().getEntityName(getInstance());
      }
      catch (TransientObjectException e) 
      {
         return getSession().getSessionFactory().getClassMetadata(getInstance().getClass()).getEntityName();
      }
   }
   
}
