package org.jboss.seam.faces;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;

/**
 * A component for direct rendering of
 * templates. Especially useful with
 * Seam Mail. 
 *
 */
@Name("org.jboss.seam.faces.renderer")
@Install(false)
public abstract class Renderer
{
    public abstract String render(String viewId);
    
    public static Renderer instance()
    {
        return (Renderer) Component.getInstance(Renderer.class);
    }
}
