package test.completeness;

import java.util.Map;

import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.log.metrics.LogMetricResult;
import org.processmining.plugins.log.metrics.LogMetricType;
import org.processmining.plugins.log.metrics.extensions.XCompletenessMetadataExtension;

public class GlobalCompletenessMetric extends LogMetricResult {

	public final static String NAME = "GlobalCompleteness";
	public final static String KEY = "GlobalCompleteness";
	private final double confidence;
	private final double maxError;
	private final long minimalLength;

	public GlobalCompletenessMetric(XLog originalLog, double value,
			double maxError, double confidence, long minimalLength) {
		super(originalLog, LogMetricType.COMPLETENESS, NAME, NAME, value);
		this.minimalLength = minimalLength;
		synchronized (this) {
			this.maxError = maxError;
			this.confidence = confidence;
		}

	}

	@Override
	public synchronized Map<String, XAttribute> getLogAttributes() {
		Map<String, XAttribute> attributes = super.getLogAttributes();

		XAttribute completenessAttribute = attributes.get(
				XCompletenessMetadataExtension.EXTENSION_KEY).getAttributes()
				.get(getFullName());

		completenessAttribute.getAttributes().put(
				"confidence",
				XFactoryRegistry.instance().currentDefault()
						.createAttributeContinuous("confidence", confidence,
								null));
		completenessAttribute.getAttributes().put(
				"maxError",
				XFactoryRegistry.instance().currentDefault()
						.createAttributeContinuous("maxError", maxError, null));
		completenessAttribute.getAttributes().put(
				"minLength",
				XFactoryRegistry.instance().currentDefault()
						.createAttributeDiscrete("minLength", minimalLength,
								null));

		return attributes;
	}
}
