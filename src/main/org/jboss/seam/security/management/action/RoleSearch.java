package org.jboss.seam.security.management.action;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.security.management.IdentityManager;

@Name("org.jboss.seam.security.management.roleSearch")
@Scope(SESSION)
@Install(precedence = BUILT_IN)
public class RoleSearch implements Serializable
{
   @DataModel
   List<String> roles;
   
   @DataModelSelection
   String selectedRole;
   
   @In IdentityManager identityManager;
   
   public void loadRoles()
   {
      roles = identityManager.listRoles();     
   }
   
   public String getRoleGroups(String role)
   {
      List<String> roles = identityManager.getRoleGroups(role);
      
      if (roles == null) return "";
      
      StringBuilder sb = new StringBuilder();
      
      for (String r : roles)
      {
         sb.append((sb.length() > 0 ? ", " : "") + r); 
      }
      
      return sb.toString();      
   }
   
   public String getSelectedRole()
   {
      return selectedRole;
   }
}