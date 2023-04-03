package de.jaskerx.bteg.utilities.bungee.utils;

import java.util.List;

import net.md_5.bungee.api.ProxyServer;
import org.json.JSONArray;
import org.json.JSONObject;

import de.jaskerx.bteg.utilities.bungee.main.Main;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;

public class MaintenanceRunnable implements Runnable {

	private final JSONObject object;
	private boolean proxy;
	private final List<ServerInfo> servers;
	
	public MaintenanceRunnable(JSONObject object, List<ServerInfo> servers, boolean proxy) {
		this.object = object;
		this.servers = servers;
		this.proxy = proxy;
	}

	@Override
	public void run() {
		
		JSONArray json = Main.json;
		for(int i = 0; i < json.length(); i++) {

			if(json.getJSONObject(i).equals(object)) {
				servers.forEach(s -> {
					s.getPlayers().forEach(p -> {
						if(!p.hasPermission("bteg.maintenance.join")) {
							p.connect(ProxyServer.getInstance().getServerInfo("Lobby-1"));
							p.sendMessage(new ComponentBuilder("§b§lBTEG §7> §cAuf §cdiesem §cServer §cfinden §czum §caktuellen §cZeitpunkt §cWartungsarbeiten §cstatt!").create());
						}
					});
				});
				if(proxy) {
					ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "cloudnet syncproxy target Proxy maintenance true");
					proxy = false;
				}
			}
		}
	}
	
}
