package com.sammarder.iheartdevs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sammarder.iheartdevs.traceprocessing.ErrorFileComparator;
import com.sammarder.iheartdevs.traceprocessing.ErrorFileFilter;

public class FolderManager {
	private File directory;
	private int nextFileNumber;
	private ErrorFileFilter filter;
	private ErrorFileComparator<File> comparator;

	public FolderManager(File directory, ErrorFileFilter filter, ErrorFileComparator<File> comparator) {
		this.directory = directory;
		this.filter = filter;
		this.comparator = comparator;
		List<File> files = Arrays.asList(directory.listFiles(filter));
		Collections.sort(files, comparator);
		if (files.size() > 0) {
			File lastFile = files.get(files.size() - 1);
			// Gets the integer portion of the last file sorted in this directory.
			String lastFileName = lastFile.getName().split("\\.")[0];
			nextFileNumber = Integer.parseInt(lastFileName);
			nextFileNumber++;
		} else {
			nextFileNumber = 0;
		}

	}

	/**
	 * Creates a new file with an auto-generated, valid ID.
	 * 
	 * @return A File object that points to the newly created file or null if it couldn't be created.
	 */
	public File createFile() {
		File file = createFile(String.valueOf(nextFileNumber) + ".txt");
		// The file could be null for a few reasons, so just increment the counter anyway so the next attempt doesn't
		// try to create a file that already exists.
		nextFileNumber++;
		// This may return null.
		return file;
	}

	/**
	 * Creates a new file with a given filename in the directory referenced by this object.
	 * 
	 * @param localName
	 *            The name (without a path) of the new file.
	 * @return A File object that points to the newly created file or null if it couldn't be created.
	 */
	private File createFile(String localName) {
		try {
			File file = new File(directory, localName);
			if (file.createNewFile()) {
				return file;
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Gets an existing file from this directory.
	 * 
	 * @param localName
	 *            The name (without a path) of the existing file.
	 * @return A File object that points to the existing file or null if it doesn't exist.
	 */
	public File getFile(String localName) {
		File file = new File(directory, localName);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

	// TODO: Cleanup a bit
	public File[] getFilesFromPage(int page) {
		List<File> filesList = Arrays.asList(directory.listFiles(filter));
		Collections.sort(filesList, comparator);

		// lastIndex refers to the first index that should be used.
		// filesList.size() - 1 refers to the last valid index (which is effectively the newest file in the directory).
		// filesList.size() - (10 * (page - 1)) refers to the page modifier. One is subtracted from page so that page 1
		// doesn't have a modifier - (0). Then the 10 items that are at the end of the list are put into an array (in
		// reverse order).
		int lastIndex = 0;
		// Treat illegal numbers as page 1.
		if (page <= 1) {
			lastIndex = filesList.size() - 1;
		} else {
			lastIndex = filesList.size() - (10 * (page - 1)) - 1;
		}

		if (lastIndex < 0) {
			return null;
		}
		int displayNumber = 10;
		if (lastIndex < 10) {
			displayNumber = lastIndex + 1;
		}

		File[] filesArray = new File[displayNumber];
		for (int i = 0; i < displayNumber; i++) {
			filesArray[i] = filesList.get(lastIndex - i);
		}

		return filesArray;
	}

}
