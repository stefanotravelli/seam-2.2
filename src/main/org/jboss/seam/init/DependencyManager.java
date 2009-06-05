package org.jboss.seam.init;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Evaluates component dependencies to determine which
 * components are installed.
 * 
 * @author Norman Richards
 *
 */
public class DependencyManager 
{
    private Map<String, Set<ComponentDescriptor>> componentDescriptors;
    private Set<ComponentDescriptor> currentTestSet;
    private Set<ComponentDescriptor> installedSet;    

    public DependencyManager(Map<String, Set<ComponentDescriptor>> componentDescriptors) 
    {
        this.componentDescriptors = new HashMap<String, Set<ComponentDescriptor>>(componentDescriptors);
    }
    
    public Set<ComponentDescriptor> installedSet()
    {
        computeInstallSet();
        return installedSet;        
    }
    
    private static final Comparator<ComponentDescriptor> ORDER = new Comparator<ComponentDescriptor>() 
    { 
       public int compare(ComponentDescriptor x, ComponentDescriptor y) 
       { 
          return x.getName().compareTo( y.getName() ); 
       } 
    };
        
    private void computeInstallSet() 
    {
        installedSet = new TreeSet<ComponentDescriptor>(ORDER);
        Set<String> keys = componentDescriptors.keySet();
        for (String key: keys) {
            currentTestSet = new HashSet<ComponentDescriptor>();            
            
            if (tryToInstall(key)) {
                installedSet.addAll(currentTestSet);
            }
            
            currentTestSet = null;            
        }        
    }
    
    private boolean tryToInstall(String key) 
    {
        Set<ComponentDescriptor> descriptors = componentDescriptors.get(key);
        if (descriptors == null) 
        {
            return false;
        }
        
        for (ComponentDescriptor descriptor : descriptors) 
        {
            
            Set<ComponentDescriptor> saved = new HashSet<ComponentDescriptor>(currentTestSet);
            if (tryToInstall(descriptor)) 
            {                
                return true;
            } 
            else 
            {               
                currentTestSet = saved;
            }
            
        }
        
        return false;
    }
    

    private boolean tryToInstall(ComponentDescriptor descriptor) 
    {        
        if (isInInstallSet(descriptor.getName())) 
        {            
            return true;
        }
        
        currentTestSet.add(descriptor);
        
        return checkAllDependencies(descriptor);
    }
    
    private boolean checkAllDependencies(ComponentDescriptor descriptor) 
    {
        return descriptor.isInstalled() &&
               checkClassDependencies(descriptor) &&
               checkComponentDependencies(descriptor) &&
               checkGenericDependencies(descriptor);     
    }
    
    
    private boolean checkComponentDependencies(ComponentDescriptor descriptor) 
    {
        String[] dependencies = descriptor.getDependencies();
        if (dependencies == null) 
        { 
           return true;
        }
        
        for (String componentName: dependencies) 
        {
            if (!tryToInstall(componentName)) 
            {
                return false;
            }
        }
        
        return true;
    }


    private boolean checkClassDependencies(ComponentDescriptor descriptor) 
    {
        String[] classDependencies = descriptor.getClassDependencies();
        
        if (classDependencies == null) 
        {
            return true;   
        }
            
        for (String className: classDependencies) 
        {   
            try 
            {   
                descriptor.getComponentClass().getClassLoader().loadClass(className);
            }
            catch (NoClassDefFoundError e)
            {
               return false;
            }
            catch (Exception e)
            {
                return false;                 
            }
        }

        return true;
    }
    
    private boolean checkGenericDependencies(ComponentDescriptor descriptor) 
    {
        Class[] dependencies = descriptor.getGenericDependencies();
        if (dependencies == null) 
        {
            return true;
        }
        
        for (Class dependency: dependencies) 
        {
            if (!isInInstallSet(dependency)) 
            {                                
                Set<String> searchList = findPotentialComponents(dependency);
                               
                if (!tryToSatisfyDependencyUsing(dependency, searchList)) 
                {
                    return false;
                }
            }
        }        

        return true;        
    }
    
    
    private boolean tryToSatisfyDependencyUsing(Class dependency, Set<String> searchList) 
    {
        for (String componentName:searchList) 
        {

            Set<ComponentDescriptor> saved = new HashSet<ComponentDescriptor>(currentTestSet);
            
            // the second check is important for edge case
            if (tryToInstall(componentName) && isInInstallSet(dependency)) 
            {
                return true;
            } 
            else 
            {                        
                currentTestSet = saved;
            }
        }
        return false;
    }


    private Set<String> findPotentialComponents(Class dependency) 
    {
        Set<String> keys = new HashSet<String>();
        
        for (String candidateKey: componentDescriptors.keySet()) 
        {            
            if (componentMightSatisfy(candidateKey, dependency)) 
            {
                keys.add(candidateKey);
            }
        }
        
        return keys;
    }

    private boolean componentMightSatisfy(String candidateKey, Class dependency) 
    {
        for (ComponentDescriptor descriptor: componentDescriptors.get(candidateKey)) 
        {            
            if (descriptor.getComponentClass().equals(dependency)) 
            {
                return true;
            }
        }
        return false;
    }


    private boolean isInInstallSet(Class dependency) 
    {
        for (ComponentDescriptor descriptor: currentTestSet) 
        {
            if (dependency.equals(descriptor.getComponentClass())) 
            {
                return true;
            }
        }
        
        for (ComponentDescriptor descriptor: installedSet) 
        {
            if (dependency.equals(descriptor.getComponentClass())) 
            {
                return true;
            }
        }

        return false;
    }


    // install set is already installed or the current working set
    private boolean isInInstallSet(String key) 
    {
        for (ComponentDescriptor descriptor: currentTestSet) 
        {
            if (key.equals(descriptor.getName())) 
            {
                return true;
            }
        }
        
        for (ComponentDescriptor descriptor: installedSet) 
        {
            if (key.equals(descriptor.getName())) 
            {
                return true;
            }
        }
        
        return false;
    }

}
