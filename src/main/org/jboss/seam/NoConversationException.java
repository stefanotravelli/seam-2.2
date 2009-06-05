package org.jboss.seam;

/**
 * Throw when a component marked @Conversational is called
 * outside the scope of a long-running conversation.
 * 
 * @author Gavin King
 *
 */
public class NoConversationException extends RuntimeException
{

   private static final long serialVersionUID = -5437384703541823179L;

   public NoConversationException(String message)
   {
      super(message);
   }
}
