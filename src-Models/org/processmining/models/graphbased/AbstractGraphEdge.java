package org.processmining.models.graphbased;

import org.processmining.models.graphbased.directed.AbstractDirectedGraphEdge;

public abstract class AbstractGraphEdge<S, T> extends AbstractGraphElement implements
		Comparable<AbstractGraphEdge<S, T>> {

	protected S source;
	protected T target;

	public AbstractGraphEdge(S source, T target) {
		super();
		this.source = source;
		this.target = target;
	}

	public int hashCode() {
		// Hashcode based on source and target, which
		// respects contract that this.equals(o) implies
		// this.hashCode()==o.hashCode()
		return source.hashCode() + 37 * target.hashCode();
	}

	public boolean equals(Object o) {
		if (!(this.getClass().equals(o.getClass()))) {
			return false;
		}
		AbstractDirectedGraphEdge<?, ?> edge = (AbstractDirectedGraphEdge<?, ?>) o;

		return edge.source.equals(source) && edge.target.equals(target);

	}

	public S getSource() {
		return source;
	}

	public T getTarget() {
		return target;
	}

}
