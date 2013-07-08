package com.sammarder.iheartdevs.traceprocessing;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

/**
 * Custom event that is fired when a stack trace is successfully logged to a file.
 */
public class StackTraceCreationEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private TraceMetaInfo info;

	/**
	 * Constructor for creating a new STCEvent.
	 * 
	 * @param plugin
	 *            The plugin that caused the stack trace to occur.
	 */
	public StackTraceCreationEvent(TraceMetaInfo info) {
		this.info = info;
	}

	/**
	 * Gets the plugin that caused the stack trace.
	 * 
	 * @return The plugin that caused this stack trace.
	 */
	public Plugin getPlugin() {
		return info.getPlugin();
	}

	/**
	 * Gets the full file name where the
	 * 
	 * @return
	 */
	public String getQualifiedFileName() {
		return info.getFile();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
