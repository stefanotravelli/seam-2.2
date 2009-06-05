package org.jboss.seam.intercept;

import java.util.List;

/**
 * Adapts from Seam's InvocationContext API to the standard EE5 API.
 * (Not much to see here, they are identical apart from package names.)
 * 
 * @author Gavin King
 *
 */
class EE5SeamInvocationContext extends SeamInvocationContext implements javax.interceptor.InvocationContext
{

   public EE5SeamInvocationContext(InvocationContext context, EventType type, List<Object> userInterceptors, List<Interceptor> interceptors)
   {
      super(context, type, userInterceptors, interceptors);
   }

}
