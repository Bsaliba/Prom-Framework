package test;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.models.impl.DivideAndConquerFactory;

public class TestCAGMatch {

	public static void main(String[] args) {
		Set<XEventClass> activities1 = new HashSet<XEventClass>();
		for (int i = 0; i < 10; i++) {
			activities1.add(new XEventClass("a" + i, i));
		}
		Set<XEventClass> activities2 = new HashSet<XEventClass>();
		for (int i = 5; i < 15; i++) {
			activities2.add(new XEventClass("a" + i, i));
		}
		Set<XEventClass> activities12 = new HashSet<XEventClass>(activities1);
		activities12.retainAll(activities2);
		System.out.println("One\tZero\tZero\tMinusOne\tThird\tMinusThird");
		for (int i = 0; i < 1000; i++) {
			CausalActivityMatrix g1 = getRandomMatrix(activities1);
			CausalActivityMatrix g2 = getRandomMatrix(activities1);
			CausalActivityMatrix g3 = getRandomMatrix(activities2);
			double m1 = g1.getMatch(g1);
			double m2 = g1.getMatch(g2);
			double m3 = g1.getMatch(g3);
			for (XEventClass a1 : activities1) {
				for (XEventClass a2 : activities1) {
					g2.setValue(a1, a2, -g1.getValue(a1, a2));
				}
			}
			double m4 = g1.getMatch(g2);
			for (XEventClass a1 : activities12) {
				for (XEventClass a2 : activities12) {
					g3.setValue(a1, a2, g1.getValue(a1, a2));
				}
			}
			double m5 = g1.getMatch(g3);
			for (XEventClass a1 : activities12) {
				for (XEventClass a2 : activities12) {
					g3.setValue(a1, a2, -g1.getValue(a1, a2));
				}
			}
			double m6 = g1.getMatch(g3);
			System.out.println(m1 + "\t" + m2 + "\t" + m3 + "\t" + m4 + "\t" + m5 + "\t" + m6);
		}
	}
	
	private static CausalActivityMatrix getRandomMatrix(Set<XEventClass> activities) {
		CausalActivityMatrix matrix = DivideAndConquerFactory.createCausalActivityMatrix();
		matrix.init("test", activities);
		for (XEventClass a1 : activities) {
			for (XEventClass a2 : activities) {
				matrix.setValue(a1, a2, 1.0 - 2*Math.random());
			}
		}
		return matrix;
	}
}
