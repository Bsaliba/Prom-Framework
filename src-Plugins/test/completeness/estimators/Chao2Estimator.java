package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * Chao's Estimator #2.
 * <p>
 * refer to [CandolfiSastri2004]
 * 
 * @author hedong
 * 
 */
@UnobservedClassesEstimator
public class Chao2Estimator extends AbstractBaseEstimator implements Estimator {
	public Chao2Estimator() {
		super("Chao2");
	}

	public void estimate(StatRes res) {
		int n1 = res.getClassesOccurringWithFrequency(1);
		int n2 = res.getClassesOccurringWithFrequency(2);
		res.setEstimatedNumberOfClasses(1.0 * res.getObservedclasses() + 1.0 * n1 * n1 / (2 * n2 + 2) - 1.0 * n1 * n2
				/ (2 * (n2 + 1) * (n2 + 1)));
		res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
	}
}