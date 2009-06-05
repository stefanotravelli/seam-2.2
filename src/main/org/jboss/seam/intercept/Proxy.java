package org.jboss.seam.intercept;

import java.io.Serializable;

public interface Proxy extends Serializable
{
   public Object writeReplace();
}
