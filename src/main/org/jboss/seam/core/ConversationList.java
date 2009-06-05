package org.jboss.seam.core;

import static org.jboss.seam.ScopeType.PAGE;
import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.web.Session;

/**
 * Factory for the conversation list
 * 
 * @author Gavin King
 */
@Scope(STATELESS)
@Name("org.jboss.seam.core.conversationListFactory")
@Install(precedence=BUILT_IN)
@BypassInterceptors
public class ConversationList
{
   
   protected List<ConversationEntry> createConversationEntryList()
   {
      ConversationEntries conversationEntries = ConversationEntries.getInstance();
      if (conversationEntries==null)
      {
         return Collections.EMPTY_LIST;
      }
      else
      {
         Set<ConversationEntry> orderedEntries = new TreeSet<ConversationEntry>();
         orderedEntries.addAll( conversationEntries.getConversationEntries() );
         List<ConversationEntry> conversationEntryList = new ArrayList<ConversationEntry>( conversationEntries.size() );
         for ( ConversationEntry entry: orderedEntries )
         {
            if ( entry.isDisplayable() && !Session.instance().isInvalid() )
            {
               conversationEntryList.add(entry);
            }
         }
         return conversationEntryList;
      }
   }
   
   @Factory(value="org.jboss.seam.core.conversationList", autoCreate=true, scope=PAGE)
   public List<ConversationEntry> getConversationEntryList()
   {
      return createConversationEntryList();
   }
}
