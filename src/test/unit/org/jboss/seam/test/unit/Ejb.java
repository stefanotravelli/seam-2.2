//$Id$
package org.jboss.seam.test.unit;

import javax.ejb.Local;

@Local
public interface Ejb
{
   public void foo();
   public void destroy();
}
