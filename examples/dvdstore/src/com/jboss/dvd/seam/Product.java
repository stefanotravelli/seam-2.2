/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package com.jboss.dvd.seam;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.NGramFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

@Entity
@Table(name="PRODUCTS")
@Indexed
@AnalyzerDef(name="ngrams", tokenizer=@TokenizerDef(factory=StandardTokenizerFactory.class),
      filters={
         @TokenFilterDef(factory = LowerCaseFilterFactory.class),
         @TokenFilterDef(factory = NGramFilterFactory.class,
               params = { @Parameter(name = "minGramSize", value = "3"), @Parameter(name = "maxGramSize", value = "3") })
   })
public class Product implements Serializable
{
    private static final long serialVersionUID = -5378546367347755065L;
 
    long productId;
    String asin;
    String title;
    String description;
    String imageURL;
    BigDecimal price = BigDecimal.ZERO;

    List<Actor>    actors;
    Set<Category> categories;
    Inventory inventory;

    @Id @GeneratedValue
    @Column(name="PROD_ID")
    @DocumentId
    public long getProductId() {
        return productId;
    }                    
    public void setProductId(long id) {
        this.productId = id;
    }     

    @Column(name="ASIN", length=16)
    @Field(index=Index.UN_TOKENIZED)
    public String getASIN() {
        return asin;
    }

    public void setASIN(String asin) {
        this.asin = asin;
    }

    @OneToOne(fetch=FetchType.LAZY,mappedBy="product")
    public Inventory getInventory() {
        return inventory;
    }
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="PRODUCT_ACTORS",
               joinColumns=@JoinColumn(name="PROD_ID"),
               inverseJoinColumns=@JoinColumn(name="ACTOR_ID"))
    @IndexedEmbedded
    public List<Actor> getActors() {
        return actors;
    }
    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    
    @ManyToMany
    @JoinTable(name="PRODUCT_CATEGORY",
               joinColumns=@JoinColumn(name="PROD_ID"),
               inverseJoinColumns=@JoinColumn(name="CATEGORY"))
    @IndexedEmbedded
    public Set<Category> getCategories() {
        return categories;
    }
    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }
    
    @Column(name="TITLE",nullable=false,length=100)
    @Fields({
       @Field(index=Index.TOKENIZED),
       @Field(index=Index.TOKENIZED, name="title:ngrams", analyzer=@Analyzer(definition="ngrams"))})
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name="DESCRIPTION",length=1024)
    @Fields({
       @Field(index=Index.TOKENIZED),
       @Field(index=Index.TOKENIZED, name="description:ngrams", analyzer=@Analyzer(definition="ngrams"))})
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name="IMAGE_URL",length=256)
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Column(name="PRICE",nullable=false,precision=12,scale=2)
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price=price;
    }
}
