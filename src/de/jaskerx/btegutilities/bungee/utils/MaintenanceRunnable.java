package de.jaskerx.btegutilities.bungee.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import de.jaskerx.btegutilities.bungee.main.Main;
import net.md_5.bungee.BungeeCord;

public class MaintenanceRunnable implements Runnable {

	private JSONObject object;
	private boolean proxy;
	
	public MaintenanceRunnable(JSONObject object, boolean proxy) {
		this.object = object;
		this.proxy = proxy;
	}

	@Override
	public void run() {
		
		JSONArray json = Main.json;
		for(int i = 0; i < json.length(); i++) {

			if(json.getJSONObject(i).equals(object)) {
				Main.json.remove(i);
				Main.saveJson();
				if(proxy) BungeeCord.getInstance().getPluginManager().dispatchCommand(BungeeCord.getInstance().getConsole(), "cloudnet syncproxy target Proxy maintenance true");
				break;
			}
		}
	}
	
}
