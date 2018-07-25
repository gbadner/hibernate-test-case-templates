package org.hibernate.bugs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Persistence;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
		assertEquals( "string", anEntity.strings.iterator().next() );
		entityManager.clear();

		anEntity = entityManager.createQuery( "from AnEntity a left join fetch a.strings", AnEntity.class ).getSingleResult();
		assertEquals( "description", anEntity.description );
		assertEquals( "string", anEntity.strings.iterator().next() );

		// on flush, the entity and collection will be dirty, so they will be updated
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
		@Fetch( value = FetchMode.JOIN )
		private Set<String> strings = new HashSet<String>();
	}

	public static class EntityPostLoadListener {
		public void onPostLoad(Object entity) {
			if ( AnEntity.class.isInstance( entity ) ) {
				final AnEntity anEntity = (AnEntity) entity;
				anEntity.description = anEntity.description.trim();
				final Set<String> trimmedStrings = new HashSet<String>();
				for ( Iterator<String> it=anEntity.strings.iterator(); it.hasNext(); ) {
					final String originalString = it.next();
					final String trimmedString = originalString.trim();
					if ( trimmedString.length() < originalString.length() ) {
						it.remove();
						trimmedStrings.add( trimmedString );
					}
				}
				if ( !trimmedStrings.isEmpty() ) {
					anEntity.strings.addAll( trimmedStrings );
				}
			}
		}
	}
}
