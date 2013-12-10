/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.metamodel.source.annotations.xml;

import org.junit.Test;

import org.hibernate.metamodel.MetadataSources;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.internal.MetadataImpl;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.testing.junit4.BaseUnitTestCase;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
public class OrmXmlParserTests extends BaseUnitTestCase {
	@Test
	public void testSimpleOrmVersion2() {
		MetadataSources sources = new MetadataSources( new ServiceRegistryBuilder().buildServiceRegistry() );
		sources.addResource( "org/hibernate/metamodel/source/annotations/xml/orm-father.xml" );
		MetadataImpl metadata = (MetadataImpl) sources.buildMetadata();

		EntityBinding binding = metadata.getEntityBinding( Father.class.getName() );
		assertNotNull( binding );
	}

	@Test
	public void testSimpleOrmVersion1() {
		MetadataSources sources = new MetadataSources( new ServiceRegistryBuilder().buildServiceRegistry() );
		sources.addResource( "org/hibernate/metamodel/source/annotations/xml/orm-star.xml" );
		MetadataImpl metadata = (MetadataImpl) sources.buildMetadata();

		EntityBinding binding = metadata.getEntityBinding( Star.class.getName() );
		assertNotNull( binding );
	}

	@Test(expected = MappingException.class)
	public void testInvalidOrmXmlThrowsException() {
		MetadataSources sources = new MetadataSources( new ServiceRegistryBuilder().buildServiceRegistry() );
		sources.addResource( "org/hibernate/metamodel/source/annotations/xml/orm-invalid.xml" );
		sources.buildMetadata();
	}
}


