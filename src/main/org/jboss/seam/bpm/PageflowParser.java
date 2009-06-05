package org.jboss.seam.bpm;

import java.io.Reader;

import org.dom4j.Element;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.NodeCollection;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.ProblemListener;
import org.xml.sax.InputSource;

/**
 * A jPDL parser for Seam pageflow definitions
 * 
 * @author Tom Baeyens
 *
 */
public class PageflowParser extends JpdlXmlReader 
{

  private static final long serialVersionUID = 1L;

  public PageflowParser(InputSource inputSource, ProblemListener problemListener) 
  {
     super(inputSource, problemListener);
  }

  public PageflowParser(InputSource inputSource) 
  {
     super(inputSource);
  }

  public PageflowParser(Reader reader) 
  {
     super(reader);
  }
  
  @Override
  public void readNodes(Element nodeCollectionElement, NodeCollection nodeCollection) 
  {
     super.readNodes(nodeCollectionElement, nodeCollection);
    
     if ( "pageflow-definition".equals( nodeCollectionElement.getName() ) ) 
     {
        String startPageName = nodeCollectionElement.attributeValue("start-page");
        if (startPageName==null) 
        {
           Element startPageElement = nodeCollectionElement.element("start-page");
           if (startPageElement!=null) 
           {
              startPageName = startPageElement.attributeValue("name");
           }
        }
        if (startPageName!=null) 
        {
           Node startPage = getProcessDefinition().getNode(startPageName);
           if (startPage!=null) 
           {
              getProcessDefinition().setStartState(startPage);
           }
        }
     }
  }
  
}
