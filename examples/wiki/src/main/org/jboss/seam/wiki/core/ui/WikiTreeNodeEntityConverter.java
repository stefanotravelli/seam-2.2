package org.jboss.seam.wiki.core.ui;

import org.jboss.seam.ui.EntityConverter;
import org.jboss.seam.wiki.core.model.WikiNode;
import org.jboss.seam.wiki.core.model.WikiTreeNode;

import javax.faces.convert.ConverterException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Making the broken JSF implementation happy.
 */
public class WikiTreeNodeEntityConverter extends EntityConverter {

    @Override
    public String getAsString(FacesContext facesContext,
                              UIComponent uiComponent, Object o)
            throws ConverterException {
        String result;
        if (o instanceof WikiTreeNode) {
            result = super.getAsString(facesContext, uiComponent, ((WikiTreeNode) o).getNode());
            return result;
        } else {
            throw new IllegalArgumentException("Can not convert: " + o);
        }
    }

    @Override
    public Object getAsObject(FacesContext facesContext,
                              UIComponent uiComponent, String s)
            throws ConverterException {
        Object o = super.getAsObject(facesContext, uiComponent, s);
        return new WikiTreeNode(0, (WikiNode) o);
    }
    
}
