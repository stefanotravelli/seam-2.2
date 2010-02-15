package org.jboss.seam.example.restbay.resteasy;

import javax.ejb.Local;

@Local
public interface TestEjbLocal
{
   boolean foo();
   void remove();
}
