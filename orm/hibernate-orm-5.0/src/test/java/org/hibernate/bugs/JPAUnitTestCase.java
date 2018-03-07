package org.hibernate.bugs;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "templatePU" );
	}

	@After
	public void destroy() {
		entityManagerFactory.close();
	}

	// Entities are auto-discovered, so just add them anywhere on class-path
	// Add your tests, using standard JUnit.
	@Test
	public void hhh123Test() throws Exception {
		Product product = new Product();
		ProductInfo productInfo = new ProductInfo();
		product.id = 1;
		productInfo.id = 2;
		product.productInfo = productInfo;
		productInfo.productId = product.id;
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist( product );
		entityManager.flush();
		entityManager.clear();
		product = entityManager.find( Product.class, product.id );
		assertNotNull( product.productInfo );
		assertEquals( 2, product.productInfo.id );
		assertEquals( 1, product.productInfo.productId );
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Entity(name = "Product")
	public static class Product {
		@Id
		@Column(name = "id")
		private int id;

		@OneToOne(cascade = CascadeType.ALL)
		@JoinColumn(name = "id", referencedColumnName = "productId", insertable = false, updatable = false)
		private ProductInfo productInfo;

	}

	@Entity(name = "ProductInfo")
	public static class ProductInfo{
		@Id
		private int id;

		@Column(name = "productId", unique = true, updatable = false)
		private int productId;
	}
}
