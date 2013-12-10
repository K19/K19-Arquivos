/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2007-2011, Red Hat Inc. or third-party contributors as
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
package org.hibernate.ejb.internal;

import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import org.hibernate.internal.CoreMessageLogger;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The jboss-logging {@link MessageLogger} for the hibernate-entitymanager module.  It reserves message ids ranging from
 * 15001 to 20000 inclusively.
 * <p/>
 * New messages must be added after the last message defined to ensure message codes are unique.
 */
@MessageLogger( projectCode = "HHH" )
public interface EntityManagerMessageLogger extends CoreMessageLogger {

    @LogMessage( level = INFO )
    @Message( value = "Bound Ejb3Configuration to JNDI name: %s", id = 15001 )
    void boundEjb3ConfigurationToJndiName( String name );

    @LogMessage( level = INFO )
    @Message( value = "Ejb3Configuration name: %s", id = 15002 )
    void ejb3ConfigurationName( String name );

    @LogMessage( level = INFO )
    @Message( value = "An Ejb3Configuration was renamed from name: %s", id = 15003 )
    void ejb3ConfigurationRenamedFromName( String name );

    @LogMessage( level = INFO )
    @Message( value = "An Ejb3Configuration was unbound from name: %s", id = 15004 )
    void ejb3ConfigurationUnboundFromName( String name );

    @LogMessage( level = WARN )
    @Message( value = "Exploded jar file does not exist (ignored): %s", id = 15005 )
    void explodedJarDoesNotExist( URL jarUrl );

    @LogMessage( level = WARN )
    @Message( value = "Exploded jar file not a directory (ignored): %s", id = 15006 )
    void explodedJarNotDirectory( URL jarUrl );

    @LogMessage( level = ERROR )
    @Message( value = "Illegal argument on static metamodel field injection : %s#%s; expected type :  %s; encountered type : %s", id = 15007 )
    void illegalArgumentOnStaticMetamodelFieldInjection( String name,
                                                         String name2,
                                                         String name3,
                                                         String name4 );

    @LogMessage( level = ERROR )
    @Message( value = "Malformed URL: %s", id = 15008 )
    void malformedUrl( URL jarUrl,
                       @Cause URISyntaxException e );

    @LogMessage( level = WARN )
    @Message( value = "Malformed URL: %s", id = 15009 )
    void malformedUrlWarning( URL jarUrl,
                              @Cause URISyntaxException e );

    @LogMessage( level = WARN )
    @Message( value = "Unable to find file (ignored): %s", id = 15010 )
    void unableToFindFile( URL jarUrl,
                           @Cause Exception e );

    @LogMessage( level = ERROR )
    @Message( value = "Unable to locate static metamodel field : %s#%s", id = 15011 )
    void unableToLocateStaticMetamodelField( String name,
                                             String name2 );

    @LogMessage( level = INFO )
    @Message( value = "Using provided datasource", id = 15012 )
    void usingProvidedDataSource();


	@LogMessage( level = DEBUG )
	@Message( value = "Returning null (as required by JPA spec) rather than throwing EntityNotFoundException, " +
			"as the entity (type=%s, id=%s) does not exist", id = 15013 )
	void ignoringEntityNotFound( String entityName, String identifier);

}
