package org.jboss.seam.ioc.spring;

import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Post processor that makes all of the seam scopes available in
 * spring and takes all of the beans with those scopes and creates
 * Seam Components out of them. <p/> To use simply define the
 * namespace handler in in your ApplicationContext.
 * &lt;seam:configure-scopes/&gt;
 *
 * @author youngm
 */
public class SeamScopePostProcessor
    implements BeanFactoryPostProcessor,
               InitializingBean
{
    private static final LogProvider log = Logging.getLogProvider(SeamScopePostProcessor.class);

    /**
     * Default seam scope prefix.
     */
    public static final String DEFAULT_SCOPE_PREFIX = "seam.";

    private String prefix;
    
    private boolean defaultAutoCreate = false;

    /**
     * Null is not a valid prefix so make it the default is used if null or empty.
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        if (prefix == null || "".equals(prefix))
        {
            prefix = DEFAULT_SCOPE_PREFIX;
        }
    }

    /**
     * Add all of the seam scopes to this beanFactory.
     *
     * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
        throws BeansException
    {
        for (ScopeType scope : ScopeType.values())
        {
            // Don't create a scope for Unspecified
            if (scope != ScopeType.UNSPECIFIED)
            {
                beanFactory.registerScope(prefix + scope.name(), new SeamScope(scope));
                beanFactory.registerScope(prefix + scope.name().toLowerCase(), new SeamScope(scope));
            }
        }
        // Create a mock application context if not available.
        // TODO Reuse
        boolean unmockApplication = false;
        if (!Contexts.isApplicationContextActive())
        {
            Lifecycle.setupApplication();
            unmockApplication = true;
        }
        try
        {
            // Iterate through all the beans in the factory
            for (String beanName : beanFactory.getBeanDefinitionNames())
            {
                BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
                ScopeType scope;
                if (definition.getScope().startsWith(prefix))
                {
                    // Will throw an error if the scope is not found.
                    scope = ScopeType.valueOf(definition.getScope().replaceFirst(prefix, "").toUpperCase());
                }
                else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("No scope could be derived for bean with name: " + beanName);
                    }
                    continue;
                }
                if (scope == ScopeType.UNSPECIFIED)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Discarding bean with scope UNSPECIFIED.  Spring will throw an error later: "
                                  + beanName);
                    }
                    continue;
                }
                // Cannot be a seam component without a specified class seam:component will need to be used for this
                // bean.
                if (definition.getBeanClassName() == null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Unable to create component for bean: " + beanName
                                  + ".  No class defined try seam:component instead.");
                    }
                    continue;
                }
                SpringComponent.addSpringComponent(beanName, beanName, definition.getBeanClassName(), scope, beanFactory, null);
                if (defaultAutoCreate)
                {
                    Init.instance().addAutocreateVariable(beanName);
                }
            }
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
     * @param prefix case sensitive the prefix to use to identify seam scopes for spring beans. Default is "seam."
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
    
    /**
     * @param defaultAutoCreate whether or not context variables should be set to auto-create. Default is false.
     */
    public void setDefaultAutoCreate(boolean defaultAutoCreate) {
        this.defaultAutoCreate = defaultAutoCreate;   
    }
}
