package org.jboss.seam.web;


/**
 * functions related to the faces servlet mapping and the translation from viewId to URL and back
 */
public class ServletMapping {
    String mapping;
    
    public ServletMapping(String mapping) {
        this.mapping = mapping;
    }
    

    public String mapViewIdToURL(String viewId) {
        if (mapping.endsWith("/*")) {
            return mapping.substring(0,mapping.length()-2) + viewId;
        
        } else if (mapping.startsWith("*.")){
            int pos = viewId.lastIndexOf(".");
            if (pos != -1) {
                return viewId.substring(0, pos) + mapping.substring(1);
            }
        }
        
        return null;
    }
    
    private String stripExtension(String text) {
        int pos = text.lastIndexOf('.');
        return (pos == -1) ? null : text.substring(0,pos);
    }
    
    // this method should really be one that converts the baseURL to the viewId,
    // but we need default faces extension for that
    public boolean isMapped(String baseURL, String viewId)
    {
        if (mapping.startsWith("*.")) {
            String baseValue = stripExtension(baseURL);
            String viewValue = stripExtension(viewId);
           
            return baseValue!=null && viewValue!=null && baseValue.equals(viewValue);
        } else if (mapping.endsWith("/*")) {
            String prefix = mapping.substring(0,mapping.length()-2) + viewId;
            return baseURL.equals(prefix);
        }
        
        return false;        
    }

}
