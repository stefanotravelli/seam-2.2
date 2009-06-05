//$Id$
package org.jboss.seam.core;


import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.seam.Component;
import org.jboss.seam.Namespace;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.async.AsynchronousInterceptor;
import org.jboss.seam.bpm.BusinessProcessInterceptor;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions.MethodExpression;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.ejb.RemoveInterceptor;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.persistence.EntityManagerProxyInterceptor;
import org.jboss.seam.persistence.HibernateSessionProxyInterceptor;
import org.jboss.seam.persistence.ManagedEntityInterceptor;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SecurityInterceptor;
import org.jboss.seam.transaction.RollbackInterceptor;
import org.jboss.seam.transaction.TransactionInterceptor;
import org.jboss.seam.util.Resources;
import org.jboss.seam.webservice.WSSecurityInterceptor;

/**
 * A Seam component that holds Seam configuration settings
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.core.init")
@Install(value=false, precedence=BUILT_IN)
public class Init
{
   
   public static List<String> DEFAULT_INTERCEPTORS = new ArrayList<String>(Arrays.asList(
         SynchronizationInterceptor.class.getName(),
         AsynchronousInterceptor.class.getName(),
         RemoveInterceptor.class.getName(),
         HibernateSessionProxyInterceptor.class.getName(),
         EntityManagerProxyInterceptor.class.getName(),
         MethodContextInterceptor.class.getName(),
         EventInterceptor.class.getName(),
         ConversationalInterceptor.class.getName(),
         BusinessProcessInterceptor.class.getName(),
         ConversationInterceptor.class.getName(),
         BijectionInterceptor.class.getName(),
         RollbackInterceptor.class.getName(),
         TransactionInterceptor.class.getName(),
         WSSecurityInterceptor.class.getName(),
         SecurityInterceptor.class.getName()
         )); 
   
   private LogProvider log = Logging.getLogProvider(Init.class);
   
   private Namespace rootNamespace = new Namespace(null);
   
   private Collection<Namespace> globalImports = new ArrayList<Namespace>();
   
   //private boolean isClientSideConversations = false;
   private boolean jbpmInstalled;
   private String jndiPattern;
   private boolean debug;
   private boolean myFacesLifecycleBug;
   private boolean transactionManagementEnabled = true;
   private boolean distributable = false;
   
   private List<String> interceptors = new ArrayList<String>(DEFAULT_INTERCEPTORS);
   
   private Map<String, List<ObserverMethod>> observerMethods = new HashMap<String, List<ObserverMethod>>();
   private Map<String, List<ObserverMethodExpression>> observerMethodBindings = new HashMap<String, List<ObserverMethodExpression>>();
   private Map<String, FactoryMethod> factories = new HashMap<String, FactoryMethod>();
   private Map<String, FactoryExpression> factoryMethodExpressions = new HashMap<String, FactoryExpression>();
   private Map<String, FactoryExpression> factoryValueExpressions = new HashMap<String, FactoryExpression>();
   
   private Set<String> autocreateVariables = new HashSet<String>();
   private Set<String> installedFilters = new HashSet<String>();
   private Set<String> resourceProviders = new HashSet<String>();
   private Set<String> permissionResolvers = new HashSet<String>();
   
   private Set<String> hotDeployableComponents = new HashSet<String>();
   
   private Map<String, String> converters = new HashMap<String, String>();
   private Map<String, String> validators = new HashMap<String, String>();
   private Map<Class, String> convertersByClass = new HashMap<Class, String>();
   
   private long timestamp;
   private long warTimestamp;
   private File[] hotDeployPaths;
   
   public static Init instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application scope");
      }
      Init init = (Init) Contexts.getApplicationContext().get(Init.class);
      //commented out because of some test cases:
      /*if (init==null)
      {
         throw new IllegalStateException("No Init exists");
      }*/
      return init;
   }
   
   /*public boolean isClientSideConversations()
   {
      return isClientSideConversations;
   }

   public void setClientSideConversations(boolean isClientSideConversations)
   {
      this.isClientSideConversations = isClientSideConversations;
   }*/
   
   public static class FactoryMethod {
       private Method method;
       private Component component;
       private ScopeType scope;
      
	   FactoryMethod(Method method, Component component)
	   {
	       this.method = method;
           this.component = component;
           scope = method.getAnnotation(org.jboss.seam.annotations.Factory.class).scope();
	   }
      
      public ScopeType getScope()
      {
         return scope;
      }
      public Component getComponent()
      {
         return component;
      }
      public Method getMethod()
      {
         return method;
      }
      @Override
      public String toString()
      {
         return "FactoryMethod(" + method + ')';
      }
   }
   
   public static class FactoryExpression 
   {
      private String expression;
      private ScopeType scope;
      
      FactoryExpression(String expression, ScopeType scope)
      {
         this.expression = expression;
         this.scope = scope;
      }
      
      public MethodExpression getMethodBinding()
      {
         //TODO: figure out some way to cache this!!
         return Expressions.instance().createMethodExpression(expression);
      }
      public ValueExpression getValueBinding()
      {
         //TODO: figure out some way to cache this!!
         return Expressions.instance().createValueExpression(expression);
      }
      public ScopeType getScope()
      {
         return scope;
      }
      @Override
      public String toString()
      {
         return "FactoryBinding(" + expression + ')';
      }
   }
   
   public FactoryMethod getFactory(String variable)
   {
      return factories.get(variable);
   }
   
   public FactoryExpression getFactoryMethodExpression(String variable)
   {
      return factoryMethodExpressions.get(variable);
   }
   
   public FactoryExpression getFactoryValueExpression(String variable)
   {
      return factoryValueExpressions.get(variable);
   }
   
   private void checkDuplicateFactory(String variable)
   {
      if ( factories.containsKey(variable) )
      {
          throw new IllegalStateException("duplicate factory for: " + variable + " (duplicate is specified in a component)");
      }
      checkDuplicateFactoryExpressions(variable);
   }
   
   private void checkDuplicateFactoryExpressions(String variable)
   {
       if ( factoryMethodExpressions.containsKey(variable) || factoryValueExpressions.containsKey(variable) )
       {
           throw new IllegalStateException("duplicate factory for: " + variable + " (duplicate is specified in components.xml)");
       }
   }
   
   private void checkDuplicateFactory(String variable, Component component)
   {
       if (factories.containsKey(variable))
       {
           String otherComponentName = factories.get(variable).getComponent().getName();
           String componentName = component.getName();
           if (componentName != null && !componentName.equals(otherComponentName))
           {
               throw new IllegalStateException("duplicate factory for: " + variable + " (duplicates are specified in " + componentName + " and " + otherComponentName + ")");
           }
       }
       checkDuplicateFactoryExpressions(variable);
   }
   
   
   /** 
    * makes sure appropriate namespaces exist for a name.  isComponent indicates the
    * name is for a component type, in which case we don't create a namespace for the 
    * last part
    */
   public Namespace initNamespaceForName(String name, boolean isComponent) {
       Namespace namespace = getRootNamespace();
       
       StringTokenizer tokens = new StringTokenizer(name, ".");
       while (tokens.hasMoreTokens()) {
           String token = tokens.nextToken();

           if (tokens.hasMoreTokens() || !isComponent) {
               //we don't want to create a namespace for a componentName
               namespace = namespace.getOrCreateChild(token);               
           }
       }

       return namespace;
   }
   
   public void addFactoryMethod(String variable, Method method, Component component)
   {
       checkDuplicateFactory(variable, component);
	   factories.put(variable, new FactoryMethod(method, component));
	   initNamespaceForName(variable, true);
   }

   public void addFactoryMethodExpression(String variable, String methodBindingExpression, ScopeType scope)
   {
      checkDuplicateFactory(variable);
      factoryMethodExpressions.put(variable, new FactoryExpression(methodBindingExpression, scope));
      initNamespaceForName(variable, true);
   }
   
   public void addFactoryValueExpression(String variable, String valueBindingExpression, ScopeType scope)
   {
      checkDuplicateFactory(variable);
      factoryValueExpressions.put(variable, new FactoryExpression(valueBindingExpression, scope));
      initNamespaceForName(variable, true);

   }
   
   public static class ObserverMethod 
   {
      private Method method;
      private Component component;
      private boolean create;
      
      ObserverMethod(Method method, Component component, boolean create)
      {
         this.method = method;
         this.component = component;
         this.create = create;
      }

      public Component getComponent()
      {
         return component;
      }

      public Method getMethod()
      {
         return method;
      }

      public boolean isCreate()
      {
         return create;
      }

      @Override
      public String toString()
      {
         return "ObserverMethod(" + method + ')';
      }
      
      @Override
      public boolean equals(Object obj)
      {
         if (!(obj instanceof ObserverMethod)) return false;
           
         ObserverMethod other = (ObserverMethod) obj;
         return this.component.equals(other.component) &&
           Arrays.equals(this.method.getParameterTypes(), other.method.getParameterTypes()) &&
           this.method.getName().equals(other.getMethod().getName());         
      }
   }
   
   public static class ObserverMethodExpression
   {
      private MethodExpression methodBinding;
      
      ObserverMethodExpression(MethodExpression method)
      {
         this.methodBinding = method;
      }

      public MethodExpression getMethodBinding()
      {
         return methodBinding;
      }

      @Override
      public String toString()
      {
         return "ObserverMethodBinding(" + methodBinding + ')';
      }
   }
   
   public List<ObserverMethod> getObserverMethods(String eventType)
   {
      return observerMethods.get(eventType);
   }
   
   public List<ObserverMethodExpression> getObserverMethodExpressions(String eventType)
   {
      return observerMethodBindings.get(eventType);
   }
   
   public void addObserverMethod(String eventType, Method method, Component component, boolean create)
   {
      List<ObserverMethod> observerList = observerMethods.get(eventType);
      if (observerList==null)
      {
         observerList = new ArrayList<ObserverMethod>();
         observerMethods.put(eventType, observerList);
      }
      
      ObserverMethod observerMethod = new ObserverMethod(method, component, create); 
      if (!observerList.contains(observerMethod))
      {
         observerList.add( observerMethod );
      }
   }
   
   public void addObserverMethodExpression(String eventType, MethodExpression methodBinding)
   {
      List<ObserverMethodExpression> observerList = observerMethodBindings.get(eventType);
      if (observerList==null)
      {
         observerList = new ArrayList<ObserverMethodExpression>();
         observerMethodBindings.put(eventType, observerList);
      }
      observerList.add( new ObserverMethodExpression(methodBinding) );
   }
   
   /**
    * Remove any observer methods registered on the component. Needed to clean
    * out old observer methods on hot deploy
    * @param component
    */
   public void removeObserverMethods(Component component)
   {
      // TODO Better implementation ;-)
      for (String eventType : observerMethods.keySet())
      {
         List<ObserverMethod> observerMethodsToRemove = new ArrayList<ObserverMethod>();
         for (ObserverMethod observerMethod : observerMethods.get(eventType))
         {
            if (observerMethod.getComponent().equals(component))
            {
               observerMethodsToRemove.add(observerMethod);
            }
         }
         observerMethods.get(eventType).removeAll(observerMethodsToRemove);
      }
   }
   
   public boolean isJbpmInstalled()
   {
      return jbpmInstalled;
   }
   
   public String getJndiPattern() 
   {
      return jndiPattern;
   }
    
   public void setJndiPattern(String jndiPattern) 
   {
	   this.jndiPattern = jndiPattern;
   }
   public boolean isDebug()
   {
      return debug;
   }
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }
   
   /**
    * The debug page is considered available if debug JAR is on the classpath
    * and Seam is running in debug mode (to prevent it from being enabling in
    * the event the JAR is inadvertently packaged).
    */
   public boolean isDebugPageAvailable()
   {
      return debug && Resources.getResource("META-INF/debug.xhtml", null) != null;   
   }
   
   public boolean isMyFacesLifecycleBug()
   {
      return myFacesLifecycleBug;
   }
   
   public void setMyFacesLifecycleBug(boolean myFacesLifecycleBugExists)
   {
      this.myFacesLifecycleBug = myFacesLifecycleBugExists;
   }

   public void setJbpmInstalled(boolean jbpmInstalled)
   {
      this.jbpmInstalled = jbpmInstalled;
   }

   public boolean isAutocreateVariable(String name)
   {
      return autocreateVariables.contains(name);
   }
   
   public void addAutocreateVariable(String name)
   {
      autocreateVariables.add(name);
   }

   public Namespace getRootNamespace()
   {
      return rootNamespace;
   }
   
   public void importNamespace(String namespaceName)
   {
      Namespace namespace = getRootNamespace();
      StringTokenizer tokens = new StringTokenizer(namespaceName, ".");
      while ( tokens.hasMoreTokens() )
      {
         namespace = namespace.getOrCreateChild( tokens.nextToken() );
      }
      globalImports.add(namespace);
   }

   public void addInstalledFilter(String name)
   {
      installedFilters.add(name);
   }
   
   public Set<String> getInstalledFilters()
   {
      return installedFilters;
   }
   
   public void addResourceProvider(String name)
   {
      resourceProviders.add(name);
   }
   
   public Set<String> getResourceProviders()
   {
      return resourceProviders;
   }
   
   public void addPermissionResolver(String name)
   {
      permissionResolvers.add(name);
   }
   
   public Set<String> getPermissionResolvers()
   {
      return permissionResolvers;
   }

   public Set<String> getHotDeployableComponents()
   {
      return hotDeployableComponents;
   }

   public void addHotDeployableComponent(String name)
   {
      this.hotDeployableComponents.add(name);
   }

   public Map<String, String> getConverters()
   {
      return converters;
   }

   public Map<Class, String> getConvertersByClass()
   {
      return convertersByClass;
   }

   public Map<String, String> getValidators()
   {
      return validators;
   }
   
   public boolean hasHotDeployableComponents()
   {
      return hotDeployPaths!=null;
   }

   public File[] getHotDeployPaths()
   {
      return hotDeployPaths;
   }

   public void setHotDeployPaths(File[] hotDeployJars)
   {
      this.hotDeployPaths = hotDeployJars;
   }

   public long getTimestamp()
   {
      return timestamp;
   }

   public void setTimestamp(long timestamp)
   {
      this.timestamp = timestamp;
   }
   
   public long getWarTimestamp()
   {
      return warTimestamp;
   }
   
   public void setWarTimestamp(long warTimestamp)
   {
      this.warTimestamp = warTimestamp;
   }

   public boolean isTransactionManagementEnabled()
   {
      return transactionManagementEnabled;
   }

   public void setTransactionManagementEnabled(boolean transactionManagementEnabled)
   {
      this.transactionManagementEnabled = transactionManagementEnabled;
   }

   public boolean isSecurityEnabled()
   {
      return Identity.isSecurityEnabled();
   }

   public void setSecurityEnabled(boolean securityEnabled)
   {
      Identity.setSecurityEnabled(securityEnabled);
   }

   public Collection<Namespace> getGlobalImports()
   {
      return globalImports;
   }
    
   public List<String> getInterceptors()
   {
      return interceptors;
   }
    
   public void setInterceptors(List<String> interceptors)
   {
      this.interceptors = interceptors;
   }
      
   public boolean isDistributable()
   {
      return distributable;
   }

   public void setDistributable(boolean distributable)
   {
      this.distributable = distributable;
   }

   /**
    * Sanity check to warn users if they have disabled core interceptors
    */
   public void checkDefaultInterceptors()
   {
      for (String defaultInterceptor : DEFAULT_INTERCEPTORS)
      {
         if (!interceptors.contains(defaultInterceptor))
         {
            log.warn("The built-in interceptor " + defaultInterceptor + " is missing. This application may not function as expected");
         }
      }
      
      if (distributable && !interceptors.contains(ManagedEntityInterceptor.class.getName()))
      {
         interceptors.add(ManagedEntityInterceptor.class.getName());
      }
   }
   
}
