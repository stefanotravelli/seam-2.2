/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ConcurrentRequestTimeoutException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.ConversationIdParameter;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.seam.util.Id;
import org.jboss.seam.web.Session;

/**
 * The Seam conversation manager.
 *
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
@Scope(ScopeType.EVENT)
@Name("org.jboss.seam.core.manager")
@Install(precedence=BUILT_IN)
@BypassInterceptors
public class Manager
{
   public static final String EVENT_CONVERSATION_TIMEOUT = "org.jboss.seam.conversationTimeout";
   public static final String EVENT_CONVERSATION_DESTROYED = "org.jboss.seam.conversationDestroyed";
   public static final String EVENT_CONVERSATION_BEGIN = "org.jboss.seam.beginConversation";
   public static final String EVENT_CONVERSATION_END = "org.jboss.seam.endConversation";
   
   private static final LogProvider log = Logging.getLogProvider(Manager.class);
   
   public static final String REDIRECT_FROM_MANAGER = "org.jboss.seam.core.Manager";

   private static final String DEFAULT_ENCODING = "UTF-8";

   //The id of the current conversation
   private String currentConversationId;
   private List<String> currentConversationIdStack;

   //Is the current conversation "long-running"?
   private boolean isLongRunningConversation;
   
   //private boolean updateModelValuesCalled;

   private boolean destroyBeforeRedirect;
   
   private int conversationTimeout = 600000; //10 mins
   private int concurrentRequestTimeout = 1000; //one second
   
   private String conversationIdParameter = "conversationId";
   private String parentConversationIdParameter = "parentConversationId";

   private String URIEncoding = DEFAULT_ENCODING;
   
   private FlushModeType defaultFlushMode;

   /**
    * Kills all conversations except the current one 
    */
   public void killAllOtherConversations()
   {
      ConversationEntries conversationEntries = ConversationEntries.instance();
      Events events = Events.exists() ? Events.instance() : null;

      if (conversationEntries != null)
      {
         List<ConversationEntry> entries = new ArrayList<ConversationEntry>(
               conversationEntries.getConversationEntries());

         for (ConversationEntry conversationEntry : entries)
         {
            // kill all entries expect the current one
            // current conversation entry will be null if , kill-all is called
            // inside a new @Begin
            if (getCurrentConversationEntry() == null
                  || !getCurrentConversationIdStack().contains(
                        conversationEntry.getId()))
            {
               log.debug("Kill all other conversations, executed: kill conversation id = "
                           + conversationEntry.getId());

               boolean locked = conversationEntry.lockNoWait(); // we had better
               // not wait for it, or we would be waiting for ALL other requests
               try
               {
                  if (locked)
                  {
                     if (log.isDebugEnabled())
                     {
                        log.debug("conversation killed manually: " + conversationEntry.getId());
                     }
                  } 
                  else
                  {
                     // if we could not acquire the lock, someone has left a
                     // garbage lock lying around
                     // the reason garbage locks can exist is that we don't
                     // require a servlet filter to
                     // exist - but if we do use SeamExceptionFilter, it will
                     // clean up garbage and this
                     // case should never occur

                     // NOTE: this is slightly broken - in theory there is a
                     // window where a new request
                     // could have come in and got the lock just before us but
                     // called touch() just
                     // after we check the timeout - but in practice this would
                     // be extremely rare,
                     // and that request will get an
                     // IllegalMonitorStateException when it tries to
                     // unlock() the CE
                     log.debug("kill conversation with garbage lock: "
                           + conversationEntry.getId());
                  }
                  if (events != null)
                  {
                     events.raiseEvent(EVENT_CONVERSATION_DESTROYED, conversationEntry);
                  }
                  destroyConversation(conversationEntry.getId(), getSessionMap());
               } 
               finally
               {
                  if (locked)
                  {
                     conversationEntry.unlock();
                  }
               }
            }
         }
      }      
   }
   
   /**
    * @return Map session
    */
   private Map<String, Object> getSessionMap()
   {
      // this method could be moved to a utility class
      Map<String, Object> session = new HashMap<String, Object>();
      String[] sessionAttributeNames = Contexts.getSessionContext().getNames();
   
      for (String attributeName : sessionAttributeNames)
      {
         session.put(attributeName, Contexts.getSessionContext().get(attributeName));
      }
      return session;
   }   
   
   // DONT BREAK, icefaces uses this
   public String getCurrentConversationId()
   {
      return currentConversationId;
   }

   /**
    * Only public for the unit tests!
    * @param id
    */
   public void setCurrentConversationId(String id)
   {
      currentConversationId = id;
      currentConversationEntry = null;
   }
   
   /**
    * Change the id of the current conversation.
    * 
    * @param id the new conversation id
    */
   public void updateCurrentConversationId(String id)
   {
      if (id != null && id.equals(currentConversationId))
      {
         // the conversation id hasn't changed, do nothing       
         return;
      }
      
      if ( ConversationEntries.instance().getConversationIds().contains(id) )
      {
         throw new IllegalStateException("Conversation id is already in use: " + id);
      }
      
      String[] names = Contexts.getConversationContext().getNames();
      Object[] values = new Object[names.length];
      for (int i=0; i<names.length; i++)
      {
         values[i] = Contexts.getConversationContext().get(names[i]);
         Contexts.getConversationContext().remove(names[i]);
      }
      Contexts.getConversationContext().flush();
      
      ConversationEntry ce = ConversationEntries.instance().updateConversationId(currentConversationId, id);
      String priorId = currentConversationId;
      setCurrentConversationId(id);
      
      if (ce!=null)
      {
         setCurrentConversationIdStack( ce.getConversationIdStack() );
         //TODO: what about child conversations?!
      } 
      else 
      {
          // when ce is null, the id stack will be left with a reference to
          // the old conversation id, so we need patch that up
          int pos = currentConversationIdStack.indexOf(priorId);
          if (pos != -1) 
          {
              currentConversationIdStack.set(pos, id);
          }          
      }
      
      for (int i=0; i<names.length; i++)
      {
         Contexts.getConversationContext().set(names[i], values[i]);
      }
   }

   private void touchConversationStack(List<String> stack)
   {
      if ( stack!=null )
      {
         //iterate in reverse order, so that current conversation 
         //sits at top of conversation lists
         ListIterator<String> iter = stack.listIterator( stack.size() );
         while ( iter.hasPrevious() )
         {
            String conversationId = iter.previous();
            ConversationEntry conversationEntry = ConversationEntries.instance().getConversationEntry(conversationId);
            if (conversationEntry!=null)
            {
               conversationEntry.touch();
            }
         }
      }
   }
   
   private void endNestedConversations(String id)
   {
      for ( ConversationEntry ce: ConversationEntries.instance().getConversationEntries() )
      {
         if ( ce.getConversationIdStack().contains(id) )
         {
            ce.end();
         }
      }
   }

   public List<String> getCurrentConversationIdStack()
   {
      return currentConversationIdStack;
   }

   public void setCurrentConversationIdStack(List<String> stack)
   {
      currentConversationIdStack = stack;
   }

   private List<String> createCurrentConversationIdStack(String id)
   {
      currentConversationIdStack = new ArrayList<String>();
      currentConversationIdStack.add(id);
      return currentConversationIdStack;
   }

   public String getCurrentConversationDescription()
   {
      ConversationEntry ce = getCurrentConversationEntry();
      if ( ce==null ) return null;
      return ce.getDescription();
   }

   public Integer getCurrentConversationTimeout()
   {
      ConversationEntry ce = getCurrentConversationEntry();
      if ( ce==null ) return null;
      return ce.getTimeout();
   }
   
   public Integer getCurrentConversationConcurrentRequestTimeout()
   {
      ConversationEntry ce = getCurrentConversationEntry();
      if (ce == null) return null;
      return ce.getConcurrentRequestTimeout();
   }

   public String getCurrentConversationViewId()
   {
      ConversationEntry ce = getCurrentConversationEntry();
      if ( ce==null ) return null;
      return ce.getViewId();
   }
   
   public String getParentConversationViewId()
   {
      ConversationEntry conversationEntry = ConversationEntries.instance().getConversationEntry(getParentConversationId());
      return conversationEntry==null ? null : conversationEntry.getViewId();
   }
   
   public String getParentConversationId()
   {
      return currentConversationIdStack==null || currentConversationIdStack.size()<2 ?
            null : currentConversationIdStack.get(1);
   }

   public String getRootConversationId()
   {
      return currentConversationIdStack==null || currentConversationIdStack.size()<1 ?
            null : currentConversationIdStack.get( currentConversationIdStack.size()-1 );
   }

   // DONT BREAK, icefaces uses this
   public boolean isLongRunningConversation()
   {
      return isLongRunningConversation;
   }

   public boolean isLongRunningOrNestedConversation()
   {
      return isLongRunningConversation() || isNestedConversation();
   }

   public boolean isReallyLongRunningConversation()
   {
      return isLongRunningConversation() && 
            !getCurrentConversationEntry().isRemoveAfterRedirect() &&
            !Session.instance().isInvalid();
   }
   
   public boolean isNestedConversation()
   {
      return currentConversationIdStack!=null && 
            currentConversationIdStack.size()>1;
   }

   public void setLongRunningConversation(boolean isLongRunningConversation)
   {
      this.isLongRunningConversation = isLongRunningConversation;
   }

   public static Manager instance()
   {
      if ( !Contexts.isEventContextActive() )
      {
         throw new IllegalStateException("No active event context");
      }
      Manager instance = (Manager) Component.getInstance(Manager.class, ScopeType.EVENT);
      if (instance==null)
      {
         throw new IllegalStateException("No Manager could be created, make sure the Component exists in application scope");
      }
      return instance;
   }

   /**
    * Clean up timed-out conversations
    */
   public void conversationTimeout(Map<String, Object> session)
   {
      long currentTime = System.currentTimeMillis();
      ConversationEntries conversationEntries = ConversationEntries.getInstance();
      if (conversationEntries!=null)
      {
         List<ConversationEntry> entries = new ArrayList<ConversationEntry>( conversationEntries.getConversationEntries() );
         for (ConversationEntry conversationEntry: entries)
         {
            boolean locked = conversationEntry.lockNoWait(); //we had better not wait for it, or we would be waiting for ALL other requests
            try
            {
               long delta = currentTime - conversationEntry.getLastRequestTime();
               if ( delta > conversationEntry.getTimeout() )
               {
                  if ( locked )
                  { 
                     if ( log.isDebugEnabled() )
                     {
                        log.debug("conversation timeout for conversation: " + conversationEntry.getId());
                     }
                  }
                  else
                  {
                     //if we could not acquire the lock, someone has left a garbage lock lying around
                     //the reason garbage locks can exist is that we don't require a servlet filter to
                     //exist - but if we do use SeamExceptionFilter, it will clean up garbage and this
                     //case should never occur
                     
                     //NOTE: this is slightly broken - in theory there is a window where a new request 
                     //      could have come in and got the lock just before us but called touch() just 
                     //      after we check the timeout - but in practice this would be extremely rare, 
                     //      and that request will get an IllegalMonitorStateException when it tries to 
                     //      unlock() the CE
                     log.debug("destroying conversation with garbage lock: " + conversationEntry.getId());
                  }
                  if ( Events.exists() ) 
                  {
                     Events.instance().raiseEvent(EVENT_CONVERSATION_TIMEOUT, conversationEntry.getId());
                  }
                  destroyConversation( conversationEntry.getId(), session );
               }
            }
            finally
            {
               if (locked) conversationEntry.unlock();
            }
         }
      }
   }

   /**
    * Clean up all state associated with a conversation
    */
   private void destroyConversation(String conversationId, Map<String, Object> session)
   {
      Lifecycle.destroyConversationContext(session, conversationId);
      ConversationEntries.instance().removeConversationEntry(conversationId);
   }

   /**
    * Touch the conversation stack, destroy ended conversations, 
    * and timeout inactive conversations.
    */
   public void endRequest(Map<String, Object> session)
   {
      if ( isLongRunningConversation() )
      {
         if ( log.isDebugEnabled() )
         {
            log.debug("Storing conversation state: " + getCurrentConversationId());
         }
         touchConversationStack( getCurrentConversationIdStack() );
      }
      else
      {
         if ( log.isDebugEnabled() )
         {
            log.debug("Discarding conversation state: " + getCurrentConversationId());
         }
         //now safe to remove the entry
         removeCurrentConversationAndDestroyNestedContexts(session);
      }

      /*if ( !Init.instance().isClientSideConversations() ) 
      {*/
         // difficult question: is it really safe to do this here?
         // right now we do have to do it after committing the Seam
         // transaction because we can't close EMs inside a txn
         // (this might be a bug in HEM)
         Manager.instance().conversationTimeout(session);
      //}
   }
   
   public void unlockConversation()
   {
      ConversationEntry ce = getCurrentConversationEntry();
      if (ce!=null) 
      {
         if ( ce.isLockedByCurrentThread() )
         {
            ce.unlock();
         }
      }
      else if ( isNestedConversation() )
      {
         ConversationEntries.instance().getConversationEntry( getParentConversationId() ).unlock();
      }
   }

   private void removeCurrentConversationAndDestroyNestedContexts(Map<String, Object> session) 
   {
      ConversationEntries conversationEntries = ConversationEntries.getInstance();
      if (conversationEntries!=null)
      {
         conversationEntries.removeConversationEntry( getCurrentConversationId() );
         destroyNestedConversationContexts( session, getCurrentConversationId() );
      }
   }

   private void destroyNestedConversationContexts(Map<String, Object> session, String conversationId) 
   {
      List<ConversationEntry> entries = new ArrayList<ConversationEntry>( ConversationEntries.instance().getConversationEntries() );
      for  ( ConversationEntry ce: entries )
      {
         if ( ce.getConversationIdStack().contains(conversationId) )
         {
            String entryConversationId = ce.getId();
            log.debug("destroying nested conversation: " + entryConversationId);
            destroyConversation(entryConversationId, session);
         }
      }
   }

   /**
    * Look for a conversation propagation style in the request
    * parameters and begin, nested or join the conversation,
    * as necessary.
    * 
    * @param parameters the request parameters
    */
   public void handleConversationPropagation(Map parameters)
   {      
      ConversationPropagation propagation = ConversationPropagation.instance();
      
      if (propagation.getPropagationType() == null)
      {
         return;
      }

      switch (propagation.getPropagationType())
      {
         case BEGIN:
            if ( isLongRunningConversation )
            {
               throw new IllegalStateException("long-running conversation already active");
            }
            beginConversation();
            
            if (propagation.getPageflow() != null)
            {
               Pageflow.instance().begin( propagation.getPageflow() );
            }
            break;
         case JOIN:
            if ( !isLongRunningConversation )
            {
               beginConversation();
               
               if (propagation.getPageflow() != null)
               {
                  Pageflow.instance().begin( propagation.getPageflow() );
               }
            }
            break;
         case NESTED:
            if ( isLongRunningOrNestedConversation() ) 
            {
                beginNestedConversation();
            }
            else 
            {
                beginConversation();
            }
            
            if (propagation.getPageflow() != null)
            {
               Pageflow.instance().begin( propagation.getPageflow() );
            }
            break;
         case END:
            endConversation(false);
            break;
         case ENDROOT:
            endRootConversation(false);
            break;
      }
   }
   
   /**
    * Initialize the request conversation context, given the 
    * conversation id and optionally a parent conversation id.
    * If no conversation entry is found for the first id, try
    * the parent, and if that also fails, initialize a new 
    * temporary conversation context.
    * 
    * @return false if the conversation entry was not found
    *         and it was required
    */
   public boolean restoreConversation() 
   {
      ConversationPropagation cp = ConversationPropagation.instance();
      String conversationId = cp.getConversationId();
      String parentConversationId = cp.getParentConversationId();
      ConversationEntry ce = null;
      if (conversationId!=null)
      {
         ConversationEntries entries = ConversationEntries.instance();
         ce = entries.getConversationEntry(conversationId);
         if (ce==null)
         {
            ce = entries.getConversationEntry(parentConversationId);
         }
      }
      
      return restoreAndLockConversation(ce) || !cp.isValidateLongRunningConversation();
   }

   private boolean restoreAndLockConversation(ConversationEntry ce)
   {
      if (ce == null)
      {
         //there was no id in either place, so there is no
         //long-running conversation to restore
         log.debug("No stored conversation");
         initializeTemporaryConversation();
         return false;
      }
      else if ( ce.lock() )
      {
         // do this ASAP, since there is a window where conversationTimeout() might  
         // try to destroy the conversation, even if he cannot obtain the lock!
         touchConversationStack( ce.getConversationIdStack() );

         //we found an id and obtained the lock, so restore the long-running conversation
         log.debug("Restoring conversation with id: " + ce.getId());
         setLongRunningConversation(true);
         setCurrentConversationId( ce.getId() );
         setCurrentConversationIdStack( ce.getConversationIdStack() );

         boolean removeAfterRedirect = ce.isRemoveAfterRedirect() && !Pages.isDebugPage(); //TODO: hard dependency to JSF!!
         if (removeAfterRedirect)
         {
            setLongRunningConversation(false);
            ce.setRemoveAfterRedirect(false);
         }
         
         return true;

      } 
      else
      {
         log.debug("Concurrent call to conversation");
         throw new ConcurrentRequestTimeoutException("Concurrent call to conversation");
      }
   }

   /**
    * Initialize a new temporary conversation context,
    * and assign it a conversation id.
    */
   public void initializeTemporaryConversation()
   {
      String id = generateInitialConversationId();
      setCurrentConversationId(id);
      createCurrentConversationIdStack(id);
      setLongRunningConversation(false);
   }

   protected String generateInitialConversationId()
   {
      return Id.nextId();
   }

   private ConversationEntry createConversationEntry()
   {
      ConversationEntry entry = ConversationEntries.instance()
            .createConversationEntry( getCurrentConversationId(), getCurrentConversationIdStack() );
      if ( !entry.isNested() ) 
      {
         //if it is a newly created nested 
         //conversation, we already own the
         //lock
         entry.lock();
      }
      return entry;
   }

   /**
    * Promote a temporary conversation and make it long-running
    */
   public void beginConversation()
   {
      if ( !isLongRunningConversation() )
      {
         log.debug("Beginning long-running conversation");
         setLongRunningConversation(true);
         createConversationEntry();
         Conversation.instance(); //force instantiation of the Conversation in the outer (non-nested) conversation
         storeConversationToViewRootIfNecessary();
         if ( Events.exists() ) Events.instance().raiseEvent(EVENT_CONVERSATION_BEGIN);
      }
   }

   /**
    * Begin a new nested conversation.
    */
   public void beginNestedConversation()   
   {
      log.debug("Beginning nested conversation");
      List<String> oldStack = getCurrentConversationIdStack();
      if (oldStack==null)
      {
         throw new IllegalStateException("No long-running conversation active");
      }

      String id = Id.nextId();
      setCurrentConversationId(id);      
      createCurrentConversationIdStack(id).addAll(oldStack);
      createConversationEntry();
      storeConversationToViewRootIfNecessary();
      if ( Events.exists() ) Events.instance().raiseEvent(EVENT_CONVERSATION_BEGIN);
   }
   
   /**
    * Make a long-running conversation temporary.
    */
   public void endConversation(boolean beforeRedirect)
   {
      if ( isLongRunningConversation() )
      {
         log.debug("Ending long-running conversation");
         if ( Events.exists() ) Events.instance().raiseEvent(EVENT_CONVERSATION_END);
         setLongRunningConversation(false);
         destroyBeforeRedirect = beforeRedirect;
         endNestedConversations( getCurrentConversationId() );
         storeConversationToViewRootIfNecessary();
      }
   }
   
   /**
    * Make the root conversation in the current conversation stack temporary.
    */
   public void endRootConversation(boolean beforeRedirect)
   {
      if(isNestedConversation())
      {
         switchConversation(getRootConversationId());
      }
      
      endConversation(beforeRedirect);
   }
   
   protected void storeConversationToViewRootIfNecessary() {}

   // two reasons for this: 
   // (1) a cache
   // (2) so we can unlock() it after destruction of the session context 
   private ConversationEntry currentConversationEntry; 
   
   public ConversationEntry getCurrentConversationEntry() 
   {
      if (currentConversationEntry==null)
      {
         currentConversationEntry = ConversationEntries.instance().getConversationEntry( getCurrentConversationId() );
      }
      return currentConversationEntry;
   }
   
   /**
    * Leave the scope of the current conversation, leaving
    * it completely intact.
    */
   public void leaveConversation()
   {
      unlockConversation();
      initializeTemporaryConversation();
   }

   /**
    * Switch to another long-running conversation and mark the conversation as long-running,
    * overriding a previous call in the same thread to demote a long-running conversation.
    * 
    * @param id the id of the conversation to switch to
    * @return true if the conversation exists
    */
   public boolean switchConversation(String id)
   {
      return switchConversation(id, true);
   }

   /**
    * Switch to another long-running conversation.
    * 
    * @param id the id of the conversation to switch to
    * @param promote promote the current conversation to long-running, overriding any previous demotion
    * @return true if the conversation exists
    */
   public boolean switchConversation(String id, boolean promote)
   {
      ConversationEntry ce = ConversationEntries.instance().getConversationEntry(id);
      if (ce!=null)
      {
         if ( ce.lock() )
         {
            unlockConversation();
            setCurrentConversationId(id);
            setCurrentConversationIdStack( ce.getConversationIdStack() );
            if (promote)
            {
               setLongRunningConversation(true);
            }
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }
   }

   public int getConversationTimeout() {
      return conversationTimeout;
   }

   public void setConversationTimeout(int conversationTimeout) {
      this.conversationTimeout = conversationTimeout;
   }
   
   /**
    * Temporarily promote a temporary conversation to
    * a long running conversation for the duration of
    * a browser redirect. After the redirect, the 
    * conversation will be demoted back to a temporary
    * conversation.
    */
   public void beforeRedirect()
   {
      //DONT BREAK, icefaces uses this
      if (!destroyBeforeRedirect)
      {
         ConversationEntry ce = getCurrentConversationEntry();
         if (ce==null)
         {
            ce = createConversationEntry();
         }
         //ups, we don't really want to destroy it on this request after all!
         ce.setRemoveAfterRedirect( !isLongRunningConversation() );
         setLongRunningConversation(true);
      }
   }

   protected static boolean isDifferentConversationId(ConversationIdParameter sp, ConversationIdParameter tp)
   {
      return sp.getName()!=tp.getName() && ( sp.getName()==null || !sp.getName().equals( tp.getName() ) );
   }
   
   /**
    * Add the conversation id to a URL, if necessary
    * 
    * @deprecated use encodeConversationId(String url, String viewId)
    */
   public String encodeConversationId(String url)
   {
      //DONT BREAK, icefaces uses this
      return encodeConversationIdParameter( url, getConversationIdParameter(), getCurrentConversationId() );
   }
         
   /**
    * Add the conversation id to a URL, if necessary
    */
   public String encodeConversationId(String url, String viewId) 
   {
      //DONT BREAK, icefaces uses this
      ConversationIdParameter cip = Pages.instance().getPage(viewId).getConversationIdParameter();
      return encodeConversationIdParameter( url, cip.getParameterName(), cip.getParameterValue() );
   }
 
   /**
    * Add the conversation id to a URL, if necessary
    */
   public String encodeConversationId(String url, String viewId, String conversationId) 
   {
      //DONT BREAK, icefaces uses this
      ConversationIdParameter cip = Pages.instance().getPage(viewId).getConversationIdParameter();
      return encodeConversationIdParameter( url, cip.getParameterName(), cip.getParameterValue(conversationId) );
   }
 
   protected String encodeConversationIdParameter(String url, String paramName, String paramValue)
   {         
      if ( Session.instance().isInvalid() || containsParameter(url, paramName) )
      {
         return url;
      }
      else if (destroyBeforeRedirect)
      {
         if ( isNestedConversation() )
         {
            return new StringBuilder( url.length() + paramName.length() + 5 )
                  .append(url)
                  .append( url.contains("?") ? '&' : '?' )
                  .append(paramName)
                  .append('=')
                  .append( encode( getParentConversationId() ) )
                  .toString();
         }
         else
         {
            return url;
         }
      }
      else
      {
         StringBuilder builder = new StringBuilder( url.length() + paramName.length() + 5 )
               .append(url)
               .append( url.contains("?") ? '&' : '?' )
               .append(paramName)
               .append('=')
               .append( encode(paramValue) );
         if ( isNestedConversation() && !isReallyLongRunningConversation() )
         {
            builder.append('&')
                  .append(parentConversationIdParameter)
                  .append('=')
                  .append( encode( getParentConversationId() ) );
         }
         return builder.toString();
      }
   }

   /**
    * Add the parameters to a URL
    */
   public String encodeParameters(String url, Map<String, Object> parameters)
   {
      if ( parameters.isEmpty() ) return url;
      
      StringBuilder builder = new StringBuilder(url);
      for ( Map.Entry<String, Object> param: parameters.entrySet() )
      {
         String parameterName = param.getKey();
         if ( !containsParameter(url, parameterName) )
         {
            Object parameterValue = param.getValue();
            if (parameterValue instanceof Iterable)
            {
               for ( Object value: (Iterable) parameterValue )
               {
                  builder.append('&')
                        .append(parameterName)
                        .append('=');
                  if (value!=null)
                  {
                     builder.append(encode(value));
                  }
               }
            }
            else
            {
               builder.append('&')
                     .append(parameterName)
                     .append('=');
               if (parameterValue!=null)
               {
                  builder.append(encode(parameterValue));
               }
            }
         }
      }
      if ( url.indexOf('?')<0 ) 
      {
         builder.setCharAt( url.length() ,'?' );
      }
      return builder.toString();
   }

   private boolean containsParameter(String url, String parameterName)
   {
      return url.indexOf('?' + parameterName + '=')>0 || 
            url.indexOf( '&' + parameterName + '=')>0;
   }

   private String encode(Object value)
   {
      try
      {
         return URLEncoder.encode(String.valueOf(value),getUriEncoding());
      }
      catch (UnsupportedEncodingException iee)
      {
         throw new RuntimeException(iee);
      }
   }
   
   public String getConversationIdParameter()
   {
      return conversationIdParameter;
   }

   public void setConversationIdParameter(String conversationIdParameter)
   {
      this.conversationIdParameter = conversationIdParameter;
   }

   public String getParentConversationIdParameter()
   {
      return parentConversationIdParameter;
   }

   public void setParentConversationIdParameter(String nestedConversationIdParameter)
   {
      this.parentConversationIdParameter = nestedConversationIdParameter;
   }

   public int getConcurrentRequestTimeout()
   {
      return concurrentRequestTimeout;
   }

   public void setConcurrentRequestTimeout(int requestWait)
   {
      this.concurrentRequestTimeout = requestWait;
   }
   
   public FlushModeType getDefaultFlushMode()
   {
      return defaultFlushMode;
   }
   
   public void setDefaultFlushMode(FlushModeType defaultFlushMode)
   {
      this.defaultFlushMode = defaultFlushMode;
   }

   @Override
   public String toString()
   {
      return "Manager(" + currentConversationIdStack + ")";
   }

   public void redirect(String viewId, String id)
   {
      //declare it here since ConversationEntry calls it!
      throw new UnsupportedOperationException();
   }

   public void redirect(String viewId)
   {
      //declare it here since Conversation calls it!
      throw new UnsupportedOperationException();
   }

   protected void flushConversationMetadata()
   {
      if ( isLongRunningConversation() )
      {
         //important: only do this stuff when a long-running
         //           conversation exists, otherwise we would
         //           force creation of a conversation entry
         Conversation.instance().flush();
      }
   }

   public String getUriEncoding() {
       return URIEncoding;
   }

   public void setUriEncoding(String encoding) {
       URIEncoding = encoding;
   }

}
