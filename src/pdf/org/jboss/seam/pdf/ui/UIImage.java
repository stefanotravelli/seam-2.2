package org.jboss.seam.pdf.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;
import org.jboss.seam.ui.graphicImage.ImageTransform;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;

public class UIImage extends UIRectangle {
    public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIImage";

    Image image;

    Object value;
    float rotation;
    float height;
    float width;
    String alignment;
    String alt;    

    Float indentationLeft;
    Float indentationRight;
    Float spacingBefore;
    Float spacingAfter;
    Float widthPercentage;
    Float initialRotation;
    String dpi;
    String scalePercent;
    String scaleToFit;
    
    Boolean wrap;
    Boolean underlying;

    java.awt.Image imageData;

    

    public void setValue(Object value) {
        this.value = value;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setWrap(Boolean wrap) {
        this.wrap = wrap;
    }

    public void setUnderlying(Boolean underlying) {
        this.underlying = underlying;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public void setIndentationLeft(Float indentationLeft) {
        this.indentationLeft = indentationLeft;
    }

    public void setIndentationRight(Float indentationRight) {
        this.indentationRight = indentationRight;
    }

    public void setInitialRotation(Float initialRotation) {
        this.initialRotation = initialRotation;
    }

    public void setSpacingAfter(Float spacingAfter) {
        this.spacingAfter = spacingAfter;
    }

    public void setSpacingBefore(Float spacingBefore) {
        this.spacingBefore = spacingBefore;
    }

    public void setWidthPercentage(Float widthPercentage) {
        this.widthPercentage = widthPercentage;
    }

    public void setScalePercent(String scalePercent) {
        this.scalePercent = scalePercent;
    }
    
    public void setScaleToFit(String scaleToFit) {
        this.scaleToFit = scaleToFit;
    }
    

    @Override
    public Object getITextObject() {
        return image;
    }

    @Override
    public void removeITextObject() {
        image = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createITextObject(FacesContext context) throws IOException,
            DocumentException {
        value = valueBinding(context, "value", value);

        // instance() doesn't work here - we need a new instance
        org.jboss.seam.ui.graphicImage.Image seamImage = new org.jboss.seam.ui.graphicImage.Image();
        if (value instanceof BufferedImage) {
            seamImage.setBufferedImage((BufferedImage) value);
        } else {
            seamImage.setInput(value);
        }

        for (UIComponent cmp : this.getChildren()) {
            if (cmp instanceof ImageTransform) {
                ImageTransform imageTransform = (ImageTransform) cmp;
                imageTransform.applyTransform(seamImage);
            }
        }

        byte[] data = seamImage.getImage();
        image = Image.getInstance(data);

        rotation = (Float) valueBinding(context, "rotation", rotation);
        if (rotation != 0) {
            image.setRotationDegrees(rotation);
        }

        height = (Float) valueBinding(context, "height", height);
        width  = (Float) valueBinding(context, "width",  width);
        if (height > 0 || width > 0) {
            image.scaleAbsolute(width, height);
        } 
        
        int alignmentValue = 0;
        alignment = (String) valueBinding(context, "alignment", alignment);
        if (alignment != null) {
            alignmentValue = (ITextUtils.alignmentValue(alignment));
        }

        wrap = (Boolean) valueBinding(context, "wrap", wrap);
        if (wrap != null && wrap.booleanValue()) {
            alignmentValue |= Image.TEXTWRAP;
        }

        underlying = (Boolean) valueBinding(context, "underlying", underlying);
        if (underlying != null && underlying.booleanValue()) {
            alignmentValue |= Image.UNDERLYING;
        }

        image.setAlignment(alignmentValue);

        alt = (String) valueBinding(context, "alt", alt);
        if (alt != null) {
            image.setAlt(alt);
        }

        indentationLeft = (Float) valueBinding(context, "indentationLeft",
                indentationLeft);
        if (indentationLeft != null) {
            image.setIndentationLeft(indentationLeft);
        }

        indentationRight = (Float) valueBinding(context, "indentationRight",
                indentationRight);
        if (indentationRight != null) {
            image.setIndentationRight(indentationRight);
        }

        spacingBefore = (Float) valueBinding(context, "spacingBefore",
                spacingBefore);
        if (spacingBefore != null) {
            image.setSpacingBefore(spacingBefore);
        }

        spacingAfter = (Float) valueBinding(context, "spacingAfter",
                spacingAfter);
        if (spacingAfter != null) {
            image.setSpacingAfter(spacingAfter);
        }
        widthPercentage = (Float) valueBinding(context, "widthPercentage",
                widthPercentage);
        if (widthPercentage != null) {
            image.setWidthPercentage(widthPercentage);
        }

        initialRotation = (Float) valueBinding(context, "initialRotation",
                initialRotation);
        if (initialRotation != null) {
            image.setInitialRotation(initialRotation);
        }

        dpi = (String) valueBinding(context, "dpi", dpi);
        if (dpi != null) {
            int[] dpiValues = ITextUtils.stringToIntArray(dpi);
            image.setDpi(dpiValues[0], dpiValues[1]);
        }

        applyRectangleProperties(context, image);

        scaleToFit = (String) valueBinding(context, "scaleToFit", scaleToFit);        
        if (scaleToFit != null) {
            float[] scale = ITextUtils.stringToFloatArray(scaleToFit);
            if (scale.length == 2) {
                image.scaleToFit(scale[0],scale[1]);
            } else {
                throw new RuntimeException("scaleToFit must contain two dimensions");
            }
        }
        
        scalePercent = (String) valueBinding(context, "scalePercent", scalePercent);
        if (scalePercent != null) {
            float[] scale = ITextUtils.stringToFloatArray(scalePercent);
            if (scale.length == 1) {
                image.scalePercent(scale[0]);
            } else if (scale.length == 2) {
                image.scalePercent(scale[0], scale[1]);
            } else {
                throw new RuntimeException(
                        "scalePercent must contain one or two scale percentages");
            }
        }
    }

    @Override
    public void handleAdd(Object o) {
        throw new RuntimeException("can't add " + o.getClass().getName()
                + " to image");
    }

}
