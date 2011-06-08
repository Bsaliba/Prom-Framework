package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * GS Bayesian Estimator.
 * <p>
 * refer to [CandolfiSastri2004]
 * 
 * @author hedong
 * 
 */
@UnobservedClassesEstimator
public class GS2Estimator extends AbstractBaseEstimator implements Estimator {
	public GS2Estimator() {
		super("GS2");
	}

	public void estimate(StatRes res) {
		double n1 = res.getClassesOccurringWithFrequency(1);
		double n = res.getLoglength();
		double N = res.getObservedclasses();
		double gamma2 = -n * n1 - N * n1 + n1 * n1 + n1
				* Math.sqrt(5 * n * n + 2 * n * (N - 3 * n1) + (N - n1) * (N - n1));
		gamma2 = gamma2 / (2 * n * n1);
		if ((n1 == n) || (gamma2 == 0)) {
			return;
		}
		double c = n * N / (n - n1) + n * n1 / (n - n1) * gamma2;
		if (c >= N) {
			res.setEstimatedNumberOfClasses(c);
			res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
		}
	}
}