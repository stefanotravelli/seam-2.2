package org.jboss.seam.security.permission;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.security.permission.Identifier;

/**
 * A policy for the generation of object "identifiers" - unique Strings that identify a specific
 * instance of an object.  A policy can consist of numerous identifier strategies, each with the
 * ability to generate identifiers for specific classes of objects. 
 *  
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.identifierPolicy")
@Scope(APPLICATION)
@BypassInterceptors
@Install(precedence = Install.BUILT_IN)
public class IdentifierPolicy
{
   private Map<Class,IdentifierStrategy> strategies = new ConcurrentHashMap<Class,IdentifierStrategy>();
   
   private Set<IdentifierStrategy> registeredStrategies = new HashSet<IdentifierStrategy>();
   
   @Create
   public void create()
   {
      if (registeredStrategies.isEmpty())
      {
         registeredStrategies.add(new EntityIdentifierStrategy());
         registeredStrategies.add(new ClassIdentifierStrategy());
      }
   }
   
   public String getIdentifier(Object target)
   {
      if (target instanceof String)
      {
         return (String) target;
      }
      
      IdentifierStrategy strategy = strategies.get(target.getClass());
      
      if (strategy == null)
      {
         if (target.getClass().isAnnotationPresent(Identifier.class))
         {
            Class<? extends IdentifierStrategy> strategyClass = 
               target.getClass().getAnnotation(Identifier.class).value();
            
            if (strategyClass != IdentifierStrategy.class)
            {
               try
               {
                  strategy = strategyClass.newInstance();
                  strategies.put(target.getClass(), strategy);
               }
               catch (Exception ex)
               {
                  throw new RuntimeException("Error instantiating IdentifierStrategy for object " + target, ex);
               }
            }
         }

         for (IdentifierStrategy s : registeredStrategies)
         {
            if (s.canIdentify(target.getClass()))
            {
               strategy = s;
               strategies.put(target.getClass(), strategy);
               break;
            }
         }
      }
      
      return strategy != null ? strategy.getIdentifier(target) : null;
   }
   
   public Set<IdentifierStrategy> getRegisteredStrategies()
   {
      return registeredStrategies;
   }
   
   public void setRegisteredStrategies(Set<IdentifierStrategy> registeredStrategies)
   {
      this.registeredStrategies = registeredStrategies;
   }
}
