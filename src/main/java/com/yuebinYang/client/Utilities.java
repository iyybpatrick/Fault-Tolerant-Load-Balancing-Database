package com.bittiger.client;

import com.bittiger.logic.Server;

public class Utilities {
	public static String getUrl(Server server) {
		return "jdbc:mysql://" + server.getIp() + "/tpcw";
	}

	public static String getStatsUrl(String serverIp) {
		return "jdbc:mysql://" + serverIp + "/canvasjs_db";
	}
}
