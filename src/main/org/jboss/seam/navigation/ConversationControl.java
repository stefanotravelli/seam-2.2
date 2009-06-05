package org.jboss.seam.navigation;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.ConversationEntries;
import org.jboss.seam.core.ConversationEntry;
import org.jboss.seam.core.Manager;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.pageflow.Pageflow;
/**
 * 
 * Demarcation of the conversation in pages.xml
 *
 * @author Gavin King
 */
public class ConversationControl
{
   private boolean isBeginConversation;
   private boolean isEndConversation;
   private boolean isEndConversationBeforeRedirect;
   private boolean isEndRootConversation;
   private boolean join;
   private boolean nested;
   private FlushModeType flushMode;
   private String pageflow;
   private ValueExpression<Boolean> beginConversationCondition;
   private ValueExpression<Boolean> endConversationCondition;
   private String conversationName;
   public boolean isBeginConversation()
   {
      return isBeginConversation;
   }
   public void setBeginConversation(boolean isBeginConversation)
   {
      this.isBeginConversation = isBeginConversation;
   }
   public boolean isEndConversation()
   {
      return isEndConversation;
   }
   public void setEndConversation(boolean isEndConversation)
   {
      this.isEndConversation = isEndConversation;
   }
   
   public void beginOrEndConversation()
   {
      if ( endConversation() )
      {
         if(isEndRootConversation)
         {
            Manager.instance().endRootConversation(isEndConversationBeforeRedirect);
         }
         else
         {
            Manager.instance().endConversation(isEndConversationBeforeRedirect);
         }
      }
      if ( beginConversation() )
      {
         if (conversationName != null)
         {
            ConversationIdParameter param = Pages.instance().getConversationIdParameter(conversationName);
            
            ConversationEntry ce = ConversationEntries.instance().getConversationEntry(param.getConversationId());
            if (ce != null)
            {
               ce.redirect();
               return;
            }            
         }
         
         boolean begun = Conversation.instance().begin(join, nested);
         if (begun)
         {
            if ( flushMode!=null )
            {
               Conversation.instance().changeFlushMode(flushMode);
            }
            if ( pageflow!=null )
            {
               Pageflow.instance().begin(pageflow);
            }
         }
      }
   }
   private boolean beginConversation()
   {
      return isBeginConversation && 
         (beginConversationCondition==null || Boolean.TRUE.equals( beginConversationCondition.getValue() ) );
   }
   private boolean endConversation()
   {
      return isEndConversation && 
         (endConversationCondition==null || Boolean.TRUE.equals( endConversationCondition.getValue() ) );
   }
   public FlushModeType getFlushMode()
   {
      return flushMode;
   }
   public void setFlushMode(FlushModeType flushMode)
   {
      this.flushMode = flushMode;
   }
   public boolean isJoin()
   {
      return join;
   }
   public void setJoin(boolean join)
   {
      this.join = join;
   }
   public boolean isNested()
   {
      return nested;
   }
   public void setNested(boolean nested)
   {
      this.nested = nested;
   }
   public String getPageflow()
   {
      return pageflow;
   }
   public void setPageflow(String pageflow)
   {
      this.pageflow = pageflow;
   }
   public boolean isEndConversationBeforeRedirect()
   {
      return isEndConversationBeforeRedirect;
   }
   public void setEndConversationBeforeRedirect(boolean isEndConversationBeforeRedirect)
   {
      this.isEndConversationBeforeRedirect = isEndConversationBeforeRedirect;
   }
   public boolean isEndRootConversation()
   {
      return isEndConversationBeforeRedirect;
   }
   public void setEndRootConversation(boolean isEndRootConversation)
   {
      this.isEndRootConversation = isEndRootConversation;
   }
   public ValueExpression<Boolean> getBeginConversationCondition()
   {
      return beginConversationCondition;
   }
   public void setBeginConversationCondition(ValueExpression<Boolean> beginConversationCondition)
   {
      this.beginConversationCondition = beginConversationCondition;
   }
   public ValueExpression<Boolean> getEndConversationCondition()
   {
      return endConversationCondition;
   }
   public void setEndConversationCondition(ValueExpression<Boolean> endConversationCondition)
   {
      this.endConversationCondition = endConversationCondition;
   }
   
   public String getConversationName()
   {
      return conversationName;
   }
   
   public void setConversationName(String conversationName)
   {
      this.conversationName = conversationName;
   }
   
}