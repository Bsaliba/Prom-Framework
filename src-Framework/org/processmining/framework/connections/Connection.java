package org.processmining.framework.connections;

import java.util.Collection;
import java.util.Set;

import org.processmining.framework.connections.annotations.ConnectionDoesntExistMessage;
import org.processmining.framework.util.collection.MultiSet;

/**
 * A connection describes a relation between objects. The connection keeps a
 * number of weak references between the objects in the relation. Each object
 * should have a label describing the role of the object in the relation. These
 * roles are unique
 * 
 * Note that for all implementations of Connection, it is essential to consider
 * memory consumption. Connections are kept in the framework for as long as the
 * isRemoved() method returns false, hence this method should return true at the
 * earliest occasion.
 * 
 * All implementations of this class should carry the @ConnectionAnnotation
 * annotation! 
 * 
 * @author bfvdonge
 * 
 */
@ConnectionAnnotation
@ConnectionDoesntExistMessage(message = "Connection Doesn't Exist")
public interface Connection {

	/**
	 * Return the label of the connection
	 * 
	 * @return
	 */
	public String getLabel();

	/**
	 * Return the ID of the connection.
	 * 
	 * @return
	 */
	public ConnectionID getID();

	/**
	 * Return true if all objects given as parameter are contained in the
	 * connection. Multiplicities are taken into account, however order is
	 * abstracted from
	 * 
	 * @param objects
	 *            the objects to check for
	 * @return
	 */
	public boolean containsObjects(Object... objects);

	/**
	 * Return true if all objects given as parameter are contained in the
	 * connection. Multiplicities are taken into account, i.e. each object
	 * should occur as often as it is returned by the iterator of the given
	 * collection however order is abstracted from
	 * 
	 * @param objects
	 *            the objects as a collection
	 * @return
	 */
	public boolean containsObjects(Collection<?> objects);

	/**
	 * Return true if one of the objects connected by this connection no longer
	 * exists, i.e. it is collected by the garbage collector.
	 * 
	 * @return
	 */
	public boolean isRemoved();

	/**
	 * Return all objects contained in this connection, without their labels.
	 * 
	 * @return
	 */
	public MultiSet<Object> getObjects();

	/**
	 * Return the roles of all objects in this connection
	 * 
	 * @return
	 */
	public Set<String> getRoles();

	/**
	 * Return the object with the given role in this connection The type of the
	 * returned object is T. However, no checks have to be performed to see if
	 * the cast can be made. It is up to the calling method to ensure this cast
	 * is safe.
	 * 
	 * @param <T>
	 *            The type of object that should be returned.
	 * @param role
	 *            the role the returned object has to have
	 * @return the object attached to this role (not null).
	 */
	public <T> T getObjectWithRole(String role);

	/**
	 * Removes the connection. After calling this method, isRemoved()returns
	 * true;
	 */
	public void remove();

	/**
	 * sets the label of the connection to the new name
	 * 
	 * @param name
	 */
	public void setLabel(String name);

}
