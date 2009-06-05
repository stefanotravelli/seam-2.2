package org.jboss.seam.test.unit;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("action")
public class Action {
   
   @In(create=true) String name;
   
   public String go() {
      return "success";
   }
}
