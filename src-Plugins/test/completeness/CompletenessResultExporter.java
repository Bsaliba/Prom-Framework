package test.completeness;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@UIExportPlugin(description = "Export completeness result as CSV file", extension = "csv")
@Plugin(name = "CSV export (Completeness result)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Completeness result", "File" }, userAccessible = true)
public class CompletenessResultExporter {

	public final static String SEPCHAR = ",";

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public static void export(PluginContext context, CompletenessResult result, File file) throws IOException {

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		String line = "Log" + SEPCHAR + " number of traces considered" + SEPCHAR;

		for (int e = 0; e < result.unobservedEstimators.size(); e++) {
			line += result.unobservedEstimators.get(e).getName() +"(classes)"+ SEPCHAR;
		}
		for (int e = 0; e < result.coverageEstimators.size(); e++) {
			line += result.coverageEstimators.get(e).getName() +"(coverage)"+ SEPCHAR;
		}
		line += "\n";
		bw.write(line);

		for (int i = 0; i < result.logNames.length; i++) {
			for (int j = 0; j < result.measurements; j++) {
				line = result.logNames[i] + SEPCHAR;
				line += result.covers[i][0].getDataItem(j).getXValue() + SEPCHAR;

				for (int e = 0; e < result.unobservedEstimators.size(); e++) {
					if (result.total[i][e].getItemCount() > j) {
						line += result.total[i][e].getDataItem(j).getYValue() + SEPCHAR;
					} else {
						line += "-1.0" + SEPCHAR;
					}
				}
				for (int e = 0; e < result.coverageEstimators.size(); e++) {
					if (result.covers[i][e].getItemCount() > j) {
						line += result.covers[i][e].getDataItem(j).getYValue() + SEPCHAR;
					} else {
						line += "-1.0" + SEPCHAR;
					}
				}

				line += "\n";
				bw.write(line);
			}
		}
		bw.close();

	}
}
