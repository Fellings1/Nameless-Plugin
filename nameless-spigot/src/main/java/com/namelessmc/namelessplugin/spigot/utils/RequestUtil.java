package com.namelessmc.namelessplugin.spigot.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

public class RequestUtil {

	NamelessPlugin plugin;

	public RequestUtil(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public void getNotifications(Player player) throws Exception{
		String toPostString = "uuid=" + URLEncoder.encode(player.getUniqueId().toString().replace("-", ""), "UTF-8");

		URL apiConnection = new URL(plugin.getAPIUrl() + "/getNotifications");

		HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Length", Integer.toString(toPostString.length()));
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setDoOutput(true);
		connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

		// Initialise output stream
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

		// Write request
		outputStream.writeBytes(toPostString);

		// Initialise input stream
		InputStream inputStream = connection.getInputStream();

		// Handle response
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		StringBuilder responseBuilder = new StringBuilder();

		String responseString;
		while((responseString = streamReader.readLine()) != null)
			responseBuilder.append(responseString);
		JsonObject response = new JsonObject();
		JsonParser parser = new JsonParser();
		JsonObject message = new JsonObject();

		response = parser.parse(responseBuilder.toString()).getAsJsonObject();

		if(response.has("error")){
			// Error with request
			player.sendMessage(ChatColor.RED + "Error: " + response.get("message").getAsString());
		} else if(response.has("error") && response.getAsString().equalsIgnoreCase("Can't find user with that UUID!")){
			player.sendMessage(ChatColor.RED + "You must register to get notifications.");
		} else if(message.get("alerts").toString() == "0" && message.get("messages").toString() == "0"){
			player.sendMessage(ChatColor.GOLD + "Alerts: " + ChatColor.RED + "None");
			player.sendMessage(ChatColor.GOLD + "PMs: " + ChatColor.RED + "None");
		} else if(message.get("alerts").toString() == "0"){
			player.sendMessage(ChatColor.GOLD + "Alerts: " + ChatColor.RED + "None");
			player.sendMessage(ChatColor.GOLD + "PMs: " + ChatColor.GREEN + message.get("messages").toString());
		} else if(message.get("messages").toString() == "0"){
			player.sendMessage(ChatColor.GOLD + "Alerts: " + ChatColor.GREEN + message.get("alerts").toString());
			player.sendMessage(ChatColor.GOLD + "PMs: " + ChatColor.RED + "None");
		} else {
			player.sendMessage(ChatColor.GOLD + "Alerts: " + ChatColor.GREEN + message.get("alerts").toString());
			player.sendMessage(ChatColor.GOLD + "PMs: " + ChatColor.GREEN + message.get("messages").toString());
		}

		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();
	}

}