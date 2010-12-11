package test;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;

@Plugin(name = "Remove internal edge points", parameterLabels = { "Graph" }, returnLabels = {}, returnTypes = {}, userAccessible = true, mostSignificantResult = -1)
public class RemoveAllEdgePoints {

	@SuppressWarnings("unchecked")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0 })
	public static void removeAll(PluginContext context, Object graph) {
		if (graph instanceof DirectedGraph) {
			removeAll((DirectedGraph) graph);
			removeAll((DirectedGraph) graph);
			removeAll((DirectedGraph) graph);
		}
	}

	protected static void removeAll(DirectedGraph<?, ?> graph) {
		for (DirectedGraphEdge<?, ?> edge : graph.getEdges()) {
			edge.getAttributeMap().put(AttributeMap.EDGEPOINTS, new ArrayList<Point2D>(0));
		}
		graph.signalViews();
	}

}
