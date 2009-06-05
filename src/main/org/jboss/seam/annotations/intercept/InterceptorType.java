package org.jboss.seam.annotations.intercept;

/**
 * The type of an Interceptor, "client-side" (around the EJB proxy object)
 * or "server-side" (inside the EJB interceptor stack).
 * 
 * @author Gavin King
 *
 */
public enum InterceptorType
{
   /**
    * An interceptor that wraps the EJB proxy object, and intercepts
    * invocations before EJB itself does any work.
    */
   CLIENT,
   /**
    * An interceptor that runs as part of the EJB interceptor stack.
    */
   SERVER,
   ANY
}
