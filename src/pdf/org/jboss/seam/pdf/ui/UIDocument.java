package org.jboss.seam.pdf.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;

import org.jboss.seam.core.Manager;
import org.jboss.seam.document.ByteArrayDocumentData;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentStore;
import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.pdf.ITextUtils;

import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.HtmlWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;

public class UIDocument extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIDocument";

   public static DocumentType PDF = new DocumentType("pdf", "application/pdf");
   public static DocumentType RTF = new DocumentType("rtf", "text/rtf");
   public static DocumentType HTML = new DocumentType("html", "text/html");

   DocWriter writer;
   Document document;
   ByteArrayOutputStream stream;

   DocumentType documentType;

   String type;
   String title;
   String subject;
   String keywords;
   String author;
   String creator;
   String orientation;

   String pageSize;
   String margins;
   Boolean marginMirroring;

   String disposition;
   String fileName;

   boolean sendRedirect = true;

   UISignature signatureField;

   public void setDisposition(String disposition)
   {
      this.disposition = disposition;
   }

   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public void setMargins(String margins)
   {
      this.margins = margins;
   }

   public void setPageSize(String pageSize)
   {
      this.pageSize = pageSize;
   }

   public void setMarginMirroring(Boolean marginMirroring)
   {
      this.marginMirroring = marginMirroring;
   }

   public void setAuthor(String author)
   {
      this.author = author;
   }

   public void setCreator(String creator)
   {
      this.creator = creator;
   }

   public void setKeywords(String keywords)
   {
      this.keywords = keywords;
   }

   public void setSubject(String subject)
   {
      this.subject = subject;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public void setOrientation(String orientation)
   {
      this.orientation = orientation;
   }

   public void setSendRedirect(boolean sendRedirect)
   {
      this.sendRedirect = sendRedirect;
   }

   public boolean getSendRedirect()
   {
      return sendRedirect;
   }

   @Override
   public Object getITextObject()
   {
      return document;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      type = (String) valueBinding(context, "type", type);
      documentType = documentTypeForName(type);

      document = new Document();
      // most of this needs to be done BEFORE document.open();

      pageSize = (String) valueBinding(context, "pageSize", pageSize);
      if (pageSize != null)
      {
         document.setPageSize(ITextUtils.pageSizeValue(pageSize));
      }

      orientation = (String) valueBinding(context, "orientation", orientation);
      if (orientation != null)
      {
         if (orientation.equalsIgnoreCase("portrait"))
         {
            // do nothing
         }
         else if (orientation.equalsIgnoreCase("landscape"))
         {
            Rectangle currentSize = document.getPageSize();
            document.setPageSize(new Rectangle(currentSize.getHeight(), currentSize.getWidth()));
         }
         else
         {
            throw new RuntimeException("orientation value " + orientation + "unknown");
         }
      }

      margins = (String) valueBinding(context, "margins", margins);
      if (margins != null)
      {
         float[] vals = ITextUtils.stringToFloatArray(margins);
         if (vals.length != 4)
         {
            throw new RuntimeException("margins must contain 4 float values");
         }

         document.setMargins(vals[0], vals[1], vals[2], vals[3]);
      }

      marginMirroring = (Boolean) valueBinding(context, "marginMirroring", marginMirroring);
      if (marginMirroring != null)
      {
         document.setMarginMirroring(marginMirroring);
      }
   }

   protected void initMetaData(FacesContext context)
   {
      title = (String) valueBinding(context, "title", title);
      if (title != null)
      {
         document.addTitle(title);
      }

      subject = (String) valueBinding(context, "subject", subject);
      if (subject != null)
      {
         document.addSubject(subject);
      }

      keywords = (String) valueBinding(context, "keywords", keywords);
      if (keywords != null)
      {
         document.addKeywords(keywords);
      }

      author = (String) valueBinding(context, "author", author);
      if (author != null)
      {
         document.addAuthor(author);
      }

      creator = (String) valueBinding(context, "creator", creator);
      if (creator != null)
      {
         document.addCreator(creator);
      }
   }

   @Override
   public void removeITextObject()
   {
      document = null;
   }

   @Override
   public void handleAdd(Object o)
   {
      if (o instanceof Element)
      {
         try
         {
            document.add((Element) o);
         }
         catch (DocumentException e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         throw new IllegalArgumentException("cannot add " + o);
      }
   }

   public void addSignature(UISignature signatureField)
   {
      this.signatureField = signatureField;
   }

   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      super.encodeBegin(context);

      stream = new ByteArrayOutputStream();

      try
      {
         writer = createWriterForStream(stream);

         initMetaData(context);
         processHeaders();

         document.open();
      }
      catch (DocumentException e)
      {
         throw new RuntimeException(e);
      }
   }

   protected void processHeaders()
   {
      UIComponent facet = getFacet("header");

      if (facet == null)
      {
         return;
      }

      try
      {
         encode(FacesContext.getCurrentInstance(), facet);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      document.resetHeader();
      document.resetFooter();
      document.close();

      byte[] bytes = stream.toByteArray();

      if (signatureField != null)
      {
         bytes = signatureField.sign(bytes);
      }

      String viewId = Pages.getViewId(context);
      String baseName = Pages.getCurrentBaseName();

      DocumentData documentData = new ByteArrayDocumentData(baseName, documentType, bytes);
      String dispositionValue = (String) valueBinding(context, "disposition", disposition);
      if (dispositionValue != null)
      {
         documentData.setDisposition(dispositionValue);
      }

      String fileNameValue = (String) valueBinding(context, "fileName", fileName);
      if (fileNameValue != null)
      {
         documentData.setFilename(fileNameValue);
      }

      if (sendRedirect)
      {
         DocumentStore store = DocumentStore.instance();
         String id = store.newId();

         String url = store.preferredUrlForContent(baseName, documentType.getExtension(), id);
         url = Manager.instance().encodeConversationId(url, viewId);

         store.saveData(id, documentData);

         removeITextObject();

         context.getExternalContext().redirect(url);
      }
      else
      {
         UIComponent parent = getParent();

         if (parent instanceof ValueHolder)
         {
            ValueHolder holder = (ValueHolder) parent;
            holder.setValue(documentData);
         }
      }
   }

   public DocWriter getWriter()
   {
      return writer;
   }

   public PdfContentByte getPdfContent()
   {
      PdfWriter writer = (PdfWriter) getWriter();
      return writer.getDirectContent();
   }

   public PdfTemplate createPdfTemplate(float width, float height)
   {
      return getPdfContent().createTemplate(width, height);
   }

   private DocumentType documentTypeForName(String typeName)
   {
      if (typeName != null)
      {
         if (typeName.equalsIgnoreCase("pdf"))
         {
            return PDF;
         }
         else if (typeName.equalsIgnoreCase("rtf"))
         {
            return RTF;
         }
         else if (typeName.equalsIgnoreCase("html"))
         {
            return HTML;
         }
      }
      return PDF;
   }

   protected DocWriter createWriterForStream(OutputStream stream) throws DocumentException
   {
      if (documentType == PDF)
      {
         return PdfWriter.getInstance(document, stream);
      }
      else if (documentType == RTF)
      {
         return RtfWriter2.getInstance(document, stream);
      }
      else if (documentType == HTML)
      {
         return HtmlWriter.getInstance(document, stream);
      }

      throw new IllegalArgumentException("unknown document type");
   }
}
