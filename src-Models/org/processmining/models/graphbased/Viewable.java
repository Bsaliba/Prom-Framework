package org.processmining.models.graphbased;

import java.util.List;

public interface Viewable {

	/**
	 * Implementations should store the views as Weakreferences
	 * 
	 * @param v
	 */
	View addView(View v);

	List<View> getViews();

	void removeView(View v);

	boolean hasViews();

	void signalViews();

	void signalViews(View toIgnore);

}
