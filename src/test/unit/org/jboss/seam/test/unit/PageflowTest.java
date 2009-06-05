package org.jboss.seam.test.unit;

import java.io.StringReader;

import org.jboss.seam.bpm.PageflowParser;
import org.jboss.seam.pageflow.Page;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.node.StartState;
import org.testng.annotations.Test;

public class PageflowTest 
{
  
  JbpmConfiguration jbpmConfiguration = JbpmConfiguration.parseXmlString(
    "<jbpm-configuration />"
  );

  @Test
  public void testPageflowWithStartState() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    
    StringReader stringReader = new StringReader(
      "<pageflow-definition name='hoepla'>" +
      "  <start-state name='start' />" +
      "</pageflow-definition>"
    );
    PageflowParser pageflowParser = new PageflowParser(stringReader);
    ProcessDefinition processDefinition = pageflowParser.readProcessDefinition();
    assert "start".equals(processDefinition.getStartState().getName());
    
    jbpmContext.close();
  }
  
  @Test
  public void testPageflowWithStartPage() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();    
    StringReader stringReader = new StringReader(
    "<pageflow-definition name='hoepla'>" +
    "  <start-page name='start' view-id='/start.xhtml'/>" +
    "</pageflow-definition>");
    PageflowParser pageflowParser = new PageflowParser(stringReader);
    ProcessDefinition processDefinition = pageflowParser.readProcessDefinition();
    
    assert "start".equals(processDefinition.getStartState().getName());
    
    jbpmContext.close();
  }
  
  @Test
  public void testPageflowWithStartPageAttribute() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    
    StringReader stringReader = new StringReader(
      "<pageflow-definition name='hoepla' start-page='start'>" +
      "  <page name='start' view-id='/start.xhtml'/>" +
      "</pageflow-definition>"
    );
    PageflowParser pageflowParser = new PageflowParser(stringReader);
    ProcessDefinition processDefinition = pageflowParser.readProcessDefinition();
    assert "start".equals(processDefinition.getStartState().getName());
    
    jbpmContext.close();
  }
  
  @Test
  public void testOrderPageflow() {
     StringReader stringReader = new StringReader(
      "<pageflow-definition name='checkout'>" +
      "  <start-state name='start'>" +
      "    <transition to='confirm'/>" +
      "  </start-state>" +
      "  <page name='confirm' view-id='/confirm.xhtml'>" +
      "    <redirect/>" +
      "    <transition name='update'   to='continue'/>" + 
      "    <transition name='purchase' to='complete'>" +
      "      <action expression='#{checkout.submitOrder}' />" +
      "    </transition>" +
      "  </page>" +
      "  <page name='complete' view-id='/complete.xhtml'>" +
      "    <redirect/>" +
      "    <end-conversation/>" +
      "  </page>" +    
      "  <page name='continue' view-id='/browse.xhtml'>" +
      "    <end-conversation/>" +
      "  </page>" +
      "</pageflow-definition>"
    );
    PageflowParser pageflowParser = new PageflowParser(stringReader);
    ProcessDefinition processDefinition = pageflowParser.readProcessDefinition();
    
    StartState start = (StartState) processDefinition.getStartState();
    Page confirm = (Page) processDefinition.getNode("confirm");
    Page complete = (Page) processDefinition.getNode("complete");
    Page cont = (Page) processDefinition.getNode("continue");
    assert confirm!=null;
    assert complete!=null;
    assert cont!=null;
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    Token token = processInstance.getRootToken();
    assert start.equals(token.getNode());
    
    processInstance.signal();
  }
  
}
