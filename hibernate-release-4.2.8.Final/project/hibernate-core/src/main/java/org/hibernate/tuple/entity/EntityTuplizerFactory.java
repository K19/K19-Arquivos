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
package org.hibernate.tuple.entity;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;

/**
 * A registry allowing users to define the default {@link EntityTuplizer} class to use per {@link EntityMode}.
 *
 * @author Steve Ebersole
 */
public class EntityTuplizerFactory implements Serializable {
	public static final Class[] ENTITY_TUP_CTOR_SIG = new Class[] { EntityMetamodel.class, PersistentClass.class };
	public static final Class[] ENTITY_TUP_CTOR_SIG_NEW = new Class[] { EntityMetamodel.class, EntityBinding.class };

	private Map<EntityMode,Class<? extends EntityTuplizer>> defaultImplClassByMode = buildBaseMapping();

	/**
	 * Method allowing registration of the tuplizer class to use as default for a particular entity-mode.
	 *
	 * @param entityMode The entity-mode for which to register the tuplizer class
	 * @param tuplizerClass The class to use as the default tuplizer for the given entity-mode.
	 */
	public void registerDefaultTuplizerClass(EntityMode entityMode, Class<? extends EntityTuplizer> tuplizerClass) {
		assert isEntityTuplizerImplementor( tuplizerClass )
				: "Specified tuplizer class [" + tuplizerClass.getName() + "] does not implement " + EntityTuplizer.class.getName();
		// TODO: for now we need constructors for both PersistentClass and EntityBinding
		assert hasProperConstructor( tuplizerClass, ENTITY_TUP_CTOR_SIG )
				: "Specified tuplizer class [" + tuplizerClass.getName() + "] is not properly instantiatable";
		assert hasProperConstructor( tuplizerClass, ENTITY_TUP_CTOR_SIG_NEW )
				: "Specified tuplizer class [" + tuplizerClass.getName() + "] is not properly instantiatable";
		defaultImplClassByMode.put( entityMode, tuplizerClass );
	}

	/**
	 * Construct an instance of the given tuplizer class.
	 *
	 * @param tuplizerClassName The name of the tuplizer class to instantiate
	 * @param metamodel The metadata for the entity.
	 * @param persistentClass The mapping info for the entity.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException If class name cannot be resolved to a class reference, or if the
	 * {@link Constructor#newInstance} call fails.
	 */
	@SuppressWarnings({ "unchecked" })
	public EntityTuplizer constructTuplizer(
			String tuplizerClassName,
			EntityMetamodel metamodel,
			PersistentClass persistentClass) {
		try {
			Class<? extends EntityTuplizer> tuplizerClass = ReflectHelper.classForName( tuplizerClassName );
			return constructTuplizer( tuplizerClass, metamodel, persistentClass );
		}
		catch ( ClassNotFoundException e ) {
			throw new HibernateException( "Could not locate specified tuplizer class [" + tuplizerClassName + "]" );
		}
	}

	/**
	 * Construct an instance of the given tuplizer class.
	 *
	 * @param tuplizerClassName The name of the tuplizer class to instantiate
	 * @param metamodel The metadata for the entity.
	 * @param entityBinding The mapping info for the entity.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException If class name cannot be resolved to a class reference, or if the
	 * {@link Constructor#newInstance} call fails.
	 */
	@SuppressWarnings({ "unchecked" })
	public EntityTuplizer constructTuplizer(
			String tuplizerClassName,
			EntityMetamodel metamodel,
			EntityBinding entityBinding) {
		try {
			Class<? extends EntityTuplizer> tuplizerClass = ReflectHelper.classForName( tuplizerClassName );
			return constructTuplizer( tuplizerClass, metamodel, entityBinding );
		}
		catch ( ClassNotFoundException e ) {
			throw new HibernateException( "Could not locate specified tuplizer class [" + tuplizerClassName + "]" );
		}
	}

	/**
	 * Construct an instance of the given tuplizer class.
	 *
	 * @param tuplizerClass The tuplizer class to instantiate
	 * @param metamodel The metadata for the entity.
	 * @param persistentClass The mapping info for the entity.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException if the {@link Constructor#newInstance} call fails.
	 */
	public EntityTuplizer constructTuplizer(
			Class<? extends EntityTuplizer> tuplizerClass,
			EntityMetamodel metamodel,
			PersistentClass persistentClass) {
		Constructor<? extends EntityTuplizer> constructor = getProperConstructor( tuplizerClass, ENTITY_TUP_CTOR_SIG );
		assert constructor != null : "Unable to locate proper constructor for tuplizer [" + tuplizerClass.getName() + "]";
		try {
			return constructor.newInstance( metamodel, persistentClass );
		}
		catch ( Throwable t ) {
			throw new HibernateException( "Unable to instantiate default tuplizer [" + tuplizerClass.getName() + "]", t );
		}
	}

	/**
	 * Construct an instance of the given tuplizer class.
	 *
	 * @param tuplizerClass The tuplizer class to instantiate
	 * @param metamodel The metadata for the entity.
	 * @param entityBinding The mapping info for the entity.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException if the {@link Constructor#newInstance} call fails.
	 */
	public EntityTuplizer constructTuplizer(
			Class<? extends EntityTuplizer> tuplizerClass,
			EntityMetamodel metamodel,
			EntityBinding entityBinding) {
		Constructor<? extends EntityTuplizer> constructor = getProperConstructor( tuplizerClass, ENTITY_TUP_CTOR_SIG_NEW );
		assert constructor != null : "Unable to locate proper constructor for tuplizer [" + tuplizerClass.getName() + "]";
		try {
			return constructor.newInstance( metamodel, entityBinding );
		}
		catch ( Throwable t ) {
			throw new HibernateException( "Unable to instantiate default tuplizer [" + tuplizerClass.getName() + "]", t );
		}
	}

	/**
	 * Construct am instance of the default tuplizer for the given entity-mode.
	 *
	 * @param entityMode The entity mode for which to build a default tuplizer.
	 * @param metamodel The entity metadata.
	 * @param persistentClass The entity mapping info.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException If no default tuplizer found for that entity-mode; may be re-thrown from
	 * {@link #constructTuplizer} too.
	 */
	public EntityTuplizer constructDefaultTuplizer(
			EntityMode entityMode,
			EntityMetamodel metamodel,
			PersistentClass persistentClass) {
		Class<? extends EntityTuplizer> tuplizerClass = defaultImplClassByMode.get( entityMode );
		if ( tuplizerClass == null ) {
			throw new HibernateException( "could not determine default tuplizer class to use [" + entityMode + "]" );
		}

		return constructTuplizer( tuplizerClass, metamodel, persistentClass );
	}

	/**
	 * Construct am instance of the default tuplizer for the given entity-mode.
	 *
	 * @param entityMode The entity mode for which to build a default tuplizer.
	 * @param metamodel The entity metadata.
	 * @param entityBinding The entity mapping info.
	 *
	 * @return The instantiated tuplizer
	 *
	 * @throws HibernateException If no default tuplizer found for that entity-mode; may be re-thrown from
	 * {@link #constructTuplizer} too.
	 */
	public EntityTuplizer constructDefaultTuplizer(
			EntityMode entityMode,
			EntityMetamodel metamodel,
			EntityBinding entityBinding) {
		Class<? extends EntityTuplizer> tuplizerClass = defaultImplClassByMode.get( entityMode );
		if ( tuplizerClass == null ) {
			throw new HibernateException( "could not determine default tuplizer class to use [" + entityMode + "]" );
		}

		return constructTuplizer( tuplizerClass, metamodel, entityBinding );
	}

	private boolean isEntityTuplizerImplementor(Class tuplizerClass) {
		return ReflectHelper.implementsInterface( tuplizerClass, EntityTuplizer.class );
	}

	private boolean hasProperConstructor(Class<? extends EntityTuplizer> tuplizerClass, Class[] constructorArgs) {
		return getProperConstructor( tuplizerClass, constructorArgs ) != null
				&& ! ReflectHelper.isAbstractClass( tuplizerClass );
	}

	private Constructor<? extends EntityTuplizer> getProperConstructor(
			Class<? extends EntityTuplizer> clazz,
			Class[] constructorArgs) {
		Constructor<? extends EntityTuplizer> constructor = null;
		try {
			constructor = clazz.getDeclaredConstructor( constructorArgs );
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

	private static Map<EntityMode,Class<? extends EntityTuplizer>> buildBaseMapping() {
		Map<EntityMode,Class<? extends EntityTuplizer>> map = new ConcurrentHashMap<EntityMode,Class<? extends EntityTuplizer>>();
		map.put( EntityMode.POJO, PojoEntityTuplizer.class );
		map.put( EntityMode.MAP, DynamicMapEntityTuplizer.class );
		return map;
	}
}
