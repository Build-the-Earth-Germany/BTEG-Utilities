package de.jaskerx.btegutilities.bungee.utils;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

public class Servers {

	public static Map<String, ServerInfo> fromInput(String[] serversArgs) {
		
		Map<String, ServerInfo> serversRes = new HashMap<>();
		for(String s : serversArgs) {
			s = Character.toUpperCase(s.charAt(0)) + s.toLowerCase().substring(1);
			Map<String, ServerInfo> servers = BungeeCord.getInstance().getServers();
			
			if(s.equalsIgnoreCase("all")) {
				serversRes.putAll(servers);
				serversRes.put("Proxy-1", null);
				break;
			}
			
			String[] filters = new String[] {s + "-1", "Terra-" + s};
			
			for(String f : filters) {
				if(servers.containsKey(f)) {
					serversRes.put(f, servers.get(f));
				} else if(f.equals("Proxy-1")) {
					serversRes.put(f, null);
				}
			}
			
			if(s.length() == 3 && s.charAt(1) == '-') {
				String[] range = s.split("-");
				for(int i = Integer.valueOf(range[0]); i <= Integer.valueOf(range[1]); i++) {
					if(servers.containsKey("Terra-" + i)) {
						serversRes.put("Terra-" + i, servers.get("Terra-" + i));
					}
				}
				
			}
		}
		return serversRes;
	}
	
}
