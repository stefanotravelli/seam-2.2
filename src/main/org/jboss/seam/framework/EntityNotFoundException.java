package org.jboss.seam.framework;

import javax.ejb.ApplicationException;

import org.jboss.seam.annotations.exception.HttpError;

@HttpError(errorCode=404)
@ApplicationException(rollback=true) 
public class EntityNotFoundException extends RuntimeException
{
   private static final long serialVersionUID = -3469578090343847583L;
   
   private Object id;
   private Class entityClass;
   
   public EntityNotFoundException(Object id, Class entityClass)
   {
      super( String.format("entity not found: %s#%s", entityClass.getName(), id) );
      this.id = id;
      this.entityClass = entityClass;
   }
   
   public Class getEntityClass()
   {
      return entityClass;
   }

   public Object getId()
   {
      return id;
   }
   
}
