package org.jboss.seam.theme;

import static org.jboss.seam.ScopeType.EVENT;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.*;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Interpolator;

/**
 * Factory for a Map of resources that may be used for skinning the user
 * interface.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Name("org.jboss.seam.theme.themeFactory")
@Install(precedence = BUILT_IN)
public class Theme {

    protected Map<String, String> createMap() {
        final java.util.ResourceBundle bundle = ThemeSelector.instance().getThemeResourceBundle();

        if (bundle == null) {
            return null;
        }

        return new AbstractMap<String, String>() {
            @Override
            public String get(Object key) {
                if (key instanceof String) {
                    String resourceKey = (String) key;

                    String resource;
                    try {
                        resource = bundle.getString(resourceKey);
                    } catch (MissingResourceException mre) {
                        return resourceKey;
                    }
                    if (resource == null) {
                        return resourceKey;
                    } else {
                        return Interpolator.instance().interpolate(resource);
                    }
                } else {
                    return null;
                }
            }

            @Override
            public Set<Map.Entry<String, String>> entrySet() {
                Set<Map.Entry<String, String>> entrySet = new HashSet<Map.Entry<String, String>>();
                
                Enumeration<String> keys = bundle.getKeys();

                while (keys.hasMoreElements()) {                
                    final String key = keys.nextElement();
                    
                    entrySet.add(new Map.Entry<String, String>() {
                        
                        public String getKey() {
                            return key;
                        }

                        public String getValue() {
                            return get(key);
                        }

                        public String setValue(String arg0) {
                            throw new UnsupportedOperationException("not implemented");
                        }
                    });
                }
                   
                return entrySet;
            }

        };

    }

    /**
     * Create a Map in the event scope. When the theme is changed, ThemeSelector
     * is responsible for removing the Map from the event context.
     * 
     */
    @Factory(value = "org.jboss.seam.theme.theme", autoCreate = true, scope = EVENT)
    public java.util.Map getTheme() {
        return createMap();
    }

    public static java.util.Map instance() {
        return (java.util.Map) Component.getInstance("org.jboss.seam.theme.theme", true);
    }
}
