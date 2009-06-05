//$Id$
//Contributed by Markus Meissner
package org.jboss.seam.util;

import java.io.InputStream;
import java.io.Serializable;

import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * An {@link EntityResolver} implementation which attempts to resolve
 * various systemId URLs to local classpath lookups<ol>
 * <li>Any systemId URL beginning with <tt>http://jboss.com/products/seam/</tt> is
 * searched for as a classpath resource in the classloader which loaded the
 * Seam classes.</li>
 * <li>Any systemId URL using <tt>classpath</tt> as the scheme (i.e. starting
 * with <tt>classpath://</tt> is searched for as a classpath resource using first
 * the current thread context classloader and then the classloader which loaded
 * the Seam classes.
 * </ol>
 * <p/>
 * Any entity references which cannot be resolved in relation to the above
 * rules result in returning null, which should force the SAX reader to
 * handle the entity reference in its default manner.
 */
public class DTDEntityResolver implements EntityResolver, Serializable 
{

    private static final long serialVersionUID = -4553926061006790714L;

    private static final LogProvider log = Logging.getLogProvider(DTDEntityResolver.class);

    private static final String SEAM_NAMESPACE = "http://jboss.com/products/seam/";
    private static final String USER_NAMESPACE = "classpath://";

    public InputSource resolveEntity(String publicId, String systemId) 
    {
        if (systemId != null) {
            log.trace("trying to resolve system-id [" + systemId + "]");
            if (systemId.startsWith(SEAM_NAMESPACE)) {
                log.trace("recognized Seam namespace; attempting to resolve on classpath under org/jboss/seam/");
                String path = "org/jboss/seam/" + systemId.substring(SEAM_NAMESPACE.length());
                
                InputStream dtdStream = resolveInSeamNamespace(path);
                if (dtdStream == null)  {
                    log.warn("unable to locate [" + systemId + "] on classpath");
                } else {
                    log.debug("located [" + systemId + "] in classpath");
                    InputSource source = new InputSource(dtdStream);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            } else if (systemId.startsWith(USER_NAMESPACE)) {
                log.trace("recognized local namespace; attempting to resolve on classpath");
                String path = systemId.substring(USER_NAMESPACE.length());
                
                InputStream stream = resolveInLocalNamespace(path);
                if (stream == null) {
                    log.warn("unable to locate [" + systemId + "] on classpath");                
                } else {
                    log.debug("located [" + systemId + "] in classpath");
                    InputSource source = new InputSource(stream);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            }
        }
        // use default behavior
        return null;
    }

    protected InputStream resolveInSeamNamespace(String path) 
    {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    protected InputStream resolveInLocalNamespace(String path) 
    {
        try  {
            return Resources.getResourceAsStream(path, ServletLifecycle.getServletContext());
        } catch (Throwable t) {
            return null;
        }
    }
}
