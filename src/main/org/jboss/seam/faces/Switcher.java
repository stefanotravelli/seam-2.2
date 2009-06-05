package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.ConversationEntries;
import org.jboss.seam.core.ConversationEntry;
import org.jboss.seam.core.Manager;
import org.jboss.seam.web.Session;

/**
 * Support for the conversation switcher drop-down menu.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.PAGE)
@Name("org.jboss.seam.faces.switcher")
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@BypassInterceptors
public class Switcher implements Serializable 
{
   
   private static final long serialVersionUID = -6403911073853051938L;
   private List<SelectItem> selectItems;
   private String conversationIdOrOutcome;
   private String resultingConversationIdOrOutcome;
      
   @Create
   public void createSelectItems()
   {
      ConversationEntries conversationEntries = ConversationEntries.getInstance();
      if (conversationEntries==null)
      {
         selectItems = Collections.EMPTY_LIST;
      }
      else
      {
         Set<ConversationEntry> orderedEntries = new TreeSet<ConversationEntry>();
         orderedEntries.addAll( conversationEntries.getConversationEntries() );
         selectItems = new ArrayList<SelectItem>( conversationEntries.size() );
         for ( ConversationEntry entry: orderedEntries )
         {
            if ( entry.isDisplayable() && !Session.instance().isInvalid() )
            {
               selectItems.add( new SelectItem( entry.getId(), entry.getDescription() ) );
            }
         }
      }
   }
   
   public List<SelectItem> getSelectItems()
   {
      return selectItems;
   }
      
   private String getLongRunningConversationId()
   {
      Manager manager = Manager.instance();
      if ( manager.isLongRunningConversation() )
      {
         return manager.getCurrentConversationId();
      }
      else if ( manager.isNestedConversation() )
      {
         return manager.getParentConversationId();
      }
      else
      {
         //TODO: is there any way to set it to the current outcome, instead of null?
         return null;
      }
   }

   public String getConversationIdOrOutcome() 
   {
      return resultingConversationIdOrOutcome==null ? 
            getLongRunningConversationId() :
            resultingConversationIdOrOutcome;
   }

   public void setConversationIdOrOutcome(String selectedId) 
   {
      this.conversationIdOrOutcome = selectedId;
   }
   
   public String select()
   {

      boolean isOutcome = conversationIdOrOutcome==null || 
                    (!Character.isDigit(conversationIdOrOutcome.charAt(0)) && conversationIdOrOutcome.indexOf(':') < 0);       
      
      String actualOutcome;
      if (isOutcome)
      {
         resultingConversationIdOrOutcome = conversationIdOrOutcome;
         actualOutcome = conversationIdOrOutcome;
      }
      else
      {
         ConversationEntry ce = ConversationEntries.instance().getConversationEntry(conversationIdOrOutcome);
         if (ce!=null)
         {
            resultingConversationIdOrOutcome = ce.getId();
            ce.redirect();
         }
         actualOutcome = null;
      }
      return actualOutcome;
   }
  
}
