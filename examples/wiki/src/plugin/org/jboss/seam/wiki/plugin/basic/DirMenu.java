/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.plugin.basic;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.wiki.core.dao.WikiNodeDAO;
import org.jboss.seam.wiki.core.model.WikiDirectory;
import org.jboss.seam.wiki.core.model.WikiTreeNode;
import org.jboss.seam.wiki.core.model.WikiNode;
import org.jboss.seam.wiki.core.plugin.WikiPluginMacro;
import org.jboss.seam.wiki.preferences.Preferences;

import java.io.Serializable;
import java.util.List;

/**
 * Menu tree, base is the current directory.
 *
 * @author Christian Bauer
 */
@Name("dirMenu")
@Scope(ScopeType.PAGE)
public class DirMenu implements Serializable {

    public static final String MACRO_ATTR_DIRMENUTREE = "dirMenuTree";

    @In
    WikiDirectory currentDirectory;

    public List<WikiTreeNode<WikiDirectory>> getTree(WikiPluginMacro macro) {
        // We cache the result in the macro, so that when the getter is called over and over during rendering, we have it
        if (macro.getAttributes().get(MACRO_ATTR_DIRMENUTREE) == null) {
            List<WikiTreeNode<WikiDirectory>> tree;
            DirMenuPreferences prefs  = Preferences.instance().get(DirMenuPreferences.class, macro);
            if (prefs.getOnlyMenuItems() != null && prefs.getOnlyMenuItems()) {
                tree = WikiNodeDAO.instance().findMenuItemTree(currentDirectory, 3l, 3l, false);
            } else {
                tree = WikiNodeDAO.instance().findWikiDirectoryTree(currentDirectory, WikiNode.SortableProperty.name, true);
            }
            macro.getAttributes().put(MACRO_ATTR_DIRMENUTREE, tree);
        }
        return (List<WikiTreeNode<WikiDirectory>>)macro.getAttributes().get(MACRO_ATTR_DIRMENUTREE);
    }

}
