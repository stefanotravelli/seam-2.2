package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.persistence.PersistenceContexts;

/**
 * Allows the conversation timeout to be set per-conversation,
 * and the conversation description and switchable outcome to
 * be set when the application requires workspace management
 * functionality.
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.CONVERSATION)
@Name("org.jboss.seam.core.conversation")
@Install(precedence=BUILT_IN)
@BypassInterceptors
public class Conversation implements Serializable 
{
   private static final long serialVersionUID = -6131304128727444876L;
   private Integer timeout;
   private Integer concurrentRequestTimeout;
   String description;
   String viewId;
   
   /**
    * Kills all conversations except the current one
    */
   public void killAllOthers()
   {
      Manager.instance().killAllOtherConversations();
   }

   /**
    * Get the timeout for this conversation instance.
    * @return the timeout in millis
    */
   public Integer getTimeout() 
   {
      return timeout==null ?
            Manager.instance().getCurrentConversationTimeout() :
            timeout;
   }
   
   /**
    * Set the timeout for this converstaion instance.
    * @param timeout the timeout in millis
    */
   public void setTimeout(Integer timeout) 
   {
      this.timeout = timeout;
   }
   
   public Integer getConcurrentRequestTimeout()
   {
      return concurrentRequestTimeout == null ? Manager.instance().getCurrentConversationConcurrentRequestTimeout() : concurrentRequestTimeout;
   }
   
   public void setConcurrentRequestTimeout(Integer concurrentRequestTimeout)
   {
      this.concurrentRequestTimeout = concurrentRequestTimeout;
   }
   
   /**
    * Get the conversation id.
    */
   public String getId()
   {
      return Manager.instance().getCurrentConversationId();
   }
   
   public String getDescription()
   {
      return description==null ? 
            Manager.instance().getCurrentConversationDescription() : 
            description;
   }
   
   public String getViewId()
   {
      return viewId==null ? 
            Manager.instance().getCurrentConversationViewId() :
            viewId;
   }
   
   /**
    * Sets the description of this conversation, for use
    * in the conversation list, breadcrumbs, or conversation
    * switcher.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * Sets the JSF outcome to be used when we switch back to this
    * conversation from the conversation list, breadcrumbs, or 
    * conversation switcher.
    */
   public void setViewId(String outcome)
   {
      this.viewId = outcome;
   }
   
   public static Conversation instance()
   {
      if ( !Contexts.isConversationContextActive() )
      {
         throw new IllegalStateException("No active conversation context");
      }
      return (Conversation) Component.getInstance(Conversation.class, ScopeType.CONVERSATION);
   }
   
   void flush()
   {
      //we need to flush this stuff asynchronously to handle 
      //nested and temporary conversations which have no
      //ConversationEntry

      Manager manager = Manager.instance();
      
      if ( !manager.isLongRunningConversation() )
      {
         throw new IllegalStateException("only long-running conversation outcomes are switchable");
      }
      
      ConversationEntry entry = manager.getCurrentConversationEntry();
      if (entry==null)
      {
         throw new IllegalStateException("missing conversation entry"); //should never happen
      }
      if (viewId!=null)
      {
         entry.setViewId(viewId);
      }
      if (description!=null)
      {
         entry.setDescription(description);
      }
      if (timeout!=null)
      {
         entry.setTimeout(timeout);
      }
      if (concurrentRequestTimeout != null)
      {
         entry.setConcurrentRequestTimeout(concurrentRequestTimeout);
      }
      
      description = null;
      viewId = null;
      timeout = null;
   }
   
   /**
    * Switch back to the last defined view-id for the
    * current conversation.
    * 
    * @return true if a redirect occurred
    */
   public boolean redirect()
   {
      Manager manager = Manager.instance();
      return redirect( manager, manager.getCurrentConversationViewId() );
   }

   private boolean redirect(Manager manager, String viewId)
   {
      if (viewId==null)
      {
         return false;
      }
      else
      {
         manager.redirect(viewId);
         return true;
      }         
   }
   
   /**
    * End a child conversation and redirect to the last defined
    * view-id for the parent conversation.
    * 
    * @return true if a redirect occurred
    */
   public boolean endAndRedirect()
   {
      return endAndRedirect(false);
   }
   
   /**
    * End a child conversation and redirect to the last defined
    * view-id for the parent conversation.
    * 
    * @param endBeforeRedirect should the conversation be destroyed before the redirect?
    * @return true if a redirect occurred
    */
   public boolean endAndRedirect(boolean endBeforeRedirect)
   {
      end(endBeforeRedirect);
      Manager manager = Manager.instance();
      return redirect( manager, manager.getParentConversationViewId() );
   }
   
   /**
    * Leave the scope of the current conversation
    */
   public void leave()
   {
      Manager.instance().leaveConversation();
   }
   
   /**
    * Start a long-running conversation, if no long-running
    * conversation is active.
    * 
    * @return true if a new long-running conversation was begun
    */
   public boolean begin()
   {
      if ( Manager.instance().isLongRunningOrNestedConversation() )
      {
         return false;
      }
      else
      {
         reallyBegin();
         return true;
      }
   }

   private void reallyBegin()
   {
      Manager.instance().beginConversation( );
   }
   
   /**
    * Start a nested conversation.
    * 
    * @throws IllegalStateException if no long-running conversation was active
    */
   public void beginNested()
   {
      if ( Manager.instance().isLongRunningConversation() )
      {
         Manager.instance().beginNestedConversation( );
      }
      else
      {
         throw new IllegalStateException("beginNested() called with no long-running conversation");
      }
   }
   
   /**
    * Begin or join a conversation, or begin a new nested conversation.
    * 
    * @param join if a conversation is active, should we join it?
    * @param nested if a conversation is active, should we start a new nested conversation?
    * @return true if a new long-running conversation was begun
    */
   public boolean begin(boolean join, boolean nested)
   {
      boolean longRunningConversation = Manager.instance().isLongRunningOrNestedConversation();
      if ( !join && !nested && longRunningConversation  )
      {
         throw new IllegalStateException("begin() called from long-running conversation, try join=true");
      }
      else if ( !longRunningConversation )
      {
         reallyBegin();
         return true;
      }
      else if (nested)
      {
         beginNested();
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * End a long-runnning conversation.
    */
   public void end()
   {
      end(false);   
   }
   
   /**
    * End a long-runnning conversation and destroy
    * it before a redirect.
    */
   public void endBeforeRedirect()
   {
      end(true);   
   }   
   
   /**
    * End a long-runnning conversation.
    * 
    * @param beforeRedirect should the conversation be destroyed before any redirect?
    */
   public void end(boolean beforeRedirect)
   {
      Manager.instance().endConversation(beforeRedirect);   
   }
   
   /**
    * Is this conversation long-running? Note that this method returns
    * false even when the conversation has been temporarily promoted
    * to long-running for the course of a redirect, so it does what
    * the user really expects.
    */
   public boolean isLongRunning()
   {
      return Manager.instance().isReallyLongRunningConversation();
   }
   
   /**
    * Is this conversation a nested conversation?
    */
   public boolean isNested()
   {
      return Manager.instance().isNestedConversation();
   }
   
   /**
    * Get the id of the immediate parent of a nested conversation
    */
   public String getParentId()
   {
      return Manager.instance().getParentConversationId();
   }
   
   /**
    * Get the id of root conversation of a nested conversation
    */
   public String getRootId()
   {
      return Manager.instance().getRootConversationId();
   }
   
   /**
    * "Pop" the conversation stack, switching to the parent conversation
    */
   public void pop()
   {
      String parentId = getParentId();
      if (parentId!=null)
      {
         Manager.instance().switchConversation(parentId);
      }
   }
   
   /**
    * Pop the conversation stack and redirect to the last defined
    * view-id for the parent conversation.
    * 
    * @return true if a redirect occurred
    */
   public boolean redirectToParent()
   {
      pop();
      return redirect();
   }
   
   /**
    * Switch to the root conversation
    */
   public void root()
   {
      String rootId = getRootId();
      if (rootId!=null)
      {
         Manager.instance().switchConversation(rootId);
      }
   }

   /**
    * Switch to the root conversation and redirect to the 
    * last defined view-id for the root conversation.
    * 
    * @return true if a redirect occurred
    */
   public boolean redirectToRoot()
   {
      root();
      return redirect();
   }
   
   /**
    * Change the flush mode of all Seam-managed persistence 
    * contexts in this conversation.
    */
   public void changeFlushMode(FlushModeType flushMode)
   {
      PersistenceContexts.instance().changeFlushMode(flushMode);
   }

}
