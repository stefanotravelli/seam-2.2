package org.jboss.seam.cache;

import org.jboss.cache.Fqn;

public abstract class AbstractJBossCacheProvider<T> extends CacheProvider<T>
{
   
   public AbstractJBossCacheProvider()
   {
      super.setConfiguration("treecache.xml");
   }
   
   private Fqn defaultFqn;
   
   protected Fqn getFqn(String region)
   {
      if (region != null)
      {
         return Fqn.fromString(region);
      }
      else
      {
         if (defaultFqn == null)
         {
            defaultFqn = Fqn.fromString(getDefaultRegion());
         }
         return defaultFqn;
      }
   }
   
   @Override
   public void setDefaultRegion(String defaultRegion)
   {
      super.setDefaultRegion(defaultRegion);
      this.defaultFqn = Fqn.fromString(defaultRegion);
   }

}