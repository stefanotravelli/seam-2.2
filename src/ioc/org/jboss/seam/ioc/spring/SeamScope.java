package org.jboss.seam.ioc.spring;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Allows for the creation of seam scoped component in spring. Seam
 * scopes are automatically made available if the
 * SeamScopePostProcessor is declared in the current
 * BeanFactory. &lt;seam:configure-scopes/&gt;
 *
 * @author youngm
 * @see SeamScopePostProcessor
 */
public class SeamScope 
    implements Scope 
{
    private static final LogProvider log = Logging.getLogProvider(SeamScope.class);

    private ScopeType scope;

    public SeamScope(ScopeType scope) 
    {
        this.scope = scope;
    }

    /**
     * Gets an instance of a Seam component providing the current ObjectFactory if needed.
     *
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String,
     *      org.springframework.beans.factory.ObjectFactory)
     */
    public Object get(String name, ObjectFactory objectFactory) 
    {
        try 
        {
            SpringComponent.setObjectFactory(objectFactory);
            Component component = SpringComponent.forSpringBeanName(name);
            return Component.getInstance(component.getName(), scope, true);
        } 
        finally 
        {
            SpringComponent.setObjectFactory(null);
        }
    }

    /**
     * Not used yet.
     *
     * @see org.springframework.beans.factory.config.Scope#getConversationId()
     */
    public String getConversationId() 
    {
        return null;
    }

    /**
     * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String,
     *      java.lang.Runnable)
     */
    public void registerDestructionCallback(String name, Runnable callback) 
    {
        SpringComponent.forSpringBeanName(name).registerDestroyCallback(name, callback);
    }

    /**
     * On remove destroys the seam component.
     *
     * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
     */
    public Object remove(String name) {
        // copied from Component.callDestory should be able to reuse. Needed because if remove is called then for some
        // reason spring doesn't use the destroy callback.
        log.debug("destroying: " + name);
        Component component = SpringComponent.forSpringBeanName(name);
        Object bean = null;
        if (component != null) 
        {
            bean = scope.getContext().get(component.getName());
            if (bean != null) // in a portal environment, this is possible
            {
                if (Events.exists())
                {
                    Events.instance().raiseEvent("org.jboss.seam.preDestroy." + name);
                }
                try 
                {
                    if (component.hasDestroyMethod()) 
                    {
                        component.callComponentMethod(bean, component.getDestroyMethod());
                    }
                } 
                catch (Exception e) 
                {
                    log.warn("Could not destroy component: " + component.getName(), e);
                }
            }
            scope.getContext().remove(component.getName());
        }
        return bean;
    }
}
