package org.processmining.models.graphbased.directed;

/**
 * Interface to represent a directed graph element that can be the child of a
 * node. (E.g.: a task in a subprocess.)
 * 
 * @author Remco Dijkman
 * 
 */
public interface ContainableDirectedGraphElement {

	ContainingDirectedGraphNode getParent();

}
