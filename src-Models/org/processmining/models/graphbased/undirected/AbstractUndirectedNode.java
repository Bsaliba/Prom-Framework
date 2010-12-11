package org.processmining.models.graphbased.undirected;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.processmining.models.graphbased.AbstractGraphNode;
import org.processmining.models.graphbased.AttributeMap;

public abstract class AbstractUndirectedNode extends AbstractGraphNode implements UndirectedGraphNode {

	private final AbstractUndirectedGraph<?, ?> graph;

	public AbstractUndirectedNode(String label, AbstractUndirectedGraph<?, ?> graph) {
		super();
		this.graph = graph;
		getAttributeMap().put(AttributeMap.LABEL, label);
		getAttributeMap().put(AttributeMap.SIZE, new Dimension(50, 50));
		getAttributeMap().put(AttributeMap.POSITION, new Point2D.Double(10, 10));
	}

	public void attributeChanged(String key, Object oldValue, Object newValue, AttributeMap map) {
		if (graph != null) {
			// graph==null Should only occur if the constructor is called with
			// null.
			// Note that setGraph() should be called ASAP.
			graph.graphElementChanged(this);
			if (key.equals(AttributeMap.POSITION) || key.equals(AttributeMap.SIZE)) {
				for (UndirectedGraphEdge<?> edge : graph.getEdges(this)) {
					graph.graphElementChanged(edge);
				}
			}
		}
	}

	public UndirectedGraph<?, ?> getGraph() {
		return graph;
	}

	public int compareTo(UndirectedGraphNode node) {
		if (node instanceof AbstractUndirectedNode) {
			return getId().compareTo(((AbstractUndirectedNode) node).getId());
		} else {
			return getLabel().compareTo(node.getLabel());
		}
	}

}
