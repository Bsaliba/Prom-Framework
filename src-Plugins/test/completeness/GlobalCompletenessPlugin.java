/**
 * Demonstrate program of log analysis: Informative completeness of event logs.
 * 
 * <p>
 * NOTE: Only logs in mxml format are supported.
 * 
 * <p>
 * Written by Hedong Yang.
 * <p>
 * March 1, 2010
 */
package test.completeness;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.metrics.LogMetricResult;

import test.completeness.estimators.BRKEstimator;


/**
 * To estimate the informative completeness of event logs for process mining.
 * 
 * <p>
 * The core part of the program is an inequality, $M*M*M/[4*e*e*(1-K)] < N$,
 * where
 * <UL>
 * <li>$N$ is the number of traces in an event log,
 * <li>$M$ is the number of trace classes of the event log,
 * <li>$e$ is the maximum acceptable error of the estimation which is
 * $\varepsilon$ in our paper, and
 * <li>$K$ is the confidence level of giving an estimation result.
 * </UL>
 * <p>
 * In the program, an event log will be parsed and each of traces is represented
 * with a String, namely a sequence of task names.
 * <p>
 * For more detail please refer to our paper.
 * 
 * @author hedong
 * 
 */
@Plugin(name = "Completeness: Global Completeness", parameterLabels = "Log", returnLabels = { "Global Completeness Metric" }, returnTypes = GlobalCompletenessMetric.class, mostSignificantResult = -1)
public class GlobalCompletenessPlugin {

	private int traceClasses;
	private int[] traceClassesOccurrence;
	private int logLength;

	private final static double DEFAULT_CONF = 0.9;
	private final static double DEFAULT_ERROR = 0.2;

	@PluginVariant(requiredParameterLabels = { 0 })
	//@LogMetaDataVariant
	public LogMetricResult requestConfidenceAndError(UIPluginContext context, XLog log) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		Progress progress = context.getProgress();
		progress.setMaximum(log.size() * log.size() + 2);

		CompletenessEstimation estimation = new CompletenessEstimation(log, classifier);
		estimation.processLog(new BRKEstimator());
		
		

		return null;

		//		GlobalCompletenessParametersUI par = new GlobalCompletenessParametersUI(DEFAULT_CONF, DEFAULT_ERROR);
		//		if (context.showConfiguration("Provide parameters for global completeness", par) != InteractionResult.CANCEL) {
		//
		//			progress.inc();
		//			return computeCompleteness(log, par.getMaxError(), par.getConfidence());
		//		} else {
		//			return null;
		//		}

	}

	private void preProcess(XLog log, XEventClassifier classifier, Progress progress) {
		traceClasses = 0; // ( M is the number of different cases)
		logLength = log.size(); // ( N is the number of cases)

		Map<Integer, Integer> classOcc = new HashMap<Integer, Integer>();

		int pr = -logLength + 1;
		outer: for (int i = 0; i < log.size(); i++) {
			pr += logLength;
			progress.setValue(pr);

			XTrace current = log.get(i);
			int size = current.size();
			for (int j = 0; j < i; j++) {
				progress.inc();
				XTrace previous = log.get(j);

				// quick check for equal length
				if (previous.size() == size) {
					int k = 0;
					while (k < size && classifier.sameEventClass(previous.get(k), current.get(k))) {
						k++;
					}
					if (k == size) {
						// current equals previous, hence
						// we can skip current and go to next.
						if (classOcc.containsKey(j)) {
							classOcc.put(j, classOcc.get(j) + 1);
						} else {
							classOcc.put(j, 1);
						}
						continue outer;
					}
				}
			}
			classOcc.put(i, 1);
			// no previous trace was equal, hence increment trace class
			traceClasses++;
		}
		traceClassesOccurrence = new int[traceClasses];
		int j = 0;
		for (Integer i : classOcc.keySet()) {
			traceClassesOccurrence[j++] = classOcc.get(i);
		}
		Arrays.sort(traceClassesOccurrence);

		int max = traceClassesOccurrence[traceClassesOccurrence.length - 1];
		int[] classesOccuringNTimes = new int[max + 1];
		int last = 0;
		int occ = traceClassesOccurrence[0];
		for (int i = 1; i < traceClassesOccurrence.length; i++) {
			if (traceClassesOccurrence[i] == occ) {
				continue;
			}
			classesOccuringNTimes[occ] = i - last;
			last = i;
			occ = traceClassesOccurrence[i];
		}
		classesOccuringNTimes[occ] = traceClassesOccurrence.length - last;

		System.out.println("Trace class occurrences: " + Arrays.toString(traceClassesOccurrence));
		System.out.println("Trace classes appearing N times: " + Arrays.toString(classesOccuringNTimes));

		// compute \gamma^2 of Chao and Lee

		double sum = 0;
		for (j = 1; j <= max; j++) {
			sum += (double) (j * (j - 1) * classesOccuringNTimes[j]) / (double) (logLength * (logLength - 1));
		}
		sum *= (double) (logLength * traceClasses) / (double) (logLength - classesOccuringNTimes[1]);
		sum = sum - 1;

		double gamma1 = Math.max(sum, 0);

		System.out.println("gamma1^2 = " + gamma1);

		sum = 0;
		for (j = 1; j <= max; j++) {
			sum += (double) (j * (j - 1) * classesOccuringNTimes[j])
					/ (double) ((logLength - 1) * (logLength - classesOccuringNTimes[1]));
		}
		sum *= classesOccuringNTimes[1];
		sum = sum + 1;
		sum *= gamma1;

		double gamma2 = Math.max(sum, 0);

		System.out.println("gamma2^2 = " + gamma2);

		double Tcl1 = (double) (logLength * traceClasses) / (double) (logLength - classesOccuringNTimes[1]);
		Tcl1 += (gamma1 * logLength * classesOccuringNTimes[1]) / (logLength - classesOccuringNTimes[1]);

		System.out.println("N = " + traceClasses);
		System.out.println("Tcl1 = " + Tcl1);

		double Tcl2 = (double) (logLength * traceClasses) / (double) (logLength - classesOccuringNTimes[1]);
		Tcl2 += (gamma2 * logLength * classesOccuringNTimes[1]) / (logLength - classesOccuringNTimes[1]);

		System.out.print("The number of trace classes is estimated to be: ");
		System.out.println("T \\approx Tcl2 = " + Tcl2);

		System.out.print("The number of trace classes is underestimated by: ");
		System.out.println("T \\geq Ttg = " + traceClasses
				/ (1.0 - ((double) classesOccuringNTimes[1]) / ((double) logLength)));

		System.out.println("Done");

	}

	private GlobalCompletenessMetric computeCompleteness(XLog log, double epsilon, double confidence) {

		// in the paper, K == confidence

		double divisor = 4 * epsilon * epsilon * (1 - confidence);
		double dividend = traceClasses * traceClasses * traceClasses;
		// a lower bound of the expected log length, given number of trace
		// classes(W), error rate (epsilon), and confidence level(K).
		long expectedlen = (long) Math.ceil(dividend / divisor);

		double divisor2 = 2 * Math.sqrt(logLength * (1 - confidence));
		double dividend2 = traceClasses * Math.sqrt(traceClasses);
		// a lower bound of informative completeness of a log, given number of
		// trace classes(W), log length(N), and confidence level(K).
		double info = 1 - dividend2 / divisor2;

		if (info < 0) {
			info = 0;
		}

		// String sinfo = "n/a";
		// if (info > 0.0) {
		// sinfo = "" + (info * 100) + "%";
		// }
		//
		// System.out.println("");
		// System.out.println("Number of observed trace classes:........... "
		// + traceClasses);
		// System.out.println("Log length:................................. "
		// + logLength);
		// System.out.println("Maximum error:.............................. "
		// + epsilon);
		// System.out.println("Minimum confidence level:................... "
		// + confidence);
		// System.out.println("");
		// System.out.println("Global completeness can be assumed:......... "
		// + (expectedlen <= logLength));
		// System.out.println("Ratio between observed and possible classes: "
		// + sinfo);
		// System.out.println("");
		// System.out.println("Minimal log length for global completeness:. "
		// + expectedlen);
		// System.out.println("Ratio between observed and possible classes: "
		// + info);

		return new GlobalCompletenessMetric(log, info, epsilon, confidence, expectedlen);
	}

}
