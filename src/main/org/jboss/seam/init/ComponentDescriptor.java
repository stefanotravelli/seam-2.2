package org.jboss.seam.init;

import java.util.Comparator;

import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.core.Init;
import org.jboss.seam.security.permission.PermissionResolver;
import org.jboss.seam.web.AbstractResource;

/**
 * Meta-data about a Seam component.
 * 
 * @author Norman Richards
 *
 */
public class ComponentDescriptor implements Comparable<ComponentDescriptor>
{
    protected String name;
    protected Class<?> componentClass;
    protected ScopeType scope;
    protected String jndiName;
    protected Boolean installed;
    protected Boolean autoCreate;
    protected Boolean startup;
    protected String[] startupDepends;
    protected Integer precedence;

    /**
     * For components.xml
     */
    public ComponentDescriptor(String name, Class<?> componentClass, ScopeType scope,
            Boolean autoCreate, Boolean startup, String[] startupDepends, String jndiName, Boolean installed, Integer precedence)
    {
        this.name = name;
        this.componentClass = componentClass;
        this.scope = scope;
        this.jndiName = jndiName;
        this.installed = installed;
        this.autoCreate = autoCreate;
        this.precedence = precedence;
        this.startup = startup;
        this.startupDepends = startupDepends;
    }

    /**
     * For a scanned role
     */
    public ComponentDescriptor(String name, Class<?> componentClass, ScopeType scope)
    {
        this.name = name;
        this.componentClass = componentClass;
        this.scope = scope;
    }

    /**
     * For a scanned default role
     */
    public ComponentDescriptor(Class componentClass)
    {
        this.componentClass = componentClass;
    }

    /**
     * For built-ins with special rules
     */
    public ComponentDescriptor(Class componentClass, Boolean installed)
    {
        this.componentClass = componentClass;
        this.installed = installed;

    }

    public String getName()
    {
        return name == null ? Seam.getComponentName(componentClass) : name;
    }

    public ScopeType getScope()
    {
        return scope == null ? Seam.getComponentScope(componentClass) : scope;
    }

    public Class getComponentClass()
    {
        return componentClass;
    }

    public String getJndiName()
    {
        return jndiName;
    }
    
    public boolean isStartup()
    {
       return startup!=null ? startup : componentClass.isAnnotationPresent(Startup.class);
    }

    public boolean isAutoCreate()
    {
        return autoCreate!=null ? autoCreate : isAutoCreateAnnotationPresent();
    }

    private boolean isAutoCreateAnnotationPresent()
    {
        if (componentClass.isAnnotationPresent(AutoCreate.class)) {
           return true;
        }
   
        Package pkg = componentClass.getPackage();
        return pkg!=null && pkg.isAnnotationPresent(AutoCreate.class);
    }

    public String[] getStartupDependencies()
    {
        if (startupDepends != null && startupDepends.length > 0) {
           return startupDepends;
        }
        Startup startup = componentClass.getAnnotation(Startup.class);
        if (startup != null)
        {
            return startup.depends();
        }
        return new String[0];
    }

   public String[] getDependencies()
    {
        Install install = componentClass.getAnnotation(Install.class);
        if (install == null)
        {
            return null;
        }
        return install.dependencies();
    }

    public Class[] getGenericDependencies()
    {
        
        Install install = componentClass.getAnnotation(Install.class);
        if (install == null)
        {
            return null;
        }
        return install.genericDependencies();
    }

    public String[] getClassDependencies() 
    {
        Install install = componentClass.getAnnotation(Install.class);
        if (install == null)
        {
            return null;
        }
        return install.classDependencies();  
    }

    public boolean isInstalled()
    {
        if (installed != null)
        {
            return installed;
        }
        Install install = componentClass.getAnnotation(Install.class);
        if (install == null)
        {
            return true;
        }
        return install.debug() ? Init.instance().isDebug() : install.value();
    }

    public int getPrecedence()
    {
        if (precedence != null)
        {
            return precedence;
        }
        Install install = componentClass.getAnnotation(Install.class);
        if (install == null)
        {
            return Install.APPLICATION;
        }
        return install.precedence();
    }

    public int compareTo(ComponentDescriptor other)
    {
        return other.getPrecedence() - getPrecedence();
    }

    public boolean isFilter()
    {
        if (javax.servlet.Filter.class.isAssignableFrom(componentClass))
        {
           for (Class clazz = componentClass; !Object.class.equals(clazz); clazz = clazz.getSuperclass())
           {
              if (clazz.isAnnotationPresent(org.jboss.seam.annotations.web.Filter.class))
              {
                 return true;
              }
           }
        }
        return false;
    }

    public boolean isResourceProvider()
    {
        return AbstractResource.class.isAssignableFrom(componentClass);
    }
    
    public boolean isPermissionResolver()
    {
       return PermissionResolver.class.isAssignableFrom(componentClass);
    }
    
    @Override
    public String toString()
    {
        return "ComponentDescriptor(" + getName() + ":" + getComponentClass() + ')';
    }
            
    public static class PrecedenceComparator    
         implements Comparator<ComponentDescriptor>
    {               
        public int compare(ComponentDescriptor obj1, ComponentDescriptor obj2) 
        {        
            return obj2.getPrecedence() - obj1.getPrecedence();
        }
    }
}
