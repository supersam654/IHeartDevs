package com.sammarder.iheartdevs.traceprocessing;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Contains static methods for getting server information. Information is initialized on the first time it is accessed.
 * This class only contains static methods and therefore cannot be instantiated.
 */
public class ServerMetaInfo {
	private static String bukkitVersion = null;
	private static String javaVersion = null;

	private static String osName = null;
	private static String osArch = null;
	private static long maxMemory = -1;

	private static String[] plugins = null;

	// Instantiation is forbidden
	private ServerMetaInfo() {
	}

	/**
	 * Gets the full version/name of Bukkit that this server is running.
	 * 
	 * @return A highly descriptive string describing what version of (Craft)Bukkit this server is running.
	 */
	public static String getBukkitVersion() {
		if (bukkitVersion == null) {
			// Adapted from http://jd.bukkit.org/rb/doxygen/d7/d14/Bukkit_8java_source.html
			bukkitVersion = Bukkit.getName() + " version " + Bukkit.getVersion() + " (Implementing API Version "
					+ Bukkit.getBukkitVersion() + ")";
		}
		return bukkitVersion;
	}

	/**
	 * Gets the version of Java that this server is using.
	 * 
	 * @return A string that represents the version of Java this server uses.
	 */
	public static String getJavaVersion() {
		if (javaVersion == null) {
			javaVersion = System.getProperty("java.version");
		}
		return javaVersion;
	}

	/**
	 * Gets the name of the operating system that this server is running,
	 * 
	 * @return A string that represents the operating system currently in use.
	 */
	public static String getOSName() {
		if (osName == null) {
			osName = System.getProperty("os.name");
		}
		return osName;
	}

	/**
	 * Gets the architecture (x86/x64) that this server is running on.
	 * 
	 * @return A string that represents the architecture type of this server.
	 */
	public static String getOSArchitecture() {
		if (osArch == null) {
			osArch = System.getProperty("os.arch");
		}
		return osArch;
	}

	/**
	 * Gets the total amount of memory available to the JVM.
	 * 
	 * @return The amount of memory, in bytes, that this CraftBukkit server can access.
	 */
	public static long getTotalMemory() {
		if (maxMemory == -1) {
			maxMemory = Runtime.getRuntime().maxMemory();
		}
		return maxMemory;
	}

	/**
	 * Gets a list of all plugins and their versions on this server.
	 * 
	 * @return A list of strings which represents all of the plugins (with versions) on this server.
	 */
	public static String[] getPluginNames() {
		if (plugins == null) {
			plugins = new String[Bukkit.getPluginManager().getPlugins().length];
			for (int i = 0; i < plugins.length; i++) {
				PluginDescriptionFile pdf = Bukkit.getPluginManager().getPlugins()[i].getDescription();
				plugins[i] = pdf.getFullName();
			}
		}
		return plugins;
	}
}
