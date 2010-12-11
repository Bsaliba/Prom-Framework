package org.processmining.models.graphbased.directedhypergraph;

import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.processmining.models.graphbased.Viewable;

public interface DirectedHypergraph<N extends DirectedHypergraphNode, I extends DirectedIncomingHyperedge<? extends N, ? extends N>, O extends DirectedOutgoingHyperedge<? extends N, ? extends N>>
		extends DirectedHypergraphElement, Viewable, Comparable<DirectedHypergraph<N, I, O>> {

	Collection<N> getNodes();

	Collection<I> getInEdges();

	Collection<O> getOutEdges();

	Collection<I> getIncomingInEdges(DirectedHypergraphNode node);

	Collection<O> getOutgoingOutEdges(DirectedHypergraphNode node);

	Collection<O> getIncomingOutEdges(DirectedHypergraphNode node);

	Collection<I> getOutgoingInEdges(DirectedHypergraphNode node);

	@SuppressWarnings("unchecked")
	void removeEdge(DirectedIncomingHyperedge edge);

	@SuppressWarnings("unchecked")
	void removeEdge(DirectedOutgoingHyperedge edge);

	/**
	 * Uses the attributes of all its nodes and edges to get a bounding box
	 */
	Rectangle2D getBounds();

	/**
	 * Uses the attributes of all its nodes and edges to get a bounding box
	 */
	Rectangle2D getBounds(Collection<?> elements);

}
