package org.jboss.seam.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("org.jboss.seam.core.ConversationIdGenerator")
@Scope(ScopeType.APPLICATION)
@Install(precedence=Install.BUILT_IN)
public class ConversationIdGenerator
{
    private static AtomicInteger uniqueId = new AtomicInteger(0);

    public String getNextId() {
        //TODO: this is not cluster safe!!!!!
        return Integer.toString(uniqueId.incrementAndGet());
    }   

    public static ConversationIdGenerator instance() {
        ConversationIdGenerator instance = 
            (ConversationIdGenerator) Component.getInstance("org.jboss.seam.core.ConversationIdGenerator");
        return (instance!=null) ? instance : new ConversationIdGenerator();
    }
}
