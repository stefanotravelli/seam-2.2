package org.jboss.seam.exception;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Interpolator;

/**
 * Base implementation of HTTP error exception handlers.
 * 
 * @author Gavin King
 *
 */
public abstract class ErrorHandler extends ExceptionHandler
{

   protected abstract int getCode(Exception e);
   protected abstract String getMessage(Exception e);
   protected abstract boolean isEnd(Exception e);

   @Override
   public void handle(Exception e) throws Exception
   {
      if ( Contexts.isConversationContextActive() && isEnd(e) ) 
      {
         Conversation.instance().end();
      }
      
      String msg = getDisplayMessage( e, getMessage(e) );
      msg = msg==null ? null : Interpolator.instance().interpolate(msg);
      error( getCode(e), msg );
   }

   @Override
   public String toString()
   {
      return "ErrorHandler";
   }
}