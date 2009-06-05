package org.jboss.seam.test.integration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Thing {
    public Thing() {}

    @GeneratedValue
    @Id
    private Long id;

    @Version
    private Integer version;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }   

    @Override
    public String toString() {
        return "Thing#" + id + "[v=" + version + "]";
    }
}