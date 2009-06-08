package org.jboss.seam.pdf.ui;

import org.jboss.seam.pdf.ITextUtils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import javax.faces.context.FacesContext;

public class UICell extends UIRectangle {
    public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UICell";

    PdfPCell cell;
    String horizontalAlignment;
    String verticalAlignment;
    Float padding;
    Float paddingLeft;
    Float paddingRight;
    Float paddingTop;
    Float paddingBottom;
    Boolean useBorderPadding;
    Float leading;
    Float multipliedLeading;
    Float indent;
    Float extraParagraphSpace;
    Float fixedHeight;
    Boolean noWrap;
    Float minimumHeight;
    Integer colspan;
    Float followingIndent;
    Float rightIndent;
    Integer spaceCharRatio;
    Integer runDirection;
    Integer arabicOptions;
    Boolean useAscender;
    Float grayFill;
    Integer rotation;

    boolean hasContent = false;

    public void setGrayFill(Float grayFill) {
        this.grayFill = grayFill;
    }

    public void setHorizontalAlignment(String horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public void setVerticalAlignment(String verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public void setPadding(Float padding) {
        this.padding = padding;
    }

    public void setPaddingLeft(Float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public void setPaddingRight(Float paddingRight) {
        this.paddingRight = paddingRight;
    }

    public void setPaddingTop(Float paddingTop) {
        this.paddingTop = paddingTop;
    }

    public void setPaddingBottom(Float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public void setUseBorderPadding(Boolean useBorderPadding) {
        this.useBorderPadding = useBorderPadding;
    }

    public void setLeading(Float leading) {
        this.leading = leading;
    }

    public void setMultipliedLeading(Float multipliedLeading) {
        this.multipliedLeading = multipliedLeading;
    }

    public void setIndent(Float indent) {
        this.indent = indent;
    }

    public void setExtraParagraphSpace(Float extraParagraphSpace) {
        this.extraParagraphSpace = extraParagraphSpace;
    }

    public void setFixedHeight(Float fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    public void setNoWrap(Boolean noWrap) {
        this.noWrap = noWrap;
    }

    public void setMinimumHeight(Float minimumHeight) {
        this.minimumHeight = minimumHeight;
    }

    public void setColspan(Integer colspan) {
        this.colspan = colspan;
    }

    public void setFollowingIndent(Float followingIndent) {
        this.followingIndent = followingIndent;
    }

    public void setRightIndent(Float rightIndent) {
        this.rightIndent = rightIndent;
    }

    public void setSpaceCharRatio(Integer spaceCharRatio) {
        this.spaceCharRatio = spaceCharRatio;
    }

    public void setRunDirection(Integer runDirection) {
        this.runDirection = runDirection;
    }

    public void setArabicOptions(Integer arabicOptions) {
        this.arabicOptions = arabicOptions;
    }

    public void setUseAscender(Boolean useAscender) {
        this.useAscender = useAscender;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    @Override
    public Object getITextObject() {
        return cell;
    }

    @Override
    public void removeITextObject() {
        cell = null;
    }

    @Override
    public void createITextObject(FacesContext context) {
        hasContent = false;
        PdfPCell defaultCell = getDefaultCellFromTable();
        if (defaultCell != null) {
            cell = new PdfPCell(defaultCell);
        } else {
            cell = new PdfPCell();
        }

        horizontalAlignment = (String) valueBinding(context,
                "horizontalAlignment", horizontalAlignment);
        if (horizontalAlignment != null) {
            cell.setHorizontalAlignment(ITextUtils
                    .alignmentValue(horizontalAlignment));            
        }
        
        verticalAlignment = (String) valueBinding(context, "verticalAlignment",
                verticalAlignment);
        if (verticalAlignment != null) {
            cell.setVerticalAlignment(ITextUtils
                    .alignmentValue(verticalAlignment));
        }

        padding = (Float) valueBinding(context, "padding", padding);
        if (padding != null) {
            cell.setPadding(padding);
        }

        paddingLeft = (Float) valueBinding(context, "paddingLeft", paddingLeft);
        if (paddingLeft != null) {
            cell.setPaddingLeft(paddingLeft);
        }

        paddingRight = (Float) valueBinding(context, "paddingRight",
                paddingRight);
        if (paddingRight != null) {
            cell.setPaddingRight(paddingRight);
        }

        paddingTop = (Float) valueBinding(context, "paddingTop", paddingTop);
        if (paddingTop != null) {
            cell.setPaddingTop(paddingTop);
        }

        paddingBottom = (Float) valueBinding(context, "paddingBottom",
                paddingBottom);
        if (paddingBottom != null) {
            cell.setPaddingBottom(paddingBottom);
        }

        useBorderPadding = (Boolean) valueBinding(context, "useBorderPadding",
                useBorderPadding);
        if (useBorderPadding != null) {
            cell.setUseBorderPadding(useBorderPadding);
        }

        leading = (Float) valueBinding(context, "leading", leading);
        multipliedLeading = (Float) valueBinding(context, "multipliedLeading",
                multipliedLeading);
        if (leading != null || multipliedLeading != null) {
            cell.setLeading(leading == null ? 0 : leading.floatValue(),
                    multipliedLeading == null ? 0 : multipliedLeading
                            .floatValue());
        }

        indent = (Float) valueBinding(context, "indent", indent);
        if (indent != null) {
            cell.setIndent(indent);
        }

        extraParagraphSpace = (Float) valueBinding(context,
                "extraParagraphSpace", extraParagraphSpace);
        if (extraParagraphSpace != null) {
            cell.setExtraParagraphSpace(extraParagraphSpace);
        }

        fixedHeight = (Float) valueBinding(context, "fixedHeight", fixedHeight);
        if (fixedHeight != null) {
            cell.setFixedHeight(fixedHeight);
        }

        noWrap = (Boolean) valueBinding(context, "noWrap", noWrap);
        if (noWrap != null) {
            cell.setNoWrap(noWrap);
        }

        minimumHeight = (Float) valueBinding(context, "minimumHeight",
                minimumHeight);
        if (minimumHeight != null) {
            cell.setMinimumHeight(minimumHeight);
        }

        colspan = (Integer) valueBinding(context, "colspan", colspan);
        if (colspan != null) {
            cell.setColspan(colspan);
        }

        followingIndent = (Float) valueBinding(context, "followingIndent",
                followingIndent);
        if (followingIndent != null) {
            cell.setFollowingIndent(followingIndent);
        }
        rightIndent = (Float) valueBinding(context, "rightIndent", rightIndent);
        if (rightIndent != null) {
            cell.setRightIndent(rightIndent);
        }
        spaceCharRatio = (Integer) valueBinding(context, "spaceCharRatio",
                spaceCharRatio);
        if (spaceCharRatio != null) {
            cell.setSpaceCharRatio(spaceCharRatio);
        }
        runDirection = (Integer) valueBinding(context, "runDirection",
                runDirection);
        if (runDirection != null) {
            cell.setRunDirection(runDirection);
        }
        arabicOptions = (Integer) valueBinding(context, "arabicOptions",
                arabicOptions);
        if (arabicOptions != null) {
            cell.setArabicOptions(arabicOptions);
        }
        useAscender = (Boolean) valueBinding(context, "useAscender",
                useAscender);
        if (useAscender != null) {
            cell.setUseAscender(useAscender);
        }

        grayFill = (Float) valueBinding(context, "grayFill", grayFill);
        if (grayFill != null) {
            cell.setGrayFill(grayFill);
        }

        rotation = (Integer) valueBinding(context, "rotation", rotation);
        if (rotation != null) {
            cell.setRotation(rotation);
        }

        applyRectangleProperties(context, cell);
    }

    private PdfPCell getDefaultCellFromTable() {
        UITable parentTable = (UITable) findITextParent(this, UITable.class);
        if (parentTable != null) {
            return parentTable.getDefaultCellFacet();
        }
        return null;
    }

    @Override
    public void handleAdd(Object o) {
        if (!hasContent) {
            if (o instanceof Image) {
                // added by user request, but it mages the logic here rather
                // ugly.
                cell.setImage((Image) o);
            } else if (o instanceof Phrase) {
                cell.setPhrase((Phrase) o);
            } else if (o instanceof Element) {
                cell.addElement((Element) o);
            } else {
                throw new RuntimeException("Can't add "
                        + o.getClass().getName() + " to cell");
            }

            hasContent = true;
        } else if (o instanceof Element) {
            if (cell.getImage() != null) {
                Image image = cell.getImage(); 
                cell.setImage(null);
                cell.addElement(image);
            }
            if (cell.getPhrase() != null) {
                Phrase p = cell.getPhrase();
                cell.setPhrase(null);
                cell.addElement(p);
            }
            cell.addElement((Element) o);
        } else {
            throw new RuntimeException("Can't add " + o.getClass().getName()
                    + " to cell");
        }
    }

}
