package org.processmining.models.graphbased;

import java.util.Set;

public interface View {

	void update(Set<?> changedElements);

	void added(Set<?> changedElements);

	void removed(Set<?> changedElements);

	boolean doViewLayout();
}
