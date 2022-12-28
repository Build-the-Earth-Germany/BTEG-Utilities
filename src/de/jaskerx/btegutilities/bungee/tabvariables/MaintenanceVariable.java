package de.jaskerx.btegutilities.bungee.tabvariables;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import codecrafter47.bungeetablistplus.api.bungee.Variable;
import de.jaskerx.btegutilities.bungee.main.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MaintenanceVariable extends Variable {

	public MaintenanceVariable() {
		super("maintenances");
	}

	@Override
	public String getReplacement(ProxiedPlayer player) {
		
		JSONArray json = Main.json;
		
		if(json.length() == 0) return null;
		
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < json.length(); i++) {
			JSONObject o = json.getJSONObject(i);
			JSONArray servers = o.getJSONArray("servers");
			JSONObject oTime = o.getJSONObject("time");
			
			for(int j = 0; j < servers.length(); j++) {
				if(player.getServer().getInfo().getName().equals(servers.getString(j)) || servers.getString(j).equals("Proxy-1")) {
					String date = Main.convertDate(oTime.getInt("year"), oTime.getInt("month"), oTime.getInt("day"));
					String time = oTime.getInt("hour") + ":" + (oTime.getInt("minute") < 10 ? "0" : "") + oTime.getInt("minute");
					builder.append("\n§6" + o.getString("name") + ": §c" + date + " §c" + time);
					break;
				}
			}
		}
		if(builder.length() == 0) return null;
		
		builder.insert(0, "\n§6§lGeplante Wartungsarbeiten");
		builder.append("\n");
		
		return new String(builder);
	}

}
