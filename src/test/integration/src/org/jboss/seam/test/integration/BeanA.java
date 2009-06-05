package org.jboss.seam.test.integration;

import java.io.Serializable;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;

@Name("beanA")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class BeanA implements Serializable {

    private String myValue;

    public String getMyValue() {
        return myValue;
    }

    public void setMyValue(String myValue) {
        this.myValue = myValue;
    }

    @Create
    public void create() {
        myValue = "Foo";
    }

    @Observer(value = "BeanA.refreshMyValue")
    public void refreshMyValue() {
        myValue = "Bar";
        Events.instance().raiseEvent("BeanA.valueModified");
    }

}
