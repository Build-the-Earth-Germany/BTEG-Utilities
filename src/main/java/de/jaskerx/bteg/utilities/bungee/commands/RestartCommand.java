package de.jaskerx.bteg.utilities.bungee.commands;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jaskerx.bteg.utilities.bungee.BTEGUtilitiesBungeeCord;
import de.jaskerx.bteg.utilities.bungee.registry.RestartsRegistry;
import de.jaskerx.bteg.utilities.bungee.restart.Restart;
import de.jaskerx.bteg.utilities.bungee.restart.RestartsIDsManager;
import de.jaskerx.bteg.utilities.bungee.Servers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class RestartCommand extends Command implements TabExecutor {

	private final BTEGUtilitiesBungeeCord plugin;
	private final RestartsRegistry restartsRegistry;
	private final RestartsIDsManager restartsIDsManager;
	
	
	public RestartCommand(BTEGUtilitiesBungeeCord plugin, RestartsRegistry restartsRegistry, RestartsIDsManager restartsIDsManager) {
		super("bteg");
		this.plugin = plugin;
		this.restartsRegistry = restartsRegistry;
		this.restartsIDsManager = restartsIDsManager;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		
		if(sender instanceof ProxiedPlayer player) {

			if(!player.hasPermission("bteg.restart")) {
				player.sendMessage(new ComponentBuilder("ᾠ §cDu §cbist §cnicht §cberechtigt, §cdiesen §cCommand §causzuführen!").create());
				return;
			}

			if(args.length == 1 && args[0].equalsIgnoreCase("restarts")) {
				if(this.restartsRegistry.getRestarts().size() == 0) {
					player.sendMessage(new ComponentBuilder("ᾠ §6Es wurden noch keine Restarts hinzugefügt.").create());
					return;
				}
				for(Restart restart : this.restartsRegistry.getRestarts().values()) {
					player.sendMessage(new ComponentBuilder("ᾠ §6ID: " + restart.getId() + (restart.getName() != null ? String.format(", %s", restart.getName()) : "")).create());
				}
				return;
			}

			if(args.length == 2 && args[0].equalsIgnoreCase("restart") && args[1].equalsIgnoreCase("help")) {
				player.sendMessage(new ComponentBuilder("ᾠ §6Nutze den Command folgendermaßen:").create());
				player.sendMessage(new ComponentBuilder("ᾠ §6/bteg server §c[server] §6restart §c[optional: Delay (z.B. 1h2m3s) oder \"empty\" (wenn Server leer restarten)] §c[optional: name]").create());
				player.sendMessage(new ComponentBuilder("ᾠ §6/bteg restarts").create());
				player.sendMessage(new ComponentBuilder("ᾠ §6/bteg restart §c[id] §6cancel").create());
				return;
			}

			if(args.length < 3) {
				ProxyServer.getInstance().getPluginManager().dispatchCommand(player, "bteg restart help");
				return;
			}

			switch (args[0].toLowerCase()) {
				case "restart" -> {
					if(!(args[1].matches("\\d+") && args[2].equalsIgnoreCase("cancel"))) {
						ProxyServer.getInstance().getPluginManager().dispatchCommand(player, "bteg restart help");
						return;
					}
					int id = Integer.parseInt(args[1]);
					Restart restart = this.restartsRegistry.getRestart(id);
					if(restart == null) {
						player.sendMessage(new ComponentBuilder("ᾠ §cDie ID ist ungültig!").create());
						return;
					}
					restart.cancel();
					this.restartsRegistry.unregister(restart);
					player.sendMessage(new ComponentBuilder("ᾠ §6Der restart wurde abgebrochen.").create());
					return;
				}
				case "server" -> {
					if(!args[2].equalsIgnoreCase("restart")) {
						ProxyServer.getInstance().getPluginManager().dispatchCommand(player, "bteg restart help");
						return;
					}
					String[] serversArgs = args[1].split(",");
					Map<String, ServerInfo> servers = Servers.fromInput(serversArgs);

					switch (args.length) {
						case 3 -> {
							int id = this.restartsIDsManager.getAndClaimNextId();
							Restart restart = new Restart(id, null, this.plugin, servers, 120, player, false, this.restartsRegistry);
							this.restartsRegistry.register(restart);
							player.sendMessage(new ComponentBuilder("ᾠ §6Der restart wurde hinzugefügt. §cID: " + id).create());
							restart.start();
							return;
						}
						case 4 -> {
							String name = null;
							int delay = 120;
							boolean whenEmpty = false;

							if(args[3].matches("(\\d+[hms])+")) {
								delay = this.getDelayFromInput(args[3]);
							} else if (args[3].equals("empty")) {
								delay = 0;
								whenEmpty = true;
							} else {
								name = args[3];
							}

							int id = this.restartsIDsManager.getAndClaimNextId();
							Restart restart = new Restart(id, name, this.plugin, servers, delay, player, whenEmpty, this.restartsRegistry);
							this.restartsRegistry.register(restart);
							player.sendMessage(new ComponentBuilder("ᾠ §6Der restart wurde hinzugefügt. §cID: " + id + (name != null ? (", Name: " + name) : "")).create());
							restart.start();
							return;
						}
						case 5 -> {
							int delay;
							boolean whenEmpty = false;

							if(args[3].matches("(\\d+[hms])+")) {
								delay = this.getDelayFromInput(args[3]);
							} else if (args[3].equals("empty")) {
								delay = 0;
								whenEmpty = true;
							} else {
								ProxyServer.getInstance().getPluginManager().dispatchCommand(player, "bteg restart help");
								return;
							}
							String name = args[4];

							int id = this.restartsIDsManager.getAndClaimNextId();
							Restart restart = new Restart(id, name, this.plugin, servers, delay, player, whenEmpty, this.restartsRegistry);
							this.restartsRegistry.register(restart);
							player.sendMessage(new ComponentBuilder("ᾠ §6Der restart wurde hinzugefügt. §cID: " + id + ", Name: " + name).create());
							restart.start();
							return;
						}
					}
				}
			}

			ProxyServer.getInstance().getPluginManager().dispatchCommand(player, "bteg restart help");
		}
	}

	private int getDelayFromInput(String input) {
		int delay = 0;
		Matcher matcherHours = Pattern.compile("\\d+h").matcher(input);
		Matcher matcherMinutes = Pattern.compile("\\d+m").matcher(input);
		Matcher matcherSeconds = Pattern.compile("\\d+s").matcher(input);
		while (matcherHours.find()) {
			delay += Integer.parseInt(matcherHours.group().substring(0, matcherHours.group().length() - 1)) * 3600;
		}
		while (matcherMinutes.find()) {
			delay += Integer.parseInt(matcherMinutes.group().substring(0, matcherMinutes.group().length() - 1)) * 60;
		}
		while (matcherSeconds.find()) {
			delay += Integer.parseInt(matcherSeconds.group().substring(0, matcherSeconds.group().length() - 1));
		}
		return delay;
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if(!sender.hasPermission("bteg.restart")) {
			sender.sendMessage(new ComponentBuilder("ᾠ §cDu §cbist §cnicht §cberechtigt, §cdiesen §cCommand §causzuführen!").create());
			return null;
		}

		Set<String> result = new HashSet<>();

		switch (args.length) {
			case 1 -> {
				if("server".startsWith(args[0].toLowerCase())) {
					result.add("server");
				}
				if("restart".startsWith(args[0].toLowerCase())) {
					result.add("restart");
				}
				if("restarts".startsWith(args[0].toLowerCase())) {
					result.add("restarts");
				}
				if("help".startsWith(args[0].toLowerCase())) {
					result.add("help");
				}
			}
			case 3 -> {
				if("restart".startsWith(args[2].toLowerCase()) && args[0].equalsIgnoreCase("server")) {
					result.add("restart");
				}
				if("cancel".startsWith(args[2].toLowerCase()) && args[0].equalsIgnoreCase("restart")) {
					result.add("cancel");
				}
			}
		}

		return result;
	}
	
	
	
}
