package org.jboss.seam.flex;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;


@Name("org.jboss.seam.flex.login")
@Install(false)
@FlexRemote(name="login")
public class FlexAuthenticationBridge
{
   private static final LogProvider log = Logging.getLogProvider(FlexAuthenticationBridge.class);

     @In Identity identity;
     @In Credentials credentials;
     
     public String login(String username, String password) {
         System.out.println("---");
         log.info("*LOGIN " + username + " " + password);
         credentials.setUsername(username);
         credentials.setPassword(password);
         
         String result = identity.login();
         
         log.info("*LOGIN RESULT " + result);
         return result;
     }
     
     public void logout() {
         log.info("*LOGOUT ");
         identity.logout();  
     }
}
