package org.jboss.seam.cache;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.lang.reflect.Method;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Reflections;

/**
 * Implementation of CacheProvider backed by JBoss Cache 2.x. for simple
 * objects.
 * 
 * @author Sebastian Hennebrueder
 * @author Pete Muir
 */

@Name("org.jboss.seam.cache.cacheProvider")
@Scope(APPLICATION)
@BypassInterceptors
@Install(value = false, precedence = BUILT_IN, classDependencies = {"org.jboss.cache.Cache", "org.jgroups.MembershipListener"})
@AutoCreate
public class JbossCache2Provider 
    extends AbstractJBossCacheProvider<Cache> 
{

    private org.jboss.cache.Cache cache;

    private static final LogProvider log = Logging.getLogProvider(JbossCache2Provider.class);

    private static Method GET;
    private static Method PUT;
    private static Method REMOVE;
    private static Method REMOVE_NODE;

    static {
        try {
            GET = Cache.class.getDeclaredMethod("get", Fqn.class, Object.class);
            PUT = Cache.class.getDeclaredMethod("put", Fqn.class, Object.class, Object.class);
            REMOVE = Cache.class.getDeclaredMethod("remove", Fqn.class, Object.class);
            REMOVE_NODE = Cache.class.getDeclaredMethod("removeNode", Fqn.class);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to use JBoss Cache 2", e);
        }
    }

    @Create
    public void create() {
        log.debug("Starting JBoss Cache");

        try {
            CacheFactory factory = new DefaultCacheFactory();
            cache = factory.createCache(getConfigurationAsStream());

            cache.create();
            cache.start();
        } catch (Exception e) {
            //log.error(e, e);
            throw new IllegalStateException("Error starting JBoss Cache", e);
        }
    }

    @Destroy
    public void destroy() {
        log.debug("Stopping JBoss Cache");
        try {
            cache.stop();
            cache.destroy();
            cache = null;
        } catch (Exception e) {
            throw new IllegalStateException("Error stopping JBoss Cache", e);
        }
    }

    @Override
    public Object get(String region, String key) {
        return Reflections.invokeAndWrap(GET, cache, getFqn(region), key);
    }

    @Override
    public void put(String region, String key, Object object) {
        Reflections.invokeAndWrap(PUT, cache, getFqn(region), key, object);
    }

    @Override
    public void remove(String region, String key) {
        Reflections.invokeAndWrap(REMOVE, cache, getFqn(region), key);
    }

    @Override
    public void clear() {
        Reflections.invokeAndWrap(REMOVE_NODE, cache, getFqn(null));
    }

    @Override
    public Cache getDelegate() {
        return cache;
    }

}