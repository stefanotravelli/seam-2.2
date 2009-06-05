package org.jboss.seam.intercept;

/**
 * The kinds of invocations that can be intercepted
 * by an EJB3 interceptor.
 * 
 * @author Gavin King
 *
 */
enum EventType
{
   AROUND_INVOKE,
   PRE_DESTORY,
   POST_CONSTRUCT,
   PRE_PASSIVATE,
   POST_ACTIVATE
}
