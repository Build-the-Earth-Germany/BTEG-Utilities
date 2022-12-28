package de.jaskerx.btegutilities.bungee.listeners;

import org.json.JSONArray;
import org.json.JSONObject;

import de.jaskerx.btegutilities.bungee.main.Main;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {

	@EventHandler
	public void onServerSwitch(ServerSwitchEvent event) {
		
		JSONArray json = Main.json;
		for(int i = 0; i < json.length(); i++) {
			JSONObject o = json.getJSONObject(i);
			JSONArray servers = o.getJSONArray("servers");
			JSONObject oTime = o.getJSONObject("time");
			
			for(int j = 0; j < servers.length(); j++) {
				
				if(event.getPlayer().getServer().getInfo().getName().equals(servers.getString(j)) || servers.getString(j).equals("Proxy-1")) {
					String date = Main.convertDate(oTime.getInt("year"), oTime.getInt("month"), oTime.getInt("day"));
					String time = oTime.getInt("hour") + ":" + (oTime.getInt("minute") < 10 ? "0" : "") + oTime.getInt("minute");
					event.getPlayer().sendMessage(new ComponentBuilder("§6Wartungsarbeiten: §c" + date + " §c" + time + " §6" + o.getString("name")).create());
					break;
				}
			}
		}
	}
	
}
