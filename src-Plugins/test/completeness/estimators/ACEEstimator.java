package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.CoverageEstimator;
import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/*
 * ACE estimation. <p> refer to [CandolfiSastri2004]
 */
@CoverageEstimator
@UnobservedClassesEstimator
public class ACEEstimator extends AbstractBaseEstimator implements Estimator {

	public ACEEstimator() {
		super("ACE");
	}

	public void estimate(StatRes res) {
		double Nr = 0, nr = 0, n3 = 0;
		for (int i = 1; i < 11; i++) {
			Nr += res.getClassesOccurringWithFrequency(i);
			nr += i * res.getClassesOccurringWithFrequency(i);
			n3 += i * (i - 1) * res.getClassesOccurringWithFrequency(i);
		}
		if (Nr == nr) {
			res.setNewClassProbability(0);
		} else {
			res.setNewClassProbability(Nr / nr);
		}
		res.setCoverageProbability(1.0 - res.getNewClassProbability());
		int n1 = res.getClassesOccurringWithFrequency(1);
		double gamma2 = Math.max(nr * Nr * n3 / ((nr - n1) * nr * (nr - 1)) - 1, 0);
		long nabund = 0;
		for (int i = 11; i <= res.getLoglength(); i++) {
			nabund += res.getClassesOccurringWithFrequency(i);
		}
		if ((n1 == nr)) {
			return;
		} else {
			res.setEstimatedNumberOfClasses(nabund + Nr / res.getCoverageProbability() + nr * n1 * gamma2 / (nr - n1));
		}
		res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());

	}
}
