package com.sammarder.iheartdevs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Contains various static utility methods for various tasks. Notably, it does not contain methods for console logging.
 * This class only contains static methods and cannot be instantiated.
 * 
 * @author sam
 * 
 */
public class Utilities {

	// Instantiation is forbidden.
	private Utilities() {
	}

	/**
	 * Utility method that messages all players with a certain permission a certain message.
	 * 
	 * @param permission
	 *            The permission to check for.
	 * @param message
	 *            The message to send if said player have said permission.
	 */
	public static void messageAllWithPermission(String permission, String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission(permission)) {
				player.sendMessage(message);
			}
		}
	}

	/**
	 * Utility method that converts a number of bytes into a human-readable string (uses megabytes instead of bytes if
	 * appropriate, etc.).
	 * 
	 * @param bytes
	 *            The number of bytes to create a string out of.
	 * @return A human-readable string based on the given number of bytes.
	 */
	public static String formatBytes(long bytes) {
		// Less than 5XB is represented in the lower unit.
		if (bytes < 1024 * 5) {
			return String.valueOf(bytes) + " bytes";
		} else if (bytes < 1024 * 1024 * 5) {
			return String.format("%.1f kilobytes", (bytes / 1024.0));
		} else if (bytes < 1024.0 * 1024.0 * 1024.0 * 5.0) {
			return String.format("%.1f megabytes", bytes / 1024.0 / 1024.0);
		} else {
			return String.format("%.1f gigabytes", bytes / 1024.0 / 1024.0 / 1024.0);
		}
	}

	/**
	 * Utility method that converts a number of milliseconds into a human-readable string (uses hours instead
	 * 
	 * @param time
	 *            The time (in milliseconds) to convert.
	 * @return A formatted string containing a more readable version of the time passed in.
	 */
	public static String formatMilliseconds(long time) {
		// Last number is a multiplier for deciding unit.
		// So this returns 75 minutes instead of 1.1 hours.
		if (time < 1000 * 10) {
			return String.valueOf(time) + " milliseconds";
		} else if (time < 1000 * 60 * 2) {
			return String.format("%.1f seconds", (time / 1000.0));
		} else if (time < 1000 * 60 * 60 * 2) {
			return String.format("%.1f minutes", (time / 1000.0 / 60.0));
		} else if (time < 1000 * 60 * 60 * 24 * 2) {
			return String.format("%.1f hours", (time / 1000.0 / 60.0 / 60.0));
		} else if (time < 1000 * 60 * 60 * 24 * 2) {
			return String.format("%.1f days", (time / 1000.0 / 60.0 / 60.0 / 24.0));
		} else {
			return String.format("%.1f weeks", (time / 1000.0 / 60.0 / 60.0 / 24.0 / 7.0));
		}
		// This doesn't support anything above weeks because this was primarily designed to format server up time.
		// Furthermore, months and longer aren't very exact.
	}

	/**
	 * Determines if a string (probably) represents a positive Java integer.
	 * 
	 * @param str
	 *            A non-null String to check.
	 * @return true if the entire string represents a single, positive integer, false otherwise.
	 */
	public static boolean isPositiveInteger(String str) {
		int length = str.length();
		// This doesn't catch values 2.5 billion to 9.9 billion but it's a risk I can afford to take.
		if (length == 0 || length > 10) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c >= ':') {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the (approximate) server uptime.
	 * 
	 * @return Server uptime in milliseconds.
	 */
	public static long getServerUptime() {
		// This needs to be here because it changes every time you call it and Bukkit doesn't have an API call for it.
		return System.currentTimeMillis() - IHeartDevs.getStartTime();
	}

	/**
	 * Formats a message so it displays as a command.
	 * 
	 * @param message
	 *            The string that represents a command.
	 * @return A string with the formatted message.
	 */
	public static String formatCommand(String message) {
		return ChatColor.GOLD + message + ChatColor.RESET;
	}

	/**
	 * Formats a message so that it displays as bold. The necessary letter in arguments should be bolded.
	 * 
	 * @param message
	 *            The string that should be bolded.
	 * @return A string with the formatted message.
	 */
	public static String formatBold(String message) {
		return ChatColor.BOLD + message + ChatColor.RESET;
	}
}
