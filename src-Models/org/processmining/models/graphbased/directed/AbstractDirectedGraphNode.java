package org.processmining.models.graphbased.directed;

import org.processmining.models.graphbased.AbstractGraphNode;
import org.processmining.models.graphbased.AttributeMap;

public abstract class AbstractDirectedGraphNode extends AbstractGraphNode implements DirectedGraphNode {

	public AbstractDirectedGraphNode() {
		super();
	}

	public void attributeChanged(String key, Object oldValue, Object newValue, AttributeMap map) {
		AbstractDirectedGraph<?, ?> graph = getGraph();
		if (graph != null) {
			// graph==null Should only occur if the constructor is called with
			// null.
			// Note that setGraph() should be called ASAP.
			graph.graphElementChanged(this);
			if (key.equals(AttributeMap.POSITION) || key.equals(AttributeMap.SIZE)) {
				for (DirectedGraphEdge<?, ?> edge : graph.getInEdges(this)) {
					graph.graphElementChanged(edge);
				}
				for (DirectedGraphEdge<?, ?> edge : graph.getOutEdges(this)) {
					graph.graphElementChanged(edge);
				}
			}
		}
	}

	public abstract AbstractDirectedGraph<?, ?> getGraph();

	public int compareTo(DirectedGraphNode node) {
		int comp = getId().compareTo(node.getId());
		//		assert (Math.abs(comp) == Math.abs(getLabel().compareTo(getLabel())));
		return comp;

	}

}
