package org.jboss.seam.mail.ui.context;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.jsf.DelegatingFacesContext;


public class MailFacesContextImpl extends DelegatingFacesContext
{
   
   private ExternalContext externalContext;
   
   private ResponseWriter responseWriter;
   
   public MailFacesContextImpl(FacesContext delegate)
   {
      this(delegate, null);
   }
   
   public MailFacesContextImpl(FacesContext delegate, String urlBase)
   {
      super(delegate);
      externalContext = new MailExternalContextImpl(getDelegate().getExternalContext(), urlBase);
      responseWriter = new MailResponseWriter(getDelegate().getResponseWriter(), getDelegate().getResponseWriter().getContentType());
   }

   @Override
   public ExternalContext getExternalContext()
   {
      return externalContext;
   }

   @Override
   public ResponseWriter getResponseWriter()
   {
      return responseWriter;
   }

   @Override
   public void setResponseWriter(ResponseWriter responseWriter)
   {
      this.responseWriter = responseWriter;
   }
   
   public static void start(String urlBase) 
   {
      FacesContext mailFacesContext = new MailFacesContextImpl(getCurrentInstance(), urlBase);
      setCurrentInstance(mailFacesContext);
   }
   
   public static void stop() 
   {
      if (getCurrentInstance() instanceof MailFacesContextImpl)
      {
         MailFacesContextImpl mailFacesContextImpl = (MailFacesContextImpl) getCurrentInstance();
         setCurrentInstance(mailFacesContextImpl.getDelegate());
         
      }
   }

}
