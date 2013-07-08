package com.sammarder.iheartdevs;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.sammarder.iheartdevs.traceprocessing.ErrorFileComparator;
import com.sammarder.iheartdevs.traceprocessing.ErrorFileFilter;
import com.sammarder.iheartdevs.traceprocessing.FilteredErrorStream;
import com.sammarder.iheartdevs.traceprocessing.GistLogPublisher;
import com.sammarder.iheartdevs.traceprocessing.IMessageProcessor;
import com.sammarder.iheartdevs.traceprocessing.StackTraceProcessor;

//TODO: Colored console log

/**
 * Main entry point for IHeartDevs. Performs any initialization required to get everything running.
 */
public final class IHeartDevs extends JavaPlugin {
	// The approximate time this plugin was loaded. If reload is used, this will be reset.
	private static long startTime = System.currentTimeMillis();
	// The print stream that replaces the regular System.err print stream.
	private PrintStream filteredErrorStream;
	// The original print stream that used to be bound to System.err.
	private PrintStream oldErr;
	// Only needed because onLoad() can't pass it to onEnable().
	private FolderManager errorFolderManager = null;

	// onLoad() is called before onEnable. This forces loading before all plugins. If a stack trace occurs when another
	// plugin is enabling, it will be caught by us.
	@Override
	public void onLoad() {
		// Ensure that all necessary directories exist.
		File errorFolder = new File(this.getDataFolder(), "Errors");
		errorFolder.mkdirs();
		errorFolderManager = new FolderManager(errorFolder, new ErrorFileFilter(), new ErrorFileComparator<File>());

		// Begin capturing stack traces!
		initializeLogFilter(errorFolderManager);
	}

	@Override
	public void onEnable() {
		// Setup standard plugin things.
		this.saveDefaultConfig();
		ConfigurationManager.loadConfiguration(this);
		Bukkit.getPluginManager().registerEvents(new ListenerManager(), this);
		this.getCommand("ihd").setExecutor(
				new CommandManager(errorFolderManager, new GistLogPublisher(), new SimpleDateFormat()));

		tempTesting();
	}

	@Override
	public void onDisable() {
		// Replace our error stream with the old one again.
		System.setErr(oldErr);
		filteredErrorStream = null;
	}

	/**
	 * Initializes the stack trace "hijacking" portion of this plugin.
	 */
	private void initializeLogFilter(FolderManager folderManager) {
		oldErr = System.err;
		IMessageProcessor stackTraceProcessor = new StackTraceProcessor(this, folderManager);
		filteredErrorStream = new FilteredErrorStream(stackTraceProcessor);
		System.setErr(filteredErrorStream);
	}

	/**
	 * Gets the time that this plugin was started (which is hopefully approximately when the server started).
	 * 
	 * @return The time this plugin was initialized.
	 */
	// I know that this does't particularly belong here, but it is initialized at startup and it's the best place I can
	// think of.
	public static long getStartTime() {
		return startTime;
	}

	private void tempTesting() {
		try {
			throw new Exception("Mother of God!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			throw new Exception("God of Mothers!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs a string to console with a given urgency.
	 * 
	 * @param level
	 *            How important the message is.
	 * @param message
	 *            The message to log.
	 */
	public static void log(Level level, String message) {
		Bukkit.getLogger().log(level, "[IHeartDevs] " + message);
	}

	/**
	 * Equivalent to log(Level.INFO, message);
	 * 
	 * @param message
	 *            The message to log.
	 */
	public static void log(String message) {
		log(Level.INFO, message);
	}
}
