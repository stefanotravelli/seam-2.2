package org.jboss.seam.contexts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
class EntityBeanSet extends AbstractEntityBeanCollection
{
   private static final long serialVersionUID = -2884601453783925804L;
   
   private Set set;
   private List<PassivatedEntity> passivatedEntityList;
   
   public EntityBeanSet(Set instance)
   {
      this.set = instance;
   }
   
   @Override
   protected Iterable<PassivatedEntity> getPassivatedEntities() 
   {
      return passivatedEntityList;
   }
   
   @Override
   protected Object getEntityCollection()
   {
      return set;
   }
   
   @Override
   protected void clearPassivatedEntities()
   {
      passivatedEntityList = null;
   }

   @Override
   protected boolean isPassivatedEntitiesInitialized()
   {
      return passivatedEntityList!=null;
   }

   @Override
   protected void activateAll()
   {
      for ( PassivatedEntity pe: passivatedEntityList )
      {
         set.add( pe.toEntityReference(true) );
      }
      passivatedEntityList = null;
   }
   
   @Override
   protected void passivateAll()
   {
       ArrayList<PassivatedEntity> newPassivatedList = new ArrayList<PassivatedEntity>( set.size() );
       boolean found = false;
       for (Object value: set){
           if (value!=null) {
               PassivatedEntity passivatedEntity = PassivatedEntity.passivateEntity(value);
               if (passivatedEntity!=null) {
                   if (!found) {
                       set = new HashSet(set);
                       found=true;
                   }
                   //this would be dangerous, except that we 
                   //are doing it to a copy of the original 
                   //list:
                   set.remove(value);                   
                   newPassivatedList.add(passivatedEntity);
               }
           }
       }     
       // if the original list was nulled out, we don't want to overwrite the passivatedEntity list
       if (found) {
           passivatedEntityList = newPassivatedList;
       }
   }
   
}
