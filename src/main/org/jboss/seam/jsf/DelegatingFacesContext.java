package org.jboss.seam.jsf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;

/**
 * Implementation of FacesContext that delegates all calls.
 * 
 * Further, it exposes {@link #setCurrentInstance(FacesContext)} as a public
 * method
 * 
 * @author Pete Muir
 *
 */
public class DelegatingFacesContext extends FacesContext
{
   
   private FacesContext delegate;
   
   public DelegatingFacesContext(FacesContext delegate)
   {
      this.delegate = delegate;
   }

   @Override
   public void addMessage(String clientId, FacesMessage message)
   {
      delegate.addMessage(clientId, message);
   }

   @Override
   public Application getApplication()
   {
     return delegate.getApplication();
   }
   
   
   public Map<Object, Object> getAttributes() {
      // FIXME: due JSF 2 new method for
      // javax.faces.context.FacesContext.getAttributes() and non existent
      // JSF 1.2 equivalent method it returns empty Map without delegating call
      return new HashMap<Object, Object>();
   }

   @Override
   public Iterator getClientIdsWithMessages()
   {
     return delegate.getClientIdsWithMessages();
   }

   @Override
   public ExternalContext getExternalContext()
   {
      return delegate.getExternalContext();
   }

   @Override
   public Severity getMaximumSeverity()
   {
      return delegate.getMaximumSeverity();
   }

   @Override
   public Iterator getMessages()
   {
     return delegate.getMessages();
   }

   @Override
   public Iterator getMessages(String clientId)
   {
      return delegate.getMessages(clientId);
   }

   @Override
   public RenderKit getRenderKit()
   {
     return delegate.getRenderKit();
   }

   @Override
   public boolean getRenderResponse()
   {
      return delegate.getRenderResponse();
   }

   @Override
   public boolean getResponseComplete()
   {
     return delegate.getResponseComplete();
   }

   @Override
   public ResponseStream getResponseStream()
   {
      return delegate.getResponseStream();
   }

   @Override
   public ResponseWriter getResponseWriter()
   {
      return delegate.getResponseWriter();
   }

   @Override
   public UIViewRoot getViewRoot()
   {
     return delegate.getViewRoot();
   }

   @Override
   public void release()
   {
      delegate.release();
   }

   @Override
   public void renderResponse()
   {
     delegate.renderResponse();
   }

   @Override
   public void responseComplete()
   {
      delegate.responseComplete();
   }

   @Override
   public void setResponseStream(ResponseStream responseStream)
   {
      delegate.setResponseStream(responseStream);
   }

   @Override
   public void setResponseWriter(ResponseWriter responseWriter)
   {
      delegate.setResponseWriter(responseWriter);
   }

   @Override
   public void setViewRoot(UIViewRoot root)
   {
      delegate.setViewRoot(root);
   }
   
   public FacesContext getDelegate() 
   {
      return delegate;
   }
   
   @Override
   public ELContext getELContext()
   {
      return delegate.getELContext();
   }

   public static void setCurrentInstance(FacesContext context) 
   {
      FacesContext.setCurrentInstance(context);
  }
   
}
