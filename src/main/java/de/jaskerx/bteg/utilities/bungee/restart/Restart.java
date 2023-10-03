package de.jaskerx.bteg.utilities.bungee.restart;

import java.util.*;
import java.util.concurrent.TimeUnit;

import de.jaskerx.bteg.utilities.bungee.BTEGUtilitiesBungeeCord;
import de.jaskerx.bteg.utilities.bungee.registry.RestartsRegistry;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class Restart {

	private final int id;
	private final String name;
	private final BTEGUtilitiesBungeeCord plugin;
	private final Map<String, ServerInfo> servers;
	private final Map<String, ServerInfo> restartedServers = new TreeMap<>();
	private final int initialDelay;
	private final ProxiedPlayer player;
	private final boolean whenEmpty;
	private final RestartsRegistry restartsRegistry;
	int counter;
	private ScheduledTask scheduledTask;
	
	
	
	public Restart(int id, String name, BTEGUtilitiesBungeeCord plugin, Map<String, ServerInfo> servers, int delay, ProxiedPlayer player, boolean whenEmpty, RestartsRegistry restartsRegistry) {
		this.id = id;
		this.name = name;
		this.plugin = plugin;
		this.whenEmpty = whenEmpty;
		this.initialDelay = delay;
		this.counter = delay;
		this.player = player;
		this.restartsRegistry = restartsRegistry;
		this.servers = new TreeMap<>(servers);
	}

	public void start() {
		this.cancel();

		if(player != null) {
			TextComponent comp = new TextComponent("§9hier ");
			comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/bteg restart " + this.id + " cancel"));
			player.sendMessage(new ComponentBuilder("ᾠ §9/bteg §9restart §9" + this.id + " §9cancel §6oder ").append(comp).append("§6klicken, §6um §6den §6Restart §6folgender §6Server §6abzubrechen:").create());
			servers.forEach((k, v) -> player.sendMessage(new ComponentBuilder(" §9" + k).create()));
		}

		this.scheduledTask = ProxyServer.getInstance().getScheduler().schedule(this.plugin, () -> {
			if(counter == 0) {
				if(this.whenEmpty) {
					this.checkRestart();
				} else {
					this.restartNow();
				}
				this.scheduledTask.cancel();
				return;
			}

			int h = counter / 60 / 60;
			int min = (counter / 60) % 60;
			int s = counter % 60;

			String hours = "§1" + h + " §1hour" + (h != 1 ? "s" : "");
			String minutes = "§1" + min + " §1minute" + (min != 1 ? "s" : "");
			String seconds = "§1" + s + " §1second" + (s != 1 ? "s" : "");

			if(counter == initialDelay && player != null) {
				player.sendMessage(new ComponentBuilder("ᾠ §6Restarting §6in §9" + h + ":" + min + ":" + s + " §9h").create());
			}

			// hours
			if(min == 0 && s == 0) {
				sendMessage(ChatMessageType.CHAT, "ᾠ §cServerrestart §cin §1" + hours + "!");
			}

			// last hour
			if(h == 0) {

				// chat	minutes
				if((s == 0 && (min == 30 || min == 15 || min == 10 || min == 5 || min == 1)) || (s == 30 && min == 1)) {
					sendMessage(ChatMessageType.CHAT, "ᾠ §cServerrestart §cin " + minutes + (s != 0 ? " §1and §1" + seconds : "") + "!");
				}
				// chat seconds last minute
				if(min == 0 && (s == 30 || s <= 15)) {
					sendMessage(ChatMessageType.CHAT, "ᾠ §cServerrestart §cin " + seconds + "!");
				}

				// action bar
				if(min < 15) {
					sendMessage(ChatMessageType.ACTION_BAR, "§c§lServerrestart §c§lin §1" + (min >= 1 ? (s >= 1 ? min + ":" + (s < 10 ? "0" : "") + s + " §1min" : minutes) : seconds));
				}
			}

			counter--;
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	public void cancel() {
		if(this.scheduledTask != null) {
			this.scheduledTask.cancel();
		}
		counter = initialDelay;
		if(player != null) player.sendMessage(new ComponentBuilder("ᾠ §6Der §6Countdown §6wurde §6abgebrochen.").create());
	}
	
	private void sendMessage(ChatMessageType type, String message) {
		ProxyServer.getInstance().getServers().forEach((k, v) -> {
			if(servers.containsKey(k) || servers.containsKey("Proxy-1")) {
				for(ProxiedPlayer p : v.getPlayers()) {
					p.sendMessage(type, new ComponentBuilder(message).create());
				}
			}
		});
	}
	
	public void restartNow() {
		for(Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
			String command = "cloud service " + entry.getKey() + (entry.getKey().equals("Proxy-1") || entry.getKey().equals("Lobby-1") ? " stop" : " restart");
			ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
		}
		this.restartsRegistry.unregister(this);
	}

	public void checkRestart() {
		if(this.counter > 0) {
			return;
		}

		for(Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
			if(this.restartedServers.containsKey(entry.getKey()) || (entry.getValue() != null && entry.getValue().getPlayers().size() > 0)) {
				continue;
			}
			if(entry.getKey().equals("Proxy-1") && ProxyServer.getInstance().getServers().values().stream().anyMatch(serverInfo -> serverInfo.getPlayers().size() > 0)) {
				continue;
			}
			String command = "cloud service " + entry.getKey() + (entry.getKey().equals("Proxy-1") || entry.getKey().equals("Lobby-1") ? " stop" : " restart");
			ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
			this.restartedServers.put(entry.getKey(), entry.getValue());
		}
		if(this.restartedServers.size() == this.servers.size()) {
			this.restartsRegistry.unregister(this);
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isWhenEmpty() {
		return whenEmpty;
	}
}