package org.jboss.seam.document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.Faces;

@Name("org.jboss.seam.document.documentStore")
@Scope(ScopeType.CONVERSATION)
@Install(precedence=Install.BUILT_IN)
public class DocumentStore 
    implements Serializable
{    
    public static final String DOCSTORE_BASE_URL = "/seam/docstore/document";

    private static final long serialVersionUID = -357154201942127711L;

    Map<String,DocumentData> dataStore = new HashMap<String,DocumentData>();   

    long nextId = 1;
    
 
    boolean useExtensions = false;
    String errorPage = null;
    
    public void setUseExtensions(boolean useExtensions) 
    {
        this.useExtensions = useExtensions;
    }
    
    public void setErrorPage(String errorPage) 
    {
        this.errorPage = errorPage;
    }
    
    public String getErrorPage() 
    {
        return errorPage;
    }
    
    public String newId() 
    {
        return String.valueOf(nextId++);
    }

    public void saveData(String id, DocumentData documentData) 
    {
        dataStore.put(id, documentData);
    }

    public boolean idIsValid(String id) 
    {
        return dataStore.get(id) != null;
    }
    
    public DocumentData getDocumentData(String id) 
    {
        return dataStore.get(id);
    }
    
    public static DocumentStore instance()
    {
       return (DocumentStore) Component.getInstance("org.jboss.seam.document.documentStore");
    }
  

    public String preferredUrlForContent(String baseName, String extension, String contentId) 
    {                
        return baseUrlForContent(baseName, extension) + "?docId=" + contentId;
    }

    protected String baseUrlForContent(String baseName, String extension) {
        if (useExtensions) {
            return baseName + "." + extension;
        } else { 
            FacesContext context = FacesContext.getCurrentInstance();
            ViewHandler handler = context.getApplication().getViewHandler();
            String url = handler.getActionURL(context, 
                    DOCSTORE_BASE_URL + Faces.getDefaultSuffix(context));
            return context.getExternalContext().encodeActionURL(url);
        }
    }

}
