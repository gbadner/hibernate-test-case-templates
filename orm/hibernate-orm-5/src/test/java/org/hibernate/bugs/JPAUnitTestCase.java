package org.hibernate.bugs;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Persistence;

import org.hibernate.EmptyInterceptor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
		AnEntity anEntity = new AnEntity();
		anEntity.description = "description   ";
		anEntity.strings.add( "string   " );
		entityManager.persist( anEntity );
		entityManager.getTransaction().commit();
		entityManager.close();

		entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		final String description = (String) entityManager.createNativeQuery(
				"select description from AnEntity a where a.id = " + anEntity.id
		).getSingleResult();
		assertEquals( "description   ", description );
		final String string = (String) entityManager.createNativeQuery(
				"select s.strings from AnEntity_strings s where s.AnEntity_id = " + anEntity.id
		).getSingleResult();
		assertEquals( "string   ", string );
		entityManager.getTransaction().commit();
		entityManager.close();

		entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		anEntity = entityManager.find( AnEntity.class, anEntity.id );
		assertEquals( "description", anEntity.description );
		// the following will fail if uncommented
		// assertEquals( "string", anEntity.strings.iterator().next() );
		entityManager.clear();

		anEntity = entityManager.createQuery( "from AnEntity e left join fetch e.strings", AnEntity.class ).getSingleResult();
		assertEquals( "description", anEntity.description );
		// the following will fail if uncommented
		// assertEquals( "string", anEntity.strings.iterator().next() );

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Entity(name = "AnEntity")
	public static class AnEntity {
		@Id
		@GeneratedValue
		private long id;

		@Column(nullable = false)
		private String description;

		@ElementCollection
		@Fetch( value = FetchMode.JOIN)
		private Set<String> strings = new HashSet<String>();
	}

	public static class AnInterceptor extends EmptyInterceptor {
		public boolean onLoad(
				Object entity,
				Serializable id,
				Object[] state,
				String[] propertyNames,
				Type[] types) {
			boolean wasModified = false;
			if ( AnEntity.class.isInstance( entity ) ) {
				for (int i = 0; i < types.length; i++ ) {
					if ( StringType.class.isInstance( types[i] ) && state[i] != null ) {
						final String originalString = (String) state[i];
						final String trimmedString = originalString.trim();
						if ( trimmedString.length() < originalString.length() ) {
							state[i] = trimmedString;
							wasModified = true;
						}
					}
					// collection isn't available yet, so cannot replace elements with
					// trailing spaces with trimmed strings.
				}
			}
			return wasModified;
		}
	}
}
