package org.jboss.seam.ioc.spring;

import org.jboss.seam.ScopeType;
import org.jboss.seam.core.Init;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * NamespaceHandler for a number of seam features in spring.
 * 
 * @author youngm
 */
public class SeamNamespaceHandler extends NamespaceHandlerSupport
{
   public static final String SEAM_SCOPE_POST_PROCESSOR = "org.jboss.seam.ioc.spring.SeamScopePostProcessor";

   public static final String SEAM_COMPONENT_POST_PROCESSOR = "org.jboss.seam.ioc.spring.SeamComponentPostProcessor";

   public static final String SEAM_COMPONENT_POST_PROCESSOR_BEAN_NAME = "org.jboss.seam.ioc.spring.seamComponentPostProcessor";

   /**
    * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
    */
   public void init()
   {
      registerBeanDefinitionParser("configure-scopes", new SeamConfigureScopeParser());
      registerBeanDefinitionParser("instance", new SeamInstanceBeanDefinitionParser());
      registerBeanDefinitionDecorator("component", new SeamComponentBeanDefinitionDecorator());
   }

   /**
    * Registers the SeamScopePostProcessor in this bean factory under the name
    * defined in SEAM_SCOPE_POST_PROCESSOR. &lt;seam:configure-scope/&gt;
    * 
    * @see SeamScopePostProcessor
    * @author youngm
    */
   private static class SeamConfigureScopeParser extends AbstractSimpleBeanDefinitionParser
   {
      /**
       * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
       */
      @Override
      protected Class getBeanClass(Element element)
      {
         return SeamScopePostProcessor.class;
      }

      /**
       * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#resolveId(org.w3c.dom.Element,
       *      org.springframework.beans.factory.support.AbstractBeanDefinition,
       *      org.springframework.beans.factory.xml.ParserContext)
       */
      @Override
      protected String resolveId(Element element, AbstractBeanDefinition definition,
               ParserContext parserContext) throws BeanDefinitionStoreException
      {
         return SEAM_SCOPE_POST_PROCESSOR;
      }
   }

   /**
    * Makes a SeamFactoryBean available for use in a spring ApplicationContext.
    * &lt;seam:instance name="someSeamComponent"/&gt;
    * 
    * @see SeamFactoryBean
    * @author youngm
    */
   private static class SeamInstanceBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser
   {
      @Override
      protected Class getBeanClass(Element element)
      {
         return SeamFactoryBean.class;
      }
   }

   /**
    * Makes an existing spring bean definition a seam component or provides
    * hints in the creation of a seam component. Will use the bean definitions
    * name and class by default and the classes annotatated InterceptionType.
    * 
    * If proxy=true will wrap the spring bean in a cglib proxy for safe
    * injection into singletons.
    * 
    * &lt;seam:component/&gt;
    * 
    * @author youngm
    */
   private static class SeamComponentBeanDefinitionDecorator implements BeanDefinitionDecorator
   {
      private static final String INTERCEPT_TYPE_ATTR = "intercept";

      private static final String SPRING_NAME_ATTR = "spring-name";

      private static final String SEAM_NAME_ATTR = "name";

      private static final String BEAN_CLASS_ATTR = "class";

      private static final String AUTO_CREATE_ATTR = "auto-create";

      /**
       * @see org.springframework.beans.factory.xml.BeanDefinitionDecorator#decorate(org.w3c.dom.Node,
       *      org.springframework.beans.factory.config.BeanDefinitionHolder,
       *      org.springframework.beans.factory.xml.ParserContext)
       */
      public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition,
               ParserContext parserContext)
      {
         // Add the Seam Component Post Processor to the bean factory if it
         // doesn't already exist
         if (!parserContext.getRegistry().containsBeanDefinition(
                  SEAM_COMPONENT_POST_PROCESSOR_BEAN_NAME))
         {
            Class cls;
            try
            {
               cls = ClassUtils.forName(SEAM_COMPONENT_POST_PROCESSOR);
            }
            catch (ClassNotFoundException e)
            {
               throw new IllegalStateException("Unable to load class '"
                        + SEAM_COMPONENT_POST_PROCESSOR
					       + "' make sure you have the jboss-seam-spring.jar in your classpath.", e);
            }
            RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinition.getPropertyValues().addPropertyValue("order", Ordered.LOWEST_PRECEDENCE);
            parserContext.getRegistry().registerBeanDefinition(
                     SEAM_COMPONENT_POST_PROCESSOR_BEAN_NAME, beanDefinition);
         }
         // get the optional beanClass
         String beanClassName = definition.getBeanDefinition().getBeanClassName();
         if (node.getAttributes().getNamedItem(BEAN_CLASS_ATTR) != null)
         {
            beanClassName = node.getAttributes().getNamedItem(BEAN_CLASS_ATTR).getNodeValue();
         }
         String beanName = definition.getBeanName();
         // get the name of the seam component to create
         String seamName = beanName;
         if (node.getAttributes().getNamedItem(SEAM_NAME_ATTR) != null)
         {
            seamName = node.getAttributes().getNamedItem(SEAM_NAME_ATTR).getNodeValue();
         }
         // get the name of the spring bean to use
         String springName = beanName;
         if (node.getAttributes().getNamedItem(SPRING_NAME_ATTR) != null)
         {
            springName = node.getAttributes().getNamedItem(SPRING_NAME_ATTR).getNodeValue();
         }
         // get the interception type to use
         Boolean interceptionType = null;
         if (node.getAttributes().getNamedItem(INTERCEPT_TYPE_ATTR) != null)
         {
            interceptionType = Boolean.valueOf(node.getAttributes().getNamedItem(
                     INTERCEPT_TYPE_ATTR).getNodeValue());
         }
         // get the requested scope
         ScopeType scope = ScopeType.valueOf(node.getAttributes().getNamedItem("scope")
                  .getNodeValue());
         if (scope != ScopeType.STATELESS
                  && !BeanDefinition.SCOPE_PROTOTYPE.equals(definition.getBeanDefinition()
                           .getScope()))
         {
            throw new IllegalStateException(
                     "The spring bean scope must be prototype to use a seam scope other than STATELESS.");
         }

         if (!(parserContext.getRegistry() instanceof BeanFactory))
         {
            throw new RuntimeException("For some reason your registry is not a BeanFactory");
         }
         SpringComponent.addSpringComponent(seamName, springName, beanClassName, scope,
                  (BeanFactory) parserContext.getRegistry(), interceptionType);
         if (node.getAttributes().getNamedItem(AUTO_CREATE_ATTR) != null)
         {
            if (Boolean.valueOf(node.getAttributes().getNamedItem(AUTO_CREATE_ATTR).getNodeValue()))
            {
               Init.instance().addAutocreateVariable(seamName);
            }
         }
         return definition;
      }
   }
}
