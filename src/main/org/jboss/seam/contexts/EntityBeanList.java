package org.jboss.seam.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Swizzles entities held in the conversation context at
 * the end of each request.
 * 
 * @see PassivatedEntity
 * 
 * @author Gavin King
 *
 */
class EntityBeanList extends AbstractEntityBeanCollection
{
   private static final long serialVersionUID = -2884601453783925804L;
 
   private List list;
   private List<PassivatedEntity> passivatedEntityList;
   
   public EntityBeanList(List instance)
   {
      this.list = instance;
   }
   
   @Override
   protected Iterable<PassivatedEntity> getPassivatedEntities() 
   {
      return passivatedEntityList;
   }
   
   @Override
   protected Object getEntityCollection()
   {
      return list;
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
      for (int i=0; i<passivatedEntityList.size(); i++)
      {
         PassivatedEntity passivatedEntity = passivatedEntityList.get(i);
         if (passivatedEntity!=null)
         {
            list.set( i, passivatedEntity.toEntityReference(true) );
         }
      }
      passivatedEntityList=null;
   }

   @Override
   protected void passivateAll()
   {       
       List<PassivatedEntity> newPassivatedList = new ArrayList<PassivatedEntity>(list.size());               

       boolean found = false;
       Iterator<Object> it = list.iterator();
       for (int i=0; it.hasNext(); i++) {
           PassivatedEntity passivatedEntity = null;
           Object value = it.next();
           if (value != null) {
               passivatedEntity = PassivatedEntity.passivateEntity(value);
               
               if (passivatedEntity!=null) {
                   if (!found) {
                       list = new ArrayList(list);
                       found=true;
                   }

                   //this would be dangerous, except that we 
                   //are doing it to a copy of the original 
                   //list:
                   list.set(i, null); 
               }                               
           }          
           newPassivatedList.add(passivatedEntity);
       }
       
       // if the original list was nulled out, we don't want to overwrite the passivatedEntity list
       if (found) {
           passivatedEntityList = newPassivatedList;
       }
   }
   
}
