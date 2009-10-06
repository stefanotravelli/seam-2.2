package org.jboss.seam.wiki.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An ugly denormalization and duplication of data, so aggregation queries
 * can execute faster on nested set trees.
 *
 * @author Christian Bauer
 */
@Entity
@Table(name = "WIKI_DOCUMENT_COUNT_COMMENT")
public class WikiDocumentCountComment {

    @Id
    @Column(name = "WIKI_DOCUMENT_ID", nullable = false)
    private Long documentId;

    @Column(name = "COMMENT_COUNT")
    protected Long commentCount;

    public WikiDocumentCountComment() {}

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
}