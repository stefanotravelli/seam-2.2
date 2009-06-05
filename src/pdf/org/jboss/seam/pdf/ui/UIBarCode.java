package org.jboss.seam.pdf.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.imageio.ImageIO;

import org.jboss.seam.core.Manager;
import org.jboss.seam.pdf.ITextUtils;
import org.jboss.seam.pdf.ui.ITextComponent;
import org.jboss.seam.ui.graphicImage.GraphicImageResource;
import org.jboss.seam.ui.graphicImage.GraphicImageStore;
import org.jboss.seam.ui.graphicImage.GraphicImageStore.ImageWrapper;
import org.jboss.seam.ui.graphicImage.Image.Type;

import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;

/**
 * based on JBSEAM-1155 submission by user ivan
 */

public class UIBarCode extends ITextComponent
{

   private Barcode barcode;
   private Object itextObject;

   private String type;
   private String code;
   private String codeType;
   private Float xpos;
   private Float ypos;
   private Float rotDegrees;
   private String altText;
   private Float barHeight;
   private Float textSize;
   private Float minBarWidth;
   private Float barMultiplier;

   String barColor;
   String textColor;

   public String getCode()
   {
      return (String) valueBinding("code", code);
   }

   public void setCode(String code)
   {
      this.code = code;
   }

   public String getType()
   {
      return (String) valueBinding("type", type);
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getCodeType()
   {
      return (String) valueBinding("codeType", codeType);
   }

   public void setCodeType(String codeType)
   {
      this.codeType = codeType;
   }

   public Float getRotDegrees()
   {
      return (Float) valueBinding("rotDegrees", rotDegrees);
   }

   public void setRotDegrees(Float rotDegrees)
   {
      this.rotDegrees = rotDegrees;
   }

   public Float getXpos()
   {
      return (Float) valueBinding("xpos", xpos);
   }

   public void setXpos(Float xpos)
   {
      this.xpos = xpos;
   }

   public Float getYpos()
   {
      return (Float) valueBinding("ypos", ypos);
   }

   public void setYpos(Float ypos)
   {
      this.ypos = ypos;
   }

   public String getAltText()
   {
      return (String) valueBinding("altText", altText);
   }

   public void setAltText(String altText)
   {
      this.altText = altText;
   }

   public Float getBarHeight()
   {
      return (Float) valueBinding("barHeight", barHeight);
   }

   public void setBarHeight(Float barHeight)
   {
      this.barHeight = barHeight;
   }

   public Float getBarMultiplier()
   {
      return (Float) valueBinding("barMultiplier", barMultiplier);
   }

   public void setBarMultiplier(Float barMultiplier)
   {
      this.barMultiplier = barMultiplier;
   }

   public Float getMinBarWidth()
   {
      return (Float) valueBinding("minBarWidth", minBarWidth);
   }

   public void setMinBarWidth(Float minBarWidth)
   {
      this.minBarWidth = minBarWidth;
   }

   public Float getTextSize()
   {
      return (Float) valueBinding("textSize", textSize);
   }

   public void setTextSize(Float textSize)
   {
      this.textSize = textSize;
   }

   public String getBarColor()
   {
      return (String) valueBinding("barColor", barColor);
   }

   public void setBarColor(String barColor)
   {
      this.barColor = barColor;
   }

   public String getTextColor()
   {
      return (String) valueBinding("textColor", textColor);
   }

   public void setTextColor(String textColor)
   {
      this.textColor = textColor;
   }

   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object[] values = (Object[]) state;
      super.restoreState(context, values[0]);

      type = (String) values[1];
      code = (String) values[2];
      xpos = (Float) values[5];
      ypos = (Float) values[6];
      rotDegrees = (Float) values[7];
      altText = (String) values[8];
      barHeight = (Float) values[9];
      textSize = (Float) values[10];
      minBarWidth = (Float) values[11];
      barMultiplier = (Float) values[12];
      codeType = (String) values[13];
      barColor = (String) values[14];
      textColor = (String) values[15];
   }

   @Override
   public Object saveState(FacesContext context)
   {
      Object[] values = new Object[16];

      values[0] = super.saveState(context);
      values[1] = type;
      values[2] = code;
      values[5] = xpos;
      values[6] = ypos;
      values[7] = rotDegrees;
      values[8] = altText;
      values[9] = barHeight;
      values[10] = textSize;
      values[11] = minBarWidth;
      values[12] = barMultiplier;
      values[13] = codeType;
      values[14] = barColor;
      values[15] = textColor;

      return values;
   }

   @Override
   public Object getITextObject()
   {
      return itextObject;
   }

   @Override
   public void createITextObject(FacesContext context) throws IOException
   {
      barcode = createBarcodeType(getType());

      barcode.setCode(getCode());

      Integer codeVal = lookupCodeType(getCodeType());
      if (codeVal != null)
      {
         barcode.setCodeType(codeVal);
      }

      if (getAltText() != null)
      {
         barcode.setAltText(getAltText());
      }

      if (getBarHeight() != null)
      {
         barcode.setBarHeight(getBarHeight());
      }

      if (getBarMultiplier() != null)
      {
         barcode.setN(getBarMultiplier());
      }

      if (getMinBarWidth() != null)
      {
         barcode.setX(getMinBarWidth());
      }

      UIDocument doc = (UIDocument) findITextParent(getParent(), UIDocument.class);

      if (doc != null)
      {
         PdfWriter writer = (PdfWriter) doc.getWriter();
         PdfContentByte cb = writer.getDirectContent();
         Image image = barcode.createImageWithBarcode(cb, ITextUtils.colorValue(getBarColor()), ITextUtils.colorValue(getTextColor()));

         if (getRotDegrees() != null)
         {
            image.setRotationDegrees(getRotDegrees());
         }
         if (getXpos() != null && getYpos() != null)
         {
            image.setAbsolutePosition(getXpos(), getYpos());
         }

         itextObject = image;
      }
      else
      {
         Color bars = ITextUtils.colorValue(getBarColor());
         if (bars == null)
         {
            bars = Color.BLACK;
         }
         byte[] imageData = imageToByteArray(barcode.createAwtImage(bars, Color.WHITE));
         itextObject = new ImageWrapper(imageData, Type.IMAGE_JPEG);
      }
   }

   private Integer lookupCodeType(String codeType)
   {
      if (codeType == null || codeType.length() == 0)
      {
         return null;
      }

      try
      {
         Field field = Barcode.class.getDeclaredField(codeType.toUpperCase());
         return field.getInt(Barcode.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private Barcode createBarcodeType(String barcodeType)
   {
      if (barcodeType == null || barcodeType.length() == 0)
      {
         return new Barcode128();
      }

      if (barcodeType.equalsIgnoreCase("code128"))
      {
         return new Barcode128();
      }
      else if (barcodeType.equalsIgnoreCase("code39"))
      {
         return new Barcode39();
      }
      else if (barcodeType.equalsIgnoreCase("codabar"))
      {
         return new BarcodeCodabar();
      }
      else if (barcodeType.equalsIgnoreCase("ean"))
      {
         return new BarcodeEAN();
      }
      else if (barcodeType.equalsIgnoreCase("inter25"))
      {
         return new BarcodeInter25();
      }
      else if (barcodeType.equalsIgnoreCase("postnet"))
      {
         return new BarcodePostnet();
      }
      throw new RuntimeException("Unknown barcode type " + barcodeType);
   }

   @Override
   public void removeITextObject()
   {
      itextObject = null;
   }

   @Override
   public void handleAdd(Object other)
   {
      throw new RuntimeException("can't add " + other.getClass().getName() + " to barcode");
   }

   public static byte[] imageToByteArray(java.awt.Image image) throws IOException
   {
      BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
      Graphics gc = bufferedImage.createGraphics();
      gc.drawImage(image, 0, 0, null);

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "jpeg", stream);

      return stream.toByteArray();
   }

   @Override
   public void noITextParentFound()
   {
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         ResponseWriter response = context.getResponseWriter();
         response.startElement("img", null);
         GraphicImageStore store = GraphicImageStore.instance();

         String key = store.put((ImageWrapper) itextObject);
         String url = context.getExternalContext().getRequestContextPath() + GraphicImageResource.GRAPHIC_IMAGE_RESOURCE_PATH + "/" + key + Type.IMAGE_JPEG.getExtension();

         response.writeAttribute("src", url, null);

         response.endElement("img");

         Manager.instance().beforeRedirect();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
