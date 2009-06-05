package org.jboss.seam.wicket.ioc;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.wicket.AbortException;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.jboss.seam.NoConversationException;
import org.jboss.seam.annotations.ApplicationException;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Conversational;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.EndTask;
import org.jboss.seam.annotations.bpm.StartTask;
import org.jboss.seam.core.ConversationEntries;
import org.jboss.seam.core.ConversationEntry;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.Manager;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.navigation.ConversationIdParameter;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.seam.persistence.PersistenceContexts;

public class ConversationInterceptor<T> implements StatelessInterceptor<T>
{
   
   public void beforeInvoke(InvocationContext<T> invocationContext)
   {
      if (invocationContext.getComponent().isConversationManagementMethod(invocationContext.getAccessibleObject()))
      {
         if ( isMissingJoin(invocationContext) )
         {
            throw new IllegalStateException("begin method invoked from a long-running conversation, try using @Begin(join=true) on method: " + invocationContext.getMember().getName());
         }
         
         checkForConversation(invocationContext);
      }
   }
   
   public Object afterInvoke(InvocationContext<T> invocationContext, Object result)
   {
      if (invocationContext.getComponent().isConversationManagementMethod(invocationContext.getAccessibleObject()))
      {
         beginConversationIfNecessary(invocationContext, result);
         endConversationIfNecessary(invocationContext, result);
      }
      return result;
   }
   
   public Exception handleException(InvocationContext<T> invocationContext, Exception exception)
   {
      if ( isEndConversationRequired(exception) )
      {
         endConversation(false);
      }
      return exception;
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

   private boolean isMissingJoin(InvocationContext invocationContext) {
      return Manager.instance().isLongRunningOrNestedConversation() && ( 
            ( 
                  invocationContext.getAccessibleObject().isAnnotationPresent(Begin.class) && 
                  !invocationContext.getAccessibleObject().getAnnotation(Begin.class).join() && 
                  !invocationContext.getAccessibleObject().getAnnotation(Begin.class).nested() 
            ) ||
            ( 
                  invocationContext.getAccessibleObject().isAnnotationPresent(org.jboss.seam.wicket.annotations.Begin.class) && 
                  !invocationContext.getAccessibleObject().getAnnotation(org.jboss.seam.wicket.annotations.Begin.class).join() && 
                  !invocationContext.getAccessibleObject().getAnnotation(org.jboss.seam.wicket.annotations.Begin.class).nested() 
            ) ||
            invocationContext.getAccessibleObject().isAnnotationPresent(BeginTask.class) ||
            invocationContext.getAccessibleObject().isAnnotationPresent(StartTask.class) 
         );
   }
   
   private void checkForConversation(InvocationContext<T> invocationContext)  
   {
      if (!Manager.instance().isLongRunningConversation() && 
           invocationContext.getAccessibleObject().isAnnotationPresent(Conversational.class))
      {
         Class<? extends Page> noConversationPage = invocationContext.getComponent().getNoConversationPage();
         if (noConversationPage != null)
         {
            final RequestCycle cycle = RequestCycle.get();
            StatusMessages.instance().addFromResourceBundleOrDefault( 
                  StatusMessage.Severity.WARN, 
                  "org.jboss.seam.NoConversation", 
                  "The conversation ended or timed" 
            );
            cycle.redirectTo(Session.get().getPageFactory().newPage(noConversationPage));
            throw new AbortException();
         }
         else
         {
            throw new NoConversationException( "no long-running conversation for @Conversational wicket component: " + invocationContext.getComponent().getClass().getName());         
         }
      }
   }


   @SuppressWarnings("deprecation")
   private void beginConversationIfNecessary(InvocationContext invocationContext, Object result)
   {
      
      boolean simpleBegin = 
            invocationContext.getAccessibleObject().isAnnotationPresent(StartTask.class) || 
            invocationContext.getAccessibleObject().isAnnotationPresent(BeginTask.class) ||
            invocationContext.getAccessibleObject().isAnnotationPresent(org.jboss.seam.wicket.annotations.Begin.class) ||
            ( invocationContext.getAccessibleObject().isAnnotationPresent(Begin.class) && invocationContext.getAccessibleObject().getAnnotation(Begin.class).ifOutcome().length==0 );
      if ( simpleBegin )
      {
         if ( result!=null || ( invocationContext.getMethod() != null && invocationContext.getMethod().getReturnType().equals(void.class)) || invocationContext.getConstructor() != null )
         {
            boolean nested = false;
            if ( invocationContext.getAccessibleObject().isAnnotationPresent(Begin.class) )
            {
               nested = invocationContext.getAccessibleObject().getAnnotation(Begin.class).nested();
            }
            else if ( invocationContext.getAccessibleObject().isAnnotationPresent(org.jboss.seam.wicket.annotations.Begin.class) )
            {
               nested = invocationContext.getAccessibleObject().getAnnotation(org.jboss.seam.wicket.annotations.Begin.class).nested();
            }
            beginConversation( nested, getProcessDefinitionName(invocationContext) );
            setFlushMode(invocationContext); //TODO: what if conversation already exists? Or a nested conversation?
         }
      }
      else if ( invocationContext.getAccessibleObject().isAnnotationPresent(Begin.class) )
      {
         String[] outcomes = invocationContext.getAccessibleObject().getAnnotation(Begin.class).ifOutcome();
         if ( outcomes.length==0 || Arrays.asList(outcomes).contains(result) )
         {
            beginConversation( 
                  invocationContext.getAccessibleObject().getAnnotation(Begin.class).nested(), 
                  getProcessDefinitionName(invocationContext) 
               );
            setFlushMode(invocationContext); //TODO: what if conversation already exists? Or a nested conversation?
         }
      }
      
   }
   
   private void setFlushMode(InvocationContext<T> invocationContext)
   {
      FlushModeType flushMode;
      if (invocationContext.getAccessibleObject().isAnnotationPresent(Begin.class))
      {
         flushMode = invocationContext.getAccessibleObject().getAnnotation(Begin.class).flushMode();
      }
      else if (invocationContext.getAccessibleObject().isAnnotationPresent(org.jboss.seam.wicket.annotations.Begin.class))
      {
         flushMode = invocationContext.getAccessibleObject().getAnnotation(org.jboss.seam.wicket.annotations.Begin.class).flushMode();
      }
      else if (invocationContext.getAccessibleObject().isAnnotationPresent(BeginTask.class))
      {
         flushMode = invocationContext.getAccessibleObject().getAnnotation(BeginTask.class).flushMode();
      }
      else if (invocationContext.getAccessibleObject().isAnnotationPresent(StartTask.class))
      {
         flushMode = invocationContext.getAccessibleObject().getAnnotation(StartTask.class).flushMode();
      }
      else
      {
         return;
      }
      
      PersistenceContexts.instance().changeFlushMode(flushMode);
   }

   private String getProcessDefinitionName(InvocationContext invocationContext) {
      if ( invocationContext.getAccessibleObject().isAnnotationPresent(Begin.class) )
      {
         return invocationContext.getAccessibleObject().getAnnotation(Begin.class).pageflow();
      }
      if ( invocationContext.getAccessibleObject().isAnnotationPresent(BeginTask.class) )
      {
         return invocationContext.getAccessibleObject().getAnnotation(BeginTask.class).pageflow();
      }
      if ( invocationContext.getAccessibleObject().isAnnotationPresent(StartTask.class) )
      {
         return invocationContext.getAccessibleObject().getAnnotation(StartTask.class).pageflow();
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
         Pageflow.instance().begin(pageflowName);
      }
   }

   @SuppressWarnings("deprecation")
   private void endConversationIfNecessary(InvocationContext<T> invocationContext, Object result)
   {
      boolean isEndAnnotation = invocationContext.getAccessibleObject().isAnnotationPresent(End.class);
      boolean isEndTaskAnnotation = invocationContext.getAccessibleObject().isAnnotationPresent(EndTask.class);
      
      boolean beforeRedirect = ( isEndAnnotation && invocationContext.getAccessibleObject().getAnnotation(End.class).beforeRedirect() ) ||
            ( isEndTaskAnnotation && invocationContext.getAccessibleObject().getAnnotation(EndTask.class).beforeRedirect() );
      
      boolean simpleEnd = 
            ( isEndAnnotation && invocationContext.getAccessibleObject().getAnnotation(End.class).ifOutcome().length==0 ) || 
            ( isEndTaskAnnotation && invocationContext.getAccessibleObject().getAnnotation(EndTask.class).ifOutcome().length==0 );
      if ( simpleEnd )
      {
         if ( result!=null || invocationContext.getConstructor() != null || (invocationContext.getMethod() != null && invocationContext.getMethod().getReturnType().equals(void.class)) ) //null outcome interpreted as redisplay
         {
            endConversation(beforeRedirect);
         }
      }
      else if ( isEndAnnotation )
      {
         String[] outcomes = invocationContext.getAccessibleObject().getAnnotation(End.class).ifOutcome();
         if ( Arrays.asList(outcomes).contains(result) )
         {
            endConversation(beforeRedirect);
         }
      }
      else if ( isEndTaskAnnotation )
      {
         //TODO: fix minor code duplication
         String[] outcomes = invocationContext.getAccessibleObject().getAnnotation(EndTask.class).ifOutcome();
         if ( Arrays.asList(outcomes).contains(result) )
         {
            endConversation(beforeRedirect);
         }
      }
   }

   private void endConversation(boolean beforeRedirect)
   {
      Manager.instance().endConversation(beforeRedirect);
   }

}
