package org.processmining.models.graphbased.directed;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;

import org.processmining.models.graphbased.Viewable;

public interface DirectedGraph<N extends DirectedGraphNode, E extends DirectedGraphEdge<? extends N, ? extends N>>
		extends DirectedGraphElement, Viewable, Comparable<DirectedGraph<N, E>> {

	Set<N> getNodes();

	Set<E> getEdges();

	Collection<E> getInEdges(DirectedGraphNode node);

	Collection<E> getOutEdges(DirectedGraphNode node);

	/**
	 * Removes the given edge from the graph.
	 * 
	 * @param edge
	 */
	@SuppressWarnings("unchecked")
	void removeEdge(DirectedGraphEdge edge);

	/**
	 * Uses the attributes of all its nodes and edges to get a bounding box
	 */
	Rectangle2D getBounds();

	/**
	 * Uses the attributes of all its nodes and edges to get a bounding box
	 */
	Rectangle2D getBounds(Collection<?> elements);

	void removeNode(DirectedGraphNode cell);

	boolean isLayedOut();

	void setLayedOut(boolean isLayedOut);

	void expandAll();

}
