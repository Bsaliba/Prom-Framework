package test.artifactmodelling;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.proclets.ArtifactModel;
import org.processmining.models.graphbased.directed.proclets.Proclet;
import org.processmining.models.graphbased.directed.proclets.elements.ProcletPlace;
import org.processmining.models.graphbased.directed.proclets.elements.ProcletPort;
import org.processmining.models.graphbased.directed.proclets.elements.ProcletTransition;
import org.processmining.models.graphbased.directed.proclets.elements.ProcletPort.Cardinality;
import org.processmining.models.graphbased.directed.proclets.elements.ProcletPort.Input;
import org.processmining.models.graphbased.directed.proclets.elements.ProcletPort.Multiplicity;
import org.processmining.models.graphbased.directed.proclets.elements.ProcletPort.Output;
import org.processmining.models.graphbased.directed.proclets.impl.ArtifactModelImpl;
import org.processmining.models.graphbased.directed.proclets.impl.ProcletImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.proclet.conversion.ArtifactModelToResetnet;

public class ArtifactProcletTest {

	@UITopiaVariant(uiLabel = UITopiaVariant.USEPLUGIN, affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "Generate Artifact Model (Specify types)", parameterLabels = {}, returnLabels = {
			"Artifact Model", "Reset net conversion", "Marking" }, returnTypes = {
			ArtifactModel.class, ResetNet.class, Marking.class }, userAccessible = true, mostSignificantResult = 2)
	public static Object[] createProcletAllOutput(UIPluginContext context)
			throws Exception {

		ConfigurationUI c = new ConfigurationUI();
		if (!context.showConfiguration("Select multiplicity/ cardinality", c)
				.equals(InteractionResult.CONTINUE)) {
			return null;
		}

		ArtifactModelImpl model = new ArtifactModelImpl("Artifact Model");

		int n = 0;

		Proclet p1 = createProclet("out:", "C=" + c.getOutputCard() + ", M="
				+ c.getOutputMult());
		n += 2;

		// add outputport
		ProcletPort outport = p1.addPort(c.getOutputCard(), c.getOutputMult(),
				true);

		for (ProcletTransition t : p1.getTransitions()) {
			if (t.getLabel().startsWith("out")) {
				p1.addSendArc(t, outport);
			}
		}

		Proclet p2 = createProclet("in:", "C=" + c.getInputCard() + ", M="
				+ c.getInputMult());
		n += 2;

		// add outputport
		ProcletPort inport = p2.addPort(c.getInputCard(), c.getInputMult(),
				false);

		for (ProcletTransition t : p2.getTransitions()) {
			if (t.getLabel().startsWith("in")) {
				p2.addReceiveArc(inport, t);
			}
		}

		model.addProclet(p1);
		model.addProclet(p2);
		model.addArc((Output) outport, (Input) inport);

		UIPluginContext child = context.createChildContext("Conversion to net");
		context.getPluginLifeCycleEventListeners().firePluginCreated(child);

		Object[] netAndMarking = (new ArtifactModelToResetnet())
				.convertInternal(child, model);

		return new Object[] { model, netAndMarking[0], netAndMarking[1] };
	}

	public static Proclet createProclet(String portName, String procletName) {

		Proclet p = new ProcletImpl(procletName);
		ProcletTransition c = p.addTransition("Create");
		ProcletTransition d = p.addTransition("Destroy");
		ProcletTransition inv = p.addTransition("Tinv");
		inv.setInvisible(true);

		// add a transition
		ProcletTransition t1 = p.addTransition(portName + " (1)");
		ProcletTransition t2 = p.addTransition(portName + " (2)");

		// add 2 3input, 3 output places
		ProcletPlace i0 = p.addPlace("i0");
		ProcletPlace i1 = p.addPlace("i1");
		ProcletPlace i2 = p.addPlace("i2");
		ProcletPlace i3 = p.addPlace("i3");
		ProcletPlace o1 = p.addPlace("o1");
		ProcletPlace o2 = p.addPlace("o2");

		// add arcs
		p.addArc(i1, t1);
		p.addArc(i2, t1);
		p.addArc(i1, t2);
		p.addArc(i3, t2);
		p.addArc(t1, o1);
		p.addArc(t1, o2);
		p.addArc(t2, o1);
		p.addArc(t2, o2);
		p.addArc(o1, d);
		p.addArc(o2, d);

		p.addArc(c, i0);
		p.addArc(c, i1);

		p.addArc(i0, inv);

		p.addArc(inv, i2);
		p.addArc(inv, i3);

		return p;
	}
}

class ConfigurationUI extends JPanel {

	private static final long serialVersionUID = 1082957704020165849L;

	JComboBox inputMul = new JComboBox(Multiplicity.values());
	JComboBox inputCar = new JComboBox(Cardinality.values());
	JComboBox outputMul = new JComboBox(Multiplicity.values());
	JComboBox outputCar = new JComboBox(Cardinality.values());

	public ConfigurationUI() {
		super(new GridLayout(4, 2));

		ComboBoxRenderer renderer = new ComboBoxRenderer();
		inputMul.setRenderer(renderer);
		inputCar.setRenderer(renderer);
		outputMul.setRenderer(renderer);
		outputCar.setRenderer(renderer);

		add(new JLabel("Output Cardinality:"));
		add(outputCar);
		add(new JLabel("Output Multiplicity:"));
		add(outputMul);
		add(new JLabel("Input Cardinality:"));
		add(inputCar);
		add(new JLabel("Input Multiplicity:"));
		add(inputMul);
	}

	Multiplicity getInputMult() {
		return (Multiplicity) inputMul.getSelectedItem();
	}

	Multiplicity getOutputMult() {
		return (Multiplicity) outputMul.getSelectedItem();
	}

	Cardinality getInputCard() {
		return (Cardinality) inputCar.getSelectedItem();
	}

	Cardinality getOutputCard() {
		return (Cardinality) outputCar.getSelectedItem();
	}
}

class ComboBoxRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 8895295393150759032L;

	public ComboBoxRenderer() {
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	/*
	 * This method finds the image and text corresponding to the selected value
	 * and returns the label, set up to display the text and image.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		// Set the icon and text. If icon was null, say so.
		String pet = "<html>" + value.toString() + "</html>";
		setText(pet);
		setFont(list.getFont());

		return this;
	}
}
