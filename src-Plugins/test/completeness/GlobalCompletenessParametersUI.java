package test.completeness;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GlobalCompletenessParametersUI extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 106176421033558749L;
	private double confidence;
	private double error;

	public GlobalCompletenessParametersUI(double confidence, double error) {
		this.confidence = confidence;
		this.error = error;

		JPanel mainPanel = new JPanel(new GridLayout(2, 2));
		mainPanel.setOpaque(false);

		JLabel confLabel = new JLabel("Confidence level: ");
		mainPanel.add(confLabel);

		JFormattedTextField confText = new JFormattedTextField(new Double(
				confidence));
		mainPanel.add(confText);

		confText.addPropertyChangeListener("value",
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {

						GlobalCompletenessParametersUI.this.confidence = ((Double) ((JFormattedTextField) evt
								.getSource()).getValue()).doubleValue();

					}
				});

		JLabel errorLabel = new JLabel("Maximum error: ");
		mainPanel.add(errorLabel);

		JFormattedTextField errorText = new JFormattedTextField(new Double(
				error));
		mainPanel.add(errorText);

		errorText.addPropertyChangeListener("value",
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {

						GlobalCompletenessParametersUI.this.error = ((Double) ((JFormattedTextField) evt
								.getSource()).getValue()).doubleValue();

					}
				});

		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.NORTH);
	}

	public double getConfidence() {
		return confidence;
	}

	public double getMaxError() {
		return error;
	}

}
