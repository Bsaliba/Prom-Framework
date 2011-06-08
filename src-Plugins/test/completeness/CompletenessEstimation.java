package test.completeness;

/**
 * Demonstrate program of log analysis: Trace coverage probability/Informative
 * completeness of event logs.
 * 
 * <P>
 * The demo will parse the given logs, calculate the occurrence frequencies of
 * traces, the estimate coverage probability of observed trace classes by means
 * of different approaches.
 * 
 * <P>
 * Coverage probability estimator: MLE, GT, ACE, BRKBayesian.
 * 
 * <p>
 * NOTE: Logs in mxml format are supported only till now.
 * 
 * <p>
 * Written by Hedong Yang.
 * <p>
 * March 29, 2011
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.plugins.completeness.annotations.CoverageEstimator;
import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

/**
 * The start point of the demo program.
 * 
 * <P>
 * It will parse the file names with wild character, e.g. '*' and '?', call the
 * log parser, and call estimators as specified.
 * 
 * @author hedong
 * 
 */

public class CompletenessEstimation {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "Fix attributes", parameterLabels = "Log", returnLabels = { "log" }, returnTypes = { XLog.class })
	public static XLog doExperiment(UIPluginContext context, XLog log) throws InstantiationException {
		XLog newLog = (XLog) log.clone();

		Map<String, String> toReplace = new HashMap<String, String>();
		findAttributable(log, newLog, toReplace);
		Iterator<XTrace> it = newLog.iterator();
		for (XTrace t : log) {
			XTrace tt = it.next();
			Iterator<XEvent> it2 = tt.iterator();
			findAttributable(t, tt, toReplace);
			for (XEvent e : t) {
				findAttributable(e, it2.next(), toReplace);
			}
		}
		for (Map.Entry<String, String> entry : toReplace.entrySet()) {
			System.out.println(entry.getKey() + "    ---->    " + entry.getValue());
		}

		return newLog;
	}

	private static <T extends XAttributable> void findAttributable(T source, T target, Map<String, String> toReplace) {
		for (String key : source.getAttributes().keySet()) {
			String newKey = toReplace.get(key);
			if (newKey == null) {
				newKey = key.replace(" ", "_");
			}
			if (!key.equals(newKey)) {
				toReplace.put(key, newKey);
				XAttribute val = target.getAttributes().get(key);
				target.getAttributes().put(newKey, val);
				target.getAttributes().remove(key);
			}
		}

	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "Automated Completeness experiment", parameterLabels = {}, returnLabels = { "Completeness result" }, returnTypes = { CompletenessResult.class })
	public static CompletenessResult doExperimentOnAllLogs(UIPluginContext context) throws Exception {
		List<XLog> logs = new ArrayList<XLog>();
		for (ProvidedObjectID id : context.getProvidedObjectManager().getProvidedObjects()) {
			if (context.getProvidedObjectManager().getProvidedObjectType(id).equals(XLog.class)) {
				logs.add((XLog) context.getProvidedObjectManager().getProvidedObjectObject(id, true));
			}
		}
		return doExperiment(context, logs.toArray(new XLog[0]));

	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "Completeness experiment", parameterLabels = "Logs", returnLabels = { "Completeness result" }, returnTypes = { CompletenessResult.class })
	public static CompletenessResult doExperiment(UIPluginContext context, XLog[] logs) throws InstantiationException,
			IllegalAccessException {
		XEventNameClassifier classifier = new XEventNameClassifier();
		CompletenessResult result = new CompletenessResult();

		Estimator obsEst = new Estimator() {

			public String getName() {
				return "Observed number of classes";
			}

			public void estimate(StatRes res) {
				res.setEstimatedNumberOfClasses(res.getObservedclasses());
				res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
				res.setCoverageProbability(1.0);
				res.setNewClassProbability(0.0);
			}
		};

		Set<Class<?>> coverageEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
				CoverageEstimator.class);
		result.coverageEstimators = new ArrayList<Estimator>();
		result.coverageEstimators.add(obsEst);
		for (Class<?> coverClass : coverageEstimatorClasses) {
			result.coverageEstimators.add((Estimator) coverClass.newInstance());
		}
		Set<Class<?>> unobservedEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
				UnobservedClassesEstimator.class);
		result.unobservedEstimators = new ArrayList<Estimator>();
		result.unobservedEstimators.add(obsEst);
		for (Class<?> unobsClass : unobservedEstimatorClasses) {
			result.unobservedEstimators.add((Estimator) unobsClass.newInstance());
		}

		result.covers = new XYSeries[logs.length][result.coverageEstimators.size()];
		result.next = new XYSeries[logs.length][result.coverageEstimators.size()];
		result.total = new XYSeries[logs.length][result.unobservedEstimators.size()];
		result.unObserved = new XYSeries[logs.length][result.unobservedEstimators.size()];

		result.measurements = 1000;
		int inc[] = new int[logs.length];
		int pmax = 0;
		for (int l = 0; l < logs.length; l++) {
			inc[l] = Math.max(1, logs[l].size() / result.measurements);
			pmax += result.measurements * result.coverageEstimators.size();
			pmax += result.measurements * result.unobservedEstimators.size();
		}
		context.getProgress().setMaximum(pmax);

		result.numberOfLogs = logs.length;
		result.logSizes = new int[logs.length];
		result.logNames = new String[logs.length];
		for (int i = 0; i < logs.length; i++) {
			result.logSizes[i] = logs[i].size();
			result.logNames[i] = context.getGlobalContext().getResourceManager().getResourceForInstance(logs[i])
					.getName();
		}

		doEstimation(context, logs, classifier, result.coverageEstimators, result.covers, result.next, inc, pmax, true);
		doEstimation(context, logs, classifier, result.unobservedEstimators, result.total, result.unObserved, inc,
				pmax, false);

		return result;

	}

	//@Visualizer
	@Plugin(name = "Visualize Completeness experiment", parameterLabels = "Completenessresult", returnLabels = { "Graph" }, returnTypes = { JComponent.class })
	public static JComponent visualize(PluginContext context, CompletenessResult result) {
		JPanel mainPanel = new JPanel(new GridLayout(1, result.numberOfLogs));
		for (int l = 0; l < result.numberOfLogs; l++) {
			CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Number of cases considered (out of "
					+ result.logSizes[l] + ")"));
			plot.setGap(10.0);

			plot.add(
					getPanel("Estimated Number of classes", l, result.unobservedEstimators.size(), result.total, 0,
							false), 1);
			plot.add(
					getPanel("Estimated Coverage (percentage)", l, result.coverageEstimators.size(), result.covers,
							100, true), 1);

			plot.setOrientation(PlotOrientation.VERTICAL);
			((XYLineAndShapeRenderer) ((XYPlot) plot.getSubplots().get(0)).getRenderer()).setSeriesPaint(0, Color.gray);
			((XYLineAndShapeRenderer) ((XYPlot) plot.getSubplots().get(0)).getRenderer()).setSeriesStroke(0,
					new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f,
							new float[] { 7.0f, 3.0f }, 0.0f));
			mainPanel
					.add(new ChartPanel(new JFreeChart(result.logNames[l], JFreeChart.DEFAULT_TITLE_FONT, plot, true)));
		}

		return mainPanel;
	}

	private static void doEstimation(UIPluginContext context, XLog[] logs, XEventNameClassifier classifier,
			List<Estimator> estimators, XYSeries[][] series1, XYSeries[][] series2, int[] inc, int pmax,
			boolean coverage) {
		int l = 0;
		for (XLog log : logs) {
			int m = 0;
			for (Estimator estimator : estimators) {
				//				Estimator estimator = (Estimator) estimatorClass.newInstance();
				series1[l][m] = new XYSeries(estimator.getName());
				series2[l][m] = new XYSeries(estimator.getName());
				m++;
			}

			int last = 0;
			CompletenessEstimation estimation = new CompletenessEstimation(log, 0, classifier);
			int in = inc[l / estimators.size()];
			for (int i = in; !context.getProgress().isCancelled() && (i < log.size()); i += in) {
				for (int j = last; j < i; j++) {
					estimation.addTrace(j);
				}
				last = i;
				m = 0;
				for (Estimator estimator : estimators) {
					context.getProgress().inc();

					//					Estimator estimator = (Estimator) estimatorClass.newInstance();
					//					context.log("Estimator: " + estimator.getName() + "  on log "
					//							+ XConceptExtension.instance().extractName(log) + "  until trace " + i);
					StatRes result;
					if (coverage) {
						result = estimateCoverage(estimation, log, i, classifier, series1[l][m], series2[l][m],
								estimator);
					} else {
						result = estimateUnobserved(estimation, log, i, classifier, series1[l][m], series2[l][m],
								estimator);
					}
					m++;
				}
			}
			for (int j = last; j < log.size(); j++) {
				estimation.addTrace(j);
			}

			m = 0;
			for (Estimator estimator : estimators) {
				//				Estimator estimator = (Estimator) estimatorClass.newInstance();
				context.log("Estimator: " + estimator.getName() + "  on full log "
						+ XConceptExtension.instance().extractName(log));
				StatRes result;
				if (coverage) {
					result = estimateCoverage(estimation, log, log.size(), classifier, series1[l][m], series2[l][m],
							estimator);
				} else {
					result = estimateUnobserved(estimation, log, log.size(), classifier, series1[l][m], series2[l][m],
							estimator);
				}
				context.getProgress().inc();
				m++;
			}
			l++;

		}
	}

	private void addTrace(int j) {
		res.addTrace(j);
	}

	private static XYPlot getPanel(String title, int log, int estimators, XYSeries[][] dataSeries, double high,
			boolean showShapes) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i = 0; i < estimators; i++) {
			XYSeries serie = dataSeries[log][i];
			if (!serie.isEmpty()) {
				dataset.addSeries(serie);
			}
		}
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setBaseShapesFilled(false);
		renderer.setBaseShapesVisible(true);
		renderer.setDrawSeriesLineAsPath(true);
		if (showShapes) {
			for (int i = 0; i < estimators; i++) {
				renderer.setSeriesStroke(i, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f,
						new float[] { 7.0f, 3.0f }, 0.0f));
			}
		}

		NumberAxis rangeAxis = new NumberAxis(title);
		rangeAxis.setAutoRangeIncludesZero(false);
		//rangeAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		return new XYPlot(dataset, null, rangeAxis, renderer);

		//		JFreeChart chart = ChartFactory.createXYLineChart(title, // chart title
		//				"", // x axis label
		//				"", // y axis label
		//				dataset, // data
		//				PlotOrientation.VERTICAL, true, // include legend
		//				true, // tooltips
		//				false // urls
		//				);
		//		chart.addSubtitle(new TextTitle(subtitle));
		//
		//		XYPlot plot = (XYPlot) chart.getPlot();
		//
		//		plot.setDomainAxes(null);
		//		plot.getDomainAxis().setAutoRange(false);
		//		plot.getDomainAxis().setRange(0, logs[log].size());
		//
		//		plot.getRangeAxis(0).setAutoRange(false);
		//		if (high > 0) {
		//			plot.getRangeAxis(0).setRange(0, high);
		//		}
		//		((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(NumberFormat.getIntegerInstance());
		//
		//		plot.setRenderer(0, new XYLineAndShapeRenderer(true, true));
		//
		//		ChartPanel p = new ChartPanel(chart);
		//
		//		return p;

	}

	private static StatRes estimateCoverage(CompletenessEstimation estimation, XLog log, int i,
			XEventClassifier classifier, XYSeries cover, XYSeries next, Estimator estimator) {
		estimation.processLog(estimator);
		//if (estimation.getResult().getCoverageProbability() >= 0.0) {
		cover.add(i, 100.0 * estimation.getResult().getCoverageProbability());
		//}
		//if (estimation.getResult().getNewClassProbability() >= 0.0) {
		next.add(i, estimation.getResult().getNewClassProbability());
		//}
		//		if (estimation.getResult().getEstimatedNumberOfClasses() >= 0) {
		//			classes.add(i, estimation.getResult().getEstimatedNumberOfClasses());
		//		}
		//		obsCl.add(i, estimation.getResult().getObservedclasses());
		return estimation.getResult();
	}

	private static StatRes estimateUnobserved(CompletenessEstimation estimation, XLog log, int i,
			XEventClassifier classifier, XYSeries total, XYSeries unObserved, Estimator estimator) {
		estimation.processLog(estimator);
		//if (estimation.getResult().getEstimatedNumberOfClasses() >= 0) {
		total.add(i, estimation.getResult().getEstimatedNumberOfClasses());
		//}
		//if (estimation.getResult().getEstimatedUnobservedClasses() >= 0) {
		unObserved.add(i, estimation.getResult().getEstimatedUnobservedClasses());
		//}

		return estimation.getResult();
	}

	private StatRes res;

	private CompletenessEstimation(XLog log, int maxTrace, XEventClassifier classifier) {
		res = new StatRes(log, maxTrace, classifier);
		//		res.displayStat();
	}

	public CompletenessEstimation(XLog log, XEventClassifier classifier) {
		this(log, log.size(), classifier);
	}

	/**
	 * Process a log file and Estimate population information based on the log.
	 * 
	 * @param logFile
	 *            the name of the log file
	 * @param ep
	 *            epsilon
	 * @param cf
	 *            confidence level
	 */
	public void processLog(Estimator estmtr) {
		res.initial4Estimating();
		estmtr.estimate(res);
	}

	public StatRes getResult() {
		return res;
	}

}
