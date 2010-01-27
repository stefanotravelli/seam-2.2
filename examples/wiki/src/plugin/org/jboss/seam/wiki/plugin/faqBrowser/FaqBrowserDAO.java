/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.plugin.faqBrowser;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.wiki.core.model.WikiDirectory;
import org.jboss.seam.wiki.core.model.WikiDocument;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Christian Bauer
 */
@Name("faqBrowserDAO")
@Scope(ScopeType.CONVERSATION)
public class FaqBrowserDAO implements Serializable {

    @In
    EntityManager restrictedEntityManager;

    public WikiDirectory findFaqRootDir(WikiDirectory startDir) {

        // This was a database query once... now it's in-memory iteration
        List<WikiDirectory> parents = new ArrayList();
        parents.add(startDir);
        parents.addAll(startDir.getParentsRecursive());
        WikiDirectory faqRootDir = null;
        // We need the highest level directory that has a document with a "faqBrowser" macro
        for (WikiDirectory parent : parents) {
            if (parent.getDefaultFile() == null) continue;
            if (((WikiDocument)parent.getDefaultFile()).getHeaderMacrosString().contains("faqBrowser")) {
                faqRootDir = parent;
                // Continue iterating, maybe we find a higher level directory with the macro
            }
        }
        return faqRootDir;
    }

}
