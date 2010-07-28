package org.jboss.seam.wiki.core.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.wiki.core.model.WikiComment;
import org.jboss.seam.wiki.core.model.WikiSpamReport;

@Name("spamReportDAO")
@AutoCreate
public class SpamReportDAO
{
   @Logger static Log log;

   @In protected EntityManager restrictedEntityManager;
   
   
   public List<WikiSpamReport> findReports(WikiComment comment) {
      if (comment == null || comment.getId() == null) throw new IllegalArgumentException("comment is null or unsaved");
      
      return restrictedEntityManager
              .createQuery(
                  "select distinct r from WikiSpamReport r " +
                  " where r.comment = :comment"
              )
              .setParameter("comment", comment)
              .getResultList();
   }
   
   public void removeReports(List<WikiSpamReport> reports)
   {
      for (WikiSpamReport report : reports)
      {
         restrictedEntityManager.remove(report);
      }
   }
}
