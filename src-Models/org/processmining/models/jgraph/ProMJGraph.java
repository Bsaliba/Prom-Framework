package org.processmining.models.jgraph;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import org.jgraph.JGraph;
import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;
import org.processmining.framework.util.Cast;
import org.processmining.framework.util.Cleanable;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.Expandable;
import org.processmining.models.graphbased.ExpansionListener;
import org.processmining.models.graphbased.View;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.BoundaryDirectedGraphNode;
import org.processmining.models.graphbased.directed.ContainableDirectedGraphElement;
import org.processmining.models.graphbased.directed.ContainingDirectedGraphNode;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.elements.ProMGraphElement;
import org.processmining.models.jgraph.elements.ProMGraphPort;
import org.processmining.models.jgraph.factory.ProMCellViewFactory;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;

public class ProMJGraph extends JGraph implements View, GraphModelListener, GraphLayoutCacheListener,
		GraphSelectionListener, Cleanable, ExpansionListener, ScalableComponent {

	private static final long serialVersionUID = -8477633603192312230L;

	public static final String PIPVIEWATTRIBUTE = "signalPIPView";

	private final ProMGraphModel model;
	private final Map<DirectedGraphNode, ProMGraphCell> nodeMap = new HashMap<DirectedGraphNode, ProMGraphCell>();
	private final Map<BoundaryDirectedGraphNode, ProMGraphPort> boundaryNodeMap = new HashMap<BoundaryDirectedGraphNode, ProMGraphPort>();
	private final Map<DirectedGraphEdge<?, ?>, ProMGraphEdge> edgeMap = new HashMap<DirectedGraphEdge<?, ?>, ProMGraphEdge>();

	private JGraphLayout layout;

	private final ViewSpecificAttributeMap viewSpecificAttributes;

	private final boolean isPIP;

	//	public ProMJGraph(ProMGraphModel model) {
	//		this(model, new ViewSpecificAttributeMap());
	//	}

	public ProMJGraph(ProMGraphModel model, ViewSpecificAttributeMap viewSpecificAttributes) {
		this(model, false, viewSpecificAttributes);
	}

	public ProMJGraph(ProMGraphModel model, boolean isPIP, ViewSpecificAttributeMap viewSpecificAttributes) {
		super(model, new GraphLayoutCache(model, new ProMCellViewFactory(isPIP, viewSpecificAttributes), true));
		getGraphLayoutCache().setShowsInvisibleEditedCells(false);
		this.isPIP = isPIP;
		this.viewSpecificAttributes = viewSpecificAttributes;

		//		ProMCellViewFactory factory = new ProMCellViewFactory(isPIP, viewSpecificAttributes);
		//		getGraphLayoutCache().setFactory(factory);
		getGraphLayoutCache().setMovesChildrenOnExpand(true);
		getGraphLayoutCache().setResizesParentsOnCollapse(true);
		getGraphLayoutCache().setMovesParentsOnCollapse(true);
		getGraphLayoutCache().setAutoSizeOnValueChange(true);

		this.model = model;

		setAntiAliased(true);
		setDisconnectable(false);
		setConnectable(false);
		setGridEnabled(false);
		setDoubleBuffered(true);
		setSelectionEnabled(!isPIP);
		setMoveBelowZero(false);
		setPortsVisible(true);
		setPortsScaled(true);

		DirectedGraph<?, ?> net = model.getGraph();

		List<DirectedGraphNode> todo = new ArrayList<DirectedGraphNode>(net.getNodes());
		List<Object> toInsert = new ArrayList<Object>();
		while (!todo.isEmpty()) {
			Iterator<DirectedGraphNode> it = todo.iterator();
			while (it.hasNext()) {
				DirectedGraphNode n = it.next();
				if (n instanceof BoundaryDirectedGraphNode) {
					DirectedGraphNode m = ((BoundaryDirectedGraphNode) n).getBoundingNode();
					if ((m != null) && !nodeMap.containsKey(m)) {
						// first make sure the bounding node is added
						continue;
					} else if (m != null) {
						// add as port
						addPort((BoundaryDirectedGraphNode) n, m);
						it.remove();
						continue;
					}
				}
				if (n instanceof ContainableDirectedGraphElement) {
					ContainingDirectedGraphNode c = Cast.<ContainableDirectedGraphElement>cast(n).getParent();
					if ((c != null) && !nodeMap.containsKey(c)) {
						// if parent is not added yet, then continue
						continue;
					} else if (c == null) {
						toInsert.add(addCell(n));
					} else {
						addCell(n);
					}
				} else {
					toInsert.add(addCell(n));
				}

				it.remove();
			}
		}

		//		getGraphLayoutCache().insert(toInsert.toArray());

		//		getGraphLayoutCache().insert(boundaryNodeMap.values().toArray());
		for (DirectedGraphEdge<?, ?> e : net.getEdges()) {
			if (e instanceof ContainableDirectedGraphElement) {
				ContainingDirectedGraphNode m = Cast.<ContainableDirectedGraphElement>cast(e).getParent();
				if (m == null) {
					toInsert.add(addEdge(e));
				} else {
					addEdge(e);
				}
			} else {
				toInsert.add(addEdge(e));
			}
		}
		getGraphLayoutCache().insert(toInsert.toArray());
		// Add the listeners, only AFTER copying the graph.

		for (DirectedGraphNode n : nodeMap.keySet()) {
			if (n instanceof Expandable) {
				Expandable ex = (Expandable) n;
				ex.getExpansionListeners().add(this);
			}
		}

		registerAsListener();

		if (!isPIP) {
			addMouseListener(new JGraphFoldingManager());
		}

		net.addView(this);

		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
	 * Returns the <code>GraphModel</code> that is providing the data.
	 * 
	 * @return the model that is providing the data
	 */
	public ProMGraphModel getModel() {
		return (ProMGraphModel) graphModel;
	}

	public void cleanUp() {
		model.getGraph().removeView(this);

		List<Cleanable> cells = new ArrayList<Cleanable>(nodeMap.values());
		cells.addAll(boundaryNodeMap.values());
		cells.addAll(edgeMap.values());
		getGraphLayoutCache().removeCells(cells.toArray());

		for (Cleanable cell : cells) {
			cell.cleanUp();
		}

		model.removeGraphModelListener(this);
		removeGraphSelectionListener(this);
		getGraphLayoutCache().removeGraphLayoutCacheListener(this);
		ToolTipManager.sharedInstance().unregisterComponent(this);

		removeAll();
		setVisible(false);
		setEnabled(false);
		setLayout(null);
		setGraphLayoutCache(null);

	}

	private ProMGraphCell addCell(DirectedGraphNode node) {
		ProMGraphCell cell = new ProMGraphCell(node, model);

		// TODO: This is probably wrong.
		// cell.addPort(new Point2D.Double(0,0));
		cell.addPort();
		((DefaultPort) cell.getChildAt(0)).setUserObject("default port");

		// getting the size
		nodeMap.put(node, cell);

		// if the node is contained in another node, its cell must be contained in the cell of that node
		if (node instanceof ContainableDirectedGraphElement) {
			ContainingDirectedGraphNode parent = Cast.<ContainableDirectedGraphElement>cast(node).getParent();
			if (parent != null) {
				ProMGraphCell parentNode = nodeMap.get(parent);
				parentNode.add(cell);
				cell.setParent(parentNode);
			}
		}

		return cell;
	}

	private ProMGraphPort addPort(BoundaryDirectedGraphNode node, DirectedGraphNode boundingNode) {
		ProMGraphCell cell = nodeMap.get(boundingNode);
		ProMGraphPort port = cell.addPort(new Point2D.Float(0, 0), node);
		assert (port.getParent() == cell);

		boundaryNodeMap.put(node, port);

		return port;
	}

	private ProMGraphEdge addEdge(DirectedGraphEdge<?, ?> e) {
		ProMGraphEdge edge = new ProMGraphEdge(e, model);
		// For now, assume a single port.
		ProMGraphPort srcPort;
		if ((e.getSource() instanceof BoundaryDirectedGraphNode)
				&& ((BoundaryDirectedGraphNode) e.getSource()).getBoundingNode() != null) {
			srcPort = boundaryNodeMap.get(e.getSource());
		} else {
			srcPort = (ProMGraphPort) nodeMap.get(e.getSource()).getChildAt(0);
		}
		ProMGraphPort tgtPort;
		if ((e.getTarget() instanceof BoundaryDirectedGraphNode)
				&& ((BoundaryDirectedGraphNode) e.getTarget()).getBoundingNode() != null) {
			tgtPort = boundaryNodeMap.get(e.getTarget());
		} else {
			tgtPort = (ProMGraphPort) nodeMap.get(e.getTarget()).getChildAt(0);
		}

		edge.setSource(srcPort);
		edge.setTarget(tgtPort);

		srcPort.addEdge(edge);
		tgtPort.addEdge(edge);

		edgeMap.put(e, edge);

		// if the edge is contained in a node, its cell must be contained in the cell of that node
		if (e instanceof ContainableDirectedGraphElement) {
			ContainingDirectedGraphNode parent = Cast.<ContainableDirectedGraphElement>cast(e).getParent();
			if (parent != null) {
				nodeMap.get(parent).add(edge);
				assert (edge.getParent() == nodeMap.get(parent));
			}
		}

		return edge;
	}

	public void added(Set<?> elements) {
		Vector<Object> cellsToAdd = new Vector<Object>();
		// The order in the toAdd list matters. First, nodes should be
		// added, then ports and then edges. This ensures that all ports created for
		// the nodes have views. When views on edges are created, they assume that ports have views.
		elements = new HashSet<Object>(elements);
		Iterator<?> it = elements.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (element instanceof DirectedGraphNode
					&& (!(element instanceof BoundaryDirectedGraphNode) || ((BoundaryDirectedGraphNode) element)
							.getBoundingNode() == null)) {
				DirectedGraphNode n = (DirectedGraphNode) element;
				if ((!(n instanceof ContainableDirectedGraphElement))
						|| (((ContainableDirectedGraphElement) n).getParent() == null)) {
					cellsToAdd.add(addCell(n));
				} else {
					addCell(n);
				}
				it.remove();
			}
		}
		while (it.hasNext()) {
			Object element = it.next();
			if ((element instanceof BoundaryDirectedGraphNode)
					&& ((BoundaryDirectedGraphNode) element).getBoundingNode() != null) {
				BoundaryDirectedGraphNode n = (BoundaryDirectedGraphNode) element;
				//				cellsToAdd.add(addPort(n));
				addPort(n, n.getBoundingNode());
				it.remove();
			}
		}
		while (it.hasNext()) {
			Object element = it.next();
			if (element instanceof DirectedGraphEdge<?, ?>) {
				DirectedGraphEdge<?, ?> e = (DirectedGraphEdge<?, ?>) element;
				if ((e instanceof ContainableDirectedGraphElement)
						&& ((ContainableDirectedGraphElement) e).getParent() != null) {
					addEdge(e);
				} else {
					cellsToAdd.add(addEdge(e));
				}
				it.remove();
			}
		}
		while (it.hasNext()) {
			Object element = it.next();
			if (element instanceof DirectedGraph<?, ?>) {
				// graph has changed
			}
		}
		getGraphLayoutCache().insert(cellsToAdd.toArray());
	}

	public boolean doViewLayout() {
		if (layout != null) {
			JGraphFacade facade = new JGraphFacade(this);

			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			facade.run(layout, true);

			//			layout.run(facade);
			facade.run(layout, true);
			model.getGraph().setLayedOut(true);
			getGraphLayoutCache().edit(facade.createNestedMap(true, true));
			//repositionToOrigin();//model.getGraph().getBounds();
			return true;
		} else {
			return false;
		}
	}

	public void removed(Set<?> elements) {
		Vector<Object> cells = new Vector<Object>(elements.size());
		for (Object element : elements) {
			if ((element instanceof BoundaryDirectedGraphNode) ? ((BoundaryDirectedGraphNode) element)
					.getBoundingNode() != null : false) {
				ProMGraphPort port = boundaryNodeMap.get(element);
				cells.add(port);
				boundaryNodeMap.remove(element);
			} else if (element instanceof DirectedGraphNode) {
				ProMGraphCell cell = nodeMap.get(element);
				cells.add(cell);
				nodeMap.remove(element);
			} else if (element instanceof DirectedGraphEdge<?, ?>) {
				ProMGraphEdge cell = edgeMap.get(element);
				cells.add(cell);
				cell.getSource().removeEdge(cell);
				cell.getTarget().removeEdge(cell);
				edgeMap.remove(element);
			} else if (element instanceof DirectedGraph<?, ?>) {
				// graph has changed
			} else {
				assert (false);
			}
		}
		model.remove(cells.toArray());
		getGraphLayoutCache().removeCells(cells.toArray());
	}

	public void update(Set<?> elements) {
		//The order in which cells, ports and edges are added matters:
		//Cells first, ports second and edges third (because ports are attached to cells and edges to ports.)
		Vector<ProMGraphElement> cellsToAdd = new Vector<ProMGraphElement>();
		Vector<ProMGraphElement> portsToAdd = new Vector<ProMGraphElement>();
		Vector<ProMGraphElement> edgesToAdd = new Vector<ProMGraphElement>();
		Vector<CellView> cellViewsToAdd = new Vector<CellView>();
		Vector<CellView> portViewsToAdd = new Vector<CellView>();
		Vector<CellView> edgeViewsToAdd = new Vector<CellView>();

		for (Object element : elements) {
			if ((element instanceof BoundaryDirectedGraphNode) ? ((BoundaryDirectedGraphNode) element)
					.getBoundingNode() != null : false) {
				ProMGraphPort cell = boundaryNodeMap.get(element);
				if (cell != null) {
					// An update on a cell that does not exist in the view should not be done.
					portsToAdd.add(cell);
					portViewsToAdd.add(cell.getView());
				}
			} else if (element instanceof DirectedGraphNode) {
				ProMGraphCell cell = nodeMap.get(element);
				if (cell != null) {
					// An update on a cell that does not exist in the view should not be done.
					cellsToAdd.add(cell);
					cellViewsToAdd.add(cell.getView());
				}
			} else if (element instanceof DirectedGraphEdge<?, ?>) {
				ProMGraphEdge cell = edgeMap.get(element);
				if (cell != null) {
					// An update on a cell that does not exist in the view should not be done.
					edgesToAdd.add(cell);
					edgeViewsToAdd.add(cell.getView());
				}
			} else if (element instanceof DirectedGraph<?, ?>) {
				// graph has changed
			} else {
				assert (false);
			}
		}

		Vector<CellView> views = cellViewsToAdd;
		views.addAll(portViewsToAdd);
		views.addAll(edgeViewsToAdd);
		Vector<ProMGraphElement> cells = cellsToAdd;
		cells.addAll(portsToAdd);
		cells.addAll(edgesToAdd);
		Rectangle2D oldBound = GraphLayoutCache.getBounds(views.toArray(new CellView[0]));
		for (ProMGraphElement cell : cells) {
			cell.update();
		}
		model.cellsChanged(cells.toArray(), oldBound);
	}

	public String toString() {
		return model.toString();
	}

	public void graphChanged(GraphModelEvent e) {
		handleChange(e.getChange());
		changeHandled();
		for (UpdateListener l : updateListeners) {
			l.updated();
		}
	}

	/**
	 * Might be overridden to signal that a change was handled
	 */
	protected void changeHandled() {

	}

	private void handleChange(GraphLayoutCacheEvent.GraphLayoutCacheChange change) {
		synchronized (model) {
			boolean signalChange = false;
			Object[] changed = change.getChanged();
			Set<ProMGraphEdge> edges = new HashSet<ProMGraphEdge>();
			for (Object o : changed) {
				if (o instanceof ProMGraphCell) {
					// handle a change for a cell
					ProMGraphCell cell = (ProMGraphCell) o;
					DirectedGraphNode node = cell.getNode();
					signalChange |= handleNodeChange(cell, node);
				}
				if (o instanceof ProMGraphEdge) {
					edges.add((ProMGraphEdge) o);
				}
			}
			for (ProMGraphEdge cell : edges) {
				// handle a change for a cell
				DirectedGraphEdge<?, ?> edge = cell.getEdge();
				signalChange |= handleEdgeChange(cell, edge);
			}
			if (signalChange && !isPIP) {
				model.getGraph().signalViews(this);
			}
		}
	}

	private boolean handleNodeChange(ProMGraphCell cell, DirectedGraphNode node) {
		boolean changed = false;

		// LABEL
		String label = cell.getUserObject();
		// The change should NOT be communicated back to this graph, since that
		// would trigger an update of the cells in the graph, leading to
		// excessive
		// prcessing times
		changed |= node.getAttributeMap().put(AttributeMap.LABEL, label);

		Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());

		if (rect != null) {
			// SIZE
			Dimension2D size = new Dimension((int) rect.getWidth(), (int) rect.getHeight());
			changed |= node.getAttributeMap().put(AttributeMap.SIZE, size);

			// POSITION
			Point2D pos = new Point2D.Double(rect.getX(), rect.getY());
			changed |= node.getAttributeMap().put(AttributeMap.POSITION, pos);

		}

		return changed;
	}

	private boolean handleEdgeChange(ProMGraphEdge cell, DirectedGraphEdge<?, ?> edge) {
		boolean changed = false;

		// LABEL
		String label = cell.getUserObject();
		changed |= edge.getAttributeMap().put(AttributeMap.LABEL, label);

		// POINTS
		List<?> points = GraphConstants.getPoints(cell.getAttributes());
		List<Point2D> list = new ArrayList<Point2D>(3);
		if (points != null) {
			for (int i = 1; i < points.size() - 1; i++) {
				Point2D point = (Point2D) points.get(i);
				list.add(new Point2D.Double(point.getX(), point.getY()));
			}
		}
		changed |= edge.getAttributeMap().put(AttributeMap.EDGEPOINTS, list);

		return changed;
	}

	public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
		handleChange(e.getChange());
		changeHandled();
	}

	public void valueChanged(GraphSelectionEvent e) {
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		// get first cell under the mouse pointer's position
		Object cell = getFirstCellForLocation(event.getX(), event.getY());

		ViewSpecificAttributeMap map = getViewSpecificAttributes();

		// determine what is being pointed to by the mouse pointer
		if (cell instanceof ProMGraphCell) {
			// mouse is pointing to a node or a port on that node
			ProMGraphCell c = ((ProMGraphCell) cell);
			return map.get(c.getNode(), AttributeMap.TOOLTIP, c.getLabel());
		} else if (cell instanceof ProMGraphEdge) {
			ProMGraphEdge e = ((ProMGraphEdge) cell);
			return map.get(e.getEdge(), AttributeMap.TOOLTIP, e.getLabel());
		}

		return null;
	}

	// returns the original origin
	public Point2D repositionToOrigin() {
		JGraphFacade facade = new JGraphFacade(this);
		/*
		 * First, push everything towards the lower left corner. Provided that
		 * we will not have many groups inside groups, 100 should be sufficient.
		 * This step is needed to assure that we do not have negative
		 * coordinates for the important second step. For some some reason,
		 * getGraphOrigin() returns 0.0 instead of negative coordinates.
		 */
		//		facade.translateCells(facade.getVertices(), 100.0, 100.0);
		//		facade.translateCells(facade.getEdges(), 100.0, 100.0);
		//		getGraphLayoutCache().edit(facade.createNestedMap(true, false));
		/*
		 * Second, pull everything back to (2,2). Works like a charm, even when
		 * a hack...
		 */
		//TODO
		double x = facade.getGraphOrigin().getX();
		double y = facade.getGraphOrigin().getY();
		facade.translateCells(facade.getVertices(), 2.0 - x, 2.0 - y);
		facade.translateCells(facade.getEdges(), 2.0 - x, 2.0 - y);
		getGraphLayoutCache().edit(facade.createNestedMap(true, false));
		return new Point2D.Double(x - 2., y - 2.);
	}

	public DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> getProMGraph() {
		return model.getGraph();
	}

	private void registerAsListener() {
		model.addGraphModelListener(this);
		addGraphSelectionListener(this);
		getGraphLayoutCache().addGraphLayoutCacheListener(this);
	}

	public int hashCode() {
		return model.getGraph().hashCode();
	}

	public JGraphLayout getUpdateLayout() {
		return layout;
	}

	public void setUpdateLayout(JGraphLayout layout) {
		this.layout = layout;
	}

	public ViewSpecificAttributeMap getViewSpecificAttributes() {
		return viewSpecificAttributes;
	}

	public void nodeCollapsed(Expandable source) {
		ProMGraphCell cell = (nodeMap.get(source));
		getGraphLayoutCache().collapse(DefaultGraphModel.getDescendants(model, new Object[] { cell }).toArray());
	}

	public void nodeExpanded(Expandable source) {
		ProMGraphCell cell = (nodeMap.get(source));
		getGraphLayoutCache().expand(DefaultGraphModel.getDescendants(model, new Object[] { cell }).toArray());
	}

	public JComponent getComponent() {
		// for interface Scalable
		return this;
	}

	Set<UpdateListener> updateListeners = new HashSet<UpdateListener>();

	public void addUpdateListener(UpdateListener listener) {
		updateListeners.add(listener);
	}

	public void removeUpdateListener(UpdateListener listener) {
		updateListeners.remove(listener);
	}

}

class Change implements GraphModelEvent.GraphModelChange {

	private final Collection<Object> added;
	private final Collection<Object> removed;
	private final Collection<Object> changed;
	private final ProMGraphModel source;
	private final Rectangle2D dirtyRegion;

	public Change(ProMGraphModel source, Collection<Object> added, Collection<Object> removed,
			Collection<Object> changed, Rectangle2D dirtyRegion) {
		this.source = source;
		this.added = added;
		this.removed = removed;
		this.changed = changed;
		this.dirtyRegion = dirtyRegion;

	}

	public ConnectionSet getConnectionSet() {
		return null;
	}

	public ParentMap getParentMap() {
		return null;
	}

	public ConnectionSet getPreviousConnectionSet() {
		return null;
	}

	public ParentMap getPreviousParentMap() {
		return null;
	}

	public CellView[] getViews(GraphLayoutCache view) {
		return null;
	}

	public void putViews(GraphLayoutCache view, CellView[] cellViews) {

	}

	public Map<?, ?> getAttributes() {
		return null;
	}

	public Object[] getChanged() {
		return changed.toArray();
	}

	public Object[] getContext() {
		return null;
	}

	public Rectangle2D getDirtyRegion() {
		return dirtyRegion;
	}

	public Object[] getInserted() {
		return added.toArray();
	}

	public Map<?, ?> getPreviousAttributes() {
		return null;
	}

	public Object[] getRemoved() {
		return removed.toArray();
	}

	public Object getSource() {
		return source;
	}

	public void setDirtyRegion(Rectangle2D dirty) {

	}

}