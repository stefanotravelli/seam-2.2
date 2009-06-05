package org.jboss.seam.exception;

import org.jboss.seam.annotations.exception.HttpError;

/**
 * Implements @HttpError
 * 
 * @see HttpError
 * @author Gavin King
 *
 */
public class AnnotationErrorHandler extends ErrorHandler
{
   @Override
   public boolean isHandler(Exception e)
   {
      return e.getClass().isAnnotationPresent(HttpError.class);
   }
   
   @Override
   protected String getMessage(Exception e)
   {
      return e.getClass().getAnnotation(HttpError.class).message();
   }
   
   @Override
   protected int getCode(Exception e)
   {
      return e.getClass().getAnnotation(HttpError.class).errorCode();
   }
   
   @Override
   @SuppressWarnings("deprecation")
   protected boolean isEnd(Exception e)
   {
      return e.getClass().getAnnotation(HttpError.class).end();
   }
   
}