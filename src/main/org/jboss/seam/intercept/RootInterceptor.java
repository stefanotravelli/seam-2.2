/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.intercept;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Mutable;
import org.jboss.seam.util.EJB;

/**
 * Abstract superclass of all controller interceptors
 * 
 * @author Gavin King
 */
public class RootInterceptor implements Serializable
{
   private static final long serialVersionUID = 8041533870186694663L;
   private static final LogProvider log = Logging.getLogProvider(RootInterceptor.class);
   
   private final InterceptorType type;
   private boolean isSeamComponent;
   private String componentName;
   private List<Object> userInterceptors;
   
   private transient Component component; //a cache of the Component reference for performance

   protected RootInterceptor(InterceptorType type)
   {
      this.type = type;
   }
   
   protected void init(Component component)
   {
      isSeamComponent = true;
      componentName = component.getName();
      userInterceptors = component.createUserInterceptors(type);
      this.component = component;
   }
   
   protected void initNonSeamComponent()
   {
      isSeamComponent = false;
   }
   
   protected void postConstruct(Object bean)
   {
      // initialize the bean instance
      if (isSeamComponent)
      {
         try
         {
            getComponent().initialize(bean);
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("exception initializing EJB component", e);
         }
      }      
   }

   protected void invokeAndHandle(InvocationContext invocation, EventType invocationType)
   {
      try
      {
         invoke(invocation, invocationType);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("exception in EJB lifecycle callback", e);
      }
   }
   
   protected Object invoke(InvocationContext invocation, EventType invocationType) throws Exception
   {
      if ( !isSeamComponent || !Lifecycle.isApplicationInitialized())
      {
         //not a Seam component
         return invocation.proceed();
      }
      else if ( Contexts.isEventContextActive() || Contexts.isApplicationContextActive() ) //not sure about the second bit (only needed at init time!)
      {

         // a Seam component, and Seam contexts exist
         return createInvocationContext(invocation, invocationType).proceed();
      }
      else
      {            
         //if invoked outside of a set of Seam contexts,
         //set up temporary Seam EVENT and APPLICATION
         //contexts just for this call
         
         Lifecycle.beginCall();         
         try
         {
            return createInvocationContext(invocation, invocationType).proceed();
         }
         finally
         {
            Lifecycle.endCall();
         }
      }
   }

   private InvocationContext createInvocationContext(InvocationContext invocation, EventType eventType) throws Exception
   {
      if ( isProcessInterceptors( invocation.getMethod(), invocation.getTarget() ) )
      {
         if ( log.isTraceEnabled() ) 
         {
            log.trace( "intercepted: " + getInterceptionMessage(invocation, eventType) );
         }
         return createSeamInvocationContext(invocation, eventType);
      }
      else 
      {
         if ( log.isTraceEnabled() ) 
         {
            log.trace( "not intercepted: " + getInterceptionMessage(invocation, eventType) );
         }
         return invocation;
      }
   }

   private SeamInvocationContext createSeamInvocationContext(InvocationContext invocation, EventType eventType) throws Exception
   {
      return EJB.INVOCATION_CONTEXT_AVAILABLE ?
            createEE5SeamInvocationContext(invocation, eventType) :
            createNonEE5SeamInvocationContext(invocation, eventType);
   }

   private SeamInvocationContext createNonEE5SeamInvocationContext(InvocationContext invocation, EventType eventType)
   {
      return new SeamInvocationContext( invocation, eventType, userInterceptors, getComponent().getInterceptors(type) );
   }
   
   private static final Constructor CONSTRUCTOR;
   static
   {
      if ( EJB.INVOCATION_CONTEXT_AVAILABLE )
      {
         try
         {
            Class[] paramTypes = {InvocationContext.class, EventType.class, List.class, List.class};
            CONSTRUCTOR = Class.forName("org.jboss.seam.intercept.EE5SeamInvocationContext").getConstructor(paramTypes);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         CONSTRUCTOR = null;
      }
      
   }

   private SeamInvocationContext createEE5SeamInvocationContext(InvocationContext invocation, EventType eventType)
   {
      try
      {
         return (SeamInvocationContext) CONSTRUCTOR.newInstance( invocation, eventType, userInterceptors, getComponent().getInterceptors(type) );
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private String getInterceptionMessage(InvocationContext invocation, EventType eventType)
   {
      return getComponent().getName() + '.' + 
            ( eventType==EventType.AROUND_INVOKE ? invocation.getMethod().getName() : eventType );
   }

   private boolean isProcessInterceptors(Method method, Object bean)
   {
      boolean res = isSeamComponent && 
            getComponent().isInterceptionEnabled() &&
            !isBypassed(method) &&
            !isClearDirtyMethod(method, bean);

      return res;
   }

   private boolean isBypassed(Method method)
   {
      return method!=null && method.isAnnotationPresent(BypassInterceptors.class);
   }

   private boolean isClearDirtyMethod(Method method, Object bean)
   {
      return bean instanceof Mutable &&
            method!=null &&
            method.getName().equals("clearDirty") &&
            method.getParameterTypes().length==0;
   }
   
   protected Component getComponent()
   {
      if (isSeamComponent && component==null) 
      {
         component = Seam.componentForName(componentName);
      }
      return component;
   }
   
   protected boolean isSeamComponent()
   {
      return isSeamComponent;
   }

   protected String getComponentName()
   {
      return componentName;
   }
   
}
