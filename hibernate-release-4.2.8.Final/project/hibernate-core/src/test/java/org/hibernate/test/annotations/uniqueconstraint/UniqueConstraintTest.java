package org.hibernate.test.annotations.uniqueconstraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.AnnotationException;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

/**
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 * @author Brett Meyer
 */
public class UniqueConstraintTest extends BaseCoreFunctionalTestCase {
	
	protected Class[] getAnnotatedClasses() {
        return new Class[]{
                Room.class,
                Building.class,
                House.class
        };
    }

	@Test
    public void testUniquenessConstraintWithSuperclassProperty() throws Exception {
        Session s = openSession();
        Transaction tx = s.beginTransaction();
        Room livingRoom = new Room();
        livingRoom.setId(1l);
        livingRoom.setName("livingRoom");
        s.persist(livingRoom);
        s.flush();
        House house = new House();
        house.setId(1l);
        house.setCost(100);
        house.setHeight(1000l);
        house.setRoom(livingRoom);
        s.persist(house);
        s.flush();
        House house2 = new House();
        house2.setId(2l);
        house2.setCost(100);
        house2.setHeight(1001l);
        house2.setRoom(livingRoom);
        s.persist(house2);
        try {
            s.flush();
            fail("Database constraint non-existant");
        } catch(JDBCException e) {
            //success
        }
        tx.rollback();
        s.close();
    }
	
	@Test
	@TestForIssue( jiraKey = "HHH-8026" )
	public void testUnNamedConstraints() {
		Configuration cfg = new Configuration();
		cfg.addAnnotatedClass( UniqueNoNameA.class );
		cfg.addAnnotatedClass( UniqueNoNameB.class );
		cfg.buildMappings();
		Iterator<org.hibernate.mapping.Table> iterator = cfg.getTableMappings();
		org.hibernate.mapping.Table tableA = null;
		org.hibernate.mapping.Table tableB = null;
		while( iterator.hasNext() ) {
			org.hibernate.mapping.Table table = iterator.next();
			if ( table.getName().equals( "UniqueNoNameA" ) ) {
				tableA = table;
			}
			else if ( table.getName().equals( "UniqueNoNameB" ) ) {
				tableB = table;
			}
		}
		
		if ( tableA == null || tableB == null ) {
			fail( "Could not find the expected tables." );
		}
		
		UniqueKey ukA = (UniqueKey) tableA.getUniqueKeyIterator().next();
		UniqueKey ukB = (UniqueKey) tableB.getUniqueKeyIterator().next();
		assertFalse( ukA.getName().equals( ukB.getName() ) );
	}
	
	@Test
	@TestForIssue( jiraKey = "HHH-8537" )
	public void testNonExistentColumn() {
		Configuration cfg = new Configuration();
		cfg.addAnnotatedClass( UniqueColumnDoesNotExist.class );
		try {
			cfg.buildMappings();
		}
		catch (NullPointerException e) {
			fail( "The @UniqueConstraint with a non-existent column name should have resulted in an AnnotationException" );
		}
		catch (AnnotationException e) {
			// expected
		}
	}
	
	@Entity
	@Table( name = "UniqueNoNameA",
			uniqueConstraints = {@UniqueConstraint(columnNames={"name"})})
	public static class UniqueNoNameA {
		@Id
		@GeneratedValue
		public long id;
		
		public String name;
	}
	
	@Entity
	@Table( name = "UniqueNoNameB",
			uniqueConstraints = {@UniqueConstraint(columnNames={"name"})})
	public static class UniqueNoNameB {
		@Id
		@GeneratedValue
		public long id;
		
		public String name;
	}

	@Entity
	public static class UniqueColumnDoesNotExist {
		@Id
		public Integer id;

		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(
				name = "tbl_strings",
				joinColumns = @JoinColumn(name = "fk", nullable = false),
				// the failure required at least 1 columnName to be correct -- all incorrect wouldn't reproduce
				uniqueConstraints =  @UniqueConstraint(columnNames = { "fk", "doesnotexist" })
		)
		@Column(name = "string", nullable = false)
		public Set<String> strings = new HashSet<String>();
	}
    
}
