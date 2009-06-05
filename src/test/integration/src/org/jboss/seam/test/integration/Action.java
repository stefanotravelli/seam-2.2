package org.jboss.seam.test.integration;

import org.jboss.seam.annotations.Name;

@Name("action")
public class Action {
      
   public String go() {
      return "success";
   }
}
