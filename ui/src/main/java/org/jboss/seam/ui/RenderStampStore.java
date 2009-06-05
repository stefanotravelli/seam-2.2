package org.jboss.seam.ui;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * A class that stores render stamps for use with &lt;s:token&gt; when client side
 * state saving is in use. By default the render stamp store will never remove a
 * render stamp unless instructed to by a UIToken. If the maxSize property is
 * larger than zero then it will control the maximum number of tokens stored,
 * with the oldest token being removed when a token is inserted that will take
 * the store over the maxSize limit. The default maxSize is 100.
 * 
 * @author Stuart Douglas
 */
@Name("org.jboss.seam.ui.renderStampStore")
@Scope(SESSION)
@Install(precedence = BUILT_IN, value = false)
@AutoCreate
@BypassInterceptors
public class RenderStampStore implements Serializable {

    class RenderStamp {
        String stamp;
        Date timeStamp;
    }

    int maxSize = 100;

    Map<String, RenderStamp> store = new ConcurrentHashMap<String, RenderStamp>();

    /**
     * Stores a stamp in the store, and returns the key it is stored under.
     */
    public String storeStamp(String stamp) {
        if (maxSize > 0) {
            if (store.size() == maxSize) {
                Date oldest = null;
                String oldestSigniture = null;
                for (String sig : store.keySet()) {
                    RenderStamp s = store.get(sig);
                    if (oldest == null || s.timeStamp.before(oldest)) {
                        oldestSigniture = sig;
                    }
                }
                store.remove(oldestSigniture);
            }
        }
        RenderStamp s = new RenderStamp();
        s.stamp = stamp;
        s.timeStamp = new Date();
        String key;
        do {
           key = UUID.randomUUID().toString();
        } while (!store.containsKey(key));
        store.put(key, s);
        return key;
    }

    public void removeStamp(String viewSigniture) {
        store.remove(viewSigniture);
    }

    public String getStamp(String viewSigniture) {
        RenderStamp s = store.get(viewSigniture);
        if (s != null) {
            return store.get(viewSigniture).stamp;
        }
        return null;
    }

    public static RenderStampStore instance() {
        return (RenderStampStore) Component.getInstance(RenderStampStore.class);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
