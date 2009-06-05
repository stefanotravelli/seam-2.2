package org.jboss.seam.persistence;

import static org.jboss.seam.ComponentType.STATEFUL_SESSION_BEAN;
import static org.jboss.seam.ComponentType.STATELESS_SESSION_BEAN;

import org.hibernate.Session;
import org.jboss.seam.Component.BijectedAttribute;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.PostActivate;
import org.jboss.seam.annotations.intercept.PostConstruct;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.util.Reflections;

/**
 * Proxy the Hibernate Session if injected using @PersistenceContext
 * 
 * @author Pete Muir
 *
 */

@Interceptor(stateless=true)
public class HibernateSessionProxyInterceptor extends AbstractInterceptor
{

   @AroundInvoke
   public Object aroundInvoke(InvocationContext ic) throws Exception
   {
      return ic.proceed();
   }
   
   @PostActivate
   public void postActivate(InvocationContext invocation) throws Exception
   {
      //just in case the container does some special handling of PC serialization
      proxyPersistenceContexts(invocation.getTarget());
      invocation.proceed();
   }
   
   @PostConstruct
   public void postConstruct(InvocationContext invocation) throws Exception
   {
      proxyPersistenceContexts(invocation.getTarget());
      invocation.proceed();
   }
   
   
   private void proxyPersistenceContexts(Object bean)
   {
      //wrap any @PersistenceContext attributes in our proxy
      for ( BijectedAttribute ba: getComponent().getPersistenceContextAttributes() )
      {
         Object object = ba.get(bean);
         if ( ! ( object instanceof HibernateSessionProxy) && object instanceof Session)
         {
            ba.set( bean, HibernatePersistenceProvider.proxySession( (Session) object ) );
         }
      }
   }
   
   public boolean isInterceptorEnabled()
   {
      return (getComponent().getType()==STATEFUL_SESSION_BEAN || getComponent().getType()==STATELESS_SESSION_BEAN) && Reflections.isClassAvailable("org.hibernate.Session");
   }

}
