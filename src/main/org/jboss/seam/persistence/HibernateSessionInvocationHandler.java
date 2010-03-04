package org.jboss.seam.persistence;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * InvocationHandler that proxies the Session, and implements EL interpolation
 * in HQL. Needs to implement SessionImplementor because DetachedCriteria casts
 * the Session to SessionImplementor.
 * 
 * @author Gavin King
 * @author Emmanuel Bernard
 * @author Mike Youngstrom
 * 
 */
public class HibernateSessionInvocationHandler implements InvocationHandler, Serializable
{
   
   private Session delegate;
   
   public HibernateSessionInvocationHandler(Session delegate)
   {
      this.delegate = delegate;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      try
      {
         if ("createQuery".equals(method.getName()) && method.getParameterTypes().length > 0 && method.getParameterTypes()[0].equals(String.class))
         {
            return handleCreateQueryWithString(method, args);
         }
         if ("reconnect".equals(method.getName()) && method.getParameterTypes().length == 0)
         {
            return handleReconnectNoArg(method);
         }
         return method.invoke(delegate, args);
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
   }
   
   protected Object handleCreateQueryWithString(Method method, Object[] args) throws Throwable
   {
      if (args[0] == null)
      {
         return method.invoke(delegate, args);
      }
      String ejbql = (String) args[0];
      if (ejbql.indexOf('#') > 0)
      {
         QueryParser qp = new QueryParser(ejbql);
         Object[] newArgs = args.clone();
         newArgs[0] = qp.getEjbql();
         Query query = (Query) method.invoke(delegate, newArgs);
         for (int i = 0; i < qp.getParameterValueBindings().size(); i++)
         {
            query.setParameter(QueryParser.getParameterName(i), qp.getParameterValueBindings().get(i).getValue());
         }
         return query;
      }
      else
      {
         return method.invoke(delegate, args);
      }
   }
   
   protected Object handleReconnectNoArg(Method method) throws Throwable
   {
      throw new UnsupportedOperationException("deprecated");
   }
}
