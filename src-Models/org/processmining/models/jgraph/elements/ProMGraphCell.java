package org.processmining.models.jgraph.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.processmining.framework.util.Cleanable;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.ModelOwner;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.views.JGraphShapeView;

public class ProMGraphCell extends DefaultGraphCell implements Cleanable, ModelOwner, ProMGraphElement {

	private static final long serialVersionUID = -5170284747077744754L;
	private DirectedGraphNode node;
	private ProMGraphModel model;
	private JGraphShapeView view;

	public ProMGraphCell(DirectedGraphNode node, ProMGraphModel model) {
		super(node.getLabel());
		this.node = node;
		this.model = model;
		// update();
		GraphConstants.setConstrained(getAttributes(), node.getAttributeMap().get(AttributeMap.SQUAREBB, false));
		GraphConstants.setSizeable(getAttributes(), node.getAttributeMap().get(AttributeMap.RESIZABLE, true));
		GraphConstants.setResize(getAttributes(), node.getAttributeMap().get(AttributeMap.AUTOSIZE, false));
		GraphConstants.setHorizontalAlignment(getAttributes(), SwingConstants.CENTER);
		GraphConstants.setInset(getAttributes(), node.getAttributeMap().get(AttributeMap.INSET, 20));
		GraphConstants.setLineWidth(getAttributes(), new Float(node.getAttributeMap().get(AttributeMap.LINEWIDTH,
				GraphConstants.getLineWidth(getAttributes()))));
		GraphConstants.setForeground(getAttributes(), node.getAttributeMap().get(AttributeMap.LABELCOLOR, Color.black));
		GraphConstants.setOrientation(getAttributes(), node.getAttributeMap().get(AttributeMap.PREF_ORIENTATION,
				SwingConstants.NORTH));
	}

	public void update() {
		assert (view != null);
		// Update the dimension / position
		Dimension2D dim = node.getAttributeMap().get(AttributeMap.SIZE, new Dimension(50, 50));
		Point2D pos = node.getAttributeMap().get(AttributeMap.POSITION, new Point2D.Double(100, 100));

		Rectangle2D rect = new Rectangle2D.Double(pos.getX(), pos.getY(), dim.getWidth(), dim.getHeight());
		Rectangle2D bounds = GraphConstants.getBounds(getAttributes());

		boolean boundsChanged = !rect.equals(bounds);
		if (boundsChanged) {
			GraphConstants.setBounds(getAttributes(), rect);
			view.setBounds(rect);
		}

		// Update the label
		boolean labelChanged;
		if (getUserObject() != null) {
			labelChanged = !getUserObject().equals(node.getLabel());
		} else {
			labelChanged = !node.getLabel().isEmpty();
		}
		if (labelChanged) {
			setUserObject(node.getLabel());
		}
	}

	public DirectedGraphNode getNode() {
		return node;
	}

	public String getUserObject() {
		return (String) super.getUserObject();
	}

	public void setView(JGraphShapeView view) {
		this.view = view;
	}

	public void cleanUp() {
		for (Object o : getChildren()) {
			if (o instanceof Cleanable) {
				Cleanable p = (Cleanable) o;
				p.cleanUp();
			}
		}
		removeAllChildren();
		if (view != null) {
			view.cleanUp();
		}
		view = null;
		model = null;
		node = null;

	}

	// This method is called by all other addPort methods.
	@Override
	public ProMGraphPort addPort(Point2D offset, Object userObject) {
		ProMGraphPort port = new ProMGraphPort(userObject, model);
		if (offset == null) {
			add(port);
		} else {
			GraphConstants.setOffset(port.getAttributes(), offset);
			add(port);
		}
		return port;
	}

	public String getLabel() {
		return node.getLabel();
	}

	public int hashCode() {
		return node.hashCode();
	}

	public ProMGraphModel getModel() {
		return model;
	}

	public JGraphShapeView getView() {
		return view;
	}

	/**
	 * This implementation of equals seems to be required by JGraph. Changing it
	 * to anything more meaningful will introduce very strange results.
	 */
	public boolean equals(Object o) {
		return o == this;
	}

}
