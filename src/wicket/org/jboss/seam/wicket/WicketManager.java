package org.jboss.seam.wicket;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;

@Scope(ScopeType.EVENT)
@Name("org.jboss.seam.core.manager")
@Install(precedence=100, classDependencies="org.apache.wicket.Application")
@BypassInterceptors
public class WicketManager extends Manager
{
   
   public String appendConversationIdFromRedirectFilter(String url)
   {
      super.beforeRedirect();
      if (ConversationPropagation.instance().getConversationId() != null)
      {
         url = encodeConversationIdParameter( url, getConversationIdParameter(), ConversationPropagation.instance().getConversationId() );
      }
      return url;
   }
   
   public static WicketManager instance()
   {
      return (WicketManager) Manager.instance();
   }
}
