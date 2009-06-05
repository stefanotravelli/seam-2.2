//$Id$
package org.jboss.seam.core;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.seam.annotations.ApplicationException;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.EndTask;
import org.jboss.seam.annotations.bpm.StartTask;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.bpm.BusinessProcessInterceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.navigation.ConversationIdParameter;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.seam.persistence.PersistenceContexts;

/**
 * Implements annotation-based conversation demarcation.
 * 
 * @author Gavin King
 */
@Interceptor(stateless=true,
             around=BijectionInterceptor.class,
             within=BusinessProcessInterceptor.class)
public class ConversationInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = -5405533438107796414L;

   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      try
      {
         Method method = invocation.getMethod();
         if ( getComponent().isConversationManagementMethod(method) ) //performance optimization 
         {
      
            if ( isMissingJoin(method) )
            {
               throw new IllegalStateException("begin method invoked from a long-running conversation, try using @Begin(join=true) on method: " + method.getName());
            }
            
            if ( redirectToExistingConversation(method) ) 
            {
               return null;
            }
            else
            {
               Object result = invocation.proceed();   
               beginConversationIfNecessary(method, result);
               endConversationIfNecessary(method, result);
               return result;
            }
            
         }
         else
         {
            return invocation.proceed();
         }
      }
      catch (Exception e)
      {
         if ( isEndConversationRequired(e) )
         {
            endConversation(false, false);
         }
         throw e;
      }
   }

   private boolean isEndConversationRequired(Exception e)
   {
      Class<? extends Exception> clazz = e.getClass();
      return clazz.isAnnotationPresent(ApplicationException.class)
            && clazz.getAnnotation(ApplicationException.class).end();
   }
   
   @SuppressWarnings("deprecation")
   public boolean redirectToExistingConversation(Method method)
   {
      if ( !Manager.instance().isLongRunningConversation() )
      {
         String id = null;
         ConversationPropagation propagation = ConversationPropagation.instance(); 
         String conversation = propagation != null ? propagation.getConversationName() : null;
         
         if ( method.isAnnotationPresent(Begin.class) )
         {
            id = method.getAnnotation(Begin.class).id();
         }
         else if ( method.isAnnotationPresent(BeginTask.class) )
         {
            id = method.getAnnotation(BeginTask.class).id();
         }
         else if ( method.isAnnotationPresent(StartTask.class) )
         {
            id = method.getAnnotation(StartTask.class).id();
         }
         
         if ( id!=null && !"".equals(id) )
         {
            id = Interpolator.instance().interpolate(id);
            ConversationEntry ce = ConversationEntries.instance().getConversationEntry(id);
            if (ce==null) 
            {
               Manager.instance().updateCurrentConversationId(id);
            }
            else
            {
               return ce.redirect();
            }
         }
         else if (conversation != null && !"".equals(conversation))
         {
            ConversationIdParameter param = Pages.instance().getConversationIdParameter(conversation);
            if (param != null)
            {
               ConversationEntry ce = ConversationEntries.instance().getConversationEntry(param.getConversationId());
               if (ce != null)
               {
                  return ce.redirect();
               }
            }            
         }
      }
      
      return false;
   }

   private boolean isMissingJoin(Method method) {
      return Manager.instance().isLongRunningOrNestedConversation() && ( 
            ( 
                  method.isAnnotationPresent(Begin.class) && 
                  !method.getAnnotation(Begin.class).join() && 
                  !method.getAnnotation(Begin.class).nested() 
            ) ||
            method.isAnnotationPresent(BeginTask.class) ||
            method.isAnnotationPresent(StartTask.class) 
         );
   }

   @SuppressWarnings("deprecation")
   private void beginConversationIfNecessary(Method method, Object result)
   {
      
      boolean simpleBegin = 
            method.isAnnotationPresent(StartTask.class) || 
            method.isAnnotationPresent(BeginTask.class) ||
            ( method.isAnnotationPresent(Begin.class) && method.getAnnotation(Begin.class).ifOutcome().length==0 );
      if ( simpleBegin )
      {
         if ( result!=null || method.getReturnType().equals(void.class) )
         {
            boolean nested = false;
            if ( method.isAnnotationPresent(Begin.class) )
            {
               nested = method.getAnnotation(Begin.class).nested();
            }
            beginConversation( nested, getProcessDefinitionName(method) );
            setFlushMode(method); //TODO: what if conversation already exists? Or a nested conversation?
         }
      }
      else if ( method.isAnnotationPresent(Begin.class) )
      {
         String[] outcomes = method.getAnnotation(Begin.class).ifOutcome();
         if ( outcomes.length==0 || Arrays.asList(outcomes).contains(result) )
         {
            beginConversation( 
                  method.getAnnotation(Begin.class).nested(), 
                  getProcessDefinitionName(method) 
               );
            setFlushMode(method); //TODO: what if conversation already exists? Or a nested conversation?
         }
      }
      
   }
   
   private void setFlushMode(Method method)
   {
      FlushModeType flushMode;
      if (method.isAnnotationPresent(Begin.class))
      {
         flushMode = method.getAnnotation(Begin.class).flushMode();
      }
      else if (method.isAnnotationPresent(BeginTask.class))
      {
         flushMode = method.getAnnotation(BeginTask.class).flushMode();
      }
      else if (method.isAnnotationPresent(StartTask.class))
      {
         flushMode = method.getAnnotation(StartTask.class).flushMode();
      }
      else
      {
         return;
      }
      
      PersistenceContexts.instance().changeFlushMode(flushMode);
   }

   private String getProcessDefinitionName(Method method) {
      if ( method.isAnnotationPresent(Begin.class) )
      {
         return method.getAnnotation(Begin.class).pageflow();
      }
      if ( method.isAnnotationPresent(BeginTask.class) )
      {
         return method.getAnnotation(BeginTask.class).pageflow();
      }
      if ( method.isAnnotationPresent(StartTask.class) )
      {
         return method.getAnnotation(StartTask.class).pageflow();
      }
      //TODO: let them pass a pageflow name as a request parameter
      return "";
   }

   private void beginConversation(boolean nested, String pageflowName)
   {
      if ( !Manager.instance().isLongRunningOrNestedConversation() )
      {
         Manager.instance().beginConversation( );
         beginNavigation(pageflowName);
      }
      else if (nested)
      {
         Manager.instance().beginNestedConversation();
         beginNavigation(pageflowName);
      }
   }
   
   private void beginNavigation(String pageflowName)
   {
      if ( !pageflowName.equals("") )
      {
         if ( !Init.instance().isJbpmInstalled() ) {
            throw new IllegalArgumentException("attempting to begin pageflow but required org.jboss.seam.bpm.jbpm component is not installed");
         }
         Pageflow.instance().begin(pageflowName);
      }
   }

   @SuppressWarnings("deprecation")
   private void endConversationIfNecessary(Method method, Object result)
   {
      boolean isEndAnnotation = method.isAnnotationPresent(End.class);
      boolean isEndTaskAnnotation = method.isAnnotationPresent(EndTask.class);
      
      boolean beforeRedirect = ( isEndAnnotation && method.getAnnotation(End.class).beforeRedirect() ) ||
            ( isEndTaskAnnotation && method.getAnnotation(EndTask.class).beforeRedirect() );
      
      boolean endRoot = ( isEndAnnotation && method.getAnnotation(End.class).root() );

      boolean simpleEnd = 
            ( isEndAnnotation && method.getAnnotation(End.class).ifOutcome().length==0 ) || 
            ( isEndTaskAnnotation && method.getAnnotation(EndTask.class).ifOutcome().length==0 );
      if ( simpleEnd )
      {
         if ( result!=null || method.getReturnType().equals(void.class) ) //null outcome interpreted as redisplay
         {
            endConversation(beforeRedirect, endRoot);
         }
      }
      else if ( isEndAnnotation )
      {
         String[] outcomes = method.getAnnotation(End.class).ifOutcome();
         if ( Arrays.asList(outcomes).contains(result) )
         {
            endConversation(beforeRedirect, endRoot);
         }
      }
      else if ( isEndTaskAnnotation )
      {
         //TODO: fix minor code duplication
         String[] outcomes = method.getAnnotation(EndTask.class).ifOutcome();
         if ( Arrays.asList(outcomes).contains(result) )
         {
            endConversation(beforeRedirect, endRoot);
         }
      }
   }

   private void endConversation(boolean beforeRedirect, boolean endRoot)
   {
      if(endRoot)
      {
         Manager.instance().endRootConversation(beforeRedirect);
      }
      else
      {
         Manager.instance().endConversation(beforeRedirect);
      }
   }
   
   public boolean isInterceptorEnabled()
   {
      return getComponent().hasConversationManagementMethods();
   }

}
