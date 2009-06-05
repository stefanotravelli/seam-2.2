package org.jboss.seam.framework;

import java.io.Serializable;

/**
 * Wrapper class that identifies an entity
 * 
 * @author Pete Muir
 *
 */
public abstract class Identifier<T> implements Serializable
{

   public Identifier(Class clazz, Object id)
   {
      if (clazz == null || id == null)
      {
         throw new IllegalArgumentException("Id and clazz must not be null");
      }
      this.clazz = clazz;
      this.id = id;
   }
   
   private Class clazz;
   private Object id;

   public Class getClazz()
   {
      return clazz;
   }
   
   public Object getId()
   {
      return id;
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof Identifier)
      {
         Identifier that = (Identifier) other; 
         if (id == null || clazz == null)
         {
            throw new IllegalArgumentException("Class and Id must not be null");
         }
         else 
         {
            return this.getId().equals(that.getId()) && this.getClazz().equals(that.getClazz());
         }
      }
      return false;
   }
   
   public abstract Object find(T persistenceContext);

}