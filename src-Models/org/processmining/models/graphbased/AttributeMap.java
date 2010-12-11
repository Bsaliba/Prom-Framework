package org.processmining.models.graphbased;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AttributeMap {
	private final static String PREFIX = "ProM_Vis_attr_";

	public enum ArrowType {
		ARROWTYPE_CLASSIC(PREFIX + "arrow_classic"), ARROWTYPE_TECHNICAL(PREFIX + "arrow_tech"), ARROWTYPE_SIMPLE(
				PREFIX + "arrow_simple"), ARROWTYPE_DIAMOND(PREFIX + "arrow_diamond"), ARROWTYPE_CIRCLE(PREFIX
				+ "arrow_circle"), ARROWTYPE_LINE(PREFIX + "arrow_line"), ARROWTYPE_DOUBLELINE(PREFIX + "arrow_double"), ARROWTYPE_NONE(
				PREFIX + "arrow_none");

		ArrowType(String s) {
		}
	}

	public final static String SHAPE = PREFIX + "shape";
	public final static String FILLCOLOR = PREFIX + "fillcolor";
	public final static String ICON = PREFIX + "icon";
	public final static String BORDERWIDTH = PREFIX + "border";
	public final static String LABEL = PREFIX + "label";
	public final static String TOOLTIP = PREFIX + "tooltip";
	public final static String EDGESTART = PREFIX + "edgestart";
	public final static String EDGESTARTFILLED = PREFIX + "edgeStartFilled";
	public final static String EDGEEND = PREFIX + "edge end";
	public final static String EDGEENDFILLED = PREFIX + "edgeEndFilled";
	public final static String LABELVERTICALALIGNMENT = PREFIX + "labelVerticalAlignment";
	public final static String PORTOFFSET = PREFIX + "portOffset";
	public final static String EDGECOLOR = PREFIX + "edgeColor"; // added by arya
	public final static String STROKECOLOR = PREFIX + "strokeColor"; // added by arya

	public final static String INSET = PREFIX + "inset"; // added by jribeiro
	public final static String STROKE = PREFIX + "stroke"; // added by jribeiro
	public final static String DASHPATTERN = PREFIX + "dashPattern"; // added by jribeiro
	public final static String DASHOFFSET = PREFIX + "dashOffset"; // added by jribeiro
	public final static String LABELCOLOR = PREFIX + "labelColor"; // added by jribeiro
	public final static String LABELALONGEDGE = PREFIX + "labelAlongEdge"; // added by jribeiro

	/**
	 * A Float representing the linewidth of a line.
	 */
	public final static String LINEWIDTH = PREFIX + "lineWidth";

	/**
	 * A List<java.awt.geom.Point2D> of points, which are the inner points of
	 * the spline.
	 * 
	 */
	public final static String EDGEPOINTS = PREFIX + "edgepoints";
	public final static String STYLE = PREFIX + "style";

	public final static String POLYGON_POINTS = PREFIX + "polygonpoints";

	/**
	 * a get on size returns a java.awt.geom.Dimension2D.
	 */
	public static final String SIZE = PREFIX + "size";

	/**
	 * a get on size returns a java.awt.geom.Point2D.
	 */
	public static final String POSITION = PREFIX + "position";
	public static final String SQUAREBB = PREFIX + "squareBB";
	public static final String RESIZABLE = PREFIX + "resizable";
	public static final String AUTOSIZE = PREFIX + "autosize";
	public static final String SHOWLABEL = PREFIX + "showLabel";
	public static final String MOVEABLE = PREFIX + "movable"; // added by arya

	/**
	 * This should be set to SwingConstants.SOUTH, SwingConstants.WEST and so
	 * on. SwingConstants.NORTH means the graph prefers drawn Top-Down
	 * SwingConstants.WEST means the graph prefers drawn Left to Right
	 */
	public static final String PREF_ORIENTATION = PREFIX + "orientation";
	public static final String LABELHORIZONTALALIGNMENT = PREFIX + "horizontal alignment";

	private final AttributeChangeListener owner;
	private final Map<String, Object> mapping = Collections.synchronizedMap(new LinkedHashMap<String, Object>());

	public AttributeMap(AttributeChangeListener owner) {
		this.owner = owner;
	}

	public AttributeChangeListener getOwner() {
		return owner;
	}

	public Object get(String key) {
		return mapping.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key, T defaultValue) {
		synchronized (mapping) {
			Object o = mapping.get(key);
			if (o != null) {
				return (T) o;
			}
			if (mapping.containsKey(key)) {
				return null;
			} else {
				return defaultValue;
			}
		}
	}

	public void clear() {
		mapping.clear();
	}

	public Set<String> keySet() {
		return mapping.keySet();
	}

	/**
	 * This method updates the map and signals the owner. The origin is passed
	 * in this update, to make sure that no unnecessary updates are performed
	 * 
	 * @param key
	 * @param value
	 * @param origin
	 * @return
	 */
	public boolean put(String key, Object value) {
		Object old;
		synchronized (mapping) {
			old = mapping.get(key);
			mapping.put(key, value);
		}
		if (value == old) {
			return false;
		}
		if ((value == null) || (old == null) || !value.equals(old)) {
			if (owner != null) {
				owner.attributeChanged(key, old, value, this);
			}
			return true;
		}
		return false;
	}

	public void remove(String key) {
		Object old;
		synchronized (mapping) {
			old = mapping.get(key);
			mapping.remove(key);
		}
		if (owner != null) {
			owner.attributeChanged(key, old, null, this);
		}
	}

	public boolean containsKey(String key) {
		return mapping.containsKey(key);
	}

}