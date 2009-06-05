package org.jboss.seam.contexts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Swizzles entities held in the conversation context at
 * the end of each request.
 * 
 * @see PassivatedEntity
 * 
 * @author Gavin King
 *
 */
class EntityBeanMap extends AbstractEntityBeanCollection
{
   private static final long serialVersionUID = -2884601453783925804L;
   
   private Map map;
   private Map<Object, PassivatedEntity> passivatedEntityMap;
   
   public EntityBeanMap(Map instance)
   {
      this.map = instance;
   }
   
   @Override
   protected Iterable<PassivatedEntity> getPassivatedEntities() 
   {
      return passivatedEntityMap.values();
   }
   
   @Override
   protected Object getEntityCollection()
   {
      return map;
   }
   
   @Override
   protected void clearPassivatedEntities()
   {
      passivatedEntityMap = null;
   }

   @Override
   protected boolean isPassivatedEntitiesInitialized()
   {
      return passivatedEntityMap!=null;
   }

   @Override
   protected void activateAll()
   {
      for ( Map.Entry<Object, PassivatedEntity> me: passivatedEntityMap.entrySet() )
      {
         map.put( me.getKey(), me.getValue().toEntityReference(true) );
      }
      clearPassivatedEntities();
   }
   
   @Override
   protected void passivateAll()
   {
      HashMap<Object, PassivatedEntity> newPassivatedMap = 
          new HashMap<Object, PassivatedEntity>(map.size());
      boolean found = false;
      for (Map.Entry me: (Set<Map.Entry>) map.entrySet()) {
         Object value = me.getValue();
         if (value!=null) {
            PassivatedEntity passivatedEntity = PassivatedEntity.passivateEntity(value);
            if (passivatedEntity!=null) {
               if (!found) {
                   map = new HashMap(map);
                   found=true;
               }

               //this would be dangerous, except that we 
               //are doing it to a copy of the original 
               //list:
               map.remove(me.getKey()); 
               newPassivatedMap.put(me.getKey(), passivatedEntity);
            }
         }
      }
      if (found) {
          passivatedEntityMap = newPassivatedMap;
      }
   }
   
}
