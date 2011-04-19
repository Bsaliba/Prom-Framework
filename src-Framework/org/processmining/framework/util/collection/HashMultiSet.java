package org.processmining.framework.util.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.annotations.TestMethod;

public class HashMultiSet<T> extends AbstractMultiSet<T, Map<T, Integer>> {

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are returned by the iterator of that
	 * collection.
	 * 
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public HashMultiSet(Collection<? extends T> collection) {
		this();
		addAll(collection);
	}

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are in the given array.
	 * 
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public HashMultiSet(T[] collection) {
		this();
		for (T par : collection) {
			add(par);
		}
	}

	/**
	 * Constructs a new, empty multiset, such that all elements of the given
	 * collection are added as many times as they are returned by the iterator
	 * of that collection.
	 */
	public HashMultiSet() {
		size = 0;
		map = new HashMap<T, Integer>();
	}

	<S> MultiSet<S> newMultiSet(Collection<S> collection) {
		return new HashMultiSet<S>(collection);
	}

	MultiSet<T> newMultiSet() {
		return new HashMultiSet<T>();
	}

	@TestMethod(output="true true")
	public static String test() {
		
		String result = "";
		
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
		
		result += Boolean.toString(hms1.equals(hms2))+" ";
		
		hms1.remove(3);
		hms1.remove(3);
		hms2.remove(3);
		hms2.remove(3);
		
		result += Boolean.toString(hms1.equals(hms2));
		
		return result;
	}
}
