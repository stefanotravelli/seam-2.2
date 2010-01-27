package org.jboss.seam.wiki.core.model;

/**
 * A simple wrapper for hierarchical display (with level) of wiki nodes.
 *
 * @author Christian Bauer
 */
public class WikiTreeNode<N extends WikiNode> {

    private long level;
    private N node;
    private Object payload; // This can be anything we want to attach for display

    public WikiTreeNode(long level, N node) {
        this.level = level;
        this.node = node;
    }

    public long getLevel() {
        return level;
    }

    public N getNode() {
        return node;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "(WikiTreeNode) L: " + getLevel() + " - " + getNode().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WikiTreeNode that = (WikiTreeNode) o;

        if (!node.getId().equals(that.node.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return node.getId().hashCode();
    }
}
