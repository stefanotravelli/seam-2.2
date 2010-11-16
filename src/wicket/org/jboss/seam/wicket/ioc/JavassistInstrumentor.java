package org.jboss.seam.wicket.ioc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.CtField.Initializer;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.wicket.WicketComponent;

/**
 * This class is responsible for instrumenting wicket component classes so that
 * they can be seam-enabled. The exact notion of what "seam-enabled" means is
 * left to the implementation in WicketComponent and WicketHandler and their
 * delegate classes, in particular the interceptor chains they create.
 * 
 * The instrumentations that take place are:
 * 
 *  <ul>
 *  <li> Add a to add a synthetic WicketHandler field similar to:
 * <pre>
 * WicketHandler handler = WicketHandler.create(this);
 * </pre>
 * as well as a synthetic getter for this field </li>
 * 
 * <li> Add a static reference to WicketComponent is created, to ensure that the instrumented
 * class is registered with WicketComponent:
 * <pre>
 * static WicketComponent component = new org.jboss.seam.wicket.WicketComponent(ThisClassName.class);
 * </pre>
 * </li>
 * <li> Make the instrumented class implement org.jboss.seam.wicket.ioc.InstrumentedComponent, which includes 
 * adding this method: 
 * <pre>
 * public InstrumentedComponent getEnclosingInstance() 
 * { 
 *     return handler == null ? null : handler.getEnclosingInstance(this); 
 * }</pre></li>
 * <li>For each non-abstract non-synthetic, non-static method (not constructor) named foobar() in this class, create
 * a synthetic private instance method, call it foobar$100, which contains the original code from foobar().  Then instrument
 * foobar to do the following:
 * <pre>
 * SomeReturnType foobar(arguments) 
 * {
 *       Method method = OurClass.class.getDeclaredMethod("foobar",argumentSignature);
 *       if (this.handler != null)
 *          this.handler.beforeInvoke(this,method);
 *       SomeReturnType result; 
 *       try {
 *         result = foobar$100(arguments);
 *       } catch (Exception e) { 
 *         throw new RuntimeException(this.handler == null ? e : this.handler.handleException(this, method, e)); 
 *       }
 *       if (this.handler != null)
 *          this.handler.affterInvoke(this,method,result);
 *       return SomeReturnType;
 *}
 *</pre></li>
 *
 * <li>A similar instrumentation occurs for constructors, with the except that a super() or this() call must precede the
 * invocation of the handler.</li>
 * </ul>
 * 
 * This instrumentor can be activated in several ways:
 * <ul>
 * <li>The WicketClassLoader will use it to instrument any class in WEB-INF/wicket</li>
 * <li>The WicketInstrumentationTask (an ant task) will use it to instrument classes specified in ant</li>
 * <li>The seam-wicket maven plugin will use it to instrument classes specified by maven configuration properties</li>
 * <li>This class implements the ClassFileTransformer interface from the java.lang.instrument package,
 * which means it can be specified with -javaagent:path/to/jboss-seam-wicket.jar.  In this case, the system
 * property "org.jboss.seam.wicket.instrumented-packages" should specify a comma-separated list of package names
 * to instrument</li></ul>
 *
 * @see java.lang.instrument.ClassFileTransformer
 * @see org.jboss.seam.wicket.ioc.WicketClassLoader
 * @see org.jboss.seam.wicket.ioc.WicketInstrumentationTask
 * @author pmuir, cpopetz
 * 
 */
public class JavassistInstrumentor implements ClassFileTransformer
{

   private static LogProvider log = Logging.getLogProvider(JavassistInstrumentor.class);

   /**
    * The javassist Classpool, used for obtaining references to needed CtClasses
    */
   private ClassPool classPool;
   
   /**
    * If the constructor is used which specifies a list of packages to instrument, for example when
    * using the -javaagent startup option, this is the list of packages
    */
   private Set<String> packagesToInstrument;

   /**
    * The CtClass for the InstrumentedComponent interface
    */
   private CtClass instrumentedComponent;

   /**
    * If true, only instrument classes annotated with @WicketComponent and their non-static inner classes.
    */
   private boolean scanAnnotations;

   /**
    * If we're only instrumenting a specific set of classes, these are the names of those classes
    */
   private Set<String> onlyTheseClasses;
   
   public JavassistInstrumentor(ClassPool classPool)
   {
      this(classPool,false);
   }
   
   public JavassistInstrumentor(ClassPool classPool, boolean scanAnnotations)
   {
      this.classPool = classPool;
      this.scanAnnotations = scanAnnotations;
   }

   public JavassistInstrumentor(ClassPool classPool, Set<String> packagesToInstrument, boolean scanAnnotations)
   {
      this(classPool,scanAnnotations);
      this.packagesToInstrument = packagesToInstrument;
   }

   public CtClass instrumentClass(String className) throws NotFoundException, CannotCompileException
   {
      log.debug("Examining " + className);
      CtClass implementation = classPool.get(className);
      if (isInstrumentable(implementation))
      {
         log.debug("Instrumenting " + className);
         instrumentClass(implementation);
      }
      return implementation;
   }

   public CtClass instrumentClass(byte[] bytes) throws IOException, RuntimeException, NotFoundException, CannotCompileException
   {
      CtClass clazz = classPool.makeClass(new ByteArrayInputStream(bytes));
      if (isInstrumentable(clazz))
      {
         instrumentClass(clazz);
         return clazz;
      }
      else
      {
         return null;
      }
   }

   /**
    * The main entry point for instrumenting a given class.  Note that this will not check if the class is instrumentable,
    * but will assume that you have.
    * @param implementation The CtClass representing the class to instrument.
    * @throws NotFoundException
    * @throws CannotCompileException
    */
   public void instrumentClass(CtClass implementation) throws NotFoundException, CannotCompileException
   {
      
      String className = implementation.getName();
      CtClass handlerClass = classPool.get(WicketHandler.class.getName());
      CtClass componentClass = classPool.get(WicketComponent.class.getName());

      /*
       * We only want one WicketHandler field per bean, so don't add that field to classes whose
       * parent has been or is to be be instrumented.
       */
      CtClass superclass = implementation.getSuperclass();
      if (!isInstrumented(superclass)) { 
         if (!isInstrumentable(superclass)) {
            //we're the top-most instrumentable class, so add the handler field
	         CtField handlerField = new CtField(handlerClass, "handler", implementation);
	         handlerField.setModifiers(Modifier.PROTECTED);
	         Initializer handlerInitializer = Initializer.byCall(handlerClass, "create");
	         implementation.addField(handlerField, handlerInitializer);
	         CtMethod getHandlerMethod = CtNewMethod.getter("getHandler", handlerField);
	         implementation.addMethod(getHandlerMethod);
         }
         else { 
            //in order for the below code to make reference to the handler instance we need to 
            //recursively instrument until we reach the top of the instrumentable class tree
            instrumentClass(superclass);
         }
      }

      CtField wicketComponentField = new CtField(componentClass, "component", implementation);
      wicketComponentField.setModifiers(Modifier.STATIC);
      Initializer componentInit = Initializer.byExpr("new org.jboss.seam.wicket.WicketComponent(" + className + ".class)");
      implementation.addField(wicketComponentField, componentInit);

      CtClass exception = classPool.get(Exception.class.getName());

      implementation.addInterface(getInstrumentedComponentInterface());
      CtMethod getEnclosingInstance = CtNewMethod.make("public " + InstrumentedComponent.class.getName() + " getEnclosingInstance() { return getHandler() == null ? null : getHandler().getEnclosingInstance(this); }", implementation);
      implementation.addMethod(getEnclosingInstance);

      for (CtMethod method : implementation.getDeclaredMethods())
      {
         if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()))
         {
            if (!("getHandler".equals(method.getName()) || "getEnclosingInstance".equals(method.getName())))
            {
               String newName = implementation.makeUniqueName(method.getName());

               CtMethod newMethod = CtNewMethod.copy(method, newName, implementation, null);
               newMethod.setModifiers(Modifier.PRIVATE);
               implementation.addMethod(newMethod);
               method.setBody(createBody(implementation, method, newMethod));
               log.trace("instrumented method " + method.getName());
            }
         }
      }
      for (CtConstructor constructor : implementation.getConstructors())
      {
         if (constructor.isConstructor())
         {
            {
               String constructorObject = createConstructorObject(className, constructor);
               constructor.insertBeforeBody(constructorObject + "getHandler().beforeInvoke(this, constructor);");
               constructor.addCatch("{" + constructorObject + "throw new RuntimeException(getHandler().handleException(this, constructor, e));}", exception, "e");
               constructor.insertAfter(constructorObject + "getHandler().afterInvoke(this, constructor);");
               log.trace("instrumented constructor " + constructor.getName());
            }
         }
      }
   }

   /**
    * Create the body of the synthetic method
    * @param clazz in this class
    * @param method for this method
    * @param newMethod the synthetic method
    * @return the string of code for the body
    * @throws NotFoundException
    */
   private static String createBody(CtClass clazz, CtMethod method, CtMethod newMethod) throws NotFoundException
   {
      String src = "{" + createMethodObject(clazz, method) + "if (getHandler() != null) getHandler().beforeInvoke(this, method);" + createMethodDelegation(newMethod) + "if (this.handler != null) result = ($r) this.handler.afterInvoke(this, method, ($w) result); return ($r) result;}";

      log.trace("Creating method " + clazz.getName() + "." + newMethod.getName() + "(" + newMethod.getSignature() + ")" + src);
      return src;
   }

   /**
    * Create the code for delegating to a given method, including handling exceptions
    * @param method The method to which we are delegating
    * @return the string of code for the delegation
    * @throws NotFoundException
    */
   private static String createMethodDelegation(CtMethod method) throws NotFoundException
   {
      CtClass returnType = method.getReturnType();
      if (returnType.equals(CtClass.voidType))
      {
         return "Object result = null; " + wrapInExceptionHandler(method.getName() + "($$);");
      }
      else
      {
         String src = returnType.getName() + " result;";
         src += wrapInExceptionHandler("result = " + method.getName() + "($$);");
         return src;
      }
   }

   /**
    * Wrap some code in an exception handler that uses the WicketHandler to handle the exception
    * @param src The code to wrap
    * @return The wrapped code
    */
   private static String wrapInExceptionHandler(String src)
   {
      return "try {" + src + "} catch (Exception e) { throw new RuntimeException(getHandler() == null ? e : getHandler().handleException(this, method, e)); }";
   }

   /**
    * Create an arrray of parameter types for a given method or constructor.
    * @param behavior The method or constructor
    * @return The source string representing the declaration and initialization of the parameterTypes array
    * @throws NotFoundException
    */
   private static String createParameterTypesArray(CtBehavior behavior) throws NotFoundException
   {
      String src = "Class[] parameterTypes = new Class[" + behavior.getParameterTypes().length + "];";
      for (int i = 0; i < behavior.getParameterTypes().length; i++)
      {
         src += "parameterTypes[" + i + "] = " + behavior.getParameterTypes()[i].getName() + ".class;";
      }
      return src;
   }

   /**
    * Create the code for initializing a Method object for a given method
    * @param clazz The class in which the method can be looked up
    * @param method The method in question
    * @return Source for looking up the method and declaring/initializing the "method" local variable
    * @throws NotFoundException
    */
   private static String createMethodObject(CtClass clazz, CtMethod method) throws NotFoundException
   {
      String src = createParameterTypesArray(method);
      src += "java.lang.reflect.Method method = " + clazz.getName() + ".class.getDeclaredMethod(\"" + method.getName() + "\", parameterTypes);";
      return src;
   }

   /**
    * Create the code for initializing a Constructor object for a given constructor
    * @param className The name of the class in which the constructor can be looked up
    * @param constructor The constructor to look up
    * @return Source for looking up the constructor and declaring/initializing the "consturctor" local variable
    * @throws NotFoundException
    */
   private static String createConstructorObject(String className, CtConstructor constructor) throws NotFoundException
   {
      String src = createParameterTypesArray(constructor);
      src += "java.lang.reflect.Constructor constructor = " + className + ".class.getDeclaredConstructor(parameterTypes);";
      return src;
   }

   /**
    * Does this class alone have the SeamWicketAnnotation?
    */
   public boolean hasWicketAnnotation(CtClass clazz) 
   { 
      
      try
      {
         for (Object a : clazz.getAnnotations())
          {
             if (a instanceof SeamWicketComponent)
             {
                return true;
             }
          }
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
      return false;
   }
   
   /**
    * Does this class, or any of its nonstatic enclosing classes, or any of its superclasses contain
    * the SeamWicketComponent marker annotation?
    */
   public boolean markedInstrumentable(CtClass clazz) 
   {
      if (hasWicketAnnotation(clazz))
      {
         return true;
      }
      try 
      {
	      CtClass enclosing = 
	         Modifier.isStatic(clazz.getModifiers()) ? null : clazz.getDeclaringClass();
	      if (enclosing != null && markedInstrumentable(enclosing))
	      {
	         return true;
	      }
	      CtClass superclass = clazz.getSuperclass();
	      if (superclass != null && markedInstrumentable(superclass))
	      {
	         return true;
	      }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
      return false;
   }
      

      
   /**
    * Returns true if the given class can be instrumented.  This will return false if:
    * <ul>
    * <li> The class is an interface or an enum
    * <li> The class is annotated with Seam's @Name annotation or is a non-static inner class of a @Named class
    * <li> The class is already instrumented.  We check this by checking if it already implements the InstrumentedComponent
    * interface
    * </ul>
    * @param clazz The class to check
    * @return boolean value if the class can be instrumented or not
    */
   public boolean isInstrumentable(CtClass clazz)
   {
      int modifiers = clazz.getModifiers();
      if (Modifier.isInterface(modifiers) || Modifier.isEnum(modifiers))
      {
         return false;
      }
      if (onlyTheseClasses != null && !onlyTheseClasses.contains(clazz.getName()))
      {
         return false;
      }

      try
      {
         // do not instrument @Named components or nested non-static classes
         // inside named components.
         CtClass checkName = clazz;
         do
         {
            for (Object a : checkName.getAnnotations())
            {
               if (a instanceof Name)
               {
                  return false;
               }
            }
            checkName = Modifier.isStatic(clazz.getModifiers()) ? null : checkName.getDeclaringClass();
         }
         while (checkName != null);

         if (scanAnnotations && !markedInstrumentable(clazz))
         {
            return false;
         }
         
         // do not instrument something we've already instrumented.
         // can't use 'isSubtype' because the superclass may be instrumented
         // while we are not
         if (isInstrumented(clazz))
            return false;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      return true;
   }

   private boolean isInstrumented(CtClass clazz)
   {
      for (String inf : clazz.getClassFile2().getInterfaces())
         if (inf.equals(getInstrumentedComponentInterface().getName()))
            return true;
      return false;
   }

   /**
    * We have to look this up lazily because when our constructor is called we may not have the appropriate paths added to our ClassPool,
    * particularly if we are doing runtime instrumentation using WEB-INF/wicket
    */
   private CtClass getInstrumentedComponentInterface() 
   {
      if (instrumentedComponent == null)
      {
         try
         {
            instrumentedComponent = classPool.get(InstrumentedComponent.class.getName());
         }
         catch (NotFoundException e)
         {
            throw new RuntimeException(e);
         }
      }
      return instrumentedComponent;
   }
   
   /**
    * This is the implementation of the ClassFileTransformer interface.  
    * @see java.lang.instrument.ClassFileTransformer
    */
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
   {
      int index = className.lastIndexOf("/");
      if (index < 1)
         return null;
      String packageName = className.substring(0, index);
      do 
      { 
         if (packagesToInstrument.contains(packageName) && !className.contains("_javassist_"))
         {
            try
            {
               CtClass clazz = classPool.get(className);
               if (clazz.isModified())
                  return clazz.toBytecode();
               clazz = instrumentClass(classfileBuffer);
               if (clazz == null)
                  return null;
               else
                  return clazz.toBytecode();
            }
            catch (Exception e)
            {
               e.printStackTrace();
               throw new RuntimeException(e);
            }
         }
         index = packageName.lastIndexOf('/');
         if (index < 1)
         {
            packageName = "";
         }
         else 
         {
            packageName = packageName.substring(0,index);
         }
      } while (packageName.length() > 0);
      
      return null;
   }

   /**
    * This premain will be called if the vm is started with -javaagent:/path/to/jar/with/this/class
    */
   public static void premain(String args, Instrumentation instrumentation)
   {
      initAgent(instrumentation);
   }

   /**
    * This premain will be called if the vm is told to use this agent after startup, which is done
    * in a vm-dependent way
    */
   public static void agentmain(String args, Instrumentation instrumentation)
   {
      initAgent(instrumentation);
   }

   /**
    * Set up instrumentation.  This adds ourselves as a transformer to the instrumentation, and
    * loads the set of packages to transform from the "org.jboss.seam.wicket.instrumented-packages"
    * System property.
    */
   private static void initAgent(Instrumentation instrumentation)
   {
      Set<String> packagesToInstrument = new HashSet<String>();
      String list = System.getProperty("org.jboss.seam.wicket.instrumented-packages");
      String scanAnnotationsProperty = System.getProperty("org.jboss.seam.wicket.scanAnnotations");
      boolean scanAnnotations = scanAnnotationsProperty == null ? false : scanAnnotationsProperty.equals("true");
      if (list == null)
         return;
      for (String packageName : list.split(","))
      {
         packagesToInstrument.add(packageName.replaceAll("\\.", "/"));
      }

      ClassPool classPool = new ClassPool();
      classPool.appendSystemPath();
      instrumentation.addTransformer(new JavassistInstrumentor(classPool, packagesToInstrument, scanAnnotations));
   }

   /**
    * This instruments a specific set of classes and writes their classes to the specified directory, if that
    * directory is non-null
    * @param toInstrument the set of class names to instrument
    * @param path where to write the modified classes, or null to not write anything
    * @throws CannotCompileException 
    * @throws NotFoundException 
    */
   public void instrumentClassSet(Set<String> toInstrument, String path) throws CannotCompileException, NotFoundException 
   {
      this.onlyTheseClasses = toInstrument;
      for (String className : toInstrument) 
      {
         CtClass clazz = instrumentClass(className);
         if (path != null && clazz.isModified())
         {
            try 
            { 
               clazz.writeFile(path);
            }
            catch (IOException e) 
            {
               throw new RuntimeException(e);
            }
         }
      }
   }
}
