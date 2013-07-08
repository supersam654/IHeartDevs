package com.sammarder.iheartdevs.traceprocessing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import com.sammarder.iheartdevs.ConfigurationManager;
import com.sammarder.iheartdevs.FolderManager;
import com.sammarder.iheartdevs.IHeartDevs;
import com.sammarder.iheartdevs.IRemindable;
import com.sammarder.iheartdevs.Utilities;

/**
 * Deals with converting multiple single-line strings into a multi-line stack trace. Fires a StackTraceCreationEvent
 * when a complete stack trace is formed.
 */
public class StackTraceProcessor implements IMessageProcessor, IRemindable {
	private static final String NL = "\n";

	private List<String> currentTrace;
	private SingleCompletionTimer timer;
	private FolderManager folderManager;

	/**
	 * Constructor for creating a new StackTraceProcessor.
	 * 
	 * @param rootTraceFolderString
	 *            A string pointing to the root directory where stack traces should be stored. It is recommended to make
	 *            this pluginDataDirectory/Errors.
	 */
	public StackTraceProcessor(Plugin plugin, FolderManager folderManager) {
		this.folderManager = folderManager;
		currentTrace = new ArrayList<String>();
		timer = new SingleCompletionTimer(new Timer(), this, 10);
	}

	@Override
	public boolean process(String message) {
		message = message.trim();
		if (isStartOfTrace(message)) {
			if (currentTrace.size() != 0) {
				// We have finished a trace. Do something with it.
				saveTrace();
			}
			currentTrace.clear();
			currentTrace.add(message);

			// Start the timer (or invalidate the current one).
			// Because the timer isn't attached to the old trace, this is perfectly acceptable.
			timer.setReminder();
			return true;
		} else if (currentTrace.size() != 0 && isContinuationOfTrace(message)) {
			currentTrace.add(message);
			timer.setReminder();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Processes a completed trace (stored as strings in currentTrace). This is responsible for saving the trace to a
	 * file and firing a StackTraceCreationEvent about it.
	 */
	private void saveTrace() {
		System.out.println("Saving trace.");
		File outputFile = generateFile();
		if (outputFile != null) {
			TraceMetaInfo traceInfo = new TraceMetaInfo(currentTrace, outputFile.toString());
			Event event = new StackTraceCreationEvent(traceInfo);
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	/**
	 * Generates a file from currentTrace.
	 * 
	 * @return The path of the file where the trace is stored or null if the file could not be written.
	 */
	private File generateFile() {
		File file = folderManager.createFile();
		if (file == null) {
			System.out.println("Could not create file.");
			return null;
		}
		FileWriter os = null;
		try {
			// Write contents to the file.
			if (file.canWrite()) {
				os = new FileWriter(file);

				os.append(ServerMetaInfo.getBukkitVersion() + NL);
				os.append("Java Version: " + ServerMetaInfo.getJavaVersion() + NL);
				os.append("Operating System: " + ServerMetaInfo.getOSName() + NL);
				os.append("OS Architecture: " + ServerMetaInfo.getOSArchitecture() + NL);
				os.append("Total Memory: " + Utilities.formatBytes(ServerMetaInfo.getTotalMemory()) + NL);
				os.append("Server Uptime: " + Utilities.formatMilliseconds(Utilities.getServerUptime()) + NL);

				if (ConfigurationManager.shouldUseExtendedInfo()) {
					os.append(NL + "Plugins:" + NL);
					for (String s : ServerMetaInfo.getPluginNames()) {
						os.append(s + NL);
					}
					os.append(NL);
				}

				os.append("Stacktrace:" + NL);
				for (String s : currentTrace) {
					os.append(s + NL);
				}

				os.flush();
			} else {
				throw new IOException();
			}
		} catch (IOException e) {
			// I can't say e.printStackTrace() otherwise I could create an infinite loop!
			IHeartDevs.log(Level.WARNING, "Could not write error report to \"" + file.toString() + "\"");
			return null;
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				// Oh well.
			}
		}
		return file;
	}

	@Override
	public void remind() {
		saveTrace();
		currentTrace.clear();
	}

	/**
	 * Helper method for determining if a string starts a stack trace.
	 * 
	 * @param message
	 *            The string to interpret.
	 * @return true if the string is believed to be the beginning of a stack trace, false otherwise.
	 */
	private boolean isStartOfTrace(String message) {
		if (message.startsWith("Exception in thread \"")) {
			return true;
		}
		String[] pieces = message.split(" ", 2);
		if (pieces[0].endsWith("Exception:") || pieces[0].endsWith("Exception")) {
			return true;
		}
		return false;
	}

	/**
	 * Helper method for determining if a string is part of an existing stack trace.
	 * 
	 * @param message
	 *            The string to interpret.
	 * @return true if the string is believed to be the beginning of a stack trace, false otherwise.
	 */
	private static boolean isContinuationOfTrace(String message) {
		// tab/whitespace before "at: xxx" lines is now just a single space.
		message = message.trim();
		String[] pieces = message.split(" ");
		if (pieces.length != 2) {
			return false;
		}
		// Caused by: xxx.xxx.xxx.XxxXxxException
		if (pieces.length == 2 && pieces[0].equals("Caused by:") && pieces[1].contains("Exception")) {
			return true;
			// [tab] at xxxx.xxx.xx.XxxXxx(Xxxxx.java:###)
		} else if (pieces.length == 2 && pieces[0].equals("at") && pieces[1].endsWith(")")) {
			return true;
			// ... ## more
		} else if (pieces.length == 3 && pieces[0].equals("...") && Utilities.isPositiveInteger(pieces[1])
				&& pieces[2].equals("more")) {
			return true;
		}
		// Didn't match. Oh well.
		return false;
	}

}
