package org.processmining.tests.framework;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.processmining.framework.util.collection.HashMultiSet;

public class FrameworkUtilCollectionsTest {

	@Test
	public void test_HashMultiSet() {
		
		// create two identical multisets
		HashMultiSet<Integer> hms1 = new HashMultiSet<Integer>();
		hms1.add(1);
		hms1.add(2, 2);
		hms1.add(3, 3);
		
		HashMultiSet<Integer> hms2 = new HashMultiSet<Integer>();
		hms2.add(1);
		hms2.add(2);
		hms2.add(3);
		hms2.add(2);
		hms2.add(3);
		hms2.add(3);

		// test for equality
		Assert.assertEquals("Equality1: "+hms1+" equals "+hms2, hms1, hms2);

		// remove some elments
		hms1.remove(3);
		hms1.remove(3);
		hms2.remove(3);
		hms2.remove(3);

		// test for equality
		Assert.assertEquals("Equality2: "+hms1+" equals "+hms2, hms1, hms2);

		// test constructors
		Integer hms2_arr [] = hms2.toArray(new Integer[hms2.size()]);
		HashMultiSet<Integer> hms3 = new HashMultiSet<Integer>(hms2_arr);
		Assert.assertEquals("Equality3: "+hms1+" equals "+hms3, hms1, hms3);
		
		List<Integer> hms2_list = hms2.toList();
		HashMultiSet<Integer> hms4 = new HashMultiSet<Integer>(hms2_list);
		Assert.assertEquals("Equality4: "+hms1+" equals "+hms4, hms1, hms4);
		
		hms3.add(17, 17);
		hms3.removeAll(hms2);
		Assert.assertTrue("Member 1: "+hms3+"(17) == 17", hms3.occurrences(17) == 17);
		Assert.assertTrue("Member 2: "+hms3.baseSet()+" has one member", hms3.baseSet().size() == 1);
		
		HashMultiSet<Integer> hms5 = new HashMultiSet<Integer>(); 
		hms5.add(17, 0);
		Assert.assertFalse("Member 4: "+hms5+" does not contain 17", hms5.contains(17));
		Assert.assertTrue("Member 4: "+hms5+" is empty", hms5.isEmpty());
	}
	
	@Test
	public void test_HashMultiSet_contains() {

		// create two multisets
		HashMultiSet<Integer> hms1 = new HashMultiSet<Integer>();
		hms1.add(1, 12);
		hms1.add(2, 3);

		HashMultiSet<Integer> hms2 = new HashMultiSet<Integer>(hms1);
		hms2.add(3, 1);
		
		Assert.assertTrue(hms2+" contains all "+hms1, hms2.containsAll(hms1));
		Assert.assertFalse(hms2+" does not contains object "+hms1, hms2.contains(hms1));
		
		hms2.remove(3);
		
		Assert.assertTrue(hms2+" contains all "+hms1, hms2.containsAll(hms1));

		hms2.remove(2);

		Assert.assertFalse(hms2+" does not contain all "+hms1, hms2.containsAll(hms1));
	}
	
	@Test(expected=java.util.ConcurrentModificationException.class)
	public void test_HashMultiSet_retainAll() {
		
		// FIXME: fix bug where ConcurrentModificationException is thrown and remove annotation
		
		// create two identical multisets
		HashMultiSet<Integer> hms1 = new HashMultiSet<Integer>();
		hms1.add(1);
		hms1.add(2, 2);
		hms1.add(3, 3);

		HashMultiSet<Integer> hms2 = new HashMultiSet<Integer>();
		hms2.add(3, 1);
		
		hms1.retainAll(hms2);
		Assert.assertEquals(hms2.toString(), "[3]");
	}
}
