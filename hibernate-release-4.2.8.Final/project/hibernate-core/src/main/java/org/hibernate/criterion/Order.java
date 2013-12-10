/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
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
 *
 */
package org.hibernate.criterion;
import java.io.Serializable;
import java.sql.Types;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NullPrecedence;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * Represents an order imposed upon a <tt>Criteria</tt> result set
 * 
 * @author Gavin King
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 * @author Brett Meyer
 */
public class Order implements Serializable {
	private boolean ascending;
	private boolean ignoreCase;
	private String propertyName;
	private NullPrecedence nullPrecedence;
	
	public String toString() {
		return propertyName + ' ' + ( ascending ? "asc" : "desc" ) + ( nullPrecedence != null ? ' ' + nullPrecedence.name().toLowerCase() : "" );
	}
	
	public Order ignoreCase() {
		ignoreCase = true;
		return this;
	}

	public Order nulls(NullPrecedence nullPrecedence) {
		this.nullPrecedence = nullPrecedence;
		return this;
	}

	/**
	 * Constructor for Order.
	 */
	protected Order(String propertyName, boolean ascending) {
		this.propertyName = propertyName;
		this.ascending = ascending;
	}

	/**
	 * Render the SQL fragment
	 *
	 */
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) 
	throws HibernateException {
		String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, propertyName);
		Type type = criteriaQuery.getTypeUsingProjection(criteria, propertyName);
		StringBuilder fragment = new StringBuilder();
		for ( int i=0; i<columns.length; i++ ) {
			final StringBuilder expression = new StringBuilder();
			SessionFactoryImplementor factory = criteriaQuery.getFactory();
			boolean lower = false;
			if ( ignoreCase ) {
				int sqlType = type.sqlTypes( factory )[i];
				lower = sqlType == Types.VARCHAR
						|| sqlType == Types.CHAR
						|| sqlType == Types.LONGVARCHAR;
			}
			
			if (lower) {
				expression.append( factory.getDialect().getLowercaseFunction() ).append('(');
			}
			expression.append( columns[i] );
			if (lower) expression.append(')');
			fragment.append(
					factory.getDialect()
							.renderOrderByElement(
									expression.toString(),
									null,
									ascending ? "asc" : "desc",
									nullPrecedence != null ? nullPrecedence : factory.getSettings().getDefaultNullPrecedence()
							)
			);
			if ( i<columns.length-1 ) fragment.append(", ");
		}
		return fragment.toString();
	}
	
	public String getPropertyName() {
		return propertyName;
	}

	public boolean isAscending() {
		return ascending;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	/**
	 * Ascending order
	 *
	 * @param propertyName
	 * @return Order
	 */
	public static Order asc(String propertyName) {
		return new Order(propertyName, true);
	}

	/**
	 * Descending order
	 *
	 * @param propertyName
	 * @return Order
	 */
	public static Order desc(String propertyName) {
		return new Order(propertyName, false);
	}

}
