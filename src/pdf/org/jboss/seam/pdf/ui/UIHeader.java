package org.jboss.seam.pdf.ui;

import com.lowagie.text.Document;
import com.lowagie.text.HeaderFooter;

public class UIHeader extends UIHeaderFooter
{
   @Override
   public void handleHeaderFooter(HeaderFooter header)
   {
      Document document = findDocument();
      if (document == null)
      {
         throw new RuntimeException("cannot locate document object");
      }

      findDocument().setHeader(header);
   }
}
