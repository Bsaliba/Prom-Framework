package org.processmining.models.graphbased.directedhypergraph;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.processmining.models.graphbased.AbstractGraphNode;
import org.processmining.models.graphbased.AttributeMap;

public class AbstractDirectedHypergraphNode extends AbstractGraphNode implements DirectedHypergraphNode {

	private final AbstractDirectedHypergraph<?, ?, ?> graph;

	public AbstractDirectedHypergraphNode(String label, AbstractDirectedHypergraph<?, ?, ?> graph) {
		super();
		this.graph = graph;
		getAttributeMap().put(AttributeMap.LABEL, label);
		getAttributeMap().put(AttributeMap.SIZE, new Dimension(50, 50));
		getAttributeMap().put(AttributeMap.POSITION, new Point2D.Double(10, 10));
	}

	public DirectedHypergraph<?, ?, ?> getGraph() {
		return graph;
	}

	public void attributeChanged(String key, Object oldValue, Object newValue, AttributeMap map) {
		if (graph != null) {
			// graph==null Should only occur if the constructor is called with
			// null.
			// Note that setGraph() should be called ASAP.
			graph.graphElementChanged(this);
			if (key.equals(AttributeMap.POSITION) || key.equals(AttributeMap.SIZE)) {
				for (DirectedIncomingHyperedge<?, ?> edge : graph.getIncomingInEdges(this)) {
					graph.graphElementChanged(edge);
				}
				for (DirectedOutgoingHyperedge<?, ?> edge : graph.getIncomingOutEdges(this)) {
					graph.graphElementChanged(edge);
				}
				for (DirectedIncomingHyperedge<?, ?> edge : graph.getOutgoingInEdges(this)) {
					graph.graphElementChanged(edge);
				}
				for (DirectedOutgoingHyperedge<?, ?> edge : graph.getOutgoingOutEdges(this)) {
					graph.graphElementChanged(edge);
				}
			}
		}
	}

	public int compareTo(DirectedHypergraphNode node) {
		if (node instanceof AbstractDirectedHypergraphNode) {
			return getId().compareTo(((AbstractDirectedHypergraphNode) node).getId());
		}
		return getLabel().compareTo(node.getLabel());

	}

}
