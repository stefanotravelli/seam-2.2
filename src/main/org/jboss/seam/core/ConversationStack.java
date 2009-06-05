package org.jboss.seam.core;

import static org.jboss.seam.ScopeType.PAGE;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.web.Session;

/**
 * Factory for the "breadcrumbs", a stack with all
 * parent conversations of the current conversation.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.STATELESS)
@Name("org.jboss.seam.core.conversationStackFactory")
@Install(precedence=BUILT_IN)
@BypassInterceptors
public class ConversationStack
{
   
   protected List<ConversationEntry> createConversationEntryStack()
   {
      ConversationEntries conversationEntries = ConversationEntries.getInstance();
      if (conversationEntries==null)
      {
         return Collections.EMPTY_LIST;
      }
      else
      {
         ConversationEntry currentConversationEntry = Manager.instance().getCurrentConversationEntry();
         if (currentConversationEntry==null)
         {
            return Collections.EMPTY_LIST;
         }
         else
         {
            List<String> idStack = currentConversationEntry.getConversationIdStack();
            List<ConversationEntry> conversationEntryStack = new ArrayList<ConversationEntry>( conversationEntries.size() );
            ListIterator<String> ids = idStack.listIterator( idStack.size() );
            while ( ids.hasPrevious() )
            {
               ConversationEntry entry = conversationEntries.getConversationEntry( ids.previous() );
               if ( entry.isDisplayable() && !Session.instance().isInvalid() ) 
               {
                  conversationEntryStack.add(entry);
               }
            }
            return conversationEntryStack;
         }
      }
   }
   
   @Factory(value="org.jboss.seam.core.conversationStack", autoCreate=true, scope=PAGE)
   public List<ConversationEntry> getConversationEntryStack()
   {
      return createConversationEntryStack();
   }
   
}
