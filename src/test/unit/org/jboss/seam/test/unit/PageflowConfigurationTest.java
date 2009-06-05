package org.jboss.seam.test.unit;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.testng.annotations.Test;

public class PageflowConfigurationTest{

  static JbpmConfiguration pageflowConfiguration = JbpmConfiguration.parseResource("org/jbpm/pageflow/jbpm.pageflow.cfg.xml");
  
  @Test
  public void testOne() {
    JbpmContext jbpmContext = pageflowConfiguration.createJbpmContext();
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='navigation'>" +
      "  <start-state name='start'>" +
      "    <transition to='a' />" +
      "  </start-state>" +
      "  <state name='a'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    
    ProcessInstance processInstance = processDefinition.createProcessInstance();

    processInstance.signal();
    processInstance.signal();
    
    assert processInstance.hasEnded();
    
    jbpmContext.close();
  }

}
