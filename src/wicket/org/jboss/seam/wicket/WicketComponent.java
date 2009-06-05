package org.jboss.seam.wicket;

import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.ScopeType.UNSPECIFIED;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.util.string.Strings;
import org.jboss.seam.Component;
import org.jboss.seam.Namespace;
import org.jboss.seam.RequiredException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Conversational;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.EndTask;
import org.jboss.seam.annotations.bpm.StartTask;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.security.RoleCheck;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.wicket.annotations.NoConversationPage;
import org.jboss.seam.wicket.ioc.BijectedAttribute;
import org.jboss.seam.wicket.ioc.BijectedField;
import org.jboss.seam.wicket.ioc.BijectedMethod;
import org.jboss.seam.wicket.ioc.BijectionInterceptor;
import org.jboss.seam.wicket.ioc.ConversationInterceptor;
import org.jboss.seam.wicket.ioc.EventInterceptor;
import org.jboss.seam.wicket.ioc.InjectedAttribute;
import org.jboss.seam.wicket.ioc.InjectedField;
import org.jboss.seam.wicket.ioc.StatelessInterceptor;

public class WicketComponent<T>
{
   
   private final class InjectedLogger extends InjectedField<Logger>
   {
      private Log logInstance;
      
      InjectedLogger(Field field, Logger annotation)
      {
         super(field, annotation);
         String category = getAnnotation().value();
         if ("".equals(category))
         {
            logInstance = Logging.getLog(getType());
         }
         else
         {
            logInstance = Logging.getLog(category);
         }
      }

      Log getLogInstance()
      {
         return logInstance;
      }
      
      public void set(Object bean)
      {
         super.set(bean, logInstance);
      }
   }

   private static LogProvider log = Logging.getLogProvider(WicketComponent.class);

   private Class<? extends T> type;
   
   private Class<?> enclosingType;
   private String enclosingInstanceVariableName;
   
   private List<BijectedAttribute<In>> inAttributes = new ArrayList<BijectedAttribute<In>>();
   private List<BijectedAttribute<Out>> outAttributes = new ArrayList<BijectedAttribute<Out>>();
   private List<InjectedLogger> loggerFields = new ArrayList<InjectedLogger>();
   
   private Set<AccessibleObject> conversationManagementMembers = new HashSet<AccessibleObject>();
   
   private List<StatelessInterceptor<T>> interceptors = new ArrayList<StatelessInterceptor<T>>();
   
   private Set<String> restrictions;
   
   boolean anyMethodHasRaiseEvent = false;
   
   private Class<? extends Page> noConversationPage;
   
   public Class<?> getType()
   {
      return type;
   }
   
   public static <T> WicketComponent<T> getInstance(Class<? extends T> type)
   {
      String name = getContextVariableName(type);
      if (Contexts.getApplicationContext().isSet(name))
      {
         return (WicketComponent) Contexts.getApplicationContext().get(name);
      }
      else
      {
         return null;
      }
   }
   

   private void initInterceptors()
   {
      // TODO Add a check to see whether we really need this
      interceptors.add(new BijectionInterceptor());
      if (!conversationManagementMembers.isEmpty())
      {
         interceptors.add(new ConversationInterceptor());
      }
      if (anyMethodHasRaiseEvent)
      {
         interceptors.add(new EventInterceptor());
      }
   }
   
   public List<StatelessInterceptor<T>> getInterceptors()
   {
      return interceptors;
   }
   
   public static String getContextVariableName(Class<?> type)
   {
      return type.getName() + ".wicketComponent";
   }
   
   public String getName()
   {
      return getContextVariableName(type);
   }

   public WicketComponent(Class<? extends T> type)
   {
      this.type = type;
      this.enclosingType = type.getEnclosingClass();
      if (this.enclosingType != null)
      {
         log.debug("Class: " + type + ", enclosed by " + enclosingType);
      }
      else
      {
         log.debug("Class: " + type);
      }
      
      scan();
      
      initInterceptors();
      
      Contexts.getApplicationContext().set(getName(), this);
   }
   
   private void scan()
   {
      Class clazz = type;
      scanClassEnclosureHierachy();
      while (clazz != Object.class) {
         for (Method method : clazz.getDeclaredMethods())
         {
            add(method);
         }
         for (Field field : clazz.getDeclaredFields())
         {
            add(field);
         }
         for(Constructor<T> constructor : clazz.getDeclaredConstructors())
         {
            add(constructor);
         } 
         clazz = clazz.getSuperclass();
      }
   }
   
   private void scanClassEnclosureHierachy()
   {     
      Class cls = type;
      int i = 0;
      while (cls != null)
      {
         for (Annotation annotation : cls.getAnnotations())
         {
            if (annotation instanceof Restrict)
            {
               Restrict restrict = (Restrict) annotation;
               if (restrictions == null) restrictions = new HashSet<String>();
               
               if ( Strings.isEmpty(restrict.value()) )
               {
                  throw new IllegalStateException("@Restrict on " + cls.getName() + " must specify an expression");
               }
               
               restrictions.add(restrict.value());
            }
            
            if (annotation.annotationType().isAnnotationPresent(RoleCheck.class))
            {
               if (restrictions == null) restrictions = new HashSet<String>();
               restrictions.add("#{identity.hasRole('" + 
                     annotation.annotationType().getSimpleName().toLowerCase() + "')}");
            }
            
            if (annotation instanceof NoConversationPage)
            {
               NoConversationPage noConversationPage = (NoConversationPage) annotation;
               this.noConversationPage = noConversationPage.value();
            }
         }
         
         cls = cls.getEnclosingClass();
         i++;
      }
      
      if (i > 1)
      {
         this.enclosingInstanceVariableName = "this$" + (i - 2);
      }
   }
   
   public void checkRestrictions()
   {
      if (Identity.isSecurityEnabled() && restrictions != null)      
      {
         for (String restriction : restrictions)
         {
            Identity.instance().checkRestriction(restriction);
         }
      }
   }

   public void outject(T target)
   {
      for (BijectedAttribute<Out> out : outAttributes)
      {
         Object value = out.get(target);
         if (value==null && out.getAnnotation().required())
         {
            throw new RequiredException("@Out attribute requires non-null value: " + out.toString());
         }
         else
         {
            Component component = null;
            if (out.getAnnotation().scope()==UNSPECIFIED)
            {
               component = Component.forName(out.getContextVariableName());
               if (value!=null && component!=null)
               {
                  if (!component.isInstance(value))
                  {
                     throw new IllegalArgumentException("attempted to bind an @Out attribute of the wrong type to: " + out.toString());
                  }
               }
            }
            else if (out.getAnnotation().scope()==STATELESS)
            {
               throw new IllegalArgumentException("cannot specify explicit scope=STATELESS on @Out: " + out.toString());
            }
         
            ScopeType outScope = component == null ? out.getAnnotation().scope() : component.getScope();
            
            if (outScope ==  null)
            {
               throw new IllegalArgumentException("cannot determine scope to outject to on @Out: " + out.toString());
            }
         
            if (outScope.isContextActive())
            {
               if (value==null)
               {
                  outScope.getContext().remove(out.getContextVariableName());
               }
               else
               {
                  outScope.getContext().set(out.getContextVariableName(), value);
               }
            }
         }
      }
   }
   

   public void disinject(T target)
   {
      for ( InjectedAttribute<In> in : inAttributes )
      {
         if ( !in.getType().isPrimitive() )
         {
            in.set(target, null);
         }
      }
         
   }
   
   public void inject(T instance)
   {
      for ( BijectedAttribute<In> in : inAttributes )
      {
         in.set( instance,  getValue(in, instance) );
      }
   }
   
   private void add(Constructor<T> constructor)
   {
      if ( constructor.isAnnotationPresent(Begin.class) || 
            constructor.isAnnotationPresent(org.jboss.seam.wicket.annotations.Begin.class) || 
            constructor.isAnnotationPresent(End.class) || 
            constructor.isAnnotationPresent(StartTask.class) ||
            constructor.isAnnotationPresent(BeginTask.class) ||
            constructor.isAnnotationPresent(EndTask.class) ) 
       {
          conversationManagementMembers.add(constructor);
       }
   }
   
   private void add(Method method)
   {
      if ( method.isAnnotationPresent(In.class) )
      {
         final In in = method.getAnnotation(In.class);
         inAttributes.add( new BijectedMethod(method, in)
         {

            @Override
            protected String getSpecifiedContextVariableName()
            {
               return in.value();
            }
            
         });
      }
      if ( method.isAnnotationPresent(Out.class) )
      {
         final Out out = method.getAnnotation(Out.class);
         outAttributes.add( new BijectedMethod(method, out)
         {

            @Override
            protected String getSpecifiedContextVariableName()
            {
               return out.value();
            }
            
         });
      }
      
      if ( method.isAnnotationPresent(Begin.class) || 
            method.isAnnotationPresent(org.jboss.seam.wicket.annotations.Begin.class) || 
            method.isAnnotationPresent(End.class) || 
            method.isAnnotationPresent(StartTask.class) ||
            method.isAnnotationPresent(BeginTask.class) ||
            method.isAnnotationPresent(EndTask.class) ||
            method.isAnnotationPresent(Conversational.class)) 
       {
          conversationManagementMembers.add(method);
       }
      
      if (method.isAnnotationPresent(RaiseEvent.class))
      {
         anyMethodHasRaiseEvent = true;
      }
   }
   
   private void add(Field field)
   {
      if ( field.isAnnotationPresent(In.class) )
      {
         final In in = field.getAnnotation(In.class);
         inAttributes.add( new BijectedField(field, in)
         {

            @Override
            protected String getSpecifiedContextVariableName()
            {
               return in.value();
            }
            
         });
      }
      if ( field.isAnnotationPresent(Out.class) )
      {
         final Out out = field.getAnnotation(Out.class);
         outAttributes.add( new BijectedField(field, out)
         {

            @Override
            protected String getSpecifiedContextVariableName()
            {
               return out.value();
            }
            
         });
      }
      if ( field.isAnnotationPresent(Logger.class) )
      {
         final Logger logger = field.getAnnotation(Logger.class);
         InjectedLogger loggerField = new InjectedLogger(field, logger);   
         
         if ( Modifier.isStatic( field.getModifiers() ) )
         {
            loggerField.set(null);
         }
         else
         {
            loggerFields.add(loggerField);
         }
      }
   }
   
   private Object getValue(BijectedAttribute<In> in, Object bean)
   {
      Object result;
      String name = in.getContextVariableName();
      if ( name.startsWith("#") )
      {
         if ( log.isDebugEnabled() )
         {
            log.trace("trying to inject with EL expression: " + name);
         }
         result = Expressions.instance().createValueExpression(name).getValue();
      }
      else if ( in.getAnnotation().scope()==UNSPECIFIED )
      {
         if ( log.isDebugEnabled() )
         {
            log.trace("trying to inject with hierarchical context search: " + name);
         }
         boolean create = in.getAnnotation().create() && !org.jboss.seam.contexts.Lifecycle.isDestroying();
         result = getInstanceInAllNamespaces(name, create);
      }
      else
      {
         if ( in.getAnnotation().create() )
         {
            throw new IllegalArgumentException(
                  "cannot combine create=true with explicit scope on @In: " +
                  in.toString()
               );
         }
         if ( in.getAnnotation().scope()==STATELESS )
         {
            throw new IllegalArgumentException(
                  "cannot specify explicit scope=STATELESS on @In: " +
                  in.toString()
               );
         }
         
         log.trace("trying to inject from specified context: " + name);
         
         if ( in.getAnnotation().scope().isContextActive() )
         {
            result = in.getAnnotation().scope().getContext().get(name);
         }
         else 
         {
            result = null;
         }
      }
      if ( result==null && in.getAnnotation().required() )
      {
         throw new RequiredException( "@In attribute requires non-null value: " + type + '.' + name );
      }
      else
      {
         return result;
      }
   }
   
   private static Object getInstanceInAllNamespaces(String name, boolean create)
   {
      Object result;
      result = Component.getInstance(name, create);
      if (result==null)
      {
         for ( Namespace namespace: Init.instance().getGlobalImports() )
         {
            result = namespace.getComponentInstance(name, create);
            if (result!=null) break; 
         }
      }
      return result;
   }
   
   public void initialize(T bean)
   {
      injectLog(bean);
   }
   
   private void injectLog(T bean)
   {
      for (InjectedLogger injectedLogger : loggerFields)
      {
         injectedLogger.set(bean);
      }
   }
   
   public Class<?> getEnclosingType()
   {
      return enclosingType;
   }
   
   public String getEnclosingInstanceVariableName()
   {
      return enclosingInstanceVariableName;
   }
   
   @Override
   public String toString()
   {
      return "WicketComponent(" + type + ")";
   }
   
   public boolean isConversationManagementMethod(AccessibleObject member)
   {
      return member!=null && 
            conversationManagementMembers.contains(member);
   }
   
   public Class<? extends Page> getNoConversationPage()
   {
      return noConversationPage;
   }
   
}
