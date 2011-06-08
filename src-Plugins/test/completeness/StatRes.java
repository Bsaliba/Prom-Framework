package test.completeness;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * A data strucuture contains statistical information of a given log and the
 * estimation results.
 * 
 * <P>
 * All estimators will share the object of the classes, which implies that
 * result of a latter-executed estimator will overwrite those of previous
 * estimators.
 * 
 * @author hedong
 * 
 */
public class StatRes {
	private final XLog log;

	private final XEventClassifier classifier;

	/**
	 * Construct method.
	 * 
	 * @param logFile
	 *            full path/name of a log file.
	 */
	public StatRes(XLog log, int maxTrace, XEventClassifier classifier) {
		this.log = log;
		this.classifier = classifier;
		freqfreqs = new Hashtable<Integer, Integer>();

		observedclasses = 0; // ( M is the number of different cases)
		loglength = 0; // ( N is the number of cases)
		traces = new HashMap<Integer, Integer>();

		for (int i = 0; i < maxTrace; i++) {
			addTrace(i);
		}

	}

	public void addTrace(int i) {

		loglength++;
		XTrace current = log.get(i);
		int size = current.size();
		for (int j : getTraces().keySet()) {
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
					int oldOcc = getTraces().get(j);
					getTraces().put(j, getTraces().get(j) + 1);
					// now, there is one category that needs to be reduced
					freqfreqs.put(oldOcc, freqfreqs.get(oldOcc) - 1);
					freqfreqs.put(oldOcc + 1, getClassesOccurringWithFrequency(oldOcc + 1) + 1);
					return;
				}
			}
		}
		getTraces().put(i, 1);
		observedclasses++;
		// now, there is one category that needs to be reduced
		freqfreqs.put(1, getClassesOccurringWithFrequency(1) + 1);

	}

	/**
	 * The 'trace' is used to store all traces of an event log.
	 * 
	 * <p>
	 * The key points to the index of the first trace in the log of a particular
	 * class. The value indicates how many traces are in this class.
	 */
	private Map<Integer, Integer> traces;

	/**
	 * The frequency of frequency.
	 * <P>
	 * The key is the appearance times, and the value is the number of observed
	 * trace classes appearing exactly key times.
	 */
	private Hashtable<Integer, Integer> freqfreqs;

	/**
	 * The length of the log, or the size of the sample.
	 */
	private int loglength = 0;
	/**
	 * Number of observed trace classes.
	 */
	private int observedclasses = 0;
	/**
	 * W,U are number of all possible trace classes and unobserved trace classes
	 * respectively. <
	 */
	private double estimatedNumberOfClasses = -1;

	private double estimatedUnobservedClasses = -1;
	/**
	 * C,N are the coverbility of observed traces and the probability of new
	 * traces.
	 */
	private double coverageProbability = -1.0;

	private double newClassProbability = -1.0;
	/**
	 * the expected length of event log.
	 */
	private long expectedCompleteLogLength = -1;

	/**
	 * Preparation for a new estimation.
	 */
	public void initial4Estimating() {
		setEstimatedNumberOfClasses(-1);
		setEstimatedUnobservedClasses(-1);
		setCoverageProbability(-1.0);
		setNewClassProbability(-1.0);
		setExpectedCompleteLogLength(-1);
	}

	/**
	 * Check the number of observed trace classes that appearing $freq$ times
	 * exactly.
	 * 
	 * @param freq
	 *            the appearing time of an observed trace class.
	 * @return the number of observed trace classes.
	 */
	public int getClassesOccurringWithFrequency(int freq) {
		if (freqfreqs.containsKey(freq))
			return freqfreqs.get(freq);
		return 0;
	}

	/**
	 * output information of statistics.
	 */
	public void displayStat() {
		System.out.print("\ttotal:" + getLoglength());
		System.out.println("\tclasses:" + getObservedclasses() + "\t");
	}

	/**
	 * display the estimation result
	 */
	public void displayEstimation(boolean success) {
		PrintStream stream;
		if (success) {
			stream = System.out;
		} else {
			stream = System.err;
		}
		if (this.getEstimatedUnobservedClasses() >= 0) {
			stream.println("Unobserved classes:    " + String.format("%1$9d", this.getEstimatedUnobservedClasses())
					+ "   Total trace classes:      " + String.format("%1$9d", this.getEstimatedNumberOfClasses()));
		}
		if (this.getNewClassProbability() >= 0) {
			stream.println("New Trace probability: " + String.format("%1$1.7f", this.getNewClassProbability())
					+ "   Coverability of observed: " + String.format("%1$1.7f", this.getCoverageProbability()));
		}
	}

	public Map<Integer, Integer> getTraces() {
		return traces;
	}

	public int getLoglength() {
		return loglength;
	}

	public int getObservedclasses() {
		return observedclasses;
	}

	public void setEstimatedNumberOfClasses(double w) {
		estimatedNumberOfClasses = w;
	}

	public double getEstimatedNumberOfClasses() {
		return estimatedNumberOfClasses;
	}

	public void setEstimatedUnobservedClasses(double u) {
		estimatedUnobservedClasses = u;
	}

	public double getEstimatedUnobservedClasses() {
		return estimatedUnobservedClasses;
	}

	public void setCoverageProbability(double c) {
		coverageProbability = c;
	}

	public double getCoverageProbability() {
		return coverageProbability;
	}

	public void setNewClassProbability(double n) {
		newClassProbability = n;
	}

	public double getNewClassProbability() {
		return newClassProbability;
	}

	public void setExpectedCompleteLogLength(long l) {
		expectedCompleteLogLength = l;
	}

	public long getExpectedCompleteLogLength() {
		return expectedCompleteLogLength;
	}

}