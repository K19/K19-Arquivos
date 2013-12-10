/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
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
package org.hibernate.cache.spi.entry;

import java.io.Serializable;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.TypeHelper;

/**
 * @author Steve Ebersole
 */
public class StandardCacheEntryImpl implements CacheEntry {
	private final Serializable[] disassembledState;
	private final String subclass;
	private final boolean lazyPropertiesAreUnfetched;
	private final Object version;

	@Override
	public boolean isReferenceEntry() {
		return false;
	}

	@Override
	public Serializable[] getDisassembledState() {
		// todo: this was added to support initializing an entity's EntityEntry snapshot during reattach;
		// this should be refactored to instead expose a method to assemble a EntityEntry based on this
		// state for return.
		return disassembledState;
	}

	@Override
	public String getSubclass() {
		return subclass;
	}

	@Override
	public boolean areLazyPropertiesUnfetched() {
		return lazyPropertiesAreUnfetched;
	}

	@Override
	public Object getVersion() {
		return version;
	}

	public StandardCacheEntryImpl(
			final Object[] state,
			final EntityPersister persister,
			final boolean unfetched,
			final Object version,
			final SessionImplementor session,
			final Object owner)
			throws HibernateException {
		//disassembled state gets put in a new array (we write to cache by value!)
		this.disassembledState = TypeHelper.disassemble(
				state,
				persister.getPropertyTypes(),
				persister.isLazyPropertiesCacheable() ?
						null : persister.getPropertyLaziness(),
				session,
				owner
		);
		subclass = persister.getEntityName();
		lazyPropertiesAreUnfetched = unfetched || !persister.isLazyPropertiesCacheable();
		this.version = version;
	}

	StandardCacheEntryImpl(Serializable[] state, String subclass, boolean unfetched, Object version) {
		this.disassembledState = state;
		this.subclass = subclass;
		this.lazyPropertiesAreUnfetched = unfetched;
		this.version = version;
	}

	public boolean isDeepCopyNeeded() {
		// for now always return true.
		// todo : See discussion on HHH-7872
		return true;
	}

	public Object[] assemble(
			final Object instance,
			final Serializable id,
			final EntityPersister persister,
			final Interceptor interceptor,
			final EventSource session)
			throws HibernateException {

		if ( !persister.getEntityName().equals(subclass) ) {
			throw new AssertionFailure("Tried to assemble a different subclass instance");
		}

		return assemble(disassembledState, instance, id, persister, interceptor, session);
	}

	private static Object[] assemble(
			final Serializable[] values,
			final Object result,
			final Serializable id,
			final EntityPersister persister,
			final Interceptor interceptor,
			final EventSource session) throws HibernateException {

		//assembled state gets put in a new array (we read from cache by value!)
		Object[] assembledProps = TypeHelper.assemble(
				values,
				persister.getPropertyTypes(),
				session, result
		);

		//persister.setIdentifier(result, id); //before calling interceptor, for consistency with normal load

		//TODO: reuse the PreLoadEvent
		final PreLoadEvent preLoadEvent = new PreLoadEvent( session )
				.setEntity( result )
				.setState( assembledProps )
				.setId( id )
				.setPersister( persister );

		final EventListenerGroup<PreLoadEventListener> listenerGroup = session
				.getFactory()
				.getServiceRegistry()
				.getService( EventListenerRegistry.class )
				.getEventListenerGroup( EventType.PRE_LOAD );
		for ( PreLoadEventListener listener : listenerGroup.listeners() ) {
			listener.onPreLoad( preLoadEvent );
		}

		persister.setPropertyValues( result, assembledProps );

		return assembledProps;
	}

	public String toString() {
		return "CacheEntry(" + subclass + ')' + ArrayHelper.toString( disassembledState );
	}
}
