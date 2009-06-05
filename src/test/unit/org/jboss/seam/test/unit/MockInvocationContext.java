//$Id$
package org.jboss.seam.test.unit;

import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.seam.intercept.InvocationContext;

public class MockInvocationContext implements InvocationContext
{

   public Object getTarget()
   {
      //TODO
      return null;
   }

   public Map getContextData()
   {
      //TODO
      return null;
   }

   public Method getMethod()
   {
      //TODO
      return null;
   }

   public Object[] getParameters()
   {
      //TODO
      return null;
   }

   public Object proceed() throws Exception
   {
      return null;
   }

   public void setParameters(Object[] params)
   {
      //TODO
      
   }

}
