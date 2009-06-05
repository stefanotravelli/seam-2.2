package org.jboss.seam.ioc.guice;

import com.google.inject.Guice;
import com.google.inject.Module;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates Guice injector from a list of modules.
 *
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 */
@Name("org.jboss.seam.ioc.guice.injector")
@Scope(ScopeType.APPLICATION)
@Startup
@Install(value=false, precedence = FRAMEWORK)
public class Injector implements Serializable
{
   private static final long serialVersionUID = 8935525407647910950L;

   private static final LogProvider log = Logging.getLogProvider(Injector.class);

   private String[] modules;

   private com.google.inject.Injector injector = null;

   @Unwrap
   public com.google.inject.Injector getInjector()
   {
      return injector;
   }

   @Create
   public void createInjector() throws IllegalAccessException, InstantiationException, ClassNotFoundException
   {
      if ((modules == null) || (modules.length == 0))
      {
         throw new IllegalArgumentException("No Guice module specified.");
      }

      log.debug("Creating injector '" +
               "'from modules: " + Arrays.toString(modules));

      final List<Module> moduleList = getModuleList(modules);

      injector = Guice.createInjector(moduleList);
   }

   private static List<Module> getModuleList(String[] modules) throws IllegalAccessException, InstantiationException, ClassNotFoundException
   {
      List<Module> moduleList = new ArrayList<Module>();

      for (String m : modules)
      {
         Module module = getModule(m);
         moduleList.add(module);
      }

      return moduleList;
   }

   private static Module getModule(String className)
   {
      try
      {
         final Class<?> clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

         return (Module) clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Unable to create guice module: " + className, e);
      }
   }

   public String[] getModules()
   {
      return modules;
   }

   public void setModules(String[] modules)
   {
      this.modules = modules;
   }
}
