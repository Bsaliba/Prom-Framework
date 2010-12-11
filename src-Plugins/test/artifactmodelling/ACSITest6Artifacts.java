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

public class ACSITest6Artifacts {

	ProcletPort[][] ports = new ProcletPort[8][4];

	@UITopiaVariant(uiLabel = UITopiaVariant.USEPLUGIN, affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "ACSI CD Shop (6 artifacts)", parameterLabels = {}, returnLabels = "Artifact Model", returnTypes = ArtifactModel.class, userAccessible = true)
	public ArtifactModel createArtifactModel(PluginContext context) {

		Proclet order = getOrder();
		Proclet quote = getQuote();
		Proclet custPay = getCustPay();
		Proclet cdNot = getCDShopNotif();
		Proclet cdShip = getCDShopShip();
		Proclet cdPay = getCDShopPay();

		ArtifactModel model = new ArtifactModelImpl("CD Shop (6 artifacts)");
		model.addProclet(quote);
		model.addProclet(order);
		model.addProclet(custPay);
		model.addProclet(cdNot);
		model.addProclet(cdShip);
		model.addProclet(cdPay);

		model.addArc((Output) ports[0][0], (Input) ports[1][0]).setLabel(
				"E-mail");
		model.addArc((Output) ports[1][1], (Input) ports[3][0]).setLabel(
				"E-mail");
		context.getFutureResult(0).setLabel("CD Shop (6 artifacts)");

		model.addArc((Output) ports[1][2], (Input) ports[6][0])
				.setLabel("Mail");
		model.addArc((Output) ports[6][1], (Input) ports[4][0]).setLabel(
				"E-Bank");

		model.addArc((Output) ports[1][3], (Input) ports[7][0]).setLabel(
				"E-Bank");

		return model;

	}

	Proclet getQuote() {
		Proclet quote = new ProcletImpl("Quote");

		ProcletTransition t1 = quote.addTransition("Create quote");
		ProcletTransition t2 = quote.addTransition("Generate request");
		ProcletTransition t3 = quote.addTransition("Send quote");
		ProcletTransition t4 = quote.addTransition("Reject quote");
		ProcletTransition t5 = quote.addTransition("Accept quote");
		ProcletTransition t6 = quote.addTransition("Finish quote");

		ProcletPlace p1 = quote.addPlace("p1");
		ProcletPlace p2 = quote.addPlace("p2");
		ProcletPlace p3 = quote.addPlace("p3");
		ProcletPlace p4 = quote.addPlace("p4");

		quote.addArc(t1, p1);
		quote.addArc(p1, t2);
		quote.addArc(t2, p2);
		quote.addArc(p2, t3);
		quote.addArc(t3, p3);
		quote.addArc(p3, t4);
		quote.addArc(p3, t5);
		quote.addArc(t4, p4);
		quote.addArc(t5, p4);
		quote.addArc(p4, t6);

		ports[0][0] = quote.addPort(Cardinality.AT_LEAST_ONE, Multiplicity.ONE,
				true);

		quote.addSendArc(t5, ports[0][0]);

		return quote;
	}

	Proclet getCustPay() {
		Proclet p = new ProcletImpl("Customer Payment");

		ProcletTransition t1 = p.addTransition("Create customer payment");
		ProcletTransition t2 = p.addTransition("Send invoice to customer");
		ProcletTransition t3 = p.addTransition("Receive payment from customer");
		ProcletTransition t4 = p.addTransition("Finish customer payment");

		ProcletPlace p1 = p.addPlace("p1");
		ProcletPlace p2 = p.addPlace("p2");
		ProcletPlace p3 = p.addPlace("p3");

		p.addArc(t1, p1);
		p.addArc(p1, t2);
		p.addArc(t2, p2);
		p.addArc(p2, t3);
		p.addArc(t3, p3);
		p.addArc(p3, t4);

		ports[4][0] = p.addPort(Cardinality.ONE, Multiplicity.ONE, false);

		p.addReceiveArc(ports[4][0], t1);

		return p;
	}

	Proclet getCDShopPay() {
		Proclet p = new ProcletImpl("CD Shop Payment");

		ProcletTransition t1 = p.addTransition("Create cd shop payment");
		ProcletTransition t2 = p.addTransition("Send invoice to CD shop");
		ProcletTransition t3 = p.addTransition("Receive payment from CD shop");
		ProcletTransition t4 = p.addTransition("Finish CD shop payment");

		ProcletPlace p1 = p.addPlace("p1");
		ProcletPlace p2 = p.addPlace("p2");
		ProcletPlace p3 = p.addPlace("p3");

		p.addArc(t1, p1);
		p.addArc(p1, t2);
		p.addArc(t2, p2);
		p.addArc(p2, t3);
		p.addArc(t3, p3);
		p.addArc(p3, t4);

		ports[7][0] = p.addPort(Cardinality.ONE, Multiplicity.ONE, false);

		p.addReceiveArc(ports[7][0], t1);

		return p;
	}

	Proclet getCDShopShip() {
		Proclet p = new ProcletImpl("CD Shop Shipment");

		ProcletTransition t1 = p.addTransition("Create CD shop shipment");
		ProcletTransition t2 = p.addTransition("Generate invoice for customer");
		ProcletTransition t3 = p.addTransition("Ship quote to the customer");
		ProcletTransition t4 = p.addTransition("Finish CD shop shipment");

		ProcletPlace p1 = p.addPlace("p1");
		ProcletPlace p2 = p.addPlace("p2");
		ProcletPlace p3 = p.addPlace("p3");
		ProcletPlace p4 = p.addPlace("p3");

		p.addArc(t1, p1);
		p.addArc(t1, p2);

		p.addArc(p1, t2);
		p.addArc(p2, t3);

		p.addArc(t2, p3);
		p.addArc(t3, p4);
		p.addArc(p3, t4);
		p.addArc(p4, t4);

		ports[6][0] = p.addPort(Cardinality.AT_LEAST_ONE, Multiplicity.ONE,
				false);
		ports[6][1] = p.addPort(Cardinality.ONE, Multiplicity.ONE, true);

		p.addReceiveArc(ports[6][0], t1);
		p.addSendArc(t2, ports[6][1]);

		return p;
	}

	Proclet getCDShopNotif() {
		Proclet p = new ProcletImpl("CD Shop Notification");

		ProcletTransition t1 = p.addTransition("Create cd shop notification");
		ProcletTransition t2 = p.addTransition("Send notification");
		ProcletTransition t3 = p.addTransition("Finish CD shop notification");

		ProcletPlace p1 = p.addPlace("p1");
		ProcletPlace p2 = p.addPlace("p2");

		p.addArc(t1, p1);
		p.addArc(p1, t2);
		p.addArc(t2, p2);
		p.addArc(p2, t3);

		ports[3][0] = p.addPort(Cardinality.AT_LEAST_ONE, Multiplicity.ONE,
				false);

		p.addReceiveArc(ports[3][0], t1);

		return p;
	}

	Proclet getOrder() {
		Proclet order = new ProcletImpl("Order");

		ProcletTransition t1 = order.addTransition("Create order");
		ProcletTransition t2 = order.addTransition("Add quote to order");
		ProcletTransition t3 = order.addTransition("Add quote to order");
		ProcletTransition t10 = order.addTransition("Order at supplier");
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

		ProcletPlace p1 = order.addPlace("p1");
		ProcletPlace p2 = order.addPlace("p2");
		ProcletPlace p3 = order.addPlace("p3");
		ProcletPlace p4 = order.addPlace("p4");
		ProcletPlace p5 = order.addPlace("p5");
		ProcletPlace p6 = order.addPlace("p6");
		ProcletPlace p7 = order.addPlace("p7");
		ProcletPlace p8 = order.addPlace("p8");

		order.addArc(t1, p1);
		order.addArc(p1, t2);
		order.addArc(t2, p1);
		order.addArc(p1, t3);

		order.addArc(t3, p8);
		order.addArc(p8, t10);

		order.addArc(t10, p2);
		order.addArc(t10, p3);
		order.addArc(t10, p4);
		
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

		order.addArc(t5, p6);
		order.addArc(t6, p6);

		order.addArc(p5, t9);
		order.addArc(p6, t9);
		order.addArc(p7, t9);

		ports[1][0] = order.addPort(Cardinality.ONE, Multiplicity.AT_LEAST_ONE,
				false);
		ports[1][1] = order.addPort(Cardinality.AT_LEAST_ONE,
				Multiplicity.AT_MOST_ONE, true);
		ports[1][2] = order.addPort(Cardinality.AT_LEAST_ONE,
				Multiplicity.AT_MOST_ONE, true);
		ports[1][3] = order.addPort(Cardinality.ONE, Multiplicity.AT_MOST_ONE,
				true);

		order.addReceiveArc(ports[1][0], t2);
		order.addReceiveArc(ports[1][0], t3);

		order.addSendArc(t7, ports[1][1]);
		order.addSendArc(t4, ports[1][2]);
		order.addSendArc(t6, ports[1][3]);

		return order;
	}

}
