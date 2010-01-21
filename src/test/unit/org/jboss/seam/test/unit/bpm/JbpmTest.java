package org.jboss.seam.test.unit.bpm;

import org.jboss.seam.bpm.Jbpm;
import org.jbpm.graph.def.ProcessDefinition;
import org.testng.annotations.Test;

/**
 * @author Marek Novotny
 *
 */
public class JbpmTest
{
   
   private static final String PROCESS_DEFINITION = "<process-definition name=\"test\"> " +
   		                                                "<start-state name=\"start\"> " +
   		                                                "   <transition to=\"decision\" /> " +
   		                                                "</start-state> " +
   		                                                "<decision name=\"decision\"> " +
   		                                                "   <transition to=\"done\" name=\"true\" />" +
   		                                                "   <transition to=\"done\" name=\"false\" /> " +
   		                                                "</decision> " +
   		                                                "<end-state name=\"done\"/> " +
   		                                           "</process-definition>";
   
   private static final String PAGE_FLOW_DEFINITION = "<pageflow-definition name=\"newuser\" >" +
                                                          "<start-state name=\"start\">" +
                                                          "   <transition to=\"account\"/>" +
                                                          "</start-state>" +
                                                          "<page name=\"account\" view-id=\"/newuser/account.xhtml\">" +
                                                          "    <redirect/>" +
                                                          "   <transition name=\"next\" to=\"checkPassword\" />" +
                                                          "</page>" + 
                                                      "</pageflow-definition>" ;
   
   @Test
   public void testGetProcessDefinitionFromXml()
   {

      Jbpm jbpm = new Jbpm(); 
      ProcessDefinition pd = jbpm.getProcessDefinitionFromXml(PROCESS_DEFINITION);
      assert "start".equals(pd.getStartState().getName());
      assert "test".equals(pd.getName());      
      
   }
   
   @Test
   public void testGetPageflowDefinitionFromXml()
   {

      Jbpm jbpm = new Jbpm(); 
      ProcessDefinition pd = jbpm.getPageflowDefinitionFromXml(PAGE_FLOW_DEFINITION);
      assert "start".equals(pd.getStartState().getName());
      assert "newuser".equals(pd.getName());
      
   }

}
