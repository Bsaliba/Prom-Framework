package test.completeness;

public interface Estimator {

	public void estimate(StatRes res);

	public String getName();

	/**
	 * References
	 * 
	 * [BoenderRinnooyKan1983] C.G.E. Boender and A.H.G. Rinnooy Kan, A Bayesian
	 * Analysis of the Number of Cells of a Multinomial Dsitribution.
	 * 
	 * [GandolfiSastri2004] Alberto Gandolfi and C.C.A. Sastri, Nonparametric
	 * Estimations about Species Not Observed in a Random Sample.
	 */

}
