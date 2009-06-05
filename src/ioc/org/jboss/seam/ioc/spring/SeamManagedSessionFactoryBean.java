package org.jboss.seam.ioc.spring;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.seam.Component;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.util.ClassUtils;

/**
 * A SessionFactory that delegates requests to open a Session to a
 * "managed-hibernate-session".
 *
 * @author Mike Youngstrom
 */
public class SeamManagedSessionFactoryBean extends AbstractFactoryBean
{
   private String sessionName;

   private SessionFactory baseSessionFactory;

   @Override
   public void afterPropertiesSet() throws Exception
   {
      super.afterPropertiesSet();
      if (sessionName == null || "".equals(sessionName))
      {
         throw new IllegalArgumentException("SesssionName cannot be empty");
      }
   }

   @Override
   protected Object createInstance() throws Exception
   {
      Class[] sessionFactoryInterfaces;
      if (baseSessionFactory != null)
      {
         sessionFactoryInterfaces = ClassUtils.getAllInterfaces(baseSessionFactory);
      }
      else
      {
         sessionFactoryInterfaces = new Class[] { SessionFactory.class };
      }
      // Create proxy of SessionFactory to implement all interfaces the
      // baseSessionFactory did.
      return Proxy.newProxyInstance(getClass().getClassLoader(), sessionFactoryInterfaces,
               new SeamManagedSessionFactoryHandler(sessionName, baseSessionFactory));
   }

   @Override
   public Class getObjectType()
   {
      if (baseSessionFactory != null)
      {
         return baseSessionFactory.getClass();
      }
      return SessionFactory.class;
   }

   /**
    * Optionally provide an instance of the SessionFactory we are wrapping. Only
    * necessary if the proxy needs to expose access to any interfaces besides
    * SessionFactory.class.
    *
    * @param baseSessionFactory
    */
   public void setBaseSessionFactory(SessionFactory baseSessionFactory)
   {
      this.baseSessionFactory = baseSessionFactory;
   }

   /**
    * The name of the Seam "managed-hibernate-session" component.
    *
    * @param sessionName
    */
   @Required
   public void setSessionName(String sessionName)
   {
      this.sessionName = sessionName;
   }

   /**
    * Proxy for a SessionFactory. Returning a close suppressing proxy on calls
    * to "openSession".
    *
    * @author Mike Youngstrom
    *
    */
   public static class SeamManagedSessionFactoryHandler implements InvocationHandler, Serializable
   {

      private SessionFactory rawSessionFactory;

      private String sessionName;

      private boolean isClosed = false;

      public SeamManagedSessionFactoryHandler(String sessionName, SessionFactory rawSessionFactory)
      {
         this.rawSessionFactory = rawSessionFactory;
         this.sessionName = sessionName;
      }

      private synchronized SessionFactory getRawSessionFactory()
      {
         if (rawSessionFactory == null)
         {
            rawSessionFactory = getSession().getSessionFactory();
         }
         return rawSessionFactory;
      }

      private Session getSession()
      {
         SeamLifecycleUtils.beginTransactionalSeamCall();
         return (Session) Component.getInstance(sessionName);
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         if (method.getName().equals("equals"))
         {
            // Only consider equal when proxies are identical.
            return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
         }
         if (method.getName().equals("hashCode"))
         {
            // Use hashCode of SessionFactory proxy.
            return new Integer(hashCode());
         }
         SessionFactory delegate = getRawSessionFactory();
         if (method.getName().equals("isClosed"))
         {
            return delegate.isClosed() || isClosed;
         }
         if (delegate.isClosed())
         {
            // Defer to delegate error if it's closed.
            try
            {
               return method.invoke(delegate, args);
            }
            catch (InvocationTargetException ex)
            {
               throw ex.getTargetException();
            }
         }
         if (isClosed)
         {
            throw new IllegalStateException("This SessionFactory is closed.");
         }
         if (method.getName().equals("close"))
         {
            Session session = getSession();
            session.disconnect();
            isClosed = true;
            return null;
         }

         if (method.getName().equals("getCurrentSession"))
         {
            try
            {
               return SessionFactoryUtils.doGetSession((SessionFactory) proxy, false);
            }
            catch (IllegalStateException ex)
            {
               throw new HibernateException(ex.getMessage());
            }
         }
         if (method.getName().equals("openSession"))
         {
            if (method.getParameterTypes().length == 0)
            {
               Session session = getSession();
               // Return close suppressing Session Proxy that implements all
               // interfaces the original did
               ClassUtils.getAllInterfaces(session);
               List<Class> interfaces = new ArrayList<Class>(Arrays.asList(ClassUtils
                        .getAllInterfaces(session)));
               //Have to bend Session implementation since HiberanteSessionProxy doesn't implement classic.Session.
               interfaces.add(org.hibernate.classic.Session.class);
               return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces
                        .toArray(new Class[interfaces.size()]), new SeamManagedSessionHandler(
                        (SessionFactory) proxy, session));
            }
            else
            {
               throw new HibernateException(
                        "This SeamManagedSessionFactory will only return a session from a call to noarg openSession()");
            }
         }
         if (method.getName().equals("openStatelessSession"))
         {
            throw new HibernateException("This SessionFactory does not support StatelessSessions");
         }
         if (method.getName().equals("getReference"))
         {
            throw new HibernateException(
                     "A SeamManagedSessionFactory is not referencable.  If this is a requirement file a feature request.");
         }
         try
         {
            if(method.getDeclaringClass().equals(org.hibernate.classic.Session.class) &&
                     !(delegate instanceof org.hibernate.classic.Session)) {
               throw new UnsupportedOperationException("Unable to execute method: "+method.toString()+" Seam managed session does not support classic.Session methods.");
            }
            return method.invoke(delegate, args);
         }
         catch (InvocationTargetException ex)
         {
            throw ex.getTargetException();
         }
      }
   }

   /**
    * Delegates calls to a hibernate session and suppresses calls to close.
    *
    * @author Mike Youngstrom
    */
   public static class SeamManagedSessionHandler implements InvocationHandler, Serializable
   {
      private static final LogProvider log = Logging
               .getLogProvider(SeamManagedSessionHandler.class);

      private Session delegate;

      private SessionFactory sessionFactory;

      private boolean closed = false;

      public SeamManagedSessionHandler(SessionFactory sessionFactory, Session delegate)
      {
         super();
         this.delegate = delegate;
         this.sessionFactory = sessionFactory;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         if (method.getName().equals("getSessionFactory"))
         {
            return sessionFactory;
         }
         else if (method.getName().equals("equals"))
         {
            // Only consider equal when proxies are identical.
            return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
         }
         else if (method.getName().equals("hashCode"))
         {
            // Use hashCode of Session proxy.
            return new Integer(hashCode());
         }
         if (method.getName().equals("isOpen"))
         {
            return delegate.isOpen() && !closed;
         }
         if (!delegate.isOpen())
         {
            // Defer to delegate error if it's closed.
            try
            {
               return method.invoke(delegate, args);
            }
            catch (InvocationTargetException ex)
            {
               throw ex.getTargetException();
            }
         }
         if (closed)
         {
            throw new IllegalStateException("This Session is closed.");
         }
         if (method.getName().equals("close"))
         {
            log.debug("Closing Session Proxy.");
            delegate.disconnect();
            closed = true;
            return null;
         }

         try
         {
            return method.invoke(delegate, args);
         }
         catch (InvocationTargetException ex)
         {
            throw ex.getTargetException();
         }
      }
   }

}
