package org.processmining.models.graphbased.undirected;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.processmining.framework.util.Cast;
import org.processmining.models.graphbased.AbstractGraphEdge;
import org.processmining.models.graphbased.AttributeMap;

public abstract class AbstractUndirectedEdge<T extends UndirectedGraphNode> extends AbstractGraphEdge<T, T> implements
		UndirectedGraphEdge<T> {

	private final AbstractUndirectedGraph<?, ?> graph;

	public AbstractUndirectedEdge(T source, T target, String label) {
		super(source, target);
		assert (source.getGraph() == target.getGraph());
		assert (source.getGraph() instanceof AbstractUndirectedGraph<?, ?>);
		this.graph = Cast.<AbstractUndirectedGraph<?, ?>>cast(source.getGraph());
		getAttributeMap().put(AttributeMap.LABEL, label);
		getAttributeMap().put(AttributeMap.EDGEPOINTS, new ArrayList<Point2D>(0));
	}

	public UndirectedGraph<?, ?> getGraph() {
		return graph;
	}

	public void attributeChanged(String key, Object oldValue, Object newValue, AttributeMap map) {
		graph.graphElementChanged(this);
	}

}
