/**
 * Abstracts all possible transaction management APIs behind a 
 * JTA-compatible interface. Unfortunately, many 
 * otherwise-perfectly-intelligent-looking Java developers like 
 * to invent their own transaction management APIs when they get 
 * bored, even though JTA is well-known to be more than good
 * enough. For example, one of the co-authors of this class was 
 * present at the creation of not one but two "alternative" 
 * transaction APIs (org.hibernate.Transaction and 
 * javax.persistence.EntityTransaction), and is more 
 * embarrassed by this than by any other of his many professional
 * blunders.
 * 
 * @see org.jboss.seam.transaction.Transaction
 * @see org.jboss.seam.transaction.UserTransaction
 */
@Namespace(value="http://jboss.com/products/seam/transaction", prefix="org.jboss.seam.transaction")
package org.jboss.seam.transaction;

import org.jboss.seam.annotations.Namespace;
