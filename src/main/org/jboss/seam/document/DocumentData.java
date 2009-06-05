package org.jboss.seam.document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public abstract class DocumentData implements Serializable
{
   DocumentType documentType;
   String baseName;

   String disposition = "inline";
   String fileName;

   public DocumentData(String baseName, DocumentType documentType)
   {
      super();
      this.documentType = documentType;
      this.baseName = baseName;
   }

   public abstract void writeDataToStream(OutputStream stream) throws IOException;

   public DocumentType getDocumentType()
   {
      return documentType;
   }

   public String getBaseName()
   {
      return baseName;
   }

   public void setFilename(String fileName)
   {
      this.fileName = fileName;
   }

   public String getFileName()
   {
      if (fileName == null)
      {
         return getBaseName() + "." + getDocumentType().getExtension();
      }
      else
      {
         return fileName;
      }
   }

   public void setDisposition(String disposition)
   {
      this.disposition = disposition;
   }

   public String getDisposition()
   {
      return disposition;
   }

   static public class DocumentType implements Serializable
   {
      private String mimeType;
      private String extension;

      public DocumentType(String extension, String mimeType)
      {
         this.extension = extension;
         this.mimeType = mimeType;
      }

      public String getMimeType()
      {
         return mimeType;
      }

      public String getExtension()
      {
         return extension;
      }

   }
}