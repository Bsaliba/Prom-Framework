package test.completeness.estimators;

import java.util.Iterator;

import org.processmining.plugins.completeness.annotations.CoverageEstimator;
import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * To estimate the magnitude of informative completeness.
 * <P>
 * Proposed in our paper.
 */
@CoverageEstimator
@UnobservedClassesEstimator
public class CompletenessEstimator extends AbstractBaseEstimator implements Estimator {

	public final static double CONFIDENCE = 0.9;
	public final static double EPSILON = 0.1;

	public CompletenessEstimator() {
		this(EPSILON, CONFIDENCE);
	}

	public CompletenessEstimator(double e, double c) {
		super("CPL");
		setEpsilon(e);
		setConfidence(c);
	}

	/**
	 * epsilon, the maximum acceptable error of the estimation.
	 * <p>
	 * The default value is 0.1. It can be reset by calling setEpsilon(ep) at
	 * running time.
	 */
	double epsilon = 0.1;
	/**
	 * Confidence level.
	 * <p>
	 * The default value is 0.95. It can be reset by calling setConfidence(cf)
	 * at running time.
	 */
	double confidence = 0.95;

	/**
	 * Set the value of epsilon.
	 * 
	 * @param ep
	 *            the value of epsilon.
	 */
	public void setEpsilon(double ep) {
		epsilon = ep;
	}

	/**
	 * Set the value of confidence level.
	 * 
	 * @param cf
	 *            the value of confidence level.
	 */
	public void setConfidence(double cf) {
		confidence = cf;
	}

	public void estimate(StatRes res) {
		//check the validity of epsilon value.
		if (epsilon <= 0 || epsilon >= 1) {
			//			System.out.println("epsilon should be in (0,1)");
			return;
		}
		if (confidence <= 0 || confidence >= 1) {
			//			System.out.println("confidence should be in (0,1)");
			return;
		}
		int loglength = 0;
		int traceclasses = 0;
		for (Iterator<Integer> itr = res.getTraces().keySet().iterator(); itr.hasNext();) {
			Integer key = itr.next();
			loglength += res.getTraces().get(key);
			traceclasses += 1;
		}
		if (1.0 * traceclasses / loglength > 0.4) {
			//			System.out.println("\t\t\tM/N%:" + 100 * traceclasses / loglength);
			return;
		}
		double divisor = 4 * epsilon * epsilon * (1 - confidence);
		double dividend = traceclasses * traceclasses * traceclasses;
		// a lower bound of the expected log length, given number of trace classes(W), error rate (epsilon), and confidence level(K).
		//long expectedlen = (long) Math.ceil(dividend / divisor);
		res.setExpectedCompleteLogLength((long) Math.ceil(dividend / divisor));
		double divisor2 = 2 * Math.sqrt(loglength * (1 - confidence));
		double dividend2 = traceclasses * Math.sqrt(traceclasses);
		// a lower bound of informative completeness of a log, given number of trace classes(W), log length(N), and confidence level(K).
		//double info = 1 - dividend2 / divisor2;
		res.setCoverageProbability(1 - dividend2 / divisor2);
		if (res.getCoverageProbability() < 0 || res.getCoverageProbability() > 1) {
			res.setCoverageProbability(-1);
			res.setNewClassProbability(-1);
		} else {
			res.setNewClassProbability(1 - res.getCoverageProbability());
		}
	}

}
