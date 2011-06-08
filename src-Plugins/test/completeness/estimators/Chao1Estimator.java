package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * Chao's Estimator #1.
 * <p>
 * refer to [CandolfiSastri2004]
 * 
 * @author hedong
 * 
 */
@UnobservedClassesEstimator
public class Chao1Estimator extends AbstractBaseEstimator implements Estimator {

	public Chao1Estimator() {
		super("Chao1");
	}

	public void estimate(StatRes res) {
		if (res.getClassesOccurringWithFrequency(2) > 0) {
			res.setEstimatedNumberOfClasses(res.getObservedclasses() + res.getClassesOccurringWithFrequency(1)
					* res.getClassesOccurringWithFrequency(1) / (2 * res.getClassesOccurringWithFrequency(2)));
			res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
		}

	}
}