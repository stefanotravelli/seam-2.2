package org.jboss.seam.el;

import org.jboss.el.util.ReflectionUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;


@Name("org.jboss.seam.el.referenceCache")
@Scope(ScopeType.APPLICATION)
@Startup
public class JBossELReferenceCache {
    @Create
    public void start() {
        ReflectionUtil.startup();
    }
    
    @Destroy 
    public void stop() {
        ReflectionUtil.shutdown();
    }
}
