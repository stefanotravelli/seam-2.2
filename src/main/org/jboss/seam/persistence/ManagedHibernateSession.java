//$Id$
package org.jboss.seam.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Mutable;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;
import org.jboss.seam.util.Naming;

/**
 * A Seam component that manages a conversation-scoped extended
 * persistence context that can be shared by arbitrary other
 * components.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ManagedHibernateSession 
   implements Serializable, HttpSessionActivationListener, Mutable, PersistenceContextManager, Synchronization
{
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 3130309555079841107L;

   private static final LogProvider log = Logging.getLogProvider(ManagedHibernateSession.class);
   
   private Session session;
   private String sessionFactoryJndiName;
   private String componentName;
   private ValueExpression<SessionFactory> sessionFactory;
   private List<Filter> filters = new ArrayList<Filter>(0);
   
   private transient boolean synchronizationRegistered;
   private transient boolean destroyed;
   
   public boolean clearDirty()
   {
      return true;
   }

   @Create
   public void create(Component component)
   {
      this.componentName = component.getName();
      if (sessionFactoryJndiName==null)
      {
         sessionFactoryJndiName = "java:/" + componentName;
      }
            
      PersistenceContexts.instance().touch(componentName);
   }

   private void initSession() throws Exception
   {
      session = getSessionFactoryFromJndiOrValueBinding().openSession();
      setSessionFlushMode( PersistenceContexts.instance().getFlushMode() );
      session = HibernatePersistenceProvider.proxySession(session);
      
      for (Filter f: filters)
      {
         if ( f.isFilterEnabled() )
         {
            enableFilter(f);
         }
      }

      if ( log.isDebugEnabled() )
      {
         log.debug("created seam managed session for session factory: "+ sessionFactoryJndiName);
      }
   }

   private void enableFilter(Filter f)
   {
      org.hibernate.Filter filter = session.enableFilter( f.getName() );
      for ( Map.Entry<String, ValueExpression> me: f.getParameters().entrySet() )
      {
	     Object filterValue = me.getValue().getValue();
		 if ( filterValue instanceof Collection ) {
		    filter.setParameterList(me.getKey(), (Collection) filterValue);
		 } else {
			filter.setParameter(me.getKey(), filterValue);
		}
      }
      filter.validate();
   }
   
   @Unwrap
   public Session getSession() throws Exception
   {
      if (session==null) initSession();
      
      if ( !synchronizationRegistered && !Lifecycle.isDestroying() )
      {
         joinTransaction();
      }
      
      return session;
   }

   private void joinTransaction() throws SystemException
   {
      UserTransaction transaction = Transaction.instance();
      if ( transaction.isActive() )
      {
         session.isOpen();
         try
         {
            transaction.registerSynchronization(this);
         }
         catch (Exception e)
         {
            session.getTransaction().registerSynchronization(this);
         }
         synchronizationRegistered = true;
      }
   }
   
   //we can't use @PrePassivate because it is intercept NEVER
   public void sessionWillPassivate(HttpSessionEvent event)
   {
      if (synchronizationRegistered)
      {
         throw new IllegalStateException("cannot passivate persistence context with active transaction");
      }
      if ( session!=null && session.isOpen() && !session.isDirty() )
      {
         session.close();
         session = null;
      }
   }
   
   //we can't use @PostActivate because it is intercept NEVER
   public void sessionDidActivate(HttpSessionEvent event) {}
   
   @Destroy
   public void destroy()
   {
      destroyed = true;
      if ( !synchronizationRegistered )
      {
         //in requests that come through SeamPhaseListener,
         //there can be multiple transactions per request,
         //but they are all completed by the time contexts
         //are destroyed
         //so wait until the end of the request to close
         //the session
         //on the other hand, if we are still waiting for
         //the transaction to commit, leave it open
         close();
      }
      PersistenceContexts.instance().untouch(componentName);
   }

   public void afterCompletion(int status)
   {
      synchronizationRegistered = false;
      //if ( !Contexts.isConversationContextActive() )
      if (destroyed)
      {
         //in calls to MDBs and remote calls to SBs, the 
         //transaction doesn't commit until after contexts
         //are destroyed, so wait until the transaction
         //completes before closing the session
         //on the other hand, if we still have an active
         //conversation context, leave it open
         close();
      }
   }
   
   public void beforeCompletion() {}
   
   private void close()
   {
      boolean transactionActive = false;
      try
      {
         transactionActive = Transaction.instance().isActive();
      }
      catch (SystemException se)
      {
         log.debug("could not get transaction status while destroying persistence context");
      }
      
      if ( transactionActive )
      {
         throw new IllegalStateException("attempting to destroy the persistence context while an active transaction exists (try installing <transaction:ejb-transaction/>)");
      }
      
      if ( log.isDebugEnabled() )
      {
         log.debug("destroying seam managed session for session factory: " + sessionFactoryJndiName);
      }
      if (session!=null && session.isOpen())
      {
         session.close();
      }
   }
   
   private SessionFactory getSessionFactoryFromJndiOrValueBinding()
   {
      SessionFactory result = null;
      //first try to find it via the value binding
      if (sessionFactory!=null)
      {
         result = sessionFactory.getValue();
      }
      //if its not there, try JNDI
      if (result==null)
      {
         try
         {
            result = (SessionFactory) Naming.getInitialContext().lookup(sessionFactoryJndiName);
         }
         catch (NamingException ne)
         {
            throw new IllegalArgumentException("SessionFactory not found in JNDI: " + sessionFactoryJndiName, ne);
         }
      }
      return result;
   }
   
   public String getComponentName() {
      return componentName;
   }
   
   public void changeFlushMode(FlushModeType flushMode)
   {
      if (session!=null && session.isOpen())
      {
         setSessionFlushMode(flushMode);
      }
   }

   protected void setSessionFlushMode(FlushModeType flushMode)
   {
      switch (flushMode)
      {
         case AUTO:
            session.setFlushMode(FlushMode.AUTO);
            break;
         case MANUAL:
            session.setFlushMode(FlushMode.MANUAL);
            break;
         case COMMIT:
            session.setFlushMode(FlushMode.COMMIT);
            break;
      }
   }
   
   /**
    * The JNDI name of the Hibernate SessionFactory, if it is
    * to be obtained from JNDI
    */
   public String getSessionFactoryJndiName()
   {
      return sessionFactoryJndiName;
   }

   public void setSessionFactoryJndiName(String sessionFactoryName)
   {
      this.sessionFactoryJndiName = sessionFactoryName;
   }

   /**
    * A value binding expression that returns a SessionFactory,
    * if it is to be obtained as a Seam component reference
    */
   public void setSessionFactory(ValueExpression<SessionFactory> sessionFactory)
   {
      this.sessionFactory = sessionFactory;
   }

   public ValueExpression<SessionFactory> getSessionFactory()
   {
      return sessionFactory;
   }

   /**
    * Hibernate filters to enable automatically
    */
   public List<Filter> getFilters()
   {
      return filters;
   }

   public void setFilters(List<Filter> filters)
   {
      this.filters = filters;
   }

   @Override
   public String toString()
   {
      return "ManagedHibernateSession(" + sessionFactoryJndiName + ")";
   }

}
