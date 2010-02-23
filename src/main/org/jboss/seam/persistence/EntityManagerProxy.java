package org.jboss.seam.persistence;

import java.io.Serializable;

import javax.persistence.EntityManager;

/**
 * Proxies the EntityManager, and implements EL interpolation
 * in JPA-QL
 * 
 * @author Gavin King
 *
 */
public interface EntityManagerProxy extends EntityManager, Serializable
{
}
