package org.jboss.seam.security.permission;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.security.permission.Identifier;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.util.Strings;

/**
 * An Identifier strategy for entity-based permission checks
 * 
 * @author Shane Bryzak
 */
public class EntityIdentifierStrategy implements IdentifierStrategy
{
   private ValueExpression<EntityManager> entityManager;   
   
   private PersistenceProvider persistenceProvider;
   
   private Map<Class,String> identifierNames = new ConcurrentHashMap<Class,String>();
   
   public EntityIdentifierStrategy()
   {
      persistenceProvider = (PersistenceProvider) Component.getInstance(PersistenceProvider.class, true);
      
      if (entityManager == null)
      {
         entityManager = Expressions.instance().createValueExpression("#{entityManager}", 
               EntityManager.class);
      }         
   }
   
   public boolean canIdentify(Class targetClass)
   {
      return targetClass.isAnnotationPresent(Entity.class);
   }

   public String getIdentifier(Object target)
   {
      return String.format("%s:%s", getIdentifierName(target.getClass()),  
        persistenceProvider.getId(target, lookupEntityManager()).toString());
   }
   
   private String getIdentifierName(Class cls)
   {
      if (!identifierNames.containsKey(cls))
      {   
         String name = null;
         
         if (cls.isAnnotationPresent(Identifier.class))
         {
            Identifier identifier = (Identifier) cls.getAnnotation(Identifier.class);
            if ( !Strings.isEmpty(identifier.name()) )
            {
               name = identifier.name();
            }
         }
         
         if (name == null)
         {
            name = Seam.getComponentName(cls);
         }
         
         if (name == null)
         {
            name = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
         }
         
         identifierNames.put(cls, name);
         return name;
      }
      
      return identifierNames.get(cls);
   }

   private EntityManager lookupEntityManager()
   {
      return entityManager.getValue();
   }
}
