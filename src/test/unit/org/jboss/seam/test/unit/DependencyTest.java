package org.jboss.seam.test.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.init.ComponentDescriptor;
import org.jboss.seam.init.DependencyManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DependencyTest {
    @Test
    public void testNoComponents() 
    {
        Assert.assertEquals(0, installSet().size());
    }
    
    @Test
    public void testNoDependencies() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        
        Set<ComponentDescriptor> installed = installSet(desc1);
        Assert.assertEquals(installed.size(), 1);        
    }
    
    
    @Test
    public void testNotInstalled() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setInstalled(false);

        Assert.assertEquals(installSet(desc1).size(), 0);        
    }
    
    
    @Test
    public void testOverride() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setPrecedence(Install.APPLICATION);
        MockDescriptor desc2 = new MockDescriptor("foo", SomeClass.class);
        desc2.setPrecedence(Install.DEPLOYMENT);
        MockDescriptor desc3 = new MockDescriptor("foo", SomeClass.class);
        desc3.setPrecedence(Install.BUILT_IN);      
                
        Set<ComponentDescriptor> installed = installSet(desc1, desc2, desc3);
        Assert.assertEquals(installed.size(), 1);                
        Assert.assertEquals(installed.iterator().next().getPrecedence(),Install.DEPLOYMENT);              
    }
    
    
    @Test
    public void testOverride2() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setDependencies(new String[] {"bar"});
        MockDescriptor desc2 = new MockDescriptor("bar", SomeClass.class);
        desc2.setPrecedence(Install.FRAMEWORK);
        MockDescriptor desc3 = new MockDescriptor("bar", SomeOtherClass.class);
        desc3.setPrecedence(Install.APPLICATION);
        desc3.setClassDependencies(new String[] {"SomeClassThatDoesntExist"});

                
        Set<ComponentDescriptor> installed = installSet(desc1, desc2, desc3);        
        Assert.assertEquals(installed.size(), 2);                
        Assert.assertTrue(installed.contains(desc1), "contains desc1");
        Assert.assertTrue(installed.contains(desc2), "contains desc2");
              
    }
    
    @Test
    public void testClassDependency() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setClassDependencies(new String[] {"SomeClassThatDoesntExist"});
                   
        Set<ComponentDescriptor> installed = installSet(desc1);
        Assert.assertEquals(installed.size(), 0);      
        
        desc1.setClassDependencies(new String[] {"SomeClassThatDoesntExist", SomeClass.class.getName()});       
        installed = installSet(desc1);
        Assert.assertEquals(installed.size(), 0);                 
        
        desc1.setClassDependencies(new String[] {SomeClass.class.getName()});       
        installed = installSet(desc1);
        Assert.assertEquals(installed.size(), 1);      
    }
    
    @Test
    public void testDependency() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setDependencies(new String[] {"bar"});
        MockDescriptor desc2 = new MockDescriptor("bar", SomeClass.class);
        desc2.setDependencies(new String[] {"baz"});
        MockDescriptor desc3 = new MockDescriptor("baz", SomeClass.class);
        
        Assert.assertEquals(installSet(desc1).size(), 0);    
        Assert.assertEquals(installSet(desc1,desc2).size(), 0);  
        Assert.assertEquals(installSet(desc1,desc2,desc3).size(), 3);           
    }
    
    @Test
    public void testCircularDependency() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setDependencies(new String[] {"bar"});
        MockDescriptor desc2 = new MockDescriptor("bar", SomeClass.class);
        desc2.setDependencies(new String[] {"foo"});
                
        Assert.assertEquals(installSet(desc1).size(), 0);    
        Assert.assertEquals(installSet(desc2).size(), 0);  
        Assert.assertEquals(installSet(desc1,desc2).size(), 2);     
        
        // just to make sure
        desc1.setDependencies(new String[] {"foo"});        
        Assert.assertEquals(installSet(desc1).size(), 1);  
    }
    
    
    @Test
    public void testComponentByClassDependency() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setGenericDependencies(new Class[] {SomeOtherClass.class});
        MockDescriptor desc2 = new MockDescriptor("bar", SomeOtherClass.class);
                
        Assert.assertEquals(installSet(desc1).size(), 0);        
        Assert.assertEquals(installSet(desc2).size(), 1);
        Assert.assertEquals(installSet(desc1,desc2).size(), 2);           
    }
    
    
    @Test
    public void testUnmetDueToOverride() {
        MockDescriptor desc1 = new MockDescriptor("foo", SomeClass.class);
        desc1.setGenericDependencies(new Class[] {SomeOtherClass.class});
        MockDescriptor desc2 = new MockDescriptor("bar", SomeOtherClass.class);
        desc2.setPrecedence(Install.FRAMEWORK);
        MockDescriptor desc3 = new MockDescriptor("bar", SomeUnrelatedClass.class);        
        
        Set<ComponentDescriptor> installed = installSet(desc1,desc2);
        Assert.assertEquals(installed.size(), 2);
                
        installed = installSet(desc1,desc2,desc3);        
        Assert.assertEquals(installed.size(), 1);
        Assert.assertEquals(installed.iterator().next().getName(), "bar");
        Assert.assertEquals(installed.iterator().next().getPrecedence(), Install.APPLICATION);      
    }
    // ------------------------------------------------------
    
    
    private Map<String,Set<ComponentDescriptor>> componentSet(ComponentDescriptor... descriptors) {
        Map<String,Set<ComponentDescriptor>> map = new HashMap<String, Set<ComponentDescriptor>>();
        
        for (ComponentDescriptor descriptor: descriptors) {
            addDependency(map, descriptor);
        }
        return map;        
    }
    
    private Set<ComponentDescriptor> installSet(ComponentDescriptor... descriptors) {
        DependencyManager manager = new DependencyManager(componentSet(descriptors));
        return manager.installedSet();
    }
    
    private void addDependency(Map<String,Set<ComponentDescriptor>> dependencies, 
                               ComponentDescriptor descriptor) 
    {
        Set<ComponentDescriptor> descriptors = dependencies.get(descriptor.getName());
        if (descriptors == null) 
        {
            descriptors = new TreeSet<ComponentDescriptor>(new ComponentDescriptor.PrecedenceComparator());
            dependencies.put(descriptor.getName(), descriptors);
        }
         
        descriptors.add(descriptor);
    }             
    
    static class MockDescriptor
        extends ComponentDescriptor 
    {        
        private String[] classDependencies;
        private String[] dependencies;
        private Class[] genericDependencies;

        public MockDescriptor(String name, Class<?> componentClass) {
            super(name, componentClass, ScopeType.SESSION);        
        }

        public void setInstalled(boolean installed) {
            this.installed = installed;
        }
        
        public void setPrecedence(int precedence) {
            this.precedence = precedence;
        }
        
        public void setClassDependencies(String[] classDependencies) {
            this.classDependencies = classDependencies;
        }

        @Override
        public String[] getClassDependencies() {
            return (classDependencies != null) ?   
                   classDependencies : super.getClassDependencies();
        }
        
        public void setDependencies(String[] dependencies) {
            this.dependencies = dependencies;
        }

        @Override
        public String[] getDependencies() {
            return (dependencies != null) ?   
                    dependencies : super.getDependencies();
        }

        public void setGenericDependencies(Class[] genericDependencies) {
            this.genericDependencies = genericDependencies;
        }
        
        @Override
        public Class[] getGenericDependencies() {
            return (genericDependencies != null) ?
                    genericDependencies : super.getGenericDependencies();
        }       
    }
    
    
    static class SomeClass {
    }    
    
    static class SomeOtherClass {
    }    
    
    static class SomeUnrelatedClass {        
    }
}
