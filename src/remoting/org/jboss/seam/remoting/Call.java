package org.jboss.seam.remoting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.remoting.wrapper.ConversionException;
import org.jboss.seam.remoting.wrapper.ConversionScore;
import org.jboss.seam.remoting.wrapper.Wrapper;
import org.jboss.seam.util.EJB;

/**
 * 
 * @author Shane Bryzak
 */
public class Call
{
   private String id;
   private String componentName;
   private String methodName;
   // private String expression;
   private Throwable exception;

   private List<Wrapper> params = new ArrayList<Wrapper>();

   private Object result;

   private CallContext context;

   private List<String> constraints = null;

   /**
    * Constructor.
    * 
    * @param componentName
    *           String
    * @param methodName
    *           String
    */
   public Call(String id, String componentName, String methodName) {
      this.id = id;
      this.componentName = componentName;
      this.methodName = methodName;
      this.context = new CallContext();
   }

   /**
    * Return the call context.
    * 
    * @return CallContext
    */
   public CallContext getContext()
   {
      return context;
   }
   
   /**
    * Returns the exception thrown by the invoked method.  If no exception was thrown,
    * will return null.
    */
   public Throwable getException()
   {
      return exception;
   }

   /**
    * Add a parameter to this call.
    * 
    * @param param
    */
   public void addParameter(Wrapper param)
   {
      params.add(param);
   }

   /**
    * Returns the result of this call.
    * 
    * @return Wrapper
    */
   public Object getResult()
   {
      return result;
   }
   
   /**
    * Required for unit tests
    * 
    * @param result
    */
   public void setResult(Object result)
   {
      this.result = result;
   }

   /**
    * Returns the id of this call.
    * 
    * @return String
    */
   public String getId()
   {
      return id;
   }

   /**
    * Returns the object graph constraints annotated on the method that is
    * called.
    * 
    * @return List The constraints
    */
   public List<String> getConstraints()
   {
      return constraints;
   }
   
   /**
    * Required for unit tests
    * 
    * @param constraints
    */
   public void setConstraints(List<String> constraints)
   {
      this.constraints = constraints;
   }

   /**
    * Execute this call
    * 
    * @throws Exception
    */
   public void execute() throws Exception
   {
      if (componentName != null)
      {
         processInvocation();
      }
   }

   private void processInvocation() throws Exception
   {
      // Find the component we're calling
      Component component = Component.forName(componentName);

      if (component == null)
      {
         throw new RuntimeException("No such component: " + componentName);
      }

      // Create an instance of the component
      Object instance = Component.getInstance(componentName, true);

      if (instance == null)
      {
         throw new RuntimeException(String.format(
               "Could not create instance of component %s", componentName));
      }

      Class type = null;

      if (component.getType().isSessionBean()
            && component.getBusinessInterfaces().size() > 0)
      {
         for (Class c : component.getBusinessInterfaces())
         {
            if (c.isAnnotationPresent(EJB.LOCAL))
            {
               type = c;
               break;
            }
         }

         if (type == null)
         {
            throw new RuntimeException(String.format(
               "Type cannot be determined for component [%s]. Please ensure that it has a local interface.",
               component));
         }
      }

      if (type == null)
      {
         type = component.getBeanClass();
      }

      // Find the method according to the method name and the parameter classes
      Method m = findMethod(methodName, type);
      if (m == null)
         throw new RuntimeException("No compatible method found.");

      if (m.getAnnotation(WebRemote.class).exclude().length > 0)
         constraints = Arrays
               .asList(m.getAnnotation(WebRemote.class).exclude());

      Object[] params = convertParams(m.getGenericParameterTypes());

      // Invoke!
      try
      {
         result = m.invoke(instance, params);
      } 
      catch (InvocationTargetException e)
      {
         this.exception = e.getCause();
      }
   }

   /**
    * Convert our parameter values to an Object array of the specified target
    * types.
    * 
    * @param targetTypes
    *           Class[] An array containing the target class types.
    * @return Object[] The converted parameter values.
    */
   private Object[] convertParams(Type[] targetTypes)
         throws ConversionException
   {
      Object[] paramValues = new Object[targetTypes.length];

      for (int i = 0; i < targetTypes.length; i++)
      {
         paramValues[i] = params.get(i).convert(targetTypes[i]);
      }

      return paramValues;
   }

   /**
    * Find the best matching method within the specified class according to the
    * parameter types that were provided to the Call.
    * 
    * @param name
    *           String The name of the method.
    * @param cls
    *           Class The Class to search in.
    * @return Method The best matching method.
    */
   private Method findMethod(String name, Class cls)
   {
      Map<Method, Integer> candidates = new HashMap<Method, Integer>();

      for (Method m : cls.getDeclaredMethods())
      {
         if (m.getAnnotation(WebRemote.class) == null) continue;

         if (name.equals(m.getName())
               && m.getParameterTypes().length == params.size())
         {
            int score = 0;

            for (int i = 0; i < m.getParameterTypes().length; i++)
            {
               ConversionScore convScore = params.get(i).conversionScore(
                     m.getParameterTypes()[i]);
               if (convScore == ConversionScore.nomatch)
                  continue;
               score += convScore.getScore();
            }
            candidates.put(m, score);
         }
      }

      Method bestMethod = null;
      int bestScore = 0;

      for (Entry<Method,Integer> entry : candidates.entrySet())
      {
         int thisScore = entry.getValue();
         if (bestMethod == null || thisScore > bestScore) {
            bestMethod = entry.getKey();
            bestScore = thisScore;
         }
      }

      return bestMethod;
   }
}
