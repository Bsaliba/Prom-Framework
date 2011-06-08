package test.completeness;

import java.util.ArrayList;

import org.jfree.data.xy.XYSeries;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Aggregate Completeness Results", returnLabels = { "Completeness result" }, returnTypes = { CompletenessResult.class }, parameterLabels = { "Completeness result" }, userAccessible = true)
public class CompletenessAggregation {

	@UITopiaVariant(affiliation = "TUE", author = "Boudewijn van Dongen", email = "b.f.v.dongen@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0 })
	public CompletenessResult aggregate(PluginContext context, CompletenessResult toAggregate) {
		CompletenessResult result = new CompletenessResult();

		double logs = toAggregate.numberOfLogs;
		int measurements = toAggregate.measurements;

		result.measurements = measurements;
		result.numberOfLogs = 1;
		result.logNames = new String[] { "Aggregated " + toAggregate.numberOfLogs + " logs" };
		result.logSizes = new int[] { toAggregate.logSizes[0] };

		result.coverageEstimators = new ArrayList<Estimator>();
		for (final Estimator e : toAggregate.coverageEstimators) {
			result.coverageEstimators.add(e);
			result.coverageEstimators.add(new Estimator() {

				public String getName() {
					return "MSE " + e.getName();
				}

				public void estimate(StatRes res) {
					e.estimate(res);
				}
			});
		}
		result.unobservedEstimators = new ArrayList<Estimator>();
		for (final Estimator e : toAggregate.unobservedEstimators) {
			result.unobservedEstimators.add(e);
			result.unobservedEstimators.add(new Estimator() {

				public String getName() {
					return "MSE " + e.getName();
				}

				public void estimate(StatRes res) {
					e.estimate(res);
				}
			});

		}

		result.covers = new XYSeries[1][result.coverageEstimators.size()];
		result.next = new XYSeries[1][result.coverageEstimators.size()];

		result.total = new XYSeries[1][result.unobservedEstimators.size()];
		result.unObserved = new XYSeries[1][result.unobservedEstimators.size()];

		for (int j = 0; j < result.coverageEstimators.size(); j += 2) {
			result.covers[0][j] = new XYSeries(result.coverageEstimators.get(j / 2).getName());
			result.covers[0][j + 1] = new XYSeries("MSE " + result.coverageEstimators.get(j / 2).getName());
			result.next[0][j] = new XYSeries(result.coverageEstimators.get(j / 2).getName());
			result.next[0][j + 1] = new XYSeries("unused " + result.coverageEstimators.get(j / 2).getName());
		}
		for (int j = 0; j < result.unobservedEstimators.size(); j += 2) {
			result.total[0][j] = new XYSeries(result.unobservedEstimators.get(j / 2).getName());
			result.total[0][j + 1] = new XYSeries("MSE " + result.unobservedEstimators.get(j / 2).getName());
			result.unObserved[0][j] = new XYSeries(result.unobservedEstimators.get(j / 2).getName());
			result.unObserved[0][j + 1] = new XYSeries("unused " + result.unobservedEstimators.get(j / 2).getName());
		}

		for (int m = 0; m < result.measurements; m++) {
			// find the minimal x value at index m.
			int mx = (int) toAggregate.covers[0][0].getDataItem(m).getXValue();

			for (int j = 0; j < result.coverageEstimators.size(); j += 2) {
				double c = 0.0;
				double n = 0.0;
				double o = 0;
				double e = 0.0;

				for (int i = 0; i < toAggregate.numberOfLogs; i++) {
					if (toAggregate.covers[i][j/2].getDataItem(m).getYValue() >= 0) {
						double v = toAggregate.covers[i][j / 2].getDataItem(m).getYValue() / 100.0;
						c += v;
						double err = (v - (toAggregate.total[i][0].getDataItem(m).getYValue()) / 60.0);
						e += err * err;
						o = o + 1;
					}
				}
				result.covers[0][j].add(mx, c / o);
				result.covers[0][j + 1].add(mx, e / o);
				o = 0;
				for (int i = 0; i < toAggregate.numberOfLogs; i++) {
					if (toAggregate.next[i][j / 2].getDataItem(m).getYValue() >= 0) {
						n += toAggregate.next[i][j / 2].getDataItem(m).getYValue();
						o = o + 1;
					}
				}
				result.next[0][j].add(mx, c / o);
				result.next[0][j + 1].add(mx, -1);

			}
			for (int j = 0; j < result.unobservedEstimators.size(); j += 2) {
				double t = 0.0;
				double u = 0.0;
				double e = 0.0;
				double o = 0;

				for (int i = 0; i < toAggregate.numberOfLogs; i++) {
					if (toAggregate.total[i][j / 2].getDataItem(m).getYValue() >= 0) {
						double v = toAggregate.total[i][j / 2].getDataItem(m).getYValue();
						t += v;
						double err = (v - 60);
						e += err * err;
						o = o + 1;
					}
				}
				result.total[0][j].add(mx, t / o);
				result.total[0][j + 1].add(mx, e / o);

				o = 0;
				for (int i = 0; i < toAggregate.numberOfLogs; i++) {
					if (toAggregate.unObserved[i][j / 2].getDataItem(m).getYValue() >= 0) {
						u += toAggregate.unObserved[i][j / 2].getDataItem(m).getYValue();
						o = o + 1;
					}
				}
				result.unObserved[0][j].add(mx, u / logs);
				result.unObserved[0][j + 1].add(mx, -1);

			}
		}

		context.getFutureResult(0).setLabel("Aggregated Completeness result");

		return result;
	}
}
