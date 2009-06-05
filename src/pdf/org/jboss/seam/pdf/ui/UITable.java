package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class UITable extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UITable";

   PdfPTable table;
   String widths;
   int columns = 1;
   Integer headerRows = 0;
   Integer footerRows = 0;
   Float widthPercentage;
   Integer horizontalAlignment;
   Boolean skipFirstHeader;
   Integer runDirection;
   Boolean lockedWidth;
   Boolean splitRows;
   Float spacingBefore;
   Float spacingAfter;
   Boolean extendLastRow;
   Boolean headersInEvent;
   Boolean splitLate;
   Boolean keepTogether;

   public void setWidths(String widths)
   {
      this.widths = widths;
   }

   public void setColumns(int columns)
   {
      this.columns = columns;
   }

   public void setHeaderRows(Integer headerRows)
   {
      this.headerRows = headerRows;
   }

   public void setFooterRows(Integer footerRows)
   {
      this.footerRows = footerRows;
   }

   public void setExtendLastRow(Boolean extendLastRow)
   {
      this.extendLastRow = extendLastRow;
   }

   public void setHeadersInEvent(Boolean headersInEvent)
   {
      this.headersInEvent = headersInEvent;
   }

   public void setHorizontalAlignment(String horizontalAlignment)
   {
      this.horizontalAlignment = ITextUtils.alignmentValue(horizontalAlignment);
   }

   public void setKeepTogether(Boolean keepTogether)
   {
      this.keepTogether = keepTogether;
   }

   public void setLockedWidth(Boolean lockedWidth)
   {
      this.lockedWidth = lockedWidth;
   }

   public void setRunDirection(Integer runDirection)
   {
      this.runDirection = runDirection;
   }

   public void setSkipFirstHeader(Boolean skipFirstHeader)
   {
      this.skipFirstHeader = skipFirstHeader;
   }

   public void setSpacingAfter(Float spacingAfter)
   {
      this.spacingAfter = spacingAfter;
   }

   public void setSpacingBefore(Float spacingBefore)
   {
      this.spacingBefore = spacingBefore;
   }

   public void setSplitLate(Boolean splitLate)
   {
      this.splitLate = splitLate;
   }

   public void setSplitRows(Boolean splitRows)
   {
      this.splitRows = splitRows;
   }

   public void setTable(PdfPTable table)
   {
      this.table = table;
   }

   public void setWidthPercentage(Float widthPercentage)
   {
      this.widthPercentage = widthPercentage;
   }

   @Override
   public Object getITextObject()
   {
      return table;
   }

   @Override
   public void removeITextObject()
   {
      table = null;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      columns = (Integer) valueBinding(context, "columns", columns);
      table = new PdfPTable(columns);

      widths = (String) valueBinding(context, "widths", widths);
      if (widths != null)
      {
         try
         {
            table.setWidths(ITextUtils.stringToFloatArray(widths));
         }
         catch (DocumentException e)
         {
            throw new RuntimeException(e);
         }
      }

      headerRows = (Integer) valueBinding(context, "headerRows", headerRows);
      if (headerRows != null)
      {
         table.setHeaderRows(headerRows);
      }

      footerRows = (Integer) valueBinding(context, "footerRows", footerRows);
      if (footerRows != null)
      {
         table.setFooterRows(footerRows);
      }

      widthPercentage = (Float) valueBinding(context, "widthPercentage", widthPercentage);
      if (widthPercentage != null)
      {
         table.setWidthPercentage(widthPercentage);
      }

      horizontalAlignment = (Integer) valueBinding(context, "horizontalAlignment", horizontalAlignment);
      if (horizontalAlignment != null)
      {
         table.setHorizontalAlignment(horizontalAlignment);
      }

      runDirection = (Integer) valueBinding(context, "runDirection", runDirection);
      if (runDirection != null)
      {
         table.setRunDirection(runDirection);
      }

      lockedWidth = (Boolean) valueBinding(context, "lockedWidth", lockedWidth);
      if (lockedWidth != null)
      {
         table.setLockedWidth(lockedWidth);
      }

      splitRows = (Boolean) valueBinding(context, "splitRows", splitRows);
      if (splitRows != null)
      {
         table.setSplitRows(splitRows);
      }

      spacingBefore = (Float) valueBinding(context, "spacingBefore", spacingBefore);
      if (spacingBefore != null)
      {
         table.setSpacingBefore(spacingBefore);
      }

      spacingAfter = (Float) valueBinding(context, "spacingAfter", spacingAfter);
      if (spacingAfter != null)
      {
         table.setSpacingAfter(spacingAfter);
      }

      extendLastRow = (Boolean) valueBinding(context, "extendLastRow", extendLastRow);
      if (extendLastRow != null)
      {
         table.setExtendLastRow(extendLastRow);
      }

      headersInEvent = (Boolean) valueBinding(context, "headersInEvent", headersInEvent);
      if (headersInEvent != null)
      {
         table.setHeadersInEvent(headersInEvent);
      }

      splitLate = (Boolean) valueBinding(context, "splitLate", splitLate);
      if (splitLate != null)
      {
         table.setSplitLate(splitLate);
      }

      keepTogether = (Boolean) valueBinding(context, "keepTogether", keepTogether);
      if (keepTogether != null)
      {
         table.setKeepTogether(keepTogether);
      }
   }

   @Override
   public void handleAdd(Object o) {
        if (o instanceof PdfPCell) {
            table.addCell((PdfPCell) o);
        } else if (o instanceof PdfPTable) {
            table.addCell((PdfPTable) o);
        } else if (o instanceof Phrase) {
            table.addCell((Phrase) o);
        } else if (o instanceof Image) {
            table.addCell((Image) o);
        } else {
            throw new RuntimeException("Can't add " + o.getClass().getName()
                    + " to table");
        }
    }

   public PdfPCell getDefaultCellFacet()
   {
      Object facet = processFacet("defaultCell");

      if (facet != null)
      {
         if (!(facet instanceof PdfPCell))
         {
            throw new RuntimeException("UITable defaultCell facet must be a PdfPCell - found " + facet.getClass());
         }
         return (PdfPCell) facet;
      }
      return null;
   }
}
