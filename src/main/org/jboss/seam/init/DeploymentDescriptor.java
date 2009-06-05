package org.jboss.seam.init;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jboss.seam.ComponentType;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.XML;

/**
 * Parser for ejb-jar.xml and orm.xml deployment descriptors
 * 
 * @author Norman Richards
 *
 */
public class DeploymentDescriptor 
{ 
    private static final LogProvider log = Logging.getLogProvider(DeploymentDescriptor.class);
    
    private Map<Class, EjbDescriptor> ejbDescriptors = new HashMap<Class, EjbDescriptor>();
    private Class componentClass;

    public DeploymentDescriptor(Class clazz) 
    {
        componentClass = clazz;
        if (clazz.getClassLoader() == null) {
            return;
        }

        try 
        {
            InputStream ejbJarXml = clazz.getClassLoader().getResourceAsStream("META-INF/ejb-jar.xml");
            if (ejbJarXml!=null)
            {
               parseEjbJarXml( XML.getRootElementSafely(ejbJarXml) );
            }
        } 
        catch (DocumentException e) 
        {
            log.warn("Couldn't parse META-INF/ejb-jar.xml for component types " + e.getMessage());
        }

        try 
        {
            InputStream ormXml = clazz.getClassLoader().getResourceAsStream("META-INF/orm.xml");
            if (ormXml!=null)
            {
               parseOrmXml( XML.getRootElementSafely(ormXml) );
            }
        } 
        catch (DocumentException e) 
        {
            log.warn("Couldn't parse META-INF/orm.xml for component types " + e.getMessage());
        }
    }
    
    public Map<Class, EjbDescriptor> getEjbDescriptors()
    {
       return ejbDescriptors;
    }
    
    
    @SuppressWarnings("unchecked")
    private void parseEjbJarXml(Element root) 
    {

        Element beans = root.element("enterprise-beans");
        if (beans != null) 
        {
            for (Element bean: (List<Element>) beans.elements("session")) 
            {
                EjbDescriptor info = new EjbDescriptor();
                info.setEjbName(bean.element("ejb-name").getTextTrim());
                Element ejbClass = bean.element("ejb-class");
                if (ejbClass != null)
                {
                   info.setEjbClassName(ejbClass.getTextTrim());
                   Element sessionType = bean.element("session-type");
                   if (sessionType != null && sessionType.getTextTrim().equalsIgnoreCase("Stateful")) 
                   {
                       info.setBeanType(ComponentType.STATEFUL_SESSION_BEAN);            
                   } 
                   else 
                   {
                       info.setBeanType(ComponentType.STATELESS_SESSION_BEAN);     
                   }
                   add(info);
                }
            }          
            for (Element bean: (List<Element>) beans.elements("message-driven")) 
            {
                EjbDescriptor info = new EjbDescriptor();
                info.setEjbName(bean.element("ejb-name").getTextTrim());
                info.setEjbClassName(bean.element("ejb-class").getTextTrim());
                info.setBeanType(ComponentType.MESSAGE_DRIVEN_BEAN);
                add(info);
            }      
        }
    }
        
    @SuppressWarnings("unchecked")
    private void parseOrmXml(Element root) 
    {
        String packagePrefix = "";
        
        Element pkg = root.element("package");
        if (pkg!=null) 
        {
            packagePrefix = pkg.getTextTrim() + ".";
        }
                
        for (Element entity: (List<Element>) root.elements("entity")) 
        {
            String className = packagePrefix + entity.attribute("class").getText();
            EjbDescriptor info = new EjbDescriptor();
            info.setBeanType(ComponentType.ENTITY_BEAN);
            info.setEjbClassName(className);
            add(info);
        }
    }

    protected void add(EjbDescriptor descriptor) 
    {
       try
       {
          Class ejbClass = componentClass.getClassLoader().loadClass( descriptor.getEjbClassName() );
          ejbDescriptors.put(ejbClass, descriptor);
       }
       catch (ClassNotFoundException cnfe)
       {
          log.warn("Could not load EJB class: " + descriptor.getEjbClassName());
       }
    }
}
