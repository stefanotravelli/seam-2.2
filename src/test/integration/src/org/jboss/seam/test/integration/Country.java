package org.jboss.seam.test.integration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Pete Muir
 *
 */
@Entity
public class Country
{

    @Id @GeneratedValue
    private Integer id;
    
    private String name;

    /**
     * @return the id
     */
    public Integer getId()
    {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    
    
}
