package org.processmining.models.jgraph.elements;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.processmining.framework.util.Cleanable;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.BoundaryDirectedGraphNode;
import org.processmining.models.jgraph.ModelOwner;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.views.JGraphPortView;

public class ProMGraphPort extends DefaultPort implements Cleanable, ModelOwner, ProMGraphElement {

	private static final long serialVersionUID = 34423826783834456L;
	private JGraphPortView view;
	private ProMGraphModel model;

	public ProMGraphPort(Object userObject, ProMGraphModel model) {
		super(userObject);
		this.model = model;
		if ((getUserObject() instanceof BoundaryDirectedGraphNode) ? ((BoundaryDirectedGraphNode) getUserObject())
				.getBoundingNode() != null : false) {
			BoundaryDirectedGraphNode node = (BoundaryDirectedGraphNode) getUserObject();
			GraphConstants.setOffset(getAttributes(), node.getAttributeMap().get(AttributeMap.PORTOFFSET,
					new Point2D.Double(GraphConstants.PERMILLE / 4, GraphConstants.PERMILLE)));
		}
	}

	@SuppressWarnings("unchecked")
	public void cleanUp() {
		view = null;
		setUserObject(null);
		Iterator<Object> edge = edges();
		List<Object> edges = new ArrayList<Object>();
		while (edge.hasNext()) {
			edges.add(edge.next());
		}
		for (Object e : edges) {
			removeEdge(e);
		}
		model = null;
	}

	public void setView(JGraphPortView view) {
		this.view = view;
	}

	public JGraphPortView getView() {
		return view;
	}

	public ProMGraphModel getModel() {
		return model;
	}

	public void update() {
		assert (view != null);

		if ((getUserObject() instanceof BoundaryDirectedGraphNode) ? ((BoundaryDirectedGraphNode) getUserObject())
				.getBoundingNode() != null : false) {
			BoundaryDirectedGraphNode node = (BoundaryDirectedGraphNode) getUserObject();

			// Update the port size
			// Note: the width and the height of a port should always be equal 			
			Dimension size = node.getAttributeMap().get(AttributeMap.SIZE, new Dimension(50, 50));
			Dimension currSize = GraphConstants.getSize(getAttributes());
			if (!size.equals(currSize)) {
				GraphConstants.setSize(getAttributes(), size);
				view.setPortSize((int) size.getWidth());
			}

			Point2D offset = node.getAttributeMap().get(AttributeMap.PORTOFFSET,
					new Point2D.Double(GraphConstants.PERMILLE / 4, GraphConstants.PERMILLE));
			Point2D currOffset = GraphConstants.getOffset(getAttributes());
			if (!offset.equals(currOffset)) {
				GraphConstants.setOffset(getAttributes(), offset);
			}
		}
	}

	/**
	 * This implementation of equals seems to be required by JGraph. Changing it
	 * to anything more meaningful will introduce very strange results.
	 */
	public boolean equals(Object o) {
		return o == this;
	}

}
