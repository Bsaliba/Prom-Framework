package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * Chao & Lee's Estimator #2.
 * <p>
 * refer to [CandolfiSastri2004]
 * 
 * @author hedong
 * 
 */
@UnobservedClassesEstimator
public class CL2Estimator extends AbstractBaseEstimator implements Estimator {
	public CL2Estimator() {
		super("CL2");
	}

	public void estimate(StatRes res) {
		double n1 = res.getClassesOccurringWithFrequency(1);
		double n = res.getLoglength();

		double gamma1 = 0;
		for (int j = 1; j <= n; j++) {
			gamma1 += j * (j - 1) * res.getClassesOccurringWithFrequency(j);
		}
		double gamma2 = 1.0 * n * res.getObservedclasses() / ((n - n1) * n * (n - 1)) * gamma1 - 1;
		if (gamma2 < 0)
			gamma2 = 0;
		double gamma3 = n1 * gamma2 / ((n - 1) * (n - n1));
		gamma3 = gamma3 * gamma2;
		if (gamma3 < 0)
			gamma3 = 0;
		res.setEstimatedNumberOfClasses(n * res.getObservedclasses() / (n - n1) + n * n1 / (n - n1) * gamma3);
		res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
	}
}