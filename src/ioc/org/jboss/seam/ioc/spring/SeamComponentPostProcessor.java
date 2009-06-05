package org.jboss.seam.ioc.spring;

import org.jboss.seam.Component;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * Intercepts when spring attempts to obtain an instance of a bean. If the bean is a seam component then we retrieve the
 * bean from seam to ensure it gets wrapped and managed by seam as well. This post processor must have a lower
 * precedence than any spring autoproxy creators.
 *
 * @author youngm
 */
public class SeamComponentPostProcessor implements BeanPostProcessor, Ordered 
{

	private int order = Ordered.LOWEST_PRECEDENCE;

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException 
    {
        // Check to see if this bean is a component.
		SpringComponent component = SpringComponent.forSpringBeanName(beanName);
		// Not a spring component skip.
		if (component == null) 
        {
			return bean;
		}
		// If this bean is a FactoryBean only request the bean from Seam if the component is a FactoryBean as well
		// The object created by the factory should come along later
		if (bean instanceof FactoryBean && !FactoryBean.class.isAssignableFrom(component.getBeanClass())) 
        {
			return bean;
		}
		// Wrap our bean instance in an object factory for the SpringComponent to use
		SpringComponent.setObjectFactory(new ObjectFactory() {
			public Object getObject() throws BeansException 
         {
				return bean;
			}
		});
		// Return the seam instance
		return Component.getInstance(beanName, component.getScope());
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException 
    {
		return bean;
	}

	/**
	 * This post processor must run after any spring AutoProxyCreator
	 *
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() 
   {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) 
    {
		this.order = order;
	}
}
