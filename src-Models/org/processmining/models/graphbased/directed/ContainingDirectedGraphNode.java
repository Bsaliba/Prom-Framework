package org.processmining.models.graphbased.directed;

import java.util.Set;

/**
 * Interface to represent a directed graph node that contain other elements.
 * 
 * @author Remco Dijkman
 * 
 */
public interface ContainingDirectedGraphNode extends DirectedGraphNode {

	Set<? extends ContainableDirectedGraphElement> getChildren();

	void addChild(ContainableDirectedGraphElement child);
}
