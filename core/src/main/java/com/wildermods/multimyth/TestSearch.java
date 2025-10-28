package com.wildermods.multimyth;

import java.util.HashSet;

import com.wildermods.multimyth.internal.JavaFinder;
import com.wildermods.multimyth.internal.JVMBinary;

public class TestSearch {

	public static void main(String[] args) {
		JavaFinder finder = new JavaFinder();
		HashSet<JVMBinary> instances = finder.locate();
		for(JVMBinary instance : instances) {
			System.out.println("Located " + instance.getJVMLocation());
		}
	}
	
}
