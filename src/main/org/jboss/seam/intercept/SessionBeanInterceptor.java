/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.intercept;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.InterceptorType;

/**
 * Controller interceptor for server-side interceptors of
 * EJB3 session bean components.
 * 
 * @author Gavin King
 */
public class SessionBeanInterceptor extends RootInterceptor
{
   private static final long serialVersionUID = -7474586917199426345L;
   private static final LogProvider log = Logging.getLogProvider(SessionBeanInterceptor.class);
   
   public static ThreadLocal<Component> COMPONENT = new ThreadLocal<Component>();

   /**
    * Called when instantiated by EJB container.
    * (In this case it might be a Seam component,
    * but we won't know until postConstruct() is
    * called.)
    */
   public SessionBeanInterceptor()
   {
      super(InterceptorType.SERVER);
   }
   
   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      return invoke( new EJBInvocationContext(invocation), EventType.AROUND_INVOKE);
   }
   
   @PrePassivate
   public void prePassivate(InvocationContext invocation)
   {
      invokeAndHandle( new EJBInvocationContext(invocation), EventType.PRE_PASSIVATE);
   }
   
   @PostActivate
   public void postActivate(InvocationContext invocation)
   {
      invokeAndHandle( new EJBInvocationContext(invocation), EventType.POST_ACTIVATE);
   }
   
   @PreDestroy
   public void preDestroy(InvocationContext invocation)
   {
      invokeAndHandle( new EJBInvocationContext(invocation), EventType.PRE_DESTORY);
   }
   
   @PostConstruct
   public void postConstruct(InvocationContext invocation)
   {
      Component invokingComponent = SessionBeanInterceptor.COMPONENT.get();
      Object bean = invocation.getTarget();
      if ( invokingComponent!=null && invokingComponent.getBeanClass().isInstance(bean) )
      {
         //the session bean was obtained by the application by
         //calling Component.getInstance(), could be a role
         //other than the default role
         //note: minor bug here, since if we got another instance of the same
         //      bean from JNDI or @EJB while constructing a component,
         //      or in an interceptor while calling a component,
         //      this will cause that bean to think it is an instance of the
         //      component role (rather than the default role)
         if ( log.isTraceEnabled() ) 
         {
            log.trace("post construct phase for instance of component: " + invokingComponent.getName());
         }
         init(invokingComponent);
      }
      else if ( bean.getClass().isAnnotationPresent(Name.class) )
      {
         //the session bean was obtained by the application from
         //JNDI, or @EJB (or it was an MDB), so assume the default role
         //TODO: look at more than just @Name, consider components.xml
         String defaultComponentName = bean.getClass().getAnnotation(Name.class).value();
         if ( log.isTraceEnabled() ) 
         {
            log.trace("post construct phase for component instantiated outside Seam, assuming default role: " + defaultComponentName);
         }
         init( Seam.componentForName(defaultComponentName) );
      }
      else
      {
         if ( log.isTraceEnabled() ) 
         {
            log.trace("post construct phase for non-component bean");
         }
         initNonSeamComponent();
      }
      
      postConstruct(bean);
      invokeAndHandle( new EJBInvocationContext(invocation), EventType.POST_CONSTRUCT );
   }
 

}
