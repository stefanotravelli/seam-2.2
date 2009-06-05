package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

/**
 * Manages a map of conversation id to ConversationEntry 
 * in the session context.
 * 
 * @author Gavin King
 */
@Name("org.jboss.seam.core.conversationEntries")
@Install(precedence=BUILT_IN)
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class ConversationEntries extends AbstractMutable implements Serializable 
{
   private static final long serialVersionUID = 7996835952419813634L;
   private Map<String, ConversationEntry> conversationIdEntryMap = new HashMap<String, ConversationEntry>();
   
   public synchronized Collection<ConversationEntry> getConversationEntries()
   {
      return Collections.unmodifiableCollection( conversationIdEntryMap.values() );
   }
   
   public synchronized int size()
   {
      return conversationIdEntryMap.size();
   }
   
   public synchronized Set<String> getConversationIds()
   {
      return Collections.unmodifiableSet( conversationIdEntryMap.keySet() );
   }
   
   public synchronized ConversationEntry createConversationEntry(String id, List<String> stack)
   {
      ConversationEntry entry = new ConversationEntry(id, stack, this);
      conversationIdEntryMap.put(id, entry);
      setDirty();
      return entry;
   }
   
   public synchronized ConversationEntry getConversationEntry(String id)
   {
      return conversationIdEntryMap.get(id);
   }
   
   public synchronized ConversationEntry removeConversationEntry(String id)
   {
      ConversationEntry entry = conversationIdEntryMap.remove(id);
      if ( entry!=null ) setDirty();
      return entry;
   }
   
   public synchronized ConversationEntry updateConversationId(String oldId, String newId)
   {
      ConversationEntry entry = conversationIdEntryMap.remove(oldId);
      if (entry==null)
      {
         return null;
      }
      else
      {
         entry.setId(newId);
         entry.getConversationIdStack().set(0, newId);
         conversationIdEntryMap.put(newId, entry);
         setDirty();
         return entry;
      }
   }
   
   public static ConversationEntries instance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No session context active");
      }
      return (ConversationEntries) Component.getInstance(ConversationEntries.class, ScopeType.SESSION);
   }
   
   public static ConversationEntries getInstance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No session context active");
      }
      return (ConversationEntries) Component.getInstance(ConversationEntries.class, ScopeType.SESSION, false);
   }
   
   @Override
   public String toString()
   {
      return "ConversationEntries(" + conversationIdEntryMap.values() + ")";
   }
}
