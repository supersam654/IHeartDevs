package com.sammarder.iheartdevs;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.sammarder.iheartdevs.traceprocessing.GistLogPublisher;

//TODO: Finish implementing commands.

/**
 * CommandExecutor for IHeartDevs. In other words, it deals with commands. Note that argument detection is a bit fuzzy.
 * Most people are used to inputting commands in a certain way. It's probably a bit different for everyone. Although
 * commands that don't perfectly fit the help file are allowed, I feel that it will improve the user's experience with
 * plugin (even though invalid commands sometimes work).
 */
public class CommandManager implements CommandExecutor {
	// Note: A bunch of these methods are not static when they could be. However, they are private and should never be
	// accessed from a static context so they are all non-static.

	// For maximum compatibility, I'm using spaces.
	private static final String TAB = "    ";

	// A FolderManager object that points to the folder where all of the error logs are stored.
	private FolderManager errorFolderManager;
	// A GistLogPublisher for publishing stack traces online.
	private GistLogPublisher publisher;
	// A SimpleDateFormat for formatting System.currentTimeMillis() in a human-readable format.
	private SimpleDateFormat dateFormat;

	/**
	 * Constructor that initializes a new CommandManager.
	 * 
	 * @param errorFolderManager
	 *            A FolderManager object that points to the error log folder.
	 */
	public CommandManager(FolderManager errorFolderManager, GistLogPublisher publisher, SimpleDateFormat dateFormat) {
		this.errorFolderManager = errorFolderManager;
		this.publisher = publisher;
		this.dateFormat = dateFormat;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// I don't believe that the current command API would allow anything else, but I might as well check for it.
		if (!command.getName().equalsIgnoreCase("ihd")) {
			// Do nothing, but don't show command help either.
			return true;
		}

		// All commands are usable from console and by players (assuming proper permissions).

		if (args.length == 0) {
			// Show main command help.
			return false;
		}

		String primaryArg = args[0].toLowerCase();
		if (primaryArg.equals("help")) {
			return processHelpCommand(sender, args);
		} else if (primaryArg.equals("list")) {
			if (sender.hasPermission("ihd.list")) {
				return processListCommand(sender, args);
			} else {
				displayNoPermissionsMessage(sender);
			}
		} else if (primaryArg.equals("view")) {
			if (sender.hasPermission("ihd.view")) {
				return processViewCommand(sender, args);
			} else {
				displayNoPermissionsMessage(sender);
			}
		} else if (primaryArg.equals("publish")) {
			if (sender.hasPermission("ihd.publish")) {
				return processPublishCommand(sender, args);
			} else {
				displayNoPermissionsMessage(sender);
			}
		}

		// Improper initial argument was supplied so display the main command help.
		return false;
	}

	/**
	 * Processes the "/ihd help ..." command. Returns false if the main command help should be called.
	 * 
	 * @param args
	 *            The arguments from the original command. The first argument should be "help" (case doesn't matter).
	 * @param sender
	 *            The player or console who sent the command. The sender is assumed to have permission to use this
	 *            command.
	 * @return false if the main command help should be displayed, true otherwise.
	 */
	private boolean processHelpCommand(CommandSender sender, String... args) {
		// Must have at least two arguments.
		if (args.length < 2) {
			// Only one argument so display the main command help.
			return false;
		}

		String secondaryArg = args[1].toLowerCase();
		if (secondaryArg.equals("view")) {
			displayViewCommandHelp(sender);
			return true;
		} else if (secondaryArg.equals("list")) {
			displayListCommandHelp(sender);
			return true;
		} else if (secondaryArg.equals("publish")) {
			displayPublishCommandHelp(sender);
			return true;
		}
		// Second arg did not match a command, so display the main command help.
		return false;
	}

	/**
	 * Processes the "/ihd list ..." command.
	 * 
	 * @param args
	 *            The arguments from the original command. The first argument must be "list" (but case doesn't matter).
	 * @param sender
	 *            The player or console who sent the command. The sender is assumed to have permission to use this
	 *            command.
	 * @return true, unless something goes awry. Then generatePseudoHelpCommand("list", sender) will be returned.
	 */
	private boolean processListCommand(CommandSender sender, String... args) {
		// Accept "list" and "list ##"
		if (args.length > 2) {
			displayListCommandHelp(sender);
			return true;
		}

		int i = 1;
		// Use the given page number, if provided.
		if (args.length == 2) {
			try {
				i = Integer.parseInt(args[1]);
				if (i < 1) {
					// Not terribly efficient, but prevents code duplication.
					throw new Exception();
				}
			} catch (Exception e) {
				displayListCommandHelp(sender);
				return true;
			}
		}

		File[] files = errorFolderManager.getFilesFromPage(i);
		if (files == null) {
			sender.sendMessage("There aren't " + String.valueOf(i) + " pages of error logs. Congratulations!");
		} else {
			sender.sendMessage("ID\t\t\tDate");
			for (File file : files) {
				sender.sendMessage(file.getName() + "\t\t" + dateFormat.format(new Date(file.lastModified())));
			}
		}

		// Turns out we always return true.
		return true;
	}

	/**
	 * Processes the "/ihd view ..." command.
	 * 
	 * @param args
	 *            The arguments from the original command. The first argument must be "view" (but case doesn't matter).
	 * @param sender
	 *            The player or console who sent the command. The sender is assumed to have permission to use this
	 *            command.
	 * @return true, unless something goes awry. then generatePseudoHelpCommand("view", sender) will be returned.
	 */
	private boolean processViewCommand(CommandSender sender, String... args) {
		// Accept view <id>
		if (args.length != 2) {
			displayViewCommandHelp(sender);
			return true;
		}

		int id = 0;

		try {
			id = Integer.parseInt(args[1]);
			if (id < 1) {
				// Not terribly efficient, but prevents code duplication.
				throw new Exception();
			}
		} catch (Exception e) {
			displayViewCommandHelp(sender);
			return true;
		}

		File file = errorFolderManager.getFile(String.valueOf(id) + ".txt");
		if (file == null) {
			sender.sendMessage("Could not find an error log with an ID of " + String.valueOf(id));
			return true;
		}
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String temp;
			boolean foundException = false;
			while ((temp = reader.readLine()) != null) {
				if (!foundException) {
					if (temp.equals("Stacktrace:")) {
						foundException = true;
					}
					continue;
				} else {
					sender.sendMessage(temp);
				}
			}
			if (!foundException) {
				sender.sendMessage("Malformed file. Please type "
						+ Utilities.formatCommand("/ihd publish " + String.valueOf(id))
						+ " and send the log to the creator of IHeartDevs.");
			}
			return true;
		} catch (FileNotFoundException e) {
			sender.sendMessage("Could not find an error log with an ID of " + String.valueOf(id));
			return true;
		} catch (IOException e) {
			sender.sendMessage("Something bad happened when trying to read error log " + String.valueOf(id));
			return true;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}

	}

	/**
	 * Processes the "/ihd publish ..." command.
	 * 
	 * @param args
	 *            The arguments from the original command. The first argument must be "publish" (but case doesn't
	 *            matter).
	 * @param sender
	 *            The player or console who sent the command. The sender is assumed to have permission to use this
	 *            command.
	 * @return true, unless something goes awry. Then generatePseudoHelpCommand("publish", sender) will be returned.
	 */
	private boolean processPublishCommand(CommandSender sender, String... args) {
		if (args.length != 2) {
			displayPublishCommandHelp(sender);
			return true;
		}

		int id = 0;
		try {
			id = Integer.parseInt(args[1]);
		} catch (Exception e) {
			displayPublishCommandHelp(sender);
			return true;
		}

		File file = errorFolderManager.getFile(String.valueOf(id) + ".txt");
		if (file == null) {
			sender.sendMessage("Could not find file: " + String.valueOf(id) + ".txt");
			return true;
		}
		String result = publisher.publish("Stacktrace", "Bug Report provided by IHeartDevs", file);
		if (result != null) {
			sender.sendMessage("Log " + String.valueOf(id) + " has been successfully published to " + result);
			sender.sendMessage("Please pass that link on to the developer of the plugin.");
		} else {
			sender.sendMessage("An error occurred when trying to publish the error report. See the console log for more details.");
		}

		return true;
	}

	/**
	 * Displays command help for the "/ihd view" command.
	 * 
	 * @param sender
	 *            The CommandSender to send the message(s) to.
	 */
	private void displayViewCommandHelp(CommandSender sender) {
		sender.sendMessage("The view command prints out the specific details of an error log.");
		sender.sendMessage("Syntax: " + Utilities.formatCommand("/ihd view <id>"));
		sender.sendMessage("Arguments: ");
		sender.sendMessage(TAB + "id: The ID the log to view.");
		sender.sendMessage("Note: Use " + Utilities.formatCommand("/ihd help list") + " for help getting a log ID.");
	}

	/**
	 * Displays command help for the "/ihd list" command.
	 * 
	 * @param sender
	 *            The CommandSender to send the message(s) to.
	 */
	private void displayListCommandHelp(CommandSender sender) {
		sender.sendMessage("The list command lists several previous error logs and their associated IDs.");
		sender.sendMessage("Syntax: " + Utilities.formatCommand("/ihd list <N>"));
		sender.sendMessage("Arguments: ");
		sender.sendMessage(TAB + "N: Displays the previous N error logs.");
		// Unimplemented arguments
		// sender.sendMessage(TAB + "-time #W#D#H#M#S: Displays error logs from a relative point in time.");
		// sender.sendMessage(TAB + TAB + "Ex: " + Utilities.formatCommand("/ihd list -t 3D40M")
		// + " lists logs that are older than 3 days, 40 minute.");
		// sender.sendMessage(TAB
		// + "-plugin Name: Displays error logs from a given plugin. Note that this is case sensitive.");
		// sender.sendMessage(TAB + "-descending: Displays results in reverse order");
	}

	/**
	 * Displays command help for the "/ihd publish" command.
	 * 
	 * @param sender
	 *            The CommandSender to send the message(s) to.
	 */
	private void displayPublishCommandHelp(CommandSender sender) {
		sender.sendMessage("The publish command uploads an error log to gist.github.com for easy sharing with developers.");
		sender.sendMessage("Syntax: " + Utilities.formatCommand("/ihd publish <id>"));
		sender.sendMessage("Arguments: ");
		sender.sendMessage(TAB + "id: The ID of the log to publish.");
		sender.sendMessage("Note: Use " + Utilities.formatCommand("/ihd help list") + " for help getting a log ID.");
	}

	/**
	 * Convenience method that allows all commands to display a uniform "no permissions" message.
	 * 
	 * @param sender
	 *            The CommandSender to send the rejection to.
	 */
	private void displayNoPermissionsMessage(CommandSender sender) {
		sender.sendMessage(Color.red + "Keep calm and try a command you actually have permissions for.");
	}

	/*
	 * private boolean containsArg(String arg, String... args) { for (String s : args) { // Turns out none of the
	 * arguments actually need to start with a -dash. if (s.startsWith("-")) { s = s.substring(1); }
	 * 
	 * // It's a match if they are the same. // It's also a match if s is a single letter and arg starts with that
	 * letter. if (arg.equalsIgnoreCase(s) || arg.substring(0, 1).equalsIgnoreCase(s)) { return true; } } return false;
	 * }
	 */
}
