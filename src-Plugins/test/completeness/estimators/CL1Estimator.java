package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * Chao & Lee's Estimator #1.
 * <p>
 * refer to [CandolfiSastri2004]
 * 
 * @author hedong
 * 
 */
@UnobservedClassesEstimator
public class CL1Estimator extends AbstractBaseEstimator implements Estimator {
	public CL1Estimator() {
		super("CL1");
	}

	public void estimate(StatRes res) {
		double n1 = res.getClassesOccurringWithFrequency(1);
		double n = res.getLoglength();

		double gamma2 = 0;
		for (int j = 1; j <= n; j++) {
			gamma2 += j * (j - 1) * res.getClassesOccurringWithFrequency(j);
		}
		gamma2 = n * res.getObservedclasses() / ((n - n1) * n * (n - 1)) * gamma2 - 1;
		if (gamma2 < 0)
			gamma2 = 0;
		res.setEstimatedNumberOfClasses(n * res.getObservedclasses() / (n - n1) + n * n1 / (n - n1) * gamma2);
		res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
	}
}
