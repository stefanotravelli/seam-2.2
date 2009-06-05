package org.jboss.seam.debug.jsf;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import com.sun.faces.spi.SerializationProvider;


/**
 *  This serialization provider can be used by JSF when restoring the view.  It will check 
 *  the hot deploy classloader(s) for class definitions that cannot be found in the context 
 *  classloader.  It is only needed when using client side state saving with hotdeployable classes 
 *  in the view.  To enable, add the following to web.xml:
 *  
 *  <pre>
 *  <context-param>
 *      <param-name>com.sun.faces.serializationProvider</param-name>
 *      <param-value>org.jboss.seam.faces.SeamDebugSerializationProvider</param-value>
 *  </context-param>
 *  </pre>
 */
public class SeamDebugSerializationProvider 
    implements SerializationProvider 
{

    public ObjectInputStream createObjectInputStream(InputStream source) throws IOException {
        return new SeamObjectInputStream(source);
    }

    public ObjectOutputStream createObjectOutputStream(OutputStream destination) throws IOException {
        return new ObjectOutputStream(destination);
    }


    static class SeamObjectInputStream
        extends ObjectInputStream
    {
        private static final LogProvider log = Logging.getLogProvider(SeamObjectInputStream.class);
        
        public SeamObjectInputStream(InputStream source) 
            throws IOException 
        {
            super(source);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) 
            throws IOException, 
                   ClassNotFoundException
        {
            String className = desc.getName();
            
            try {
                return Class.forName(className,
                                     true,
                                     Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                Class found = lookupInHotDeployLoaders(className);
                if (found != null) {
                    return found;                    
                }
                // can't find it - rethrow the class not found exception              
                throw e;
            }
        }

        private Class lookupInHotDeployLoaders(String className) {
            log.debug("need to check hotdeploy classloaders to resolve " + className);
            
            // this code can be made tighter if we assume there is only one hotdeployable location
            Set<ClassLoader> loaders = new HashSet<ClassLoader>();
            for (String name: Init.instance().getHotDeployableComponents()) {
                ClassLoader loader = loaderForComponent(name);                

                // make sure we only try the loader once                   
                if (loaders.add(loader)) {
                    Class c = tryToLoadClass(loader,className);
                    if (c != null){
                        return c;
                    }
                }
            }
            
            return null;
        }

        private Class tryToLoadClass(ClassLoader loader, String className) {
            try {
                return loader.loadClass(className);
            } catch (Exception e) {
                log.debug("class not found in loader" + loader);
                return null;
            }
        }

        private ClassLoader loaderForComponent(String name) {
            Component component = (Component) Component.getInstance(name + ".component");
            if (component == null) {
                log.debug("Couldn't find component for " + name);
                return null;
            }
            
            return component.getBeanClass().getClassLoader();
        }

    }
}