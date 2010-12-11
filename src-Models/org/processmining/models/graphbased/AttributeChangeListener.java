package org.processmining.models.graphbased;

import java.util.EventListener;

public interface AttributeChangeListener extends EventListener {

	void attributeChanged(String key, Object oldValue, Object newValue, AttributeMap map);

}
