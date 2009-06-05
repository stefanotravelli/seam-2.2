package org.jboss.seam.ioc.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.ioc.IoCComponent;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.util.ClassUtils;

/**
 * An extension of Component that allows spring to provide the base instance for a seam component.
 *
 * @author youngm
 */
public class SpringComponent extends IoCComponent
{
    private static final String SPRING_COMPONENT_NAME_MAP = "org.jboss.seam.SpringComponentNameMap";

    public static final String DESTRUCTION_CALLBACK_NAME_PREFIX = IoCComponent.class.getName()
    + ".DESTRUCTION_CALLBACK.";

    private BeanFactory beanfactory;

    private Boolean interceptionEnabled;

    private String springBeanName;

    private static final ThreadLocal<ObjectFactory> objectFactory = new ThreadLocal<ObjectFactory>();

    public static ObjectFactory getObjectFactory()
    {
        return objectFactory.get();
    }

    public static void setObjectFactory(ObjectFactory bean)
    {
        objectFactory.set(bean);
    }

    /**
     * Utility to add a SpringComponent to the seam component ApplicationContext.
     *
     * @param componentName the seam component name to use
     * @param springBeanName the spring bean name to map to this seam component
     * @param beanClassName the seam beanClass to use
     * @param scopeType the scope of this component
     * @param beanFactory the beanfactory this spring bean exists in
     * @param intercept the InterceptorTyp to force the bean to use. Will override any annotations on the bean.
     */
    public static void addSpringComponent(String componentName, String springBeanName, String beanClassName,
            ScopeType scopeType, BeanFactory beanFactory, Boolean intercept)
    {
        // mock the application context
        // TODO reuse
        boolean unmockApplication = false;
        if (!Contexts.isApplicationContextActive())
        {
            Lifecycle.setupApplication();
            unmockApplication = true;
        }
        try
        {
            if (Component.forName(componentName) != null)
            {
                throw new IllegalStateException("Cannot add spring component to seam with name: " + componentName
                        + ".  There is already a seam component with that name.");
            }
            Map<String, String> springComponentNameMap = getSpringComponentNameMap();
            // Add an entry to the spring+seam name association map
            springComponentNameMap.put(springBeanName, componentName);
            Class beanClass = ClassUtils.forName(beanClassName);
            // Add the component to seam
            Contexts.getApplicationContext().set(
                    componentName + Initialization.COMPONENT_SUFFIX,
                    new SpringComponent(beanClass, componentName, springBeanName, scopeType, beanFactory,
                            intercept));
        }
        catch (ClassNotFoundException e)
        {
            throw new FatalBeanException("Error", e);
        }
        finally
        {
            if (unmockApplication)
            {
                Lifecycle.cleanupApplication();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getSpringComponentNameMap()
    {
        if (Contexts.getApplicationContext().get(SPRING_COMPONENT_NAME_MAP) == null)
        {
            Contexts.getApplicationContext().set(SPRING_COMPONENT_NAME_MAP, new HashMap<String, String>());
        }
        return (Map<String, String>) Contexts.getApplicationContext().get(SPRING_COMPONENT_NAME_MAP);
    }

    /**
     * Just like Component.forName() but mocks the applicationContext and you provide it with the spring bean name
     * instead of the seam component name.
     *
     * @param springBeanName the spring bean name.
     * @return the SpringComponent mapped to that spring bean name.
     */
    public static SpringComponent forSpringBeanName(String springBeanName)
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
            return (SpringComponent) Component.forName(getSpringComponentNameMap().get(springBeanName));
        }
        finally
        {
            if (unmockApplication)
            {
                Lifecycle.cleanupApplication();
            }
        }
    }

    /**
     * Creates a Spring Seam Component given a beanFactory.
     *
     * @param clazz the seam beanClass to use
     * @param componentName component name
     * @param springBeanName the spring bean name
     * @param scope component scope
     * @param factory the beanfactory this spring component should use
     * @param intercept the interception type
     */
    public SpringComponent(Class clazz, String componentName, String springBeanName, ScopeType scope,
            BeanFactory factory, Boolean intercept)
    {
        super(clazz, componentName, scope);
        this.springBeanName = springBeanName;
        this.beanfactory = factory;
        this.interceptionEnabled = intercept;
    }

    @Override
    protected String getIoCName()
    {
        return "Spring";
    }

    @Override
    protected Object instantiateIoCBean() throws Exception
    {
        ObjectFactory objectFactory = getObjectFactory();
        if (objectFactory == null)
        {
            return beanfactory.getBean(springBeanName);
        }
        setObjectFactory(null);
        return objectFactory.getObject();
    }

    /**
     * Calls the spring destroy callback when seam destroys the component
     *
     * @see org.jboss.seam.Component#callDestroyMethod(Object)
     */
    @Override
    public void destroy(Object instance)
    {
        super.destroy(instance);
        // Cannot call the callback on a STATELESS bean because we have no way of storing it.
        if (getScope() != ScopeType.STATELESS)
        {
            Runnable callback = (Runnable) getScope().getContext().get(DESTRUCTION_CALLBACK_NAME_PREFIX + getName());
            if (callback != null)
            {
                callback.run();
            }
        }
    }

    /**
     * Registers a destruction callback with this bean.
     *
     * @param name bean name
     * @param destroy the destroy to set
     */
    public void registerDestroyCallback(String name, Runnable destroy)
    {
        // Not sure yet how to register a stateless bean's Destruction callback.
        if (getScope() != ScopeType.STATELESS)
        {
            getScope().getContext().set(DESTRUCTION_CALLBACK_NAME_PREFIX + name, destroy);
        }
    }

    /**
     * Overrides Components inject to unwrap all of the spring AOP layers so that fields can be injected into this bean.
     *
     * @see org.jboss.seam.Component#inject(java.lang.Object, boolean)
     */
    @Override
    public void inject(Object bean, boolean enforceRequired)
    {
        if (bean instanceof Advised)
        {
            try
            {
                inject(((Advised) bean).getTargetSource().getTarget(), enforceRequired);
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        super.inject(bean, enforceRequired);
    }

    /**
     * Use the InterceptionType override if available otherwise use the annotation or seam default.
     *
     * @see org.jboss.seam.Component#isInterceptionEnabled()
     */
    @Override
    public boolean isInterceptionEnabled()
    {
        if (interceptionEnabled == null)
        {
            return super.isInterceptionEnabled();
        }
        return interceptionEnabled;
    }
    
    @Override
    protected void checkSynchronizedForComponentType() {}
    
    @Override
    protected void checkPersistenceContextForComponentType() {}
    
    /**
     * Ignore PersistenceContextAttributes if scope is stateless
     */
    @Override    
    public List<BijectedAttribute> getPersistenceContextAttributes()
    {
       if(getScope().equals(ScopeType.STATELESS)) {
          return Collections.emptyList();          
       }
       return super.getPersistenceContextAttributes();
    }
}
