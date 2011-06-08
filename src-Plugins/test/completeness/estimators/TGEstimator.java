package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.CoverageEstimator;
import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/*
 * Turing-Good estimation. <p> refer to [CandolfiSastri2004]
 */
@UnobservedClassesEstimator
@CoverageEstimator
public class TGEstimator extends AbstractBaseEstimator implements Estimator {

	public TGEstimator() {
		super("TG");
	}

	public void estimate(StatRes res) {
		res.setNewClassProbability(res.getClassesOccurringWithFrequency(1) * 1.0 / res.getLoglength());
		res.setCoverageProbability(1.0 - res.getNewClassProbability());
		if (res.getClassesOccurringWithFrequency(1) < res.getLoglength()) {
			res.setEstimatedNumberOfClasses(Math.round(res.getObservedclasses() / res.getCoverageProbability()));//valid only when trace classes being equal-probability.
			res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
		}
	}
}
