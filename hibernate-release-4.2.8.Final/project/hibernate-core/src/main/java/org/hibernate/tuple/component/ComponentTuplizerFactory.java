/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
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
package org.hibernate.tuple.component;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.Component;

/**
 * A registry allowing users to define the default {@link ComponentTuplizer} class to use per {@link EntityMode}.
 *
 * @author Steve Ebersole
 */
public class ComponentTuplizerFactory implements Serializable {
	private static final Class[] COMPONENT_TUP_CTOR_SIG = new Class[] { Component.class };

	private Map<EntityMode,Class<? extends ComponentTuplizer>> defaultImplClassByMode = buildBaseMapping();

	/**
	 * Method allowing registration of the tuplizer class to use as default for a particular entity-mode.
	 *
	 * @param entityMode The entity-mode for which to register the tuplizer class
	 * @param tuplizerClass The class to use as the default tuplizer for the given entity-mode.
	 */
	@SuppressWarnings({ "UnusedDeclaration" })
	public void registerDefaultTuplizerClass(EntityMode entityMode, Class<? extends ComponentTuplizer> tuplizerClass) {
		assert isComponentTuplizerImplementor( tuplizerClass )
				: "Specified tuplizer class [" + tuplizerClass.getName() + "] does not implement " + ComponentTuplizer.class.getName();
		assert hasProperConstructor( tuplizerClass )
				: "Specified tuplizer class [" + tuplizerClass.getName() + "] is not properly instantiatable";

		defaultImplClassByMode.put( entityMode, tuplizerClass );
	}

	/**
	 * Construct an instance of the given tuplizer class.
	 *
	 * @param tuplizerClassName The name of the tuplizer class to instantiate
	 * @param metadata The metadata for the component.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException If class name cannot be resolved to a class reference, or if the
	 * {@link Constructor#newInstance} call fails.
	 */
	@SuppressWarnings({ "unchecked" })
	public ComponentTuplizer constructTuplizer(String tuplizerClassName, Component metadata) {
		try {
			Class<? extends ComponentTuplizer> tuplizerClass = ReflectHelper.classForName( tuplizerClassName );
			return constructTuplizer( tuplizerClass, metadata );
		}
		catch ( ClassNotFoundException e ) {
			throw new HibernateException( "Could not locate specified tuplizer class [" + tuplizerClassName + "]" );
		}
	}

	/**
	 * Construct an instance of the given tuplizer class.
	 *
	 * @param tuplizerClass The tuplizer class to instantiate
	 * @param metadata The metadata for the component.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException if the {@link java.lang.reflect.Constructor#newInstance} call fails.
	 */
	public ComponentTuplizer constructTuplizer(Class<? extends ComponentTuplizer> tuplizerClass, Component metadata) {
		Constructor<? extends ComponentTuplizer> constructor = getProperConstructor( tuplizerClass );
		assert constructor != null : "Unable to locate proper constructor for tuplizer [" + tuplizerClass.getName() + "]";
		try {
			return constructor.newInstance( metadata );
		}
		catch ( Throwable t ) {
			throw new HibernateException( "Unable to instantiate default tuplizer [" + tuplizerClass.getName() + "]", t );
		}
	}

	/**
	 * Construct am instance of the default tuplizer for the given entity-mode.
	 *
	 * @param entityMode The entity mode for which to build a default tuplizer.
	 * @param metadata The metadata for the component.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException If no default tuplizer found for that entity-mode; may be re-thrown from
	 * {@link #constructTuplizer} too.
	 */
	public ComponentTuplizer constructDefaultTuplizer(EntityMode entityMode, Component metadata) {
		Class<? extends ComponentTuplizer> tuplizerClass = defaultImplClassByMode.get( entityMode );
		if ( tuplizerClass == null ) {
			throw new HibernateException( "could not determine default tuplizer class to use [" + entityMode + "]" );
		}

		return constructTuplizer( tuplizerClass, metadata );
	}

	private boolean isComponentTuplizerImplementor(Class tuplizerClass) {
		return ReflectHelper.implementsInterface( tuplizerClass, ComponentTuplizer.class );
	}

	@SuppressWarnings({ "unchecked" })
	private boolean hasProperConstructor(Class tuplizerClass) {
		return getProperConstructor( tuplizerClass ) != null;
	}

	private Constructor<? extends ComponentTuplizer> getProperConstructor(Class<? extends ComponentTuplizer> clazz) {
		Constructor<? extends ComponentTuplizer> constructor = null;
		try {
			constructor = clazz.getDeclaredConstructor( COMPONENT_TUP_CTOR_SIG );
			if ( ! ReflectHelper.isPublic( constructor ) ) {
				try {
					// found a constructor, but it was not publicly accessible so try to request accessibility
					constructor.setAccessible( true );
				}
				catch ( SecurityException e ) {
					constructor = null;
				}
			}
		}
		catch ( NoSuchMethodException ignore ) {
		}

		return constructor;
	}

	private static Map<EntityMode,Class<? extends ComponentTuplizer>> buildBaseMapping() {
		Map<EntityMode,Class<? extends ComponentTuplizer>> map = new ConcurrentHashMap<EntityMode,Class<? extends ComponentTuplizer>>();
		map.put( EntityMode.POJO, PojoComponentTuplizer.class );
		map.put( EntityMode.MAP, DynamicMapComponentTuplizer.class );
		return map;
	}
}