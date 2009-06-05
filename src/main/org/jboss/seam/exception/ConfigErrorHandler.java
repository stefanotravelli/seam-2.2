package org.jboss.seam.exception;

/**
 * Implements &lt;http-error/&gt; for pages.xml
 * 
 * @author Gavin King
 *
 */
public final class ConfigErrorHandler extends ErrorHandler
{
   private final String message;
   private final boolean conversation;
   private final Class clazz;
   private final int code;

   public ConfigErrorHandler(String message, boolean conversation, Class clazz, int code)
   {
      this.message = message;
      this.conversation = conversation;
      this.clazz = clazz;
      this.code = code;
   }

   @Override
   protected String getMessage(Exception e)
   {
      return message;
   }

   @Override
   protected int getCode(Exception e)
   {
      return code;
   }

   @Override
   public boolean isHandler(Exception e)
   {
      return clazz.isInstance(e);
   }

   @Override
   protected boolean isEnd(Exception e)
   {
      return conversation;
   }

}