package de.jaskerx.btegutilities.bungee.commands;

import java.util.HashSet;
import java.util.Set;

import de.jaskerx.btegutilities.bungee.main.Main;
import de.jaskerx.btegutilities.bungee.utils.Restart;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class RestartCommand extends Command implements TabExecutor {
	
	Restart restart;
	int delay = -1;
	
	
	public RestartCommand() {
		super("bteg");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		
		if(sender instanceof ProxiedPlayer) {
			
			ProxiedPlayer p = (ProxiedPlayer) sender;
			
			if(!p.hasPermission("bteg.restart")) {
				p.sendMessage(new ComponentBuilder("§b§lBTEG §7> §cDu §cbist §cnicht §cberechtigt, §cdiesen §cCommand §causzuführen!").create());
				return;
			}
			
			if(args.length >= 3 && args[0].equalsIgnoreCase("server") && args[2].equalsIgnoreCase("restart")) {
				
				String[] serversArgs = args[1].split(",");
				if(restart != null) {
					restart.stop();
				}
				
				if(args.length == 4) {
					if(args[3].equalsIgnoreCase("empty")) {
						restart = new Restart(serversArgs, delay, p, true);
					} else {
						delay = Integer.valueOf(args[3]);
						restart = new Restart(serversArgs, delay, p, false);
					}
				} else {
					delay = 120;
					restart = new Restart(serversArgs, delay, p, false);
				}
				
			} else if(args.length >= 2 && args[0].equalsIgnoreCase("restart") && args[1].equalsIgnoreCase("stop")) {
				if(restart != null) {
					restart.stop();
				}
			} else if(args.length >= 3 && args[0].equalsIgnoreCase("restart") && args[1].equalsIgnoreCase("daily")) {
				if(args[2].equalsIgnoreCase("cancel")) {
					Main.timer.cancel();
					if(Main.restart != null) {
						Main.restart.stop();
					}
					p.sendMessage(new ComponentBuilder("§b§lBTEG §7> §6Die §6Server §6werden §6nicht §6automatisch §6neugestartet.").create());
				} else if(args[2].equalsIgnoreCase("start")) {
					Main.scheduleRestart();
					p.sendMessage(new ComponentBuilder("§b§lBTEG §7> §6Die §6Server §6werden §6automatisch §6neugestartet.").create());
				}
			}
		}
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> result = new HashSet<>();
		if(args.length == 1) {
			if("server".startsWith(args[0].toLowerCase())) {
				result.add("server");
			} if("restart".startsWith(args[0].toLowerCase())) {
				result.add("restart");
			}
		} else if(args.length == 2 && "stop".startsWith(args[1].toLowerCase())) {
			result.add("stop");
		} else if(args.length == 3) {
			result.add("restart");
		}
		return result;
	}
	
	
	
}
