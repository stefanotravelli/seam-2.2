package org.jboss.seam.pdf.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Manager;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.document.ByteArrayDocumentData;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentStore;
import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class UIForm extends FormComponent
{
   public static final String COMPONENT_FAMILY = "org.jboss.seam.pdf.UIForm";

   private Log log = Logging.getLog(getClass());

   private String URL;
   private String filename;
   private String exportKey;

   PdfReader reader;
   PdfStamper stamper;
   AcroFields fields;
   ByteArrayOutputStream buffer;

   public String getURL()
   {
      return (String) valueOf("URL", URL);
   }

   public void setURL(String url)
   {
      URL = url;
   }

   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      log.info("Loading template #0", getURL());
      if (getURL().indexOf("://") < 0)
      {
         reader = new PdfReader(ResourceLoader.instance().getResourceAsStream(getURL()));
      }
      else
      {
         reader = new PdfReader(new URL(getURL()));
      }
      buffer = new ByteArrayOutputStream();
      try
      {
         stamper = new PdfStamper(reader, buffer);
         Contexts.getEventContext().set(STAMPER_KEY, stamper);
      }
      catch (DocumentException e)
      {
         throw new FacesException("Could not create PDF stamper", e);
      }
      fields = stamper.getAcroFields();
      Contexts.getEventContext().set(FIELDS_KEY, fields);
   }

   @Override
   public void encodeEnd(FacesContext facesContext) throws IOException
   {
      stamper.setFormFlattening(true);
      try
      {
         stamper.close();
      }
      catch (DocumentException e)
      {
         throw new FacesException("Could not flush PDF", e);
      }

      if (getExportKey() == null)
      {
         UIComponent parent = getParent();
         if (parent != null && (parent instanceof ValueHolder))
         {
            log.debug("Storing PDF data in ValueHolder parent");
            ValueHolder valueHolder = (ValueHolder) parent;
            valueHolder.setValue(buffer.toByteArray());
            return;
         }
      }

      String viewId = Pages.getViewId(facesContext);
      String baseName = Pages.getCurrentBaseName();

      DocumentStore store = DocumentStore.instance();
      DocumentType documentType = new DocumentData.DocumentType("pdf", "application/pdf");
      DocumentData documentData = new ByteArrayDocumentData(baseName, documentType, buffer.toByteArray());
      documentData.setFilename(getFilename());

      if (getExportKey() != null)
      {
         log.debug("Exporting PDF data to event key #0", getExportKey());
         Contexts.getEventContext().set(getExportKey(), documentData);
         return;
      }

      String id = store.newId();
      String url = store.preferredUrlForContent(baseName, documentType.getExtension(), id);
      url = Manager.instance().encodeConversationId(url, viewId);
      store.saveData(id, documentData);
      log.debug("Redirecting to #0 for PDF view", url);
      facesContext.getExternalContext().redirect(url);
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   public String getFilename()
   {
      return (String) valueOf("filename", filename);
   }

   public void setFilename(String filename)
   {
      this.filename = filename;
   }

   public String getExportKey()
   {
      return (String) valueOf("exportKey", exportKey);
   }

   public void setExportKey(String exportKey)
   {
      this.exportKey = exportKey;
   }

}
