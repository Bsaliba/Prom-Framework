package org.processmining.models.graphbased;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AbstractGraph extends AbstractGraphElement {

	private transient List<WeakReference<View>> views = new ArrayList<WeakReference<View>>(2);

	protected final NodeID id = new NodeID();

	protected boolean isLayedOut;

	public AbstractGraph() {
		super();
		isLayedOut = true;
	}

	public boolean isLayedOut() {
		return isLayedOut;
	}

	public void setLayedOut(boolean isLayedOut) {
		this.isLayedOut = isLayedOut;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.models.graphbased.GraphElement#addView(org.processmining
	 * .models.graphbased.View)
	 */
	public synchronized View addView(View v) {
		views.add(new WeakReference<View>(v));
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.models.graphbased.GraphElement#removeView(org.processmining
	 * .models.graphbased.View)
	 */
	public synchronized void removeView(View v) {
		Iterator<WeakReference<View>> it = views.iterator();
		while (it.hasNext()) {
			WeakReference<View> ref = it.next();
			View view = ref.get();
			if ((view == null) || ref.isEnqueued() || view.equals(v)) {
				it.remove();
			}
		}
	}

	public boolean hasViews() {
		return !getViews().isEmpty();
	}

	public List<View> getViews() {
		ArrayList<View> list = new ArrayList<View>();
		Iterator<WeakReference<View>> it = views.iterator();
		while (it.hasNext()) {
			WeakReference<View> ref = it.next();
			View view = ref.get();
			if ((view == null) || ref.isEnqueued()) {
				it.remove();
			} else {
				list.add(view);
			}
		}
		return Collections.unmodifiableList(list);
	}

	public boolean equals(Object o) {
		if (!(o instanceof AbstractGraph)) {
			return false;
		}
		AbstractGraph net = (AbstractGraph) o;
		return net.id.equals(id);
	}

	protected synchronized <T> T removeNodeFromCollection(Collection<T> collection, T object) {
		for (T toRemove : collection) {
			if (toRemove.equals(object)) {
				collection.remove(toRemove);
				graphElementRemoved(object);
				return toRemove;
			}
		}
		return null;
	}

	/**
	 * Returns the edges from source to target, contained in the given
	 * collection
	 * 
	 * @param <T>
	 *            The type of edges
	 * @param source
	 *            the source node
	 * @param target
	 *            the target node
	 * @param collection
	 *            the collection of edges to search through
	 * @return
	 */
	protected <T extends AbstractGraphEdge<?, ?>> Collection<T> getEdges(AbstractGraphNode source,
			AbstractGraphNode target, Collection<T> collection) {
		Collection<T> s2t = new HashSet<T>();
		for (T a : collection) {
			if (a.getSource().equals(source) && a.getTarget().equals(target)) {
				s2t.add(a);
			}
		}
		return Collections.unmodifiableCollection(s2t);

	}

	protected synchronized <T extends AbstractGraphEdge<?, ?>> T removeFromEdges(AbstractGraphNode source,
			AbstractGraphNode target, Collection<T> collection) {
		for (T a : collection) {
			if (a.getSource().equals(source) && a.getTarget().equals(target)) {
				collection.remove(a);
				graphElementRemoved(a);
				return a;
			}
		}
		return null;
	}

	public int hashCode() {
		return id.hashCode();
	}

	private final Set<Object> elementsChanged = new HashSet<Object>();
	private final Set<Object> elementsRemoved = new HashSet<Object>();
	private final Set<Object> elementsAdded = new HashSet<Object>();

	private int signallerSize = 0;

	public void signalViews() {
		signalViews(null);
	}

	public synchronized void signalViews(final View toIgnore) {

		signallerSize++;

		// Copy the necessary changes to local sets
		final Set<Object> toRemove = new HashSet<Object>(elementsRemoved);

		final Set<Object> toAdd = new HashSet<Object>(elementsAdded);
		toAdd.removeAll(toRemove);

		final Set<Object> toChange = new HashSet<Object>(elementsChanged);
		toChange.removeAll(toRemove);
		toChange.removeAll(toAdd);

		// clear the global sets.
		elementsAdded.clear();
		elementsRemoved.clear();
		elementsChanged.clear();

		handleViewSignalling(toIgnore, toRemove, toAdd, toChange);

	}

	private synchronized void handleViewSignalling(View toIgnore, Set<Object> toRemove, Set<Object> toAdd,
			Set<Object> toChange) {

		int i = 0;
		for (View view : getViews()) {
			i++;
			if (view == toIgnore) {
				continue;
			}

			if (!toRemove.isEmpty()) {
				view.removed(toRemove);
			}

			if (!toAdd.isEmpty()) {
				view.added(toAdd);
			}

			if (!toChange.isEmpty()) {
				view.update(toChange);
			}

		}

		// clear the global update set to remove updates to nodes that no longer exist.
		elementsChanged.removeAll(toRemove);

		// Trigger a layout in the first view. This should trigger changes in
		// all
		// nodes/edges and hence all other views should receive a message trough
		// a second call to signalViews.
		if (!isLayedOut && (signallerSize == 1)) {
			for (View view : getViews()) {
				if (view.doViewLayout()) {
					assert (isLayedOut);
					break;
				}
			}
		}
		signallerSize--;
	}

	public synchronized void graphElementAdded(Object element) {
		isLayedOut = false;
		elementsAdded.add(element);
	}

	public synchronized void graphElementRemoved(Object element) {
		elementsRemoved.add(element);
	}

	public synchronized void graphElementChanged(Object element) {
		elementsChanged.add(element);
	}

	public synchronized void attributeChanged(String key, Object oldValue, Object newValue, AttributeMap map) {
		graphElementChanged(this);
	}

	@SuppressWarnings("unchecked")
	public Rectangle2D getBounds(Collection<?> elements) {
		double x = Double.MAX_VALUE, y = Double.MAX_VALUE, w = 0, h = 0;

		for (Object element : elements) {
			if (element instanceof AbstractGraphNode) {
				AbstractGraphNode node = (AbstractGraphNode) element;
				Point2D pos = (Point2D) node.getAttributeMap().get(AttributeMap.POSITION);
				if (pos == null) {
					continue;
				}
				x = Math.min(x, pos.getX());
				y = Math.min(y, pos.getY());
				Dimension dim = (Dimension) node.getAttributeMap().get(AttributeMap.SIZE);
				if (dim == null) {
					continue;
				}
				w = Math.max(w, pos.getX() + dim.getWidth());
				h = Math.max(h, pos.getY() + dim.getHeight());
			} else if (element instanceof AbstractGraphEdge) {
				AbstractGraphEdge<?, ?> edge = (AbstractGraphEdge<?, ?>) element;
				List<Point2D> points = (List<Point2D>) edge.getAttributeMap().get(AttributeMap.EDGEPOINTS);
				if (points == null) {
					continue;
				}
				for (Point2D point : points) {
					x = Math.min(x, point.getX());
					y = Math.min(y, point.getY());

					w = Math.max(w, point.getX());
					h = Math.max(h, point.getY());
				}
			}
		}

		return new Rectangle2D.Double(x, y, w - x, h - y);
	}

	private Object readResolve() {
		// do what you need to do here
		// System.out.println("After instantiating MyExecutor");
		views = new ArrayList<WeakReference<View>>(2);
		// at the end returns itself
		return this;
	}
}
