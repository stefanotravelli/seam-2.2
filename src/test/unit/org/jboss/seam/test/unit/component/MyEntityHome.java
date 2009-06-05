package org.jboss.seam.test.unit.component;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.test.unit.entity.SimpleEntity;

@Name("myEntityHome")
public class MyEntityHome extends EntityHome<SimpleEntity>
{
   @Override
   public void create()
   {
      if ( getEntityClass()==null )
      {
         throw new IllegalStateException("entityClass is null");
      }
      initDefaultMessages();
   }
}
