package com.sammarder.iheartdevs.traceprocessing;

import java.util.Comparator;

public class ErrorFileComparator<File> implements Comparator<java.io.File> {

	@Override
	public int compare(java.io.File file1, java.io.File file2) {
		String name1 = file1.getName();
		String name2 = file2.getName();
		// A shorter name is always lower
		if (name1.length() < name2.length()) {
			return -1;
		} else if (name1.length() > name2.length()) {
			return 1;
		} else {
			return name1.compareTo(name2);
		}
	}
}
