package org.jboss.seam.contexts;

import java.io.Serializable;


interface Wrapper extends Serializable
{
   public Object getInstance();
   public void activate();
   public boolean passivate();
}
