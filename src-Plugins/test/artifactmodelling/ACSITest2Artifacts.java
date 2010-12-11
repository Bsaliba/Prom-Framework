package test.artifactmodelling;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
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

public class ACSITest2Artifacts {

	ProcletPort[][] ports = new ProcletPort[2][3];

	@UITopiaVariant(uiLabel = UITopiaVariant.USEPLUGIN, affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "ACSI CD Shop (2 artifacts)", parameterLabels = {}, returnLabels = "Artifact Model", returnTypes = ArtifactModel.class, userAccessible = true)
	public ArtifactModel createArtifactModel(PluginContext context) {

		Proclet quote = getQuote();
		Proclet order = getOrder();

		ArtifactModel model = new ArtifactModelImpl("CD Shop (2 artifacts)");
		model.addProclet(quote);
		model.addProclet(order);

		model.addArc((Output) ports[0][0], (Input) ports[1][0]).setLabel(
				"E-mail");
		model.addArc((Output) ports[1][1], (Input) ports[0][1]).setLabel(
				"E-mail");
		model.addArc((Output) ports[1][2], (Input) ports[0][2])
				.setLabel("Mail");

		context.getFutureResult(0).setLabel("CD Shop (2 artifacts)");

		return model;

	}

	Proclet getQuote() {
		Proclet quote = new ProcletImpl("Quote");

		ProcletTransition t1 = quote.addTransition("Create quote");
		ProcletTransition t2 = quote.addTransition("Generate request");
		ProcletTransition t3 = quote.addTransition("Send quote");
		ProcletTransition t4 = quote.addTransition("Accept quote");
		ProcletTransition t5 = quote.addTransition("Reject quote");
		ProcletTransition t6 = quote.addTransition("Wait");
		t6.setInvisible(true);
		ProcletTransition t7 = quote
				.addTransition("Notify undeliverability to customer");
		ProcletTransition t8 = quote
				.addTransition("Ship quote to the customer");
		ProcletTransition t9 = quote
				.addTransition("Generate invoice for customer");
		ProcletTransition t10 = quote.addTransition("Finish quote");
		ProcletTransition t11 = quote.addTransition("invisible");
		ProcletTransition t12 = quote.addTransition("invisible");
		t11.setInvisible(true);
		t12.setInvisible(true);
		ProcletTransition t13 = quote.addTransition("Send invoice to customer");
		ProcletTransition t14 = quote
				.addTransition("Receive payment from customer");

		ProcletPlace p1 = quote.addPlace("p1");
		ProcletPlace p2 = quote.addPlace("p2");
		ProcletPlace p3 = quote.addPlace("p3");
		ProcletPlace p4 = quote.addPlace("p4");
		ProcletPlace p5 = quote.addPlace("p5");
		ProcletPlace p6 = quote.addPlace("p6");
		ProcletPlace p7 = quote.addPlace("p7");
		ProcletPlace p8 = quote.addPlace("p8");
		ProcletPlace p9 = quote.addPlace("p9");
		ProcletPlace p10 = quote.addPlace("p10");
		ProcletPlace p11 = quote.addPlace("p11");
		ProcletPlace p12 = quote.addPlace("p2");

		quote.addArc(t1, p1);
		quote.addArc(p1, t2);
		quote.addArc(t2, p2);
		quote.addArc(p2, t3);
		quote.addArc(t3, p3);
		quote.addArc(p3, t4);
		quote.addArc(p3, t5);
		quote.addArc(t4, p4);
		quote.addArc(t4, p5);
		quote.addArc(p4, t11);
		quote.addArc(p4, t7);
		quote.addArc(p5, t12);
		quote.addArc(p5, t6);
		quote.addArc(t6, p6);
		quote.addArc(t6, p7);
		quote.addArc(p6, t8);
		quote.addArc(t8, p9);

		quote.addArc(p7, t9);
		quote.addArc(t9, p11);
		quote.addArc(p11, t13);
		quote.addArc(t13, p12);
		quote.addArc(p12, t14);

		quote.addArc(t14, p10);

		quote.addArc(t5, p8);
		quote.addArc(t5, p9);
		quote.addArc(t5, p10);
		quote.addArc(t7, p8);
		quote.addArc(t12, p9);
		quote.addArc(t12, p10);
		quote.addArc(p8, t10);
		quote.addArc(p9, t10);
		quote.addArc(p10, t10);
		quote.addArc(t11, p8);

		ports[0][0] = quote.addPort(Cardinality.AT_LEAST_ONE, Multiplicity.ONE,
				true);
		ports[0][1] = quote.addPort(Cardinality.AT_LEAST_ONE,
				Multiplicity.AT_MOST_ONE, false);
		ports[0][2] = quote.addPort(Cardinality.AT_LEAST_ONE,
				Multiplicity.AT_MOST_ONE, false);

		quote.addSendArc(t4, ports[0][0]);

		quote.addReceiveArc(ports[0][1], t7);
		quote.addReceiveArc(ports[0][2], t6);

		return quote;
	}

	Proclet getOrder() {
		Proclet order = new ProcletImpl("Order");

		ProcletTransition t1 = order.addTransition("Create order");
		ProcletTransition t2 = order.addTransition("Add quote to order");
		ProcletTransition t3 = order.addTransition("Add quote to order");
		ProcletTransition t12 = order.addTransition("Order at supplier");
		ProcletTransition t4 = order.addTransition("Ship order to CD shop");
		ProcletTransition t5 = order.addTransition("invisible");
		ProcletTransition t6 = order
				.addTransition("Generate invoice for the CD shop");
		ProcletTransition t7 = order
				.addTransition("Notify undeliverability to CD shop");
		ProcletTransition t8 = order.addTransition("invisible");
		ProcletTransition t9 = order.addTransition("Finish order");
		t5.setInvisible(true);
		t8.setInvisible(true);

		ProcletTransition t10 = order.addTransition("Send invoice to CD shop");
		ProcletTransition t11 = order
				.addTransition("Receive payment from CD shop");

		ProcletPlace p1 = order.addPlace("p1");
		ProcletPlace p2 = order.addPlace("p2");
		ProcletPlace p3 = order.addPlace("p3");
		ProcletPlace p4 = order.addPlace("p4");
		ProcletPlace p5 = order.addPlace("p5");
		ProcletPlace p6 = order.addPlace("p6");
		ProcletPlace p7 = order.addPlace("p7");
		ProcletPlace p8 = order.addPlace("p8");
		ProcletPlace p9 = order.addPlace("p9");
		ProcletPlace p10 = order.addPlace("p10");

		order.addArc(t1, p1);
		order.addArc(p1, t2);
		order.addArc(t2, p1);
		order.addArc(p1, t3);
		
		order.addArc(t3, p10);
		order.addArc(p10, t12);
		order.addArc(t12, p2);
		order.addArc(t12, p3);
		order.addArc(t12, p4);

		order.addArc(p2, t4);
		order.addArc(p2, t5);

		order.addArc(p3, t5);
		order.addArc(p3, t6);

		order.addArc(p4, t7);
		order.addArc(p4, t8);
		order.addArc(t7, p7);
		order.addArc(t8, p7);

		order.addArc(t4, p5);
		order.addArc(t5, p5);

		order.addArc(t5, p9);
		order.addArc(t6, p6);
		order.addArc(p6, t10);
		order.addArc(t10, p8);
		order.addArc(p8, t11);
		order.addArc(t11, p9);

		order.addArc(p9, t9);

		order.addArc(p5, t9);
		order.addArc(p7, t9);

		ports[1][0] = order.addPort(Cardinality.ONE, Multiplicity.AT_LEAST_ONE,
				false);
		ports[1][1] = order.addPort(Cardinality.AT_LEAST_ONE,
				Multiplicity.AT_MOST_ONE, true);
		ports[1][2] = order.addPort(Cardinality.AT_LEAST_ONE,
				Multiplicity.AT_MOST_ONE, true);

		order.addReceiveArc(ports[1][0], t2);
		order.addReceiveArc(ports[1][0], t3);

		order.addSendArc(t7, ports[1][1]);
		order.addSendArc(t4, ports[1][2]);

		return order;
	}

}
