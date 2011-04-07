//$Id: FullTextSearchAction.java 7961 2008-04-17 04:30:44Z norman.richards@jboss.com $
package com.jboss.dvd.seam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.log.Log;

/**
 * Hibernate Search version of the store querying mechanism.
 * This version is updated to show the new Query capabilities
 * of Hibernate Search 3.3, which requires Hibernate 3.6 and
 * as such is the recommended (minimal) version to use for JBoss6.
 * This code resides in a separate src directory as it wouldn't
 * compile with older Hibernate Search versions.
 * 
 * @author Emmanuel Bernard
 * @author Sanne Grinovero
 */
@Stateful
@Name("search")
public class FullTextSearchAction
    implements FullTextSearch,
               Serializable
{
    static final long serialVersionUID = -6536629890251170098L;
    
    @In(create=true)
    ShoppingCart cart;
    
    /**
     * Note that when using Seam's injection the entityManager
     * can be assigned directly to a FullTextEntityManager.
     * Same trick applies to FullTextSession.
     */
    @In
    FullTextEntityManager entityManager;
    
    @Logger
    Log log;

    //@RequestParameter
    Long id;

    int pageSize = 15;
    int currentPage = 0;
    boolean hasMore = false;
    int numberOfResults;
    
    String searchQuery;

    @DataModel
    List<Product> searchResults;

    //@DataModelSelection
    Product selectedProduct;

    @Out(required = false)
    Product dvd;

    @Out(scope=ScopeType.CONVERSATION, required=false)
    Map<Product, Boolean> searchSelections;


    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    
    public int getNumberOfResults() {
        return numberOfResults;
    }
    
    @Begin(join = true)
    public String doSearch() {
        currentPage = 0;
        updateResults();
        
        return "browse";
    }
    
    public void nextPage() {
        if (!isLastPage()) {
            currentPage++;
            updateResults();
        }
    }

    public void prevPage() {
        if (!isFirstPage()) {
            currentPage--;
            updateResults();
        }
    }
    
    @Begin(join = true)
    public void selectFromRequest() {
        if (id != null)  {
            dvd = entityManager.find(Product.class, id);
        } else if (selectedProduct != null) {
            dvd = selectedProduct;
        }
    }

    public boolean isLastPage() {
        return ( searchResults != null ) && !hasMore;
    }

    public boolean isFirstPage() {
        return ( searchResults != null ) && ( currentPage == 0 );
    }

    @SuppressWarnings("unchecked")
    private void updateResults() {
       
       javax.persistence.Query query = null;  
       if (searchQuery == null || "".equals(searchQuery))
       {
          query = entityManager.createQuery("from Product");  
          numberOfResults =query.getResultList().size();
       }
       else
       {
          query = searchQuery(searchQuery);
          numberOfResults =( (FullTextQuery) query).getResultSize();
       }
       
        List<Product> items = query
            .setMaxResults(pageSize + 1)
            .setFirstResult(pageSize * currentPage)
            .getResultList();       
        
        if (items.size() > pageSize) {
            searchResults = new ArrayList(items.subList(0, pageSize));
            hasMore = true;
        } else {
            searchResults = items;
            hasMore = false;
        }

        searchSelections = new HashMap<Product, Boolean>();
    }

    private FullTextQuery searchQuery(String textQuery) {
        QueryBuilder queryBuilder = entityManager.getSearchFactory()
           .buildQueryBuilder().forEntity(Product.class).get();
        
        //Hibernate Search fulltext query example:
        
        //query to match exact terms occurrence, using custom boosts:
        Query queryToFindExactTerms = queryBuilder.keyword()
           .onFields("title").boostedTo(4f)
           .andField("description").boostedTo(2f)
           .andField("actors.name").boostedTo(2f)
           .andField("categories.name").boostedTo(0.5f)
           .matching(textQuery)
           .createQuery();
        
        //Similar query, but using NGram matching instead of exact terms:
        Query queryToFindMathingNGrams = queryBuilder.keyword()
           .onFields("title:ngrams").boostedTo(2f)
           .andField("description:ngrams")
           .andField("actors.name:ngrams")
           .andField("categories.name:ngrams").boostedTo(0.2f)
           .matching(textQuery)
           .createQuery();
        
        //Combine them for best results, note exact uses an higher boost:
        Query fullTextQuery = queryBuilder.bool()
           .should(queryToFindMathingNGrams)
           .should(queryToFindExactTerms)
           .createQuery();
        
        log.info("Executing fulltext query {0}", fullTextQuery);
        return entityManager.createFullTextQuery(fullTextQuery, Product.class);
    }
       
    /**
     * Add the selected DVD to the cart
     */
    public void addToCart()
    {
        cart.addProduct(dvd, 1);
    }
    
    /**
     * Add many items to cart
     */
    public void addAllToCart()
    {
        for (Product item : searchResults) {
            Boolean selected = searchSelections.get(item);
            if (selected != null && selected) {
                searchSelections.put(item, false);
                cart.addProduct(item, 1);
            }
        }
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getSelectedId() {
        return id;
    }

    public void setSelectedId(Long id) {
        this.id = id;
    }
    
    @End
    public void reset() { }

    @Destroy
    @Remove
    public void destroy() { }
}
