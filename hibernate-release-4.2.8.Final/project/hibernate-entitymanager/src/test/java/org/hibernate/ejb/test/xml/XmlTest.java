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
package org.hibernate.ejb.test.xml;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.SharedCacheMode;

import junit.framework.Assert;
import org.junit.Test;

import org.hibernate.Session;
import org.hibernate.ejb.AvailableSettings;
import org.hibernate.ejb.test.BaseEntityManagerFunctionalTestCase;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Emmanuel Bernard
 */
public class XmlTest extends BaseEntityManagerFunctionalTestCase {
	@Test
	public void testXmlMappingCorrectness() throws Exception {
		EntityManager em = getOrCreateEntityManager();
		em.close();
	}

	@Test
	public void testXmlMappingWithCacheable() throws Exception{
		EntityManager em = getOrCreateEntityManager();
		SessionImplementor session = em.unwrap( SessionImplementor.class );
		EntityPersister entityPersister= session.getFactory().getEntityPersister( Lighter.class.getName() );
		Assert.assertTrue(entityPersister.hasCache());
	}

	@Override
	public Class[] getAnnotatedClasses() {
		return new Class[0];
	}

	protected void addConfigOptions(Map options) {
		options.put(  AvailableSettings.SHARED_CACHE_MODE, SharedCacheMode.ENABLE_SELECTIVE );
	}

	@Override
	public String[] getEjb3DD() {
		return new String[] {
				"org/hibernate/ejb/test/xml/orm.xml",
				"org/hibernate/ejb/test/xml/orm2.xml",
		};
	}
}
