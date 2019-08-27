package org.hibernate.bugs;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.MappedSuperclass;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery cq = cb.createQuery(ConcreteEntityOne.class);
		Root<ConcreteEntityOne> root = cq.from(ConcreteEntityOne.class);
		Path<ConcreteKeyOne> id = root.get("key");
		Path<String> name = id.get("one");
		Path<String> name2 = root.get("key").get("one");
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@MappedSuperclass
	public static abstract class AbstractEntity<Key extends AbstractKey> implements Serializable {
		@EmbeddedId
		protected Key key;

		public Key getKey() {
			return key;
		}

		public void setKey(Key key) {
			this.key = key;
		}
	}

	public static class AbstractKey implements Serializable {
	}

	@Entity(name = "ConcreteEntityOne")
	public static class ConcreteEntityOne extends AbstractEntity<ConcreteKeyOne> {

	}

	@Entity(name = "ConcreteEntityTwo")
	public static class ConcreteEntityTwo extends AbstractEntity<ConcreteKeyTwo> {

	}

	@Embeddable
	public static class ConcreteKeyOne extends AbstractKey {
		private static final long serialVersionUID = 1l;

		@Column(nullable=false)
		public String one;

		public String getOne() {
			return one;
		}

		public void setOne(String value) {
			this.one = value;
		}
	}

	@Embeddable
	public static class ConcreteKeyTwo extends AbstractKey {
		private static final long serialVersionUID = 1l;

		@Column(nullable=false)
		public String two;

		public String getTwo() {
			return two;
		}

		public void setTwo(String value) {
			two = value;
		}
	}
}
