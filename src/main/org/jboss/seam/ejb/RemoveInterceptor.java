//$Id$
package org.jboss.seam.ejb;

import static org.jboss.seam.ComponentType.STATEFUL_SESSION_BEAN;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.ApplicationException;
import javax.ejb.Remove;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Removes SFSB components from the Seam context after invocation
 * of an EJB @Remove method, or when a system exception is thrown
 * from the bean.
 * 
 * @author Gavin King
 */
@Interceptor(stateless=true, type=InterceptorType.CLIENT)
public class RemoveInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = -6693606158918954699L;
   
   private static final LogProvider log = Logging.getLogProvider(RemoveInterceptor.class);

   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      //we have the method from the local interface, get the corresponding one
      //for the actual bean class (it has the @Remove annotation)
      Method removeMethod = getComponent().getRemoveMethod( invocation.getMethod().getName() );
      Object result;
      try
      {
         result = invocation.proceed();
      }
      catch (Exception exception)
      {
         removeIfNecessary(removeMethod, exception);
         throw exception;
      }
      removeIfNecessary(removeMethod);
      return result;
   }

   private void removeIfNecessary(Method removeMethod, Exception exception) {
      if ( exception instanceof RuntimeException || exception instanceof RemoteException )
      {
         if ( !exception.getClass().isAnnotationPresent(ApplicationException.class) ) 
         {
            //it is a "system exception"
            remove();
         }
      }
      else if ( removeMethod!=null )
      {
         if ( !removeMethod.getAnnotation(Remove.class).retainIfException() ) 
         {
            remove();
         }
      }
   }

   private void removeIfNecessary(Method removeMethod)
   {
      if ( removeMethod!=null ) 
      {
         remove();
      }
   }

   private void remove() 
   {
      getComponent().getScope().getContext().remove( getComponent().getName() );
      if ( log.isDebugEnabled() )
      {
         log.debug( "Stateful component was removed: " + getComponent().getName() );
      }
   }
   
   public boolean isInterceptorEnabled()
   {
      return getComponent().getType() == STATEFUL_SESSION_BEAN;
   }

}
