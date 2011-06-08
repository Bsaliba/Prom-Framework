package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.CoverageEstimator;
import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * Boender&Rinnooy Kan's Bayesian Estimator.
 * <p>
 * refer to [BoenderRinnooyKan1983]
 * 
 * @author hedong
 * 
 */
@CoverageEstimator
@UnobservedClassesEstimator
public class BRKEstimator extends AbstractBaseEstimator implements Estimator {

	public BRKEstimator() {
		super("BRK");
	}

	public void estimate(StatRes res) {
		double n = res.getLoglength();
		double w = res.getObservedclasses();
		if (n >= w + 3) {
			res.setEstimatedNumberOfClasses((w * (n - 1) / (n - w - 2)));
			res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
		}
		if (n >= w + 2) {
			// probability next trace if of a new class
			res.setNewClassProbability(w * (w + 1) / (n * (n - 1)));

			// probability that all trace classes have been seen, i.e. K=w
			res.setCoverageProbability(1 - res.getNewClassProbability());
			//res.setCoverageProbability(f(n, w));
		}
	}

	private double f(double n, double w) {
		if (w == 0) {
			return 1.0;
		}
		return (n - 1) * (n - 2) * f(n - 1, w - 1) / ((n + w - 1) * (n + w - 2));
	}

}