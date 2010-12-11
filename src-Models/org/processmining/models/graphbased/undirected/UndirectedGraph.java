package org.processmining.models.graphbased.undirected;

import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.processmining.models.graphbased.Viewable;

public interface UndirectedGraph<N extends UndirectedGraphNode, E extends UndirectedGraphEdge<? extends N>> extends
		UndirectedGraphElement, Viewable, Comparable<UndirectedGraph<N, E>> {

	Collection<N> getNodes();

	Collection<E> getEdges();

	Collection<E> getEdges(UndirectedGraphNode node);

	@SuppressWarnings("unchecked")
	void removeEdge(UndirectedGraphEdge edge);

	/**
	 * Uses the attributes of all its nodes and edges to get a bounding box
	 */
	Rectangle2D getBounds();

	/**
	 * Uses the attributes of all its nodes and edges to get a bounding box
	 */
	Rectangle2D getBounds(Collection<?> elements);

}
