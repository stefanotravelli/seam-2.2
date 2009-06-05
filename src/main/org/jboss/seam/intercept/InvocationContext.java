package org.jboss.seam.intercept;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * A copy of the EE5 standard InvocationContext API.
 * We do this because some poor souls are still using
 * J2EE. Pray for them.
 * 
 * @author Gavin King
 *
 */
public interface InvocationContext
{
   public Object getTarget();
   public Map getContextData();
   public Method getMethod();
   public Object[] getParameters();
   public Object proceed() throws Exception;
   public void setParameters(Object[] params);
}
