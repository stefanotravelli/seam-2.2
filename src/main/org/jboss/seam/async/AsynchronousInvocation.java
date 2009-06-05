package org.jboss.seam.async;

import java.lang.reflect.Method;

import org.jboss.seam.Component;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Reflections;

/**
 * An asynchronous method invocation.
 * 
 * @author Gavin King
 *
 */
public class AsynchronousInvocation extends Asynchronous
{
   static final long serialVersionUID = 7426196491669891310L;
   
   private static transient LogProvider log = Logging.getLogProvider(AsynchronousInvocation.class);
   
   private String methodName;
   private Class[] argTypes;
   private Object[] args;
   private String componentName;
   
   public AsynchronousInvocation(Method method, String componentName, Object[] args)
   {
      this.methodName = method.getName();
      this.argTypes = method.getParameterTypes();
      this.args = args==null ? new Object[0] : args;
      this.componentName = componentName;
   }
   
   public AsynchronousInvocation(InvocationContext invocation, Component component)
   {
      this( invocation.getMethod(), component.getName(), invocation.getParameters() );
   }
   
   @Override
   public void execute(Object timer)
   {
      new ContextualAsynchronousRequest(timer)
      {
         
         @Override
         protected void process()
         {
            Object target = Component.getInstance(componentName);
            
            Method method;
            try
            {
               method = target.getClass().getMethod(methodName, argTypes);
            }
            catch (NoSuchMethodException nsme)
            {
               throw new IllegalStateException(nsme);
            }
            
            Reflections.invokeAndWrap(method, target, args);
         }
         
      }.run();
      
   }
   
   @Override
   public String toString()
   {
      return "AsynchronousInvocation(" + componentName + '.' + methodName + "())";
   }

   @Override
   protected void handleException(final Exception exception, Object timer)
   {
      new ContextualAsynchronousRequest(timer)
      {
         @Override
         protected void process()
         {
            Object target = Component.getInstance(componentName);
            try
            {
               Method method = target.getClass().getMethod("handleAsynchronousException", Exception.class);
               log.trace("Using asynchronous exception handler " + componentName + ".handleAsynchronsException;");
               method.invoke(target, exception);
            }
            catch (Exception e)
            {
               log.trace("Using default asynchronous exception handler");
               AsynchronousExceptionHandler.instance().handleException(exception);
            }
         }
      }.run();
   }
}