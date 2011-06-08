package test.completeness.estimators;

import org.processmining.plugins.completeness.annotations.UnobservedClassesEstimator;

import test.completeness.Estimator;
import test.completeness.StatRes;

/**
 * Jackknife Estimator.
 * <p>
 * refer to [CandolfiSastri2004]
 * 
 * p(m,n)=p*(p-1)**(p-m+1)=m!/(m-n)! c(m,n)=p(m,n)/n!=m!/((m-n)!n!)
 * 
 * @author hedong
 * 
 */
@UnobservedClassesEstimator
public class JKEstimator extends AbstractBaseEstimator implements Estimator {
	private int k;

	public JKEstimator() {
		this(5);
	}

	public JKEstimator(int k) {
		super("JKE (" + k + ")");
		this.k = k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public void estimate(StatRes res) {
		double sum = 0;
		double coef = -1;
		double powerj = 1;
		double pkj = 1;
		for (int j = 1; j <= k; j++) {
			coef = coef * (-1);
			powerj = powerj * j;
			pkj = pkj * (k - j + 1);
			sum += coef * pkj / powerj * res.getClassesOccurringWithFrequency(j);
		}
		res.setEstimatedNumberOfClasses(sum);
		if (res.getEstimatedNumberOfClasses() < res.getObservedclasses()) {
			res.setEstimatedNumberOfClasses(-1);
			res.setEstimatedUnobservedClasses(-1);
		} else {
			res.setEstimatedUnobservedClasses(res.getEstimatedNumberOfClasses() - res.getObservedclasses());
		}
	}
}