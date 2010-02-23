package org.jboss.seam.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.security.permission.PermissionManager;

/**
 * InvocationHandler that Proxies the EntityManager, and implements EL
 * interpolation in JPA-QL
 * 
 * @author Gavin King
 * @author Mike Youngstrom
 */
public class EntityManagerInvocationHandler implements InvocationHandler
{
   
   private EntityManager delegate;
   
   public EntityManagerInvocationHandler(EntityManager delegate)
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
         if ("getDelegate".equals(method.getName()))
         {
            return handleGetDelegate(method, args);
         }
         if ("remove".equals(method.getName()) && method.getParameterTypes().length > 0)
         {
            return handleRemove(method, args);
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
   
   protected Object handleGetDelegate(Method method, Object[] args) throws Throwable
   {
      return PersistenceProvider.instance().proxyDelegate(method.invoke(delegate, args));
   }
   
   protected Object handleRemove(Method method, Object[] args) throws Throwable
   {
      if (args.length == 0)
      {
         return method.invoke(delegate, args);
      }
      Object result = method.invoke(delegate, args);
      PermissionManager.instance().clearPermissions(args[0]);
      return result;
   }
}
