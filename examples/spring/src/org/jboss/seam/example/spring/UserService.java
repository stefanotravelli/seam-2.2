package org.jboss.seam.example.spring;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mike Youngstrom
 * @author Marek Novotny
 *
 */
public class UserService extends JpaDaoSupport {

	@Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        System.out.println("change password " + oldPassword + " to " + newPassword);
        if (newPassword == null || newPassword.length()==0) {
            throw new IllegalArgumentException("newPassword cannot be null.");
        }

        User user = findUser(username);
        System.out.println("USER" + user);
        if (user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            return true;
        } else {
            return false;
        }
    }

	@Transactional
    public User findUser(String username) {
        if (username == null || "".equals(username)) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        return getJpaTemplate().find(User.class, username);
    }

	@Transactional
    public User findUser(String username, String password) {
        try {
            List result = getJpaTemplate().find("select u from User u where u.username=?1 and u.password=?2", username, password);
            if (result.size() > 0)
            {
               return (User) result.get(0);   
            }
            else
            {
               return null;
            }
        } catch (DataAccessException e) {
            return null;
        }
    }

	@Transactional
    public void createUser(User user) throws ValidationException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        User existingUser = findUser(user.getUsername());
        if (existingUser != null) {
            throw new ValidationException("Username "+user.getUsername()+" already exists");
        }
        getJpaTemplate().persist(user);
        getJpaTemplate().flush();
    }
}
