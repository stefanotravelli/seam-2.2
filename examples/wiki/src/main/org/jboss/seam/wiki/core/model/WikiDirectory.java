package org.jboss.seam.wiki.core.model;

import org.hibernate.validator.Length;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Entity
@Table(name = "WIKI_DIRECTORY")
@org.hibernate.annotations.ForeignKey(name = "FK_WIKI_DIRECTORY_NODE_ID")
public class WikiDirectory extends WikiNode<WikiDirectory> implements Serializable {

    @Column(name = "DESCRIPTION", nullable = true)
    @Length(min = 0, max = 512)
    private String description;

    // This does not work, as usual. Hibernate just ignores it and gives me a proxy sometimes, leading to CCE later on
    // Maybe because I query directories with "from WikiNode where parentId", so the instrumentation of the WikiDirectory
    // subclass has no effect.
    //    @ManyToOne(fetch = FetchType.LAZY)
    //    @org.hibernate.annotations.LazyToOne(org.hibernate.annotations.LazyToOneOption.NO_PROXY)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEFAULT_FILE_ID", nullable = true, unique = true)
    @org.hibernate.annotations.ForeignKey(name = "FK_WIKI_DIRECTORY_DEFAULT_FILE_ID")
    private WikiFile defaultFile;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "directory", cascade = CascadeType.PERSIST)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.JOIN)
    private WikiFeed feed;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Careful calling this, it always returns the assigned File, even if
     * the user has a lower access level. Hibernate filters don't filter many-to-one
     * because if we have the id, we get the instance.
     *
     * @return WikiFile The assigned default starting file of this directory
     */
    public WikiFile getDefaultFile() { return defaultFile; }
    public void setDefaultFile(WikiFile defaultFile) { this.defaultFile = defaultFile; }

    public WikiFeed getFeed() { return feed; }
    public void setFeed(WikiFeed feed) { this.feed = feed; }

    public void flatCopy(WikiDirectory original, boolean copyLazyProperties) {
        super.flatCopy(original, copyLazyProperties);
        this.description = original.description;
    }

    public WikiDirectory duplicate(boolean copyLazyProperties) {
        WikiDirectory dupe = new WikiDirectory();
        dupe.flatCopy(this, copyLazyProperties);
        return dupe;
    }

    public String[] getPropertiesForGroupingInQueries() {
        return new String[]{
            "version", "parent", "rating",
            "areaNumber", "name", "wikiname", "createdBy", "createdOn", "messageId",
            "lastModifiedBy", "lastModifiedOn", "readAccessLevel", "writeAccessLevel", "writeProtected",
            "defaultFile", "description"
        };
    }

    public String[] getLazyPropertiesForGroupingInQueries() {
        return new String[0];
    }

    public String getPermURL(String suffix) {
        return getId() + suffix;
    }

    public String getWikiURL() {
        if (getArea() == null) return ""; // Wiki ROOT
        if (getArea().getWikiname().equals(getWikiname())) {
            return getArea().getWikiname();
        } else {
            return getArea().getWikiname() + "/" + getWikiname();
        }
    }

    public List<Long> getPathIdentifiers() {
        List<Long> pathIds = new ArrayList<Long>();
        WikiDirectory current = this;
        pathIds.add(current.getId());
        while (current.getParent() != null && getParent().isInstance(WikiDirectory.class)){
            current = (WikiDirectory)current.getParent();
            pathIds.add(current.getId());
        }
        Collections.reverse(pathIds);
        return pathIds;
    }

    public List<WikiDirectory> getPath() {
        List<WikiDirectory> path = new ArrayList<WikiDirectory>();
        WikiDirectory current = this;
        path.add(current);
        while (current.getParent() != null && getParent().isInstance(WikiDirectory.class)){
            current = (WikiDirectory)current.getParent();
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }


    public List<WikiDirectory> getParentsRecursive() {
        if (this.getParent() == null) return Collections.EMPTY_LIST;
        List parents = new ArrayList();
        WikiNode currentNode = this.getParent();
        while (true) {
            if (currentNode.getParent() == null) {
                parents.add(currentNode);
                break;
            } else {
                parents.add(currentNode);
                currentNode = currentNode.getParent();
            }
        }
        return parents;
    }

    public String toString() {
        return "WikiDirectory (" + getId() + "): " + getName();
    }
}
