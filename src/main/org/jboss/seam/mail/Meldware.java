package org.jboss.seam.mail;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.List;

import org.buni.meldware.mail.management.AdminTool;
import org.buni.meldware.mail.util.MMJMXUtil;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * Creates meldware users, mailboxes and aliases
 *
 */
@Name("org.jboss.seam.mail.meldware")
@Startup
@Scope(APPLICATION)
@BypassInterceptors
@Install(precedence=BUILT_IN, classDependencies="org.buni.meldware.mail.mailbox.Mailbox", value=false)
public class Meldware
{
   
   private List<MeldwareUser> users;
   
   private List<String> domains;
   
   private Log log = Logging.getLog(Meldware.class);
   
   @Create
   public void create()
   {
      // TODO Support domain creation as well.  Currently they are written out to file.
      
         log.debug("Creating users and mailboxes");
         //MailboxService ms = MMJMXUtil.getMBean("meldware.mail:type=MailboxManager,name=MailboxManager", MailboxService.class);
         AdminTool at = MMJMXUtil.getMBean("meldware.mail:type=MailServices,name=AdminTool", AdminTool.class);
         
         for (MeldwareUser meldwareUser : getUsers())
         {
            at.createUser(meldwareUser.getUsername(), meldwareUser.getPassword(), meldwareUser.getRoles());
            // TODO This won't work on AS 4.2
            /*Mailbox mbox = ms.createMailbox(meldwareUser.getUsername());
            for (String alias : meldwareUser.getAliases())
            {
               ms.createAlias(mbox.getId(), alias);
            }*/
            log.debug("Created #0 #1 #2", meldwareUser.isAdministrator() ? "administrator" : "user", meldwareUser.getUsername(), meldwareUser.getAliases() == null || meldwareUser.getAliases().size() == 0 ? "" : "with aliases " + meldwareUser.getAliases());
         }
      }
   
   public List<MeldwareUser> getUsers()
   {
      return users;
   }
   
   public void setUsers(List<MeldwareUser> users)
   {
      this.users = users;
   }
   
   public List<String> getDomains()
   {
      return domains;
   }
   
   public void setDomains(List<String> domains)
   {
      this.domains = domains;
   }

}
