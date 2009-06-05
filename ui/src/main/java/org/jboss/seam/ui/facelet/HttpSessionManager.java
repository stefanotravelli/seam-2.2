package org.jboss.seam.ui.facelet;

import static org.jboss.seam.ScopeType.SESSION;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.mock.MockHttpSession;

@Name("org.jboss.seam.ui.facelet.mockHttpSession")
@Scope(SESSION)
@BypassInterceptors
@Install(dependencies="org.jboss.seam.faces.renderer")
@AutoCreate
public class HttpSessionManager implements Serializable
{
   
   private transient HttpSession session;
   
   @Unwrap
   public HttpSession getSession()
   {
      if (session == null)
      {
         this.session = new MockHttpSession(ServletContextManager.instance());
      }
      return session;
   }
   
   public static HttpSession instance()
   {
      if (!Contexts.isSessionContextActive())
      {
         throw new IllegalStateException("Session context is not active");
      }
      return (HttpSession) Component.getInstance(HttpSessionManager.class, SESSION);
   }

}
