package org.jboss.seam.ioc.spring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSessionActivationListener;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Mutable;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.intercept.Proxy;
import org.springframework.aop.TargetSource;

/**
 * A TargetSource for a seam component instance. Will obtain an instance given a
 * name and optionally a scope and create. Used by the SeamFactoryBean to create
 * a proxy for a requested seam component instance.
 * 
 * @author youngm
 */
@SuppressWarnings("serial")
public class SeamTargetSource implements TargetSource, Serializable
{
   private ScopeType scope;

   private String name;

   private Boolean create;

   private ValueExpression valueExpression;

   private Class type;

   public SeamTargetSource(String name, ScopeType scope, Boolean create, Class type)
   {
      this(name, scope, create);
      this.type = type;
   }

   /**
    * @param name
    *           Name of the component: required
    * @param scope
    *           Name of the scope the component is in: optional
    * @param create
    *           Whether to create a new instance if one doesn't already exist:
    *           optional
    */
   public SeamTargetSource(String name, ScopeType scope, Boolean create)
   {
      if (name == null || "".equals(name))
      {
         throw new IllegalArgumentException("Name is required.");
      }
      this.name = name;
      this.scope = scope;
      this.create = create;

      if (name.startsWith("#"))
      {
         this.valueExpression = Expressions.instance().createValueExpression(name);
      }
   }

   /**
    * Returns a component instance for this TargetSource.
    * 
    * @see org.springframework.aop.TargetSource#getTarget()
    */
   public Object getTarget() throws Exception
   {
      if (valueExpression != null)
      {
         return valueExpression.getValue();
      }
      else
      {
         if (scope == null && create == null)
         {
            return Component.getInstance(name);
         }
         else if (scope == null)
         {
            return Component.getInstance(name, create);
         }
         else if (create == null)
         {
            return Component.getInstance(name, scope);
         }
         else
         {
            return Component.getInstance(name, scope, create);
         }
      }
   }

   /**
    * Obtains the seam component beanClass or the defined type for this
    * TargetSource.
    * 
    * @see org.springframework.aop.TargetSource#getTargetClass()
    */
   public Class getTargetClass()
   {
      if (type != null)
      {
         return type;
      }
      Component component = getComponent();
      if (component == null)
      {
         return null;
      }
      if (component.hasUnwrapMethod())
      {
         return component.getUnwrapMethod().getReturnType();
      }
      return component.getBeanClass();
   }

   public List<Class> getSeamInterfaces()
   {
      List<Class> interfaces = new ArrayList<Class>();
      Component component = getComponent();
      // Attempt to piece together all of the possible interfaces to apply
      // to our proxy.
      if ( component != null && component.isInterceptionEnabled() )
      {
         if (component.getType().isSessionBean())
         {
            interfaces.addAll(component.getBusinessInterfaces());
         }
         else
         {
            interfaces.add(HttpSessionActivationListener.class);
            interfaces.add(Mutable.class);
         }
         interfaces.add(Proxy.class);
      }
      return interfaces;
   }

   /**
    * Get the component for this TargetSource
    * 
    * @return component
    */
   public Component getComponent()
   {
      if (valueExpression != null)
      {
         return null;
      }
      else
      {
         // TODO reuse
         boolean unmockApplication = false;
         if (!Contexts.isApplicationContextActive())
         {
            Lifecycle.setupApplication();
            unmockApplication = true;
         }
         try
         {
            Component component = Component.forName(name);
            if (component == null)
            {
               throw new IllegalStateException("Cannot find targetClass for seam component: "
                        + name + ".  Make sure Seam is being configured before Spring.");
            }
            return component;
         }
         finally
         {
            if (unmockApplication)
            {
               Lifecycle.cleanupApplication();
            }
         }
      }
   }

   /**
    * @see org.springframework.aop.TargetSource#isStatic()
    */
   public boolean isStatic()
   {
      return false;
   }

   /**
    * Don't think we need to do anything here.
    * 
    * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
    */
   public void releaseTarget(Object target) throws Exception
   {
      // Do Nothing
   }
}
