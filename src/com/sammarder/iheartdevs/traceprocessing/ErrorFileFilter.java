package com.sammarder.iheartdevs.traceprocessing;

import java.io.File;
import java.io.FilenameFilter;

import com.sammarder.iheartdevs.Utilities;

public class ErrorFileFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		// Only accept files in the form #########.txt
		String[] pieces = name.split("\\.");
		if (pieces.length != 2) {
			return false;
		}

		if (Utilities.isPositiveInteger(pieces[0]) && pieces[1].equals("txt")) {
			return true;
		} else {
			return false;
		}
	}
}
