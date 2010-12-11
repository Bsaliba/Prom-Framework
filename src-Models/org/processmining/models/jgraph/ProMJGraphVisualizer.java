package org.processmining.models.jgraph;

import java.util.Map;

import javax.swing.SwingConstants;

import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.PIPInteractionPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ZoomInteractionPanel;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class ProMJGraphVisualizer {

	protected ProMJGraphVisualizer() {
	};

	public static ProMJGraphPanel visualizeGraph(DirectedGraph<?, ?> graph) {
		return visualizeGraph(graph, new ViewSpecificAttributeMap());
	}

	public static ProMJGraphPanel visualizeGraph(DirectedGraph<?, ?> graph, ViewSpecificAttributeMap map) {

		if (graph.getViews().isEmpty()) {
			// shown for the first time.
			graph.expandAll();
		}
		graph.signalViews();

		ProMGraphModel model = new ProMGraphModel(graph);
		ProMJGraph jgraph = new ProMJGraph(model, map);

		JGraphHierarchicalLayout layout = getHierarchicalLayout();
		layout.setOrientation(graph.getAttributeMap().get(AttributeMap.PREF_ORIENTATION, SwingConstants.SOUTH));

		if (!graph.isLayedOut()) {

			JGraphFacade facade = new JGraphFacade(jgraph);

			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			facade.run(layout, true);

			Map<?, ?> nested = facade.createNestedMap(true, true);

			jgraph.getGraphLayoutCache().edit(nested);
			graph.setLayedOut(true);

		}
		jgraph.repositionToOrigin();

		jgraph.setUpdateLayout(layout);

		ProMJGraphPanel panel = new ProMJGraphPanel(jgraph);

		panel.addViewInteractionPanel(new PIPInteractionPanel(panel), SwingConstants.NORTH);
		panel.addViewInteractionPanel(new ZoomInteractionPanel(panel, ScalableViewPanel.MAX_ZOOM), SwingConstants.WEST);

		return panel;

	}

	protected static JGraphHierarchicalLayout getHierarchicalLayout() {
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(false);
		layout.setCompactLayout(false);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(15);
		layout.setFixRoots(false);

		return layout;
	}

}
