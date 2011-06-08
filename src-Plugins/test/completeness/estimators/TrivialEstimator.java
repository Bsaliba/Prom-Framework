package test.completeness.estimators;

import test.completeness.Estimator;
import test.completeness.StatRes;

/*
 * Trivial estimation. <P> The observed trace classes are all trace classes.
 */
// @UnobservedClassesEstimator
// @CoverageEstimator
public class TrivialEstimator extends AbstractBaseEstimator implements Estimator {
	public void estimate(StatRes res) {
		res.setEstimatedNumberOfClasses(res.getObservedclasses());
		res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
		res.setCoverageProbability(1.0);
		res.setNewClassProbability(0.0);
	}
}
 