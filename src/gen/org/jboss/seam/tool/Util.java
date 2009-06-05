package org.jboss.seam.tool;

import java.util.HashSet;
import java.util.Set;

public class Util
{
   public String lower(String name)
   {
      return name.substring(0, 1).toLowerCase() + name.substring(1);
   }
   public String upper(String name)
   {
      return name.substring(0, 1).toUpperCase() + name.substring(1);
   }
   public Set set()
   {
      return new HashSet();
   }
}
