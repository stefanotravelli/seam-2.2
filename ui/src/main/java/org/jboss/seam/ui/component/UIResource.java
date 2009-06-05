package org.jboss.seam.ui.component;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * @author Daniel Roth
 */
public abstract class UIResource extends UIComponentBase
{

   public abstract Object getData();

   public abstract void setData(Object data);

   public abstract String getContentType();

   public abstract void setContentType(String contentType);

   public abstract String getDisposition();

   public abstract void setDisposition(String disposition);

   public abstract String getFileName();

   public abstract void setFileName(String fileName);

   @Override
   public void encodeBegin(FacesContext arg0) throws IOException
   {
      if (!(getParent() instanceof UIViewRoot || getParent() instanceof UIDownload))  
      {
         throw new IllegalArgumentException("s:remote must be nested in a s:download or alone in the page");
      }
      super.encodeBegin(arg0);
   }

}
