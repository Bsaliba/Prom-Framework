package org.processmining.models.graphbased;

public interface Expandable {

	ExpansionListener.ListenerList getExpansionListeners();

	void expand();

	void collapse();

	boolean isCollapsed();

	boolean isExpanded();
}
