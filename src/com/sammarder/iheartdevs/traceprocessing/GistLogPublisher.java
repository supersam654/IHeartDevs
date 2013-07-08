package com.sammarder.iheartdevs.traceprocessing;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;

import com.sammarder.iheartdevs.IHeartDevs;

/**
 * Contains objects for publishing a log file to gist.github.com. As a general rule, you should pass in non-null
 * objects. If you receive a non-null object back, everything wen't well.
 */
public class GistLogPublisher {

	// If I end up supporting more "pastebin-like" sites, this will support much more abstraction.

	// No explicit constructor because it is not needed.

	/**
	 * Publishes a log file to gist.github.com.
	 * 
	 * @param title
	 *            The non-null title of the file.
	 * @param description
	 *            The non-null description of the file.
	 * @param log
	 *            the non-null file that contains the log information.
	 * @return A string with the url of the log, null otherwise.
	 */
	public String publish(String title, String description, File log) {
		StringBuilder payload = new StringBuilder();
		String temp;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(log));
			while ((temp = reader.readLine()) != null) {
				// Escape the json newline
				payload.append(temp + "\\n");
			}
		} catch (FileNotFoundException e) {
			IHeartDevs.log(Level.WARNING, "Could not find the file to publish.");
			return null;
		} catch (IOException e) {
			IHeartDevs.log(Level.WARNING, "Could not read from file to publish.");
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}

		// TODO: Actually use JSON/GSON
		String json = "{\"description\": \"" + description + "\",\"public\": true, \"files\": {\"" + title
				+ "\": {\"content\": \"" + payload.toString() + "\"}}}";
		String postURL = "https://api.github.com/gists";

		String response = doPOST(postURL, json);
		if (response == null) {
			return null;
		}

		String urlString = getURL(response);
		try {
			// Tests to make sure that the returned result is a website.
			new URL(urlString);
			return urlString;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Performs a POST request to a given url with the given payload.
	 * 
	 * @param urlString
	 *            The URL to POST.
	 * @param payload
	 *            The payload to send.
	 * @return The response from the POST request. This is null if something went wrong.
	 */
	private String doPOST(String urlString, String payload) {
		HttpURLConnection connection = getPOSTConnection(urlString);
		if (connection == null) {
			return null;
		}

		DataOutputStream os = null;
		try {
			os = new DataOutputStream(connection.getOutputStream());
			os.writeBytes(payload);
			os.flush();
		} catch (IOException e) {
			IHeartDevs.log(Level.WARNING, "Could not POST data to Gist.");
			e.printStackTrace();
			return null;
		} finally {
			try {
				os.close();
			} catch (Exception e) {
			}
		}

		StringBuilder sb = new StringBuilder();
		String temp;
		BufferedReader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while ((temp = is.readLine()) != null) {
				sb.append(temp);
			}
		} catch (IOException e) {
			IHeartDevs.log(Level.WARNING, "Could not receive POST message from Gist.");
			e.printStackTrace();
			return null;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}

		return sb.toString();

	}

	/**
	 * Creates a connection that is ready for a POST request.
	 * 
	 * @param urlString
	 *            The url to establish a connection with.
	 * @return A connection to the given url or null if something went wrong.
	 */
	private HttpURLConnection getPOSTConnection(String urlString) {
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (Exception e) {
			System.out.println("Bad url given.");
			e.printStackTrace();
			return null;
		}

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (Exception e) {
			System.out.println("Could not establish connection to URL.");
			e.printStackTrace();
			return null;
		}

		try {
			connection.setRequestMethod("POST");
		} catch (Exception e) {
			System.out.println("URL does not support POST.");
			e.printStackTrace();
			return null;
		}

		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		try {
			connection.connect();
		} catch (Exception e) {
			System.out.println("Could not establish connection to URL.");
			e.printStackTrace();
			return null;
		}

		return connection;
	}

	/**
	 * Gets the URL where the log is stored.
	 * 
	 * @param response
	 *            The response from the server from the POST request.
	 * @return The url to the log or null if something went wrong.
	 */
	private String getURL(String response) {
		JsonObject object = new JsonParser().parse(response).getAsJsonObject();
		String url = object.get("html_url").toString();
		// JSON is known to leave strings as "string"
		url = url.replace("\"", "");
		return url;
	}

}
