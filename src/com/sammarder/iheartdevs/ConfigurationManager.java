package com.sammarder.iheartdevs;

/**
 * Static class for easy access to config information. Because all methods are static, instantiation is forbidden.
 */
public class ConfigurationManager {
	private static boolean useExtendedInfo = true;

	// Instantiation is forbidden
	private ConfigurationManager() {
	}

	/**
	 * Reloads settings from config.yml into memory. If something goes wrong, defaults will be used (fail-safe).
	 * 
	 * @param plugin
	 *            An instance of IHeartDevs for getting config details from.
	 */
	public static void loadConfiguration(IHeartDevs plugin) {
		// The default is the current value.
		useExtendedInfo = plugin.getConfig().getBoolean("LogExtendedInfo", useExtendedInfo);
	}

	/**
	 * Determines if the extended info should be logged. Calling this before loadConfiguration() has been called causes
	 * internal defaults to be used (which is not recommended).
	 * 
	 * @return true if extended info should be logged, false otherwise.
	 */
	public static boolean shouldUseExtendedInfo() {
		return useExtendedInfo;
	}
}
