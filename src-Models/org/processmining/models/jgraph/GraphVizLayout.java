package org.processmining.models.jgraph;

import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import com.fluxicon.slickerbox.util.RuntimeUtils;

public class GraphVizLayout {

	public static String getDotPath() {
		// On Windows systems, use the dot.exe distributed with ProM by default
		if (RuntimeUtils.isRunningWindows()) {
			return "dot" + System.getProperty("file.separator") + "dot.exe";
			// On Mac OS X, use the dot executable distributed within Graphviz.app (from Pixelglow)
			// by default (assumes standard installation into system-wide /Applications folder)
		} else if (RuntimeUtils.isRunningMacOsX()) {
			return "/Applications/Graphviz.app/Contents/MacOS/dot";
		} else {
			// assume UNIX-like OS with dot executable in $PATH
			return "dot";
		}
	}

	private ProcessBuilder m_pb;

	private String m_lastLog;

	private final Pattern m_nodePattern;

	private final Pattern m_edgePattern;

	private final Pattern m_splitPattern;

	private JGraph m_graph;

	private final String orientation;

	public GraphVizLayout(String orientation) {

		this.orientation = orientation;
		m_nodePattern = Pattern.compile("^\\s*(n\\d+)\\s*\\[.*pos=\"?(\\d+),(\\d+)");
		m_edgePattern = Pattern.compile("^\\s*(n\\d+)\\s*->\\s*(n\\d+)\\s*"
				+ "\\[.*pos=\"e,(\\d+),(\\d+) (\\d+,\\d+(?:(?: \\d+,\\d+){3})+)\"" + ".*lp=\"?(\\d+),(\\d+)");
		m_splitPattern = Pattern.compile("[ ,]");
	}

	public void applyLayout(JGraph g, Object[] cells) throws Exception {
		m_pb = new ProcessBuilder(getDotPath(), "-y -Tdot");
		m_pb.redirectErrorStream(true);

		m_graph = g;
		try {
			if (!callDot(cells)) {
				String message = "Error while trying to layout the graph using Graphviz.\n\n"
						+ "See the output of the process call below:\n\n" + m_lastLog;
				throw new Exception(message);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private boolean callDot(Object[] cells) throws IOException, InterruptedException {
		GraphLayoutCache layoutCache = m_graph.getGraphLayoutCache();

		Collection<CellView> nodes = new HashSet<CellView>();
		Collection<EdgeView> edges = new HashSet<EdgeView>();

		for (Object cell : cells) {
			GraphModel model = m_graph.getModel();

			if (model.isPort(cell)) {
				continue;
			} else if (model.isEdge(cell)) {
				edges.add((EdgeView) layoutCache.getMapping(cell, true));
			} else {
				nodes.add(layoutCache.getMapping(cell, true));
			}
		}

		Process dot = m_pb.start();

		Writer w = new BufferedWriter(new OutputStreamWriter(dot.getOutputStream(), "UTF-8"));
		StringBuilder dotInput = new StringBuilder();

		// building the input for graphviz
		dotInput.append("digraph {\n" + "dpi=72;\n" + "charset=\"UTF-8\";\n");
		dotInput.append("ranksep=\".0015\";\n" + "nodesep=\".0015\" \n");
		dotInput.append("rankdir=\"" + orientation + "\";\n");
		dotInput.append("edge [labeljust=l");

		Font edgesFont = GraphConstants.getFont(edges.iterator().next().getAttributes());
		String fontname = edgesFont.getFontName(Locale.getDefault());
		String fontsize = String.valueOf(edgesFont.getSize2D());
		dotInput.append(", fontname=\"" + fontname.replace("\"", "\\\"") + "\"");
		dotInput.append(", fontsize=\"" + fontsize.replace("\"", "\\\"") + "\"");

		dotInput.append("];\n");
		dotInput.append("node [shape=box, fixedsize=true");
		dotInput.append("];\n");

		Map<CellView, String> nodes2strings = new HashMap<CellView, String>();
		Map<String, CellView> strings2nodes = new HashMap<String, CellView>();
		int id = 0;

		for (CellView node : nodes) {
			String s = "n" + (++id);
			nodes2strings.put(node, s);
			strings2nodes.put(s, node);
			Rectangle2D bounds = node.getBounds();

			dotInput.append(s + " [width=" + bounds.getWidth() / 72. + ", height=" + bounds.getHeight() / 72.
					+ ", shape=");

			dotInput.append("box");

			dotInput.append(", fixedsize=true];\n");
		}

		Map<String, EdgeView> strings2edges = new HashMap<String, EdgeView>();
		for (EdgeView edge : edges) {
			GraphModel model = m_graph.getModel();

			Object source = DefaultGraphModel.getSourceVertex(model, edge.getCell());
			Object target = DefaultGraphModel.getTargetVertex(model, edge.getCell());
			CellView sourceView = layoutCache.getMapping(source, true);
			CellView targetView = layoutCache.getMapping(target, true);

			String s = nodes2strings.get(sourceView) + " -> " + nodes2strings.get(targetView);
			strings2edges.put(s, edge);
			// the dots "..." are inserted to get some more space on the left and right side of the labels
			dotInput.append(s + " [label=\"..." + edge.getCell().toString().replace("\"", "\\\"") + "...\"" + "];\n");
		}
		dotInput.append("}\n");
		w.write(dotInput.toString());
		w.close();

		// for Debugging
		//System.out.println("\nDot input:\n" + dotInput.toString());

		// parsing the output of graphviz
		Map<EdgeView, Point> labelsAbsolutePositions = new HashMap<EdgeView, Point>();
		Map<Object, Object> nestedAttributes = new HashMap<Object, Object>();

		BufferedReader r = new BufferedReader(new InputStreamReader(dot.getInputStream(), "UTF-8"));
		StringBuilder buf = new StringBuilder();
		StringBuilder longLine = null;
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			buf.append(line).append("\n");
			if (line.endsWith("\\")) {
				if (longLine == null) {
					longLine = new StringBuilder();
				}
				longLine.append(line, 0, line.length() - 1);
				continue;
			}
			if (longLine != null) {
				longLine.append(line);
				line = longLine.toString();
				longLine = null;
			}

			/*
			 * nodePattern: ^\s(n\d+)\s\[.pos="?(\d+),(\d+)"
			 */
			Matcher matcher;
			matcher = m_nodePattern.matcher(line);
			if (matcher.find()) {
				String s = matcher.group(1);
				CellView n = strings2nodes.get(s);
				int x = Integer.parseInt(matcher.group(2));
				int y = Integer.parseInt(matcher.group(3));
				Rectangle2D bounds = n.getBounds();

				x -= bounds.getWidth() / 2;
				y -= bounds.getHeight() / 2;

				Rectangle2D newBounds = new Rectangle2D.Double(x, y, bounds.getWidth(), bounds.getHeight());

				Map<Object, Object> editAttributes = new HashMap<Object, Object>();
				GraphConstants.setBounds(editAttributes, newBounds);
				nestedAttributes.put(n.getCell(), editAttributes);

				continue;
			}

			/*
			 * edgePattern: ^\s(n\d+)\s->\s(n\d+)\s\[.pos= "e,(\d+),(\d+)
			 * (\d+,\d+(?: (?:\d+,\d+){3})+)".*lp="?(\d+),(\d+)
			 */
			matcher = m_edgePattern.matcher(line);
			if (matcher.find()) {
				String s = matcher.group(1) + " -> " + matcher.group(2);
				EdgeView edge = strings2edges.get(s);
				double endx = Double.parseDouble(matcher.group(3));
				double endy = Double.parseDouble(matcher.group(4));

				String[] coords = m_splitPattern.split(matcher.group(5));
				float[] c = new float[coords.length];

				for (int i = 0; i < coords.length; ++i) {
					c[i] = Float.parseFloat(coords[i]);
				}

				int lx = Integer.parseInt(matcher.group(6));
				int ly = Integer.parseInt(matcher.group(7));

				Point lp = new Point(lx, ly);
				labelsAbsolutePositions.put(edge, lp);

				List<Point2D> points = new ArrayList<Point2D>();

				for (int i = 0; i < c.length; i += 2) {
					Point2D p = edge.getAttributes().createPoint(c[i], c[i + 1]);
					points.add(p);
				}

				points.add(new Point2D.Double(endx, endy));

				Map<Object, Object> editAttributes = new HashMap<Object, Object>();
				GraphConstants.setPoints(editAttributes, points);

				//GraphConstants.setRouting(editAttributes, GraphConstants.ROUTING_DEFAULT);
				//GraphConstants.setRouting(editAttributes, GraphConstants.ROUTING_SIMPLE);

				//            EdgeView.renderer = new GraphvizEdgeRenderer();
				//            GraphConstants.setLineStyle(editAttributes, GraphvizEdgeRenderer.STYLE_GRAPHVIZ_BEZIER);
				//GraphConstants.setLineStyle(editAttributes, GraphConstants.STYLE_BEZIER);
				//GraphConstants.setLineStyle(editAttributes, GraphConstants.STYLE_SPLINE);
				//GraphConstants.setLineStyle(editAttributes, GraphConstants.STYLE_ORTHOGONAL);

				nestedAttributes.put(edge.getCell(), editAttributes);

				continue;
			}
		}
		// finally applys the changed attributes
		layoutCache.edit(nestedAttributes);

		m_lastLog = buf.toString();
		dot.waitFor();

		// for Debugging
		//System.out.println("\nDot output:\n" + buf.toString());

		setLabelPositions(labelsAbsolutePositions);

		if (dot.exitValue() == 0) {
			return true;
		} else {
			buf.append("\n!!! exit value of graphviz: " + dot.exitValue() + " !!!\n");
			m_lastLog = buf.toString();
			return false;
		}
	}

	/**
	 * Sets the positions for the Labels. It gets absolute positions as
	 * coordinates and translates them to relative positions according to
	 * JGraph.
	 * 
	 * @param positions
	 *            A map which maps edges and thus their labels to absolute
	 *            positions
	 */
	@SuppressWarnings("unchecked")
	private void setLabelPositions(Map<EdgeView, Point> positions) {
		Map nestedAttributes = new HashMap();

		Collection keys = positions.keySet();
		for (Object key : keys) {
			Map editAttributes = new HashMap();

			EdgeView edge = (EdgeView) key;
			Point2D p = getRelativeLabelPosition(edge, positions.get(edge));

			GraphConstants.setLabelPosition(editAttributes, p);
			nestedAttributes.put(edge.getCell(), editAttributes);
		}
		// finally applys the changed attributes
		m_graph.getGraphLayoutCache().edit(nestedAttributes);
	}

	/**
	 * Transforms absolute label positions to the JGraph edge-relative
	 * positions.
	 * 
	 * @param edge
	 *            The edge to which the label belongs to
	 * @param point
	 *            The absolute position of the label
	 * @return The relative position of the label
	 */
	private Point2D getRelativeLabelPosition(EdgeView edge, Point point) {
		/*
		 * Calculation code taken from the EdgeView.EdgeHandle.mouseDragged()
		 * Method
		 */

		Point2D p = m_graph.fromScreen(point);

		double x = p.getX();
		double y = p.getY();

		Point2D p0 = edge.getPoint(0);

		double p0x = p0.getX();
		double p0y = p0.getY();

		Point2D vector = edge.getLabelVector();
		double dx = vector.getX();
		double dy = vector.getY();

		double pex = p0.getX() + dx;
		double pey = p0.getY() + dy;

		double len = Math.sqrt(dx * dx + dy * dy);
		if (len > 0) {
			double u = GraphConstants.PERMILLE;
			double posy = len * (-y * dx + p0y * dx + x * dy - p0x * dy) / (-pey * dy + p0y * dy - dx * pex + dx * p0x);
			double posx = u * (-y * pey + y * p0y + p0y * pey - p0y * p0y - pex * x + pex * p0x + p0x * x - p0x * p0x)
					/ (-pey * dy + p0y * dy - dx * pex + dx * p0x);
			p = new Point2D.Double(posx, posy);
		} else {
			p = new Point2D.Double(x - p0.getX(), y - p0.getY());
		}

		return p;
	}

}
