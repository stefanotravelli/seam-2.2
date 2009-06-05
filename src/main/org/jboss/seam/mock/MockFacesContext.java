/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import org.jboss.seam.el.EL;

/**
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class MockFacesContext extends FacesContext
{

   private UIViewRoot viewRoot;// = new UIViewRoot();

   private final Map<FacesMessage, String> messages = new LinkedHashMap<FacesMessage, String>();

   private ExternalContext externalContext;

   private ResponseWriter responseWriter;

   private RenderKitFactory renderKitFactory;
   
   private ELContext elContext;

   public MockFacesContext(ExternalContext externalContext, Application application)
   {
      this.externalContext = externalContext;
      this.application = application;
   }

   // Create a MockFacesContext using a ApplicationFactory to get the
   // Application
   public MockFacesContext(ExternalContext externalContext)
   {
      application = ((ApplicationFactory) FactoryFinder
               .getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();
      renderKitFactory = (RenderKitFactory) FactoryFinder
               .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
      this.externalContext = externalContext;
   }

   private Application application;

   @Override
   public Application getApplication()
   {
      return application;
   }

   @Override
   public Iterator getClientIdsWithMessages()
   {
      return messages.values().iterator();
   }

   @Override
   public ExternalContext getExternalContext()
   {
      return externalContext;
   }

   @Override
   public Severity getMaximumSeverity()
   {
      Severity max = null;
      for (FacesMessage msg : messages.keySet())
      {
         if (max == null || msg.getSeverity().compareTo(max) > 0)
         {
            max = msg.getSeverity();
         }
      }
      return max;
   }

   @Override
   public Iterator getMessages()
   {
      return messages.keySet().iterator();
   }

   @Override
   public Iterator getMessages(String clientId)
   {
      List list = new ArrayList();
      for (Map.Entry<FacesMessage, String> entry : messages.entrySet())
      {
         String messageId = entry.getValue();
         if ( idsAreEqual(clientId, messageId) )
         {
            list.add(entry.getKey());
         }
      }
      return list.iterator();
   }

   private boolean idsAreEqual(String clientId, String messageId)
   {
      return (clientId==null && messageId==null) || 
            (clientId!=null && clientId.equals(messageId));
   }

   @Override
   public RenderKit getRenderKit()
   {
      if (getViewRoot() == null || getViewRoot().getRenderKitId() == null)
      {
         return MockRenderKit.INSTANCE;
      }
      else
      {
         return renderKitFactory.getRenderKit(this, getViewRoot().getRenderKitId());
      }
   }

   private boolean renderResponse;

   @Override
   public boolean getRenderResponse()
   {
      return renderResponse;
   }

   private boolean responseComplete;

   @Override
   public boolean getResponseComplete()
   {
      return responseComplete;
   }

   @Override
   public ResponseStream getResponseStream()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setResponseStream(ResponseStream stream)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public ResponseWriter getResponseWriter()
   {
      return responseWriter;
   }

   @Override
   public void setResponseWriter(ResponseWriter writer)
   {
      responseWriter = writer;
   }

   @Override
   public UIViewRoot getViewRoot()
   {
      return viewRoot;
   }

   @Override
   public void setViewRoot(UIViewRoot vr)
   {
      viewRoot = vr;
   }

   @Override
   public void addMessage(String clientId, FacesMessage msg)
   {
      messages.put(msg, clientId);
   }

   @Override
   public void release()
   {
      setCurrentInstance(null);
      MockFacesContextFactory.setFacesContext(null);
   }

   @Override
   public void renderResponse()
   {
      renderResponse = true;
   }

   @Override
   public void responseComplete()
   {
      responseComplete = true;
   }

   public MockFacesContext setCurrent()
   {
      setCurrentInstance(this);
      
      MockFacesContextFactory.setFacesContext(this);
      return this;
   }

   public MockFacesContext createViewRoot()
   {
      viewRoot = new UIViewRoot();
      viewRoot.setRenderKitId(getApplication().getViewHandler().calculateRenderKitId(this));
      return this;
   }

   @Override
   public ELContext getELContext()
   {
      if (elContext == null)
      {
         elContext = EL.createELContext(EL.createELContext(), getApplication().getELResolver());
         elContext.putContext(FacesContext.class, this);
      }
      return elContext;
   }   

}
