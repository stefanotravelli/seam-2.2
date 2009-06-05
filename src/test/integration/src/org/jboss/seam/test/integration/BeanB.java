package org.jboss.seam.test.integration;

import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

@Name("beanB")
@Scope(ScopeType.PAGE)
@AutoCreate
public class BeanB implements Serializable {

    private String myValue;

    public String getMyValue() {
        return myValue;
    }

    public void setMyValue(String myValue) {
        this.myValue = myValue;
    }

    @Observer(value = "BeanA.valueModified")
    public void takeValueFromBeanA() {
        BeanA beanA = (BeanA) Component.getInstance("beanA");
        myValue = beanA.getMyValue();
    }

}