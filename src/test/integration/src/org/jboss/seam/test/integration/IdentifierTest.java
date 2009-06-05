package org.jboss.seam.test.integration;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.jboss.seam.framework.EntityIdentifier;
import org.jboss.seam.framework.HibernateEntityIdentifier;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

/**
 * @author Pete Muir
 *
 */
public class IdentifierTest extends SeamTest
{

    @Test
    public void testEntityIdentifier() throws Exception
    {
        new ComponentTest()
        {

            @Override
            protected void testComponents() throws Exception
            {
                setValue("#{countryHome.instance.name}", "foo");
                invokeMethod("#{countryHome.persist}");
                Country country = (Country) getValue("#{countryHome.instance}");
                EntityManager entityManager = (EntityManager) getValue("#{countryHome.entityManager}");
                
                EntityIdentifier entityIdentifier = new EntityIdentifier(country, entityManager);
                assert "foo".equals(((Country) entityIdentifier.find(entityManager)).getName());
                EntityIdentifier entityIdentifier2 = new EntityIdentifier(country, entityManager);
                assert entityIdentifier.equals(entityIdentifier2);
            }
            
        }.run();
    }
    
    @Test
    public void testHibernateEntityIdentifier() throws Exception
    {
        new ComponentTest()
        {

            @Override
            protected void testComponents() throws Exception
            {
                setValue("#{countryHome.instance.name}", "foo");
                invokeMethod("#{countryHome.persist}");
                Country country = (Country) getValue("#{countryHome.instance}");
                Session session =  (Session) getValue("#{countryHome.entityManager.delegate}");
                
                HibernateEntityIdentifier hibernateEntityIdentifier = new HibernateEntityIdentifier(country, session);
                assert "foo".equals(((Country) hibernateEntityIdentifier.find(session)).getName());
                HibernateEntityIdentifier hibernateEntityIdentifier2 = new HibernateEntityIdentifier(country, session);
                assert hibernateEntityIdentifier.equals(hibernateEntityIdentifier2);
            }
            
        }.run();
    }
    
}
