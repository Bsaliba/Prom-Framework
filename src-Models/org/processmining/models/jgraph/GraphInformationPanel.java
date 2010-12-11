package org.processmining.models.jgraph;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;

/**
 * This panel builds a visualization of a directed graph, together with some
 * extra information.
 * 
 * This extra information is provided as a component, which is shown below the
 * graph.
 * 
 * @author bfvdonge
 * 
 */
public class GraphInformationPanel extends JPanel {

	private static final long serialVersionUID = -6788727587785101317L;
	protected DirectedGraph<?, ?> net;

	protected ScalableViewPanel graphVisPanel;
	protected JTabbedPane southTabs;
	protected ViewSpecificAttributeMap viewSpecificMap;

	protected JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

	public GraphInformationPanel(DirectedGraph<?, ?> net, ViewSpecificAttributeMap map, Progress progress) {
		setLayout(new BorderLayout());

		splitPane.setResizeWeight(1.);
		splitPane.setOneTouchExpandable(true);

		viewSpecificMap = map;

		this.net = net;

		graphVisPanel = ProMJGraphVisualizer.visualizeGraph(this.net, viewSpecificMap);
		splitPane.setLeftComponent(graphVisPanel);

		southTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		splitPane.setRightComponent(southTabs);


		this.add(splitPane, BorderLayout.CENTER);
	}

	public void addGraph(String label, DirectedGraph<?, ?> graph) {

	}

	protected void addComponent(String label, JComponent component) {
		splitPane.setDividerLocation(.2);
		southTabs.addTab(label, component);
	}

	public void viewSpecificMapUpdated() {
		net.signalViews();
	}

	public ScalableViewPanel getGraphVisualizationPanel() {
		return graphVisPanel;
	}
}
