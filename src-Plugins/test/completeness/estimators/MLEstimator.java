package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.CoverageEstimator;
import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/*
 * Maximum Likelihood Estimation. <P>under the condition where probability of
 * each trace class is equal to each other. <p> refer to [CandolfiSastri2004]
 */
@CoverageEstimator
@UnobservedClassesEstimator
public class MLEstimator extends AbstractBaseEstimator implements Estimator {

	public MLEstimator() {
		super("MLE");
	}

	public static int MAXITER = 10000000;

	public void estimate(StatRes res) {
		double res1, res2;
		double T = res.getObservedclasses();
		res1 = T * Math.exp(-res.getLoglength() * 1.0 / T);
		long i = 0;
		for (;; T++) {
			if (i++ > MAXITER) {
				return;
			}
			res2 = res.getObservedclasses() - T * (1 - Math.exp(-res.getLoglength() * 1.0 / T));
			if (res2 < 0) {
				if (Math.abs(res2) > res1) {
					T--;
				}
				break;
			} else {
				res1 = res2;
			}
		}
		res.setEstimatedNumberOfClasses(T);
		res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
		res.setCoverageProbability(res.getObservedclasses() * 1.0 / res.getEstimatedNumberOfClasses());
		res.setNewClassProbability(1 - res.getCoverageProbability());
	}
}
