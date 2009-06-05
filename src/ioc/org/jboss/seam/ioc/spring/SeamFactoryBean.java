package org.jboss.seam.ioc.spring;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Obtains an instance of a Seam Component in the current context given the name
 * and other optional parameters. If proxy is set to true then return a scoped
 * proxy of the seam component instance. Use &lt;seam:instance/&gt; to simplify
 * use of this factory.
 * 
 * @author youngm
 */
public class SeamFactoryBean extends AbstractFactoryBean implements InitializingBean
{
   private ScopeType scope;

   private String name;

   private Boolean create;

   private SeamTargetSource targetSource;

   private Object proxyInstance;

   private Class type;

   private boolean proxy = false;

   /**
    * Initializes the factory. If proxy=true then initialize the proxy.
    * 
    * @see org.springframework.beans.factory.config.AbstractFactoryBean#afterPropertiesSet()
    */
   @Override
   public void afterPropertiesSet() throws Exception
   {
      if (name == null)
      {
         throw new IllegalArgumentException("name must not be null");
      }
      // If we're creating a proxy then we want this to be a singleton
      setSingleton(proxy);

      this.targetSource = new SeamTargetSource(name, scope, create, type);

      if (proxy)
      {
         Class targetClass = targetSource.getTargetClass();
         if (targetClass == null)
         {
            throw new IllegalStateException("Cannot use 'proxy' for an expression without specifying a type.");
         }
         // Not sure if I should allow people to change these proxy
         // parameters or not. We'll see what issues we get hard coding them.
         ProxyFactory pf = new ProxyFactory();
         pf.setProxyTargetClass(true);
         pf.setOptimize(true);
         pf.setExposeProxy(false);
         pf.setFrozen(true);
         pf.setAopProxyFactory(new DefaultAopProxyFactory());
         pf.setTargetSource(this.targetSource);

         List<Class> interfaces = targetSource.getSeamInterfaces();
         //For some reason the targetClass cannot be an interface
         if(targetClass.isInterface()) {
            interfaces.add(targetClass);
         }
         pf.setInterfaces(interfaces.toArray(new Class[interfaces.size()]));

         this.proxyInstance = pf.getProxy(Thread.currentThread().getContextClassLoader());

      }
      super.afterPropertiesSet();
   }

   /**
    * Return the current instance of a Seam component or the proxy if proxy was
    * set to true.
    * 
    * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
    */
   @Override
   protected Object createInstance() throws Exception
   {
      if (proxy)
      {
         return proxyInstance;
      }
      else
      {
         return targetSource.getTarget();
      }
   }

   /**
    * Return the type of the component if available.
    * 
    * @throws IllegalStateException
    *            if the component cannot be found or if seam has not yet been
    *            initialized.
    * 
    * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
    */
   @Override
   public Class getObjectType()
   {
      //Incase spring calls getObjectType without calling afterPropertiesSet() 
      if(targetSource == null) {
         return null;
      }
      return targetSource.getTargetClass();
   }

   /**
    * The name of the seam component to get an instance of. (required)
    * 
    * @param name
    *           the name of the component
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * The scope of the seam component (optional)
    * 
    * @param scope
    *           the scope of the component
    */
   public void setScope(ScopeType scope)
   {
      this.scope = scope;
   }

   /**
    * Should the factory create an instance of the component if one doesn't
    * already exist in this context. If null
    * 
    * Must always be true for STATELESS components.
    * 
    * @param create
    *           do we create an instance if needed
    */
   public void setCreate(Boolean create)
   {
      this.create = create;
   }

   /**
    * Should the factory wrap the component instance in a proxy so the seam
    * component can be safely injected into a singleton.
    * 
    * @param proxy
    *           true to proxy the component
    */
   public void setProxy(boolean proxy)
   {
      this.proxy = proxy;
   }

   /**
    * Forces type of a proxy created. Useful when using EL where the type of the
    * object may not be available at Proxy creation time.
    * 
    * @param type
    */
   public void setType(Class type)
   {
      this.type = type;
   }
}
