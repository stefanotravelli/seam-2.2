package org.jboss.seam;

import org.jboss.seam.contexts.Contexts;

/**
 * Base class of metamodels. For a class which
 * is neither an entity nor a Seam component,
 * the concrete type of the metamodel object
 * will be Model. For components or entities
 * it is a subclass of Model.
 * 
 * @author Gavin King
 *
 */
public class Model
{
   private Class<?> beanClass;

   public Model(Class<?> beanClass)
   {
      this.beanClass = beanClass;
   }
   
   public final Class<?> getBeanClass()
   {
      return beanClass;
   }

   @SuppressWarnings("deprecation")
   public static Model forClass(Class clazz)
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No application context active");
      }
      
      String name = getModelName(clazz);
      Model model = (Model) Contexts.getApplicationContext().get(name);
      if ( model==null )
      {
         model = clazz.isAnnotationPresent(javax.persistence.Entity.class) ? 
                  new Entity(clazz) : new Model(clazz);
         Contexts.getApplicationContext().set(name, model);
      }
      return model;
   }

   static String getModelName(Class clazz)
   {
      return clazz.getName() + ".model";
   }

}
