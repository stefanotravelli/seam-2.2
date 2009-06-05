package org.jboss.seam.pdf.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;
import org.jboss.seam.pdf.KeyStoreConfig;
import org.jboss.seam.util.FacesResources;
import org.jboss.seam.util.Resources;

import com.lowagie.text.DocWriter;
import com.lowagie.text.pdf.PdfAcroForm;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

public class UISignature extends ITextComponent
{
   // signature box
   String field;
   String size;
   String reason;
   String location;

   public void setField(String field)
   {
      this.field = field;
   }

   public void setSize(String size)
   {
      this.size = size;
   }

   public void setReason(String reason)
   {
      this.reason = reason;
   }

   public void setLocation(String location)
   {
      this.location = location;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
   }

   @Override
   public void removeITextObject()
   {
   }

   @Override
   public Object getITextObject()
   {
      return null;
   }

   @Override
   public void handleAdd(Object other)
   {
      throw new RuntimeException("PDF signature does not accept children");
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      PdfWriter writer = findWriter();
      if (writer == null)
      {
         throw new RuntimeException("Cannot find PdfWriter - the document may not exist or may not be a pdf type");
      }

      PdfAcroForm form = writer.getAcroForm();

      field = (String) valueBinding(context, "field", field);
      if (field == null)
      {
         throw new RuntimeException("signature field named is required");
      }

      size = (String) valueBinding(context, "size", size);
      if (size == null)
      {
         throw new RuntimeException("signature size is required");
      }
      float[] rect = ITextUtils.stringToFloatArray(size);
      if (rect.length != 4)
      {
         throw new RuntimeException("size must contain four numbers");
      }
      form.addSignature(field, rect[0], rect[1], rect[2], rect[3]);

      UIDocument doc = (UIDocument) findITextParent(this, UIDocument.class);
      doc.addSignature(this);

      super.encodeEnd(context);
   }

   private PdfWriter findWriter()
   {
      UIDocument doc = (UIDocument) findITextParent(this, UIDocument.class);
      if (doc != null)
      {
         DocWriter writer = doc.getWriter();

         if (writer instanceof PdfWriter)
         {
            return (PdfWriter) writer;
         }
      }
      return null;
   }

   public byte[] sign(byte[] originalBytes)
   {
      KeyStoreConfig store = KeyStoreConfig.instance();
      InputStream is = null;
      try {
         is = FacesResources.getResourceAsStream(store.getKeyStore(), getFacesContext().getExternalContext());

         KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
         ks.load(is, store.getKeyStorePassword().toCharArray());

         PrivateKey key = (PrivateKey) ks.getKey(store.getKeyAlias(), store.getKeyPassword().toCharArray());
         Certificate[] chain = ks.getCertificateChain(store.getKeyAlias());

         PdfReader reader = new PdfReader(originalBytes);
         ByteArrayOutputStream os = new ByteArrayOutputStream();

         PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
         PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
         appearance.setCrypto(key, chain, null, PdfSignatureAppearance.SELF_SIGNED);

         appearance.setReason(reason);
         appearance.setLocation(location);

         appearance.setVisibleSignature(field);
         stamper.close();

         return os.toByteArray();
      } catch (Exception e) {
         throw new RuntimeException(e);
      } finally {
          Resources.closeStream(is);
      }

   }

}
