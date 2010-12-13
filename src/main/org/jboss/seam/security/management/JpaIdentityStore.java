package org.jboss.seam.security.management;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.security.management.PasswordSalt;
import org.jboss.seam.annotations.security.management.RoleConditional;
import org.jboss.seam.annotations.security.management.RoleGroups;
import org.jboss.seam.annotations.security.management.RoleName;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserFirstName;
import org.jboss.seam.annotations.security.management.UserLastName;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.Role;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.seam.security.crypto.BinTools;
import org.jboss.seam.util.AnnotatedBeanProperty;
import org.jboss.seam.util.TypedBeanProperty;

/**
 * The default identity store implementation, uses JPA as its persistence mechanism.
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.identityStore")
@Install(precedence = BUILT_IN, value=false) 
@Scope(APPLICATION)
@BypassInterceptors
public class JpaIdentityStore implements IdentityStore, Serializable
{  
   public static final String AUTHENTICATED_USER = "org.jboss.seam.security.management.authenticatedUser";
   
   public static final String EVENT_USER_CREATED = "org.jboss.seam.security.management.userCreated";
   public static final String EVENT_PRE_PERSIST_USER = "org.jboss.seam.security.management.prePersistUser";
   public static final String EVENT_USER_AUTHENTICATED = "org.jboss.seam.security.management.userAuthenticated";
   
   public static final String EVENT_PRE_PERSIST_USER_ROLE = "org.jboss.seam.security.management.prePersistUserRole";
   
   private static final LogProvider log = Logging.getLogProvider(JpaIdentityStore.class);    
   
   protected FeatureSet featureSet;
   
   private ValueExpression<EntityManager> entityManager;  
   
   private Class userClass;
   private Class roleClass;   
   private Class xrefClass;
   private TypedBeanProperty xrefUserProperty;
   private TypedBeanProperty xrefRoleProperty;
   
   private AnnotatedBeanProperty<UserPrincipal> userPrincipalProperty;
   private AnnotatedBeanProperty<UserPassword> userPasswordProperty;
   private AnnotatedBeanProperty<PasswordSalt> passwordSaltProperty;
   private AnnotatedBeanProperty<UserRoles> userRolesProperty;
   private AnnotatedBeanProperty<UserEnabled> userEnabledProperty;
   private AnnotatedBeanProperty<UserFirstName> userFirstNameProperty;
   private AnnotatedBeanProperty<UserLastName> userLastNameProperty;   
   private AnnotatedBeanProperty<RoleName> roleNameProperty;
   private AnnotatedBeanProperty<RoleGroups> roleGroupsProperty;
   private AnnotatedBeanProperty<RoleConditional> roleConditionalProperty;
   
   public Set<Feature> getFeatures()
   {
      return featureSet.getFeatures();
   }
   
   public void setFeatures(Set<Feature> features)
   {
      featureSet = new FeatureSet(features);
   }
   
   public boolean supportsFeature(Feature feature)
   {
      return featureSet.supports(feature);
   }
   
   @Create
   public void init()
   {                  
      if (featureSet == null)
      {
         featureSet = new FeatureSet();
         featureSet.enableAll();
      }      
      
      if (entityManager == null)
      {
         entityManager = Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class);
      }      
      
      if (userClass == null)
      {
         log.error("Error in JpaIdentityStore configuration - userClass must be configured.");
         return;
      }    
      
      initProperties();   
   }
   
   private void initProperties()
   {
      userPrincipalProperty = new AnnotatedBeanProperty(userClass, UserPrincipal.class);
      userPasswordProperty = new AnnotatedBeanProperty(userClass, UserPassword.class);
      passwordSaltProperty = new AnnotatedBeanProperty(userClass, PasswordSalt.class);
      userRolesProperty = new AnnotatedBeanProperty(userClass, UserRoles.class);
      userEnabledProperty = new AnnotatedBeanProperty(userClass, UserEnabled.class);
      userFirstNameProperty = new AnnotatedBeanProperty(userClass, UserFirstName.class);
      userLastNameProperty = new AnnotatedBeanProperty(userClass, UserLastName.class);
             
      if (!userPrincipalProperty.isSet()) 
      {
         throw new IdentityManagementException("Invalid userClass " + userClass.getName() + 
               " - required annotation @UserPrincipal not found on any Field or Method.");
      }
      
      if (!userRolesProperty.isSet())
      {
         throw new IdentityManagementException("Invalid userClass " + userClass.getName() + 
         " - required annotation @UserRoles not found on any Field or Method.");         
      }      
      
      if (roleClass != null)
      {         
         roleNameProperty = new AnnotatedBeanProperty(roleClass, RoleName.class);
         roleGroupsProperty = new AnnotatedBeanProperty(roleClass, RoleGroups.class);
         roleConditionalProperty = new AnnotatedBeanProperty(roleClass, RoleConditional.class);
         
         if (!roleNameProperty.isSet())
         {
            throw new IdentityManagementException("Invalid roleClass " + roleClass.getName() + 
            " - required annotation @RoleName not found on any Field or Method.");         
         }         
                 
         Type type = userRolesProperty.getPropertyType();
         if (type instanceof ParameterizedType && 
               Collection.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()))
         {
            Type genType = Object.class;

            for (Type t : ((ParameterizedType) type).getActualTypeArguments())
            {
               genType = t;
               break;
            }                 
         
            // If the @UserRoles property isn't a collection of <roleClass>, then assume the relationship
            // is going through a cross-reference table            
            if (!genType.equals(roleClass))
            {
               xrefClass = (Class) genType;
               xrefUserProperty = new TypedBeanProperty(xrefClass, userClass);
               xrefRoleProperty = new TypedBeanProperty(xrefClass, roleClass);
               
               if (!xrefUserProperty.isSet())
               {
                  throw new IdentityManagementException("Error configuring JpaIdentityStore - it looks like " +
                        "you're using a cross-reference table, however the user property cannot be determined.");
               }
               
               if (!xrefRoleProperty.isSet())
               {
                  throw new IdentityManagementException("Error configuring JpaIdentityStore - it looks like " +
                  "you're using a cross-reference table, however the role property cannot be determined.");                  
               }
            }
         }
      }
   }
   
   public boolean createUser(String username, String password, String firstname, String lastname)
   {
      try
      {
         if (userClass == null)
         {
            throw new IdentityManagementException("Could not create account, userClass not set");
         }
         
         if (userExists(username))
         {
            throw new IdentityManagementException("Could not create account, already exists");
         }
         
         Object user = userClass.newInstance();

         userPrincipalProperty.setValue(user, username);

         if (userFirstNameProperty.isSet()) userFirstNameProperty.setValue(user, firstname);         
         if (userLastNameProperty.isSet()) userLastNameProperty.setValue(user, lastname);
         
         if (password == null)
         {
            if (userEnabledProperty.isSet()) userEnabledProperty.setValue(user, false);
         }
         else
         {  
            setUserPassword(user, password);
            if (userEnabledProperty.isSet()) userEnabledProperty.setValue(user, true);
         }
         
         if (Events.exists()) Events.instance().raiseEvent(EVENT_PRE_PERSIST_USER, user);
         
         persistEntity(user);
         
         if (Events.exists()) Events.instance().raiseEvent(EVENT_USER_CREATED, user);
         
         return true;
      }
      catch (Exception ex)
      {
         if (ex instanceof IdentityManagementException)
         {
            throw (IdentityManagementException) ex;
         }
         else
         {
            throw new IdentityManagementException("Could not create account", ex);
         }
      }      
   }
   
   protected void setUserPassword(Object user, String password)
   {
      if (passwordSaltProperty.isSet())
      {
         byte[] salt = generateUserSalt(user);               
         passwordSaltProperty.setValue(user, BinTools.bin2hex(salt));
         userPasswordProperty.setValue(user, generatePasswordHash(password, salt));
      }
      else
      {
         userPasswordProperty.setValue(user, generatePasswordHash(password, getUserAccountSalt(user)));
      }
   }
   
   /**
    * @deprecated Use JpaIdentityStore.generateRandomSalt(Object) instead
    */
   @Deprecated
   protected String getUserAccountSalt(Object user)
   {      
      // By default, we'll use the user's username as the password salt
      return userPrincipalProperty.getValue(user).toString();
   }
   
   /**
    * Generates a 64 bit random salt value
    */
   public byte[] generateUserSalt(Object user)
   {
      return PasswordHash.instance().generateRandomSalt();
   }
   
   public boolean createUser(String username, String password)
   {
      return createUser(username, password, null, null);
   }
   
   public boolean deleteUser(String name)
   {
      Object user = lookupUser(name);
      if (user == null) 
      {
         throw new NoSuchUserException("Could not delete, user '" + name + "' does not exist");
      }
      
      removeEntity(user);
      return true;
   }
   
   public boolean grantRole(String username, String role)
   {
      if (roleClass == null) return false;
      
      Object user = lookupUser(username);
      if (user == null)
      {
         if (userPasswordProperty.isSet())
         {
            // If no userPasswordProperty is set, it means that authentication is being performed
            // by another identity store and this one is just managing roles
            throw new NoSuchUserException("Could not grant role, no such user '" + username + "'");
         }
         else
         {
            // We need to create a new user object
            if (createUser(username, null))
            {
               user = lookupUser(username);
            }
            else
            {
               throw new IdentityManagementException(
                     "Could not grant role - user does not exist and an attempt to create the user failed.");
            }
         }
      }
      
      Object roleToGrant = lookupRole(role);
      if (roleToGrant == null)
      {
         throw new NoSuchRoleException("Could not grant role, role '" + role + "' does not exist");
      }
      
      Collection userRoles = (Collection) userRolesProperty.getValue(user); 
      if (userRoles == null)
      {
         Type propType = userRolesProperty.getPropertyType();
         Class collectionType;
         
         if (propType instanceof Class && Collection.class.isAssignableFrom((Class) propType))
         {
            collectionType = (Class) propType;
         }
         else if (propType instanceof ParameterizedType &&
                  Collection.class.isAssignableFrom((Class) ((ParameterizedType) propType).getRawType()))
         {
            collectionType = (Class) ((ParameterizedType) propType).getRawType();
         }
         else
         {
            throw new IllegalStateException("Could not determine collection type for user roles.");
         }
         
         // This should either be a Set, or a List...
         if (Set.class.isAssignableFrom(collectionType))
         {
            userRoles = new HashSet();
         }
         else if (List.class.isAssignableFrom(collectionType))
         {
            userRoles = new ArrayList();
         }
         
         userRolesProperty.setValue(user, userRoles);
      }
      else if (((Collection) userRolesProperty.getValue(user)).contains(roleToGrant))
      {
         return false;
      }

      if (xrefClass == null)
      {
         // If this is a Many-To-Many relationship, simply add the role 
         ((Collection) userRolesProperty.getValue(user)).add(roleToGrant);
      }
      else
      {
         // Otherwise we need to insert a cross-reference entity instance
         try
         {
            Object xref = xrefClass.newInstance();            
            xrefUserProperty.setValue(xref, user);
            xrefRoleProperty.setValue(xref, roleToGrant);
            
            Events.instance().raiseEvent(EVENT_PRE_PERSIST_USER_ROLE, xref);
            
            ((Collection) userRolesProperty.getValue(user)).add(mergeEntity(xref));
         }
         catch (Exception ex)
         {
            throw new IdentityManagementException("Error creating cross-reference role record.", ex);
         }
      }
      
      return true;
   }   
   
   public boolean revokeRole(String username, String role)
   {
      Object user = lookupUser(username);
      if (user == null)
      {
         throw new NoSuchUserException("Could not revoke role, no such user '" + username + "'");
      }
      
      Object roleToRevoke = lookupRole(role);
      if (roleToRevoke == null)
      {
         throw new NoSuchRoleException("Could not revoke role, role '" + role + "' does not exist");
      }      
             
      boolean success = false;
      
      if (xrefClass == null)
      {
         success = ((Collection) userRolesProperty.getValue(user)).remove(roleToRevoke);
      }
      else
      {
         Collection roles = ((Collection) userRolesProperty.getValue(user));

         for (Object xref : roles)
         {
            if (xrefRoleProperty.getValue(xref).equals(roleToRevoke))
            {
               success = roles.remove(xref);
               break;
            }
         }
      }

      return success;
   }
   
   public boolean addRoleToGroup(String role, String group)
   {
      if (!roleGroupsProperty.isSet()) return false;      
      
      Object targetRole = lookupRole(role);
      if (targetRole == null)
      {
         throw new NoSuchUserException("Could not add role to group, no such role '" + role + "'");
      }
      
      Object targetGroup = lookupRole(group);
      if (targetGroup == null)
      {
         throw new NoSuchRoleException("Could not grant role, group '" + group + "' does not exist");
      }
      
      Collection roleGroups = (Collection) roleGroupsProperty.getValue(targetRole); 
      if (roleGroups == null)
      {
         // This should either be a Set, or a List...
         Class rawType = null;
         if (roleGroupsProperty.getPropertyType() instanceof ParameterizedType)
         {
            rawType = (Class) ((ParameterizedType) roleGroupsProperty.getPropertyType()).getRawType();
         }
         else
         {
            return false;
         }                   
          
         if (Set.class.isAssignableFrom(rawType))
         {
            roleGroups = new HashSet();
         }
         else if (List.class.isAssignableFrom(rawType))
         {
            roleGroups = new ArrayList();
         }
         
         roleGroupsProperty.setValue(targetRole, roleGroups);
      }
      else if (((Collection) roleGroupsProperty.getValue(targetRole)).contains(targetGroup))
      {
         return false;
      }

      ((Collection) roleGroupsProperty.getValue(targetRole)).add(targetGroup);
      
      return true;
   }

   public boolean removeRoleFromGroup(String role, String group)
   {
      if (!roleGroupsProperty.isSet()) return false;
      
      Object roleToRemove = lookupRole(role);
      if (role == null)
      {
         throw new NoSuchUserException("Could not remove role from group, no such role '" + role + "'");
      }
      
      Object targetGroup = lookupRole(group);
      if (targetGroup == null)
      {
         throw new NoSuchRoleException("Could not remove role from group, no such group '" + group + "'");
      }      
       
      boolean success = ((Collection) roleGroupsProperty.getValue(roleToRemove)).remove(targetGroup);
      return success;
   }      
   
   public boolean createRole(String role)
   {
      try
      {
         if (roleClass == null)
         {
            throw new IdentityManagementException("Could not create role, roleClass not set");
         }
         
         if (roleExists(role))
         {
            throw new IdentityManagementException("Could not create role, already exists");
         }
         
         Object instance = roleClass.newInstance();         
         roleNameProperty.setValue(instance, role);         
         persistEntity(instance);
         
         return true;
      }
      catch (Exception ex)
      {
         if (ex instanceof IdentityManagementException)
         {
            throw (IdentityManagementException) ex;
         }
         else
         {
            throw new IdentityManagementException("Could not create role", ex);
         }
      }      
   }
   
   public boolean deleteRole(String role)
   {      
      Object roleToDelete = lookupRole(role);
      if (roleToDelete == null)
      {
         throw new NoSuchRoleException("Could not delete role, role '" + role + "' does not exist");
      }        
      
      if (xrefClass != null)
      {
         lookupEntityManager().createQuery("delete " + xrefClass.getName() + " where role = :role")
         .setParameter("role", roleToDelete)
         .executeUpdate();
      }
      else
      {
         List<String> users = listUserMembers(role);
         for (String user : users)
         {
            revokeRole(user, role);
         }
      }
      
      List<String> roles = listRoleMembers(role);
      for (String r : roles)
      {
         removeRoleFromGroup(r, role);
      }
            
      removeEntity(roleToDelete);
      return true;
   }
   
   public boolean enableUser(String name)
   {
      if (!userEnabledProperty.isSet())
      {
         log.debug("Can not enable user, no @UserEnabled property configured in userClass " + userClass.getName());
         return false;
      }
      
      Object user = lookupUser(name);
      if (user == null)
      {
         throw new NoSuchUserException("Could not enable user, user '" + name + "' does not exist");
      }
      
      // Can't enable an already-enabled user, return false
      if (((Boolean) userEnabledProperty.getValue(user)) == true)
      {
         return false;
      }
      
      userEnabledProperty.setValue(user, true);   
      return true;
   }
   
   public boolean disableUser(String name)
   {
      if (!userEnabledProperty.isSet())
      {
         log.debug("Can not disable user, no @UserEnabled property configured in userClass " + userClass.getName());
         return false;
      }
      
      Object user = lookupUser(name);
      if (user == null)
      {
         throw new NoSuchUserException("Could not disable user, user '" + name + "' does not exist");
      }
      
      // Can't disable an already-disabled user, return false
      if (((Boolean) userEnabledProperty.getValue(user)) == false)
      {
         return false;
      }          
      
      userEnabledProperty.setValue(user, false);     
      return true;
   }
   
   public boolean changePassword(String username, String password)
   {
      Object user = lookupUser(username);
      if (user == null)
      {
         throw new NoSuchUserException("Could not change password, user '" + username + "' does not exist");
      }
      
      setUserPassword(user, password);
      
      return true;
   }
   
   public boolean userExists(String name)
   {
      return lookupUser(name) != null;
   }
   
   public boolean roleExists(String name)
   {
      return lookupRole(name) != null;
   }
   
   public boolean isUserEnabled(String name)
   {
      Object user = lookupUser(name);
      return user != null && (!userEnabledProperty.isSet() || (((Boolean) userEnabledProperty.getValue(user))) == true);
   }
   
   public List<String> getGrantedRoles(String name)
   {
      Object user = lookupUser(name);
      if (user == null)
      {
         throw new NoSuchUserException("No such user '" + name + "'");      
      }

      List<String> roles = new ArrayList<String>();
      
      Collection userRoles = (Collection) userRolesProperty.getValue(user);
      if (userRoles != null)
      {
         for (Object role : userRoles)
         {
            if (xrefClass == null)
            {
               roles.add((String) roleNameProperty.getValue(role));
            }
            else
            {
               Object xref = roleNameProperty.getValue(role);
               Object userRole = xrefRoleProperty.getValue(xref);
               roles.add((String) roleNameProperty.getValue(userRole));
            }
         }
      }
      
      return roles;     
   }
   
   public List<String> getRoleGroups(String name)
   {
      Object role = lookupRole(name);
      if (role == null)
      {
         throw new NoSuchUserException("No such role '" + name + "'");
      }

      List<String> groups = new ArrayList<String>();
      
      if (roleGroupsProperty.isSet())
      {
         Collection roleGroups = (Collection) roleGroupsProperty.getValue(role);
         if (roleGroups != null)
         {
            for (Object group : roleGroups)
            {
               groups.add((String) roleNameProperty.getValue(group));
            }
         }
      }
      
      return groups;      
   }
   
   public List<String> getImpliedRoles(String name)
   {
      Object user = lookupUser(name);
      if (user == null) 
      {
         throw new NoSuchUserException("No such user '" + name + "'"); 
      }

      Set<String> roles = new HashSet<String>();
      Collection userRoles = (Collection) userRolesProperty.getValue(user);
      if (userRoles != null)
      {
         for (Object role : userRoles)
         {
            addRoleAndMemberships((String) roleNameProperty.getValue(role), roles);
         }
      }
      
      return new ArrayList<String>(roles);
   }
   
   private void addRoleAndMemberships(String role, Set<String> roles)
   {
      if (roles.add(role))
      {      
         Object instance = lookupRole(role);
         
         if (roleGroupsProperty.isSet())
         {
            Collection groups = (Collection) roleGroupsProperty.getValue(instance);
            
            if (groups != null)
            {
               for (Object group : groups)
               {
                  addRoleAndMemberships((String) roleNameProperty.getValue(group), roles);
               }
            }
         }
      }
   }
   
   public String generatePasswordHash(String password, byte[] salt)
   {
      if (passwordSaltProperty.isSet())
      {
         try
         {
            return PasswordHash.instance().createPasswordKey(password.toCharArray(), salt, 
                  userPasswordProperty.getAnnotation().iterations());
         }
         catch (GeneralSecurityException ex)
         {
            throw new IdentityManagementException("Exception generating password hash", ex);
         }
      }
      else
      {
         return generatePasswordHash(password, new String(salt));
      }
   }
   
   /**
    * 
    * @deprecated Use JpaIdentityStore.generatePasswordHash(String, byte[]) instead
    */
   @Deprecated
   protected String generatePasswordHash(String password, String salt)
   {    
      String algorithm = userPasswordProperty.getAnnotation().hash();
      
      if (algorithm == null || "".equals(algorithm))
      {
         if (salt == null || "".equals(salt))
         {
            return PasswordHash.instance().generateHash(password);
         }
         else
         {
            return PasswordHash.instance().generateSaltedHash(password, salt);
         }
      }
      else if ("none".equalsIgnoreCase(algorithm))
      {
         return password;
      }      
      else
      {
         if (salt == null || "".equals(salt))
         {
            return PasswordHash.instance().generateHash(password, algorithm);
         }
         else
         {
            return PasswordHash.instance().generateSaltedHash(password, salt, algorithm);
         }
      }
   }
   
   public boolean authenticate(String username, String password)
   {
      Object user = lookupUser(username);          
      if (user == null || (userEnabledProperty.isSet() && ((Boolean) userEnabledProperty.getValue(user) == false)))
      {
         return false;
      }
      
      String passwordHash = null;
      
      if (passwordSaltProperty.isSet())
      {
         String encodedSalt = (String) passwordSaltProperty.getValue(user);
         if (encodedSalt == null)
         {
            throw new IdentityManagementException("A @PasswordSalt property was found on entity " + user + 
                  ", but it contains no value");
         }
         
         passwordHash = generatePasswordHash(password, BinTools.hex2bin(encodedSalt));
      }
      else
      {
         passwordHash = generatePasswordHash(password, getUserAccountSalt(user));   
      }
      
       
      boolean success = passwordHash.equals(userPasswordProperty.getValue(user));
            
      if (success && Events.exists())
      {
         if (Contexts.isEventContextActive())
         {
            Contexts.getEventContext().set(AUTHENTICATED_USER, user);
         }
         
         Events.instance().raiseEvent(EVENT_USER_AUTHENTICATED, user);
      }
      
      return success;
   }
   
   @Observer(Identity.EVENT_POST_AUTHENTICATE)
   public void setUserAccountForSession()
   {
      if (Contexts.isEventContextActive() && Contexts.isSessionContextActive())
      {
         Contexts.getSessionContext().set(AUTHENTICATED_USER, 
               Contexts.getEventContext().get(AUTHENTICATED_USER));
      }
   }
   
   public Object lookupUser(String username)       
   {
      try
      {
         Object user = lookupEntityManager().createQuery(
            "select u from " + userClass.getName() + " u where " + userPrincipalProperty.getName() +
            " = :username")
            .setParameter("username", username)
            .getSingleResult();
         
         return user;
      }
      catch (NoResultException ex)
      {
         return null;        
      }      
   }
   
   public String getUserName(Object user)
   {
      return (String) userPrincipalProperty.getValue(user);
   }
   
   public String getRoleName(Object role)
   {
      return (String) roleNameProperty.getValue(role);
   }
   
   public boolean isRoleConditional(String role)
   {      
      return roleConditionalProperty.isSet() ? (Boolean) roleConditionalProperty.getValue(
            lookupRole(role)) : false;
   }
   
   public Object lookupRole(String role)       
   {
      try
      {
         Object value = lookupEntityManager().createQuery(
            "select r from " + roleClass.getName() + " r where " + roleNameProperty.getName() +
            " = :role")
            .setParameter("role", role)
            .getSingleResult();
         
         return value;
      }
      catch (NoResultException ex)
      {
         return null;        
      }
   }   
   
   public List<String> listUsers()
   {
      return lookupEntityManager().createQuery(
            "select u." + userPrincipalProperty.getName() + " from " + userClass.getName() + " u")
            .getResultList();      
   }
   
   public List<String> listUsers(String filter)
   {
      return lookupEntityManager().createQuery(
            "select u." + userPrincipalProperty.getName() + " from " + userClass.getName() + 
            " u where lower(" + userPrincipalProperty.getName() + ") like :username")
            .setParameter("username", "%" + (filter != null ? filter.toLowerCase() : "") + 
                  "%")
            .getResultList();
   }

   public List<String> listRoles()
   {     
      return lookupEntityManager().createQuery(
            "select r." + roleNameProperty.getName() + " from " + roleClass.getName() + " r").getResultList();
   }
   
   public List<Principal> listMembers(String role)
   {
      List<Principal> members = new ArrayList<Principal>();
      
      for (String user : listUserMembers(role))
      {
         members.add(new SimplePrincipal(user));
      }
      
      for (String roleName : listRoleMembers(role))
      {
         members.add(new Role(roleName));
      }
      
      return members;
   }
   
   private List<String> listUserMembers(String role)
   {      
      Object roleEntity = lookupRole(role);

      if (xrefClass == null)
      {      
         return lookupEntityManager().createQuery("select u." + userPrincipalProperty.getName() + 
               " from " + userClass.getName() + " u where :role member of u." + userRolesProperty.getName())
               .setParameter("role", roleEntity)
               .getResultList();
      }
      else
      {
         List xrefs = lookupEntityManager().createQuery("select x from " + xrefClass.getName() + " x where x." +
               xrefRoleProperty.getName() + " = :role")
               .setParameter("role", roleEntity)
               .getResultList();

         List<String> members = new ArrayList<String>();
         
         for (Object xref : xrefs)
         {
            Object user = xrefUserProperty.getValue(xref);
            members.add(userPrincipalProperty.getValue(user).toString());
         }
         
         return members;
      }
     
   }
   
   private List<String> listRoleMembers(String role)
   {                
      
      if (roleGroupsProperty.isSet())
      {
         Object roleEntity = lookupRole(role);                  
         
         return lookupEntityManager().createQuery("select r." + roleNameProperty.getName() +
               " from " + roleClass.getName() + " r where :role member of r." + roleGroupsProperty.getName())
               .setParameter("role", roleEntity)
               .getResultList();
      }
      
      return new ArrayList<String>();
   }
   
   public List<String> listGrantableRoles()
   {
      StringBuilder roleQuery = new StringBuilder();
      
      roleQuery.append("select r.");
      roleQuery.append(roleNameProperty.getName());
      roleQuery.append(" from ");
      roleQuery.append(roleClass.getName());
      roleQuery.append(" r");
      
      if (roleConditionalProperty.isSet())
      {
         roleQuery.append(" where r.");
         roleQuery.append(roleConditionalProperty.getName());
         roleQuery.append(" = false");
      }
      
      return lookupEntityManager().createQuery(roleQuery.toString()).getResultList();
   }
   
   protected void persistEntity(Object entity)
   {
      lookupEntityManager().persist(entity);
   }
   
   protected Object mergeEntity(Object entity)
   {
      return lookupEntityManager().merge(entity);
   }
   
   protected void removeEntity(Object entity)
   {
      lookupEntityManager().remove(entity);
   }
   
   public Class getUserClass()
   {
      return userClass;
   }
   
   public void setUserClass(Class userClass)
   {
      this.userClass = userClass;
   }   
   
   public Class getRoleClass()
   {
      return roleClass;
   }
   
   public void setRoleClass(Class roleClass)
   {
      this.roleClass = roleClass;
   }
   
   private EntityManager lookupEntityManager()
   {
      return entityManager.getValue();
   }
   
   public ValueExpression getEntityManager()
   {
      return entityManager;
   }
   
   public void setEntityManager(ValueExpression expression)
   {
      this.entityManager = expression;
   }      
}
