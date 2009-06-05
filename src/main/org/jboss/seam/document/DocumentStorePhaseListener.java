package org.jboss.seam.document;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.web.Parameters;

public class DocumentStorePhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 7308251684939658978L;

   private static final LogProvider log = Logging.getLogProvider(DocumentStorePhaseListener.class);

   public PhaseId getPhaseId()
   {
      return PhaseId.RENDER_RESPONSE;
   }

   public void afterPhase(PhaseEvent phaseEvent)
   {
      // ...
   }

   public void beforePhase(PhaseEvent phaseEvent)
   {
      String rootId = Pages.getViewId(phaseEvent.getFacesContext());

      Parameters params = Parameters.instance();
      String id = (String) params.convertMultiValueRequestParameter(params.getRequestParameters(), "docId", String.class);
      if (rootId.contains(DocumentStore.DOCSTORE_BASE_URL))
      {
         sendContent(phaseEvent.getFacesContext(), id);
      }
   }

   public void sendContent(FacesContext context, String contentId)
   {
      try
      {
         DocumentData documentData = DocumentStore.instance().getDocumentData(contentId);

         if (documentData != null)
         {

            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            response.setContentType(documentData.getDocumentType().getMimeType());

            response.setHeader("Content-Disposition", documentData.getDisposition() + "; filename=\"" + documentData.getFileName() + "\"");

            documentData.writeDataToStream(response.getOutputStream());
            context.responseComplete();
         }
      }
      catch (IOException e)
      {
         log.warn(e);
      }
   }

}
