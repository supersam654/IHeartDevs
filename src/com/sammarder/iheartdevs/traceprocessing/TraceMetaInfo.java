package com.sammarder.iheartdevs.traceprocessing;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.sammarder.iheartdevs.InputProcessingException;
import com.sammarder.iheartdevs.Utilities;

/**
 * Gathers and stores info related to a specific stack trace. Because this information changes for every stack trace, a
 * new object should be created for every stack trace.
 */
public class TraceMetaInfo {
	private Plugin plugin;
	private String fileLoc;

	/**
	 * Constructs a new TraceMetaInfo object for a given "ad hoc" trace and the name of the file it's stored in.
	 * 
	 * @param trace
	 *            The series of strings that composes the stack trace.
	 * @param qualifiedFileName
	 *            The full path to the file where the trace is stored.
	 */
	public TraceMetaInfo(List<String> trace, String qualifiedFileName) {
		this.fileLoc = qualifiedFileName;

		for (String s : trace) {
			String className = getQualifiedClassNameFromStackTrace(s);
			if (className == null) {
				continue;
			}

			Plugin plugin = getPluginFromQualifiedClassName(className);
			if (plugin == null) {
				continue;
			}

			this.plugin = plugin;
		}
		StringBuilder exceptionMessage = new StringBuilder();
		exceptionMessage.append("Could not process the following exception: ");
		for (String line : trace) {
			// Wrap lines in '~' to prevent them from being parsed like an exception.
			exceptionMessage.append("\n~" + line + "~");
		}
		new InputProcessingException(exceptionMessage.toString());
	}

	/**
	 * Get the plugin associated with this trace.
	 * 
	 * @return The plugin that caused the stack trace.
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Gets how long the server has been online when the stack trace occurred.
	 * 
	 * @return The uptime in milliseconds.
	 */
	public long getUptime() {
		return Utilities.getServerUptime();
	}

	/**
	 * Gets the file that the stack trace has been exported to.
	 * 
	 * @return The file name with full path where the stack trace is.
	 */
	public String getFile() {
		return fileLoc;
	}

	/**
	 * Determines what plugin/program/library a line from a stack trace came from.
	 * 
	 * @param line
	 *            The string to inspect.
	 * @return null if the line references a line to a library file (Minecraft, Bukkit, Java, etc.) or doesn't reference
	 *         a line of code at all. Otherwise, returns the plugin object that the line references.
	 */
	private static String getQualifiedClassNameFromStackTrace(String line) {
		// A bunch of things could go wrong, so just pokemon exception handle them.
		try {
			line = line.trim();
			if (line.startsWith("at ")) {
				int endPos = line.lastIndexOf('(');
				int startPos = "at ".length();
				// Contains the fully qualified class with a method at the end.
				line = line.substring(startPos, endPos);
				// Contains just the fully qualified class name.
				line = line.substring(0, line.lastIndexOf('.'));
				// Can return null if the line is referencing a library like Bukkit, Java, Minecraft, etc.
				return line;
			} else {
				return null;
			}
		} catch (Exception e) {
			// Something went wrong with parsing, but it doesn't matter.
			return null;
		}
	}

	/**
	 * Determines what plugin a (fully qualified) class name came from.
	 * 
	 * @param qualifiedClass
	 * @return The plugin associated with
	 */
	private static Plugin getPluginFromQualifiedClassName(String qualifiedClass) {
		// com.sammarder.iheartdevs.IHeartDevs to com/sammarder/iheartdevs/IHeartDevs
		qualifiedClass = qualifiedClass.replace('.', '/');
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			try {
				if (plugin.getResource(qualifiedClass) != null) {
					return plugin;
				}
			} catch (Exception e) {
				// I am trying every plugin so a bunch of them will Exception.
			}
		}
		return null;
	}
}
