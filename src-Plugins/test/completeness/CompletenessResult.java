package test.completeness;

import java.util.List;

import org.jfree.data.xy.XYSeries;

public class CompletenessResult {

	public int numberOfLogs;
	public int[] logSizes;
	public String[] logNames;

	public List<Estimator> coverageEstimators;
	public List<Estimator> unobservedEstimators;
	public XYSeries[][] total;
	public XYSeries[][] covers;
	public XYSeries[][] next;
	public XYSeries[][] unObserved;
	public int measurements;

}
