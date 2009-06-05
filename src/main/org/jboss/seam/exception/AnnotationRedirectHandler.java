package org.jboss.seam.exception;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import org.jboss.seam.annotations.exception.Redirect;
import org.jboss.seam.core.Expressions;

/**
 * Implements @Redirect
 * 
 * @see Redirect
 * @author Gavin King
 *
 */
public class AnnotationRedirectHandler extends RedirectHandler
{
   @Override
   public boolean isHandler(Exception e)
   {
      return e.getClass().isAnnotationPresent(Redirect.class);
   }
   
   @Override
   protected String getMessage(Exception e)
   {
      return e.getClass().getAnnotation(Redirect.class).message();
   }
   
   @Override
   protected Severity getMessageSeverity(Exception e)
   {
      return FacesMessage.SEVERITY_INFO;
   }
   
   @Override
   protected String getViewId(Exception e)
   {
      return Expressions.instance().createValueExpression(e.getClass().getAnnotation(Redirect.class).viewId(), String.class).getValue();
   }
   
   @Override
   @SuppressWarnings("deprecation")
   protected boolean isEnd(Exception e)
   {
      return e.getClass().getAnnotation(Redirect.class).end();
   }
   
}