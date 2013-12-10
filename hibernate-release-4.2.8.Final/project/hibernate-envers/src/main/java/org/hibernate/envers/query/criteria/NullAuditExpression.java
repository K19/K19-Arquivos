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
 */
package org.hibernate.envers.query.criteria;

import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.entities.RelationDescription;
import org.hibernate.envers.query.property.PropertyNameGetter;
import org.hibernate.envers.reader.AuditReaderImplementor;
import org.hibernate.envers.tools.query.Parameters;
import org.hibernate.envers.tools.query.QueryBuilder;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class NullAuditExpression implements AuditCriterion {
    private PropertyNameGetter propertyNameGetter;

    public NullAuditExpression(PropertyNameGetter propertyNameGetter) {
        this.propertyNameGetter = propertyNameGetter;
    }

    public void addToQuery(AuditConfiguration auditCfg, AuditReaderImplementor versionsReader, String entityName,
						   QueryBuilder qb, Parameters parameters) {
		String propertyName = CriteriaTools.determinePropertyName( auditCfg, versionsReader, entityName, propertyNameGetter );
        RelationDescription relatedEntity = CriteriaTools.getRelatedEntity(auditCfg, entityName, propertyName);

        if (relatedEntity == null) {
            parameters.addNullRestriction(propertyName, true);
        } else {
            relatedEntity.getIdMapper().addIdEqualsToQuery(parameters, null, null, true);
        }
    }
}
