package org.jboss.seam.bpm;

import static org.hibernate.criterion.Order.asc;
import static org.hibernate.criterion.Order.desc;
import static org.hibernate.criterion.Restrictions.isNotNull;
import static org.hibernate.criterion.Restrictions.isNull;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * Support for the process list.
 * 
 * @author Gavin King
 */
@Name("org.jboss.seam.bpm.processInstanceFinder")
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class ProcessInstanceFinder
{
   
   private String processDefinitionName;
   private String nodeName;
   private Boolean processInstanceEnded = false;
   private Boolean sortDescending = false;
   
   @Factory(value="org.jboss.seam.bpm.processInstanceList", autoCreate=true)
   @Transactional
   public List<ProcessInstance> getProcessInstanceList()
   {
      Criteria query = ManagedJbpmContext.instance().getSession()
                  .createCriteria(ProcessInstance.class);
      if ( processInstanceEnded!=null )
      {
         query.add( processInstanceEnded ? isNotNull("end") : isNull("end") );
      }
      
      if (processDefinitionName!=null)
      {
         query.createCriteria("processDefinition")
               .add( Restrictions.eq("name", processDefinitionName) );
      }
      
      query = query.createCriteria("rootToken");
      if (sortDescending!=null)
      {
         query.addOrder( sortDescending ? desc("nodeEnter") : asc("nodeEnter") );
      }
      if (nodeName!=null)
      {
         query.createCriteria("node")
               .add( Restrictions.eq("name", nodeName) );
      }
      
      return query.list();
   }

   protected String getNodeName()
   {
      return nodeName;
   }

   protected void setNodeName(String nodeName)
   {
      this.nodeName = nodeName;
   }

   protected String getProcessDefinitionName()
   {
      return processDefinitionName;
   }

   protected void setProcessDefinitionName(String processDefinitionName)
   {
      this.processDefinitionName = processDefinitionName;
   }

   protected Boolean isSortDescending()
   {
      return sortDescending;
   }

   protected void setSortDescending(Boolean sortDescending)
   {
      this.sortDescending = sortDescending;
   }

   protected Boolean getProcessInstanceEnded()
   {
      return processInstanceEnded;
   }

   protected void setProcessInstanceEnded(Boolean ended)
   {
      this.processInstanceEnded = ended;
   }
   
}
