package de.jaskerx.btegutilities.bungee.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.jaskerx.btegutilities.bungee.main.Main;
import de.jaskerx.btegutilities.bungee.utils.Servers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MaintenanceCommand extends Command implements TabExecutor {
	
	public MaintenanceCommand() {
		super("maintenance");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		
		if(!sender.hasPermission("bteg.maintenance")) {
			sender.sendMessage(new ComponentBuilder("§b§lBTEG §7> §cDu §cbist §cnicht §cberechtigt, §cdiesen §cCommand §causzuführen!").create());
			return;
		}
		
		if(args.length >= 5 && args[0].equalsIgnoreCase("add")) {
			
			Map<String, ServerInfo> servers = Servers.fromInput(args[2].split(","));
			
			JSONArray arrayServers = new JSONArray();
			servers.forEach((k, v) -> {
				arrayServers.put(k);
			});
			
			String[] date = args[3].split("\\.");
			String[] time = args[4].split(":");
			
			Main.json.put(new JSONObject()
					.put("name", args[1])
					.put("servers", arrayServers)
					.put("time", new JSONObject()
									.put("day", Integer.parseInt(date[0]))
									.put("month", Integer.parseInt(date[1]))
									.put("year", Integer.parseInt(date[2]))
									.put("hour", Integer.parseInt(time[0]))
									.put("minute",Integer.parseInt(time[1]))));
			
			Main.saveJson();
			sender.sendMessage(new ComponentBuilder("§b§lBTEG §7> §c" + args[1] + " §6wurde §6gespeichert!").create());
		}
		
		if(args.length >= 2 && args[0].equalsIgnoreCase("cancel")) {
			
			JSONArray json = Main.json;
			
			for(int i = 0; i < json.length(); i++) {
				JSONObject o = json.getJSONObject(i);
				if(o.getString("name").equals(args[1])) {
					json.remove(i);
					Main.saveJson();
					sender.sendMessage(new ComponentBuilder("§b§lBTEG §7> §c" + args[1] + " §6wurde §6entfernt!").create());
					return;
				}
			}
			
			sender.sendMessage(new ComponentBuilder("§b§lBTEG §7> §6Es §6gibt §6keine §6Wartungsarbeiten §6mit §6diesem §6Namen!").create());
		}
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		
		List<String> res = new ArrayList<>();
		String[] options1 = new String[] {"add", "cancel"};
		
		if(args.length == 1) {
			for(String s : options1) {
				if(s.startsWith(args[0].toLowerCase())) res.add(s);
			}
		}
		
		if(args.length == 2 && args[0].equalsIgnoreCase("cancel")) {
			JSONArray json = Main.json;
			for(int i = 0; i < json.length(); i++) {
				String name = json.getJSONObject(i).getString("name");
				if(name.toLowerCase().startsWith(args[1].toLowerCase())) res.add(name);
			}
		}
		
		return res;
	}

}
