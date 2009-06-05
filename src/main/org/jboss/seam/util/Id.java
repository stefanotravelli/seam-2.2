//$Id$
package org.jboss.seam.util;

import org.jboss.seam.core.ConversationIdGenerator;

public class Id
{
    @Deprecated
    public static String nextId() {
        return ConversationIdGenerator.instance().getNextId();
    }  
}
