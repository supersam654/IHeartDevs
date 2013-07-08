package com.sammarder.iheartdevs;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.sammarder.iheartdevs.traceprocessing.StackTraceCreationEvent;

public class ListenerManager implements Listener {

	/**
	 * Handles a StackTraceCreationEvent. This is called when a new stack trace is successfully logged to a file.
	 * 
	 * @param event
	 *            The specifics for this stack trace.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onStackTraceCompletion(StackTraceCreationEvent event) {
		// Use the actual plugin name if it is detected.
		String pluginName = "an Unknown plugin";
		Plugin plugin = event.getPlugin();
		if (plugin != null) {
			pluginName = plugin.getName();
		}

		IHeartDevs.log(Level.WARNING,
				"A stack trace from " + pluginName + " was logged to " + event.getQualifiedFileName());
		IHeartDevs.log(Level.WARNING, "Type " + Utilities.formatCommand("IHD view latest") + " for more information.");

		String message = "An error occurred with " + pluginName + ". Type "
				+ Utilities.formatCommand("/IHD view latest") + " for more information.";
		Utilities.messageAllWithPermission("IHeartDevs.view", message);
	}
}
