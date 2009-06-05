package org.jboss.seam.intercept;

/**
 * Interface that may be optionally implemented by an
 * interceptor, to make the stacktrace smaller.
 * 
 * @author Gavin King
 * @author Pete Muir
 *
 */
public interface OptimizedInterceptor
{
   
   public Object aroundInvoke(InvocationContext ic) throws Exception;
   
   /**
    * Returns true if this interceptor should be enabled. The component and the
    * annotation will be injected into the interceptor instance before this 
    * method is called, and can be used to decide whether the interceptor should
    * be enabled
    */
   public boolean isInterceptorEnabled();
   
}
