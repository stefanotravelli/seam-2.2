package org.jboss.seam.wiki.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**   
 * Stores user reports of comment spam
 * 
 * @author Shane Bryzak
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "WIKI_SPAM_REPORT")
public class WikiSpamReport implements Serializable
{   
   private static final long serialVersionUID = -5259814434570251579L;
   
   private Long reportId;
   private WikiComment comment;
   private User reporter;
   
   @Id
   @GeneratedValue(generator = "wikiSequenceGenerator")
   @Column(name = "REPORT_ID")
   public Long getReportId()
   {
      return reportId;
   }
   
   public void setReportId(Long reportId)
   {
      this.reportId = reportId;
   }
   
   @ManyToOne
   @JoinColumn(name = "COMMENT_ID")
   public WikiComment getComment()
   {
      return comment;
   }
   
   public void setComment(WikiComment comment)
   {
      this.comment = comment;
   }
   
   @ManyToOne
   @JoinColumn(name = "REPORTER_USER_ID")
   public User getReporter()
   {
      return reporter;
   }
   
   public void setReporter(User reporter)
   {
      this.reporter = reporter;
   }
}
