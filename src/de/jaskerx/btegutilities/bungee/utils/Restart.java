package de.jaskerx.btegutilities.bungee.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import de.jaskerx.btegutilities.bungee.main.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Restart {
	
	Map<String, ServerInfo> servers = new TreeMap<>();
	Map<String, ServerInfo> restartedServers = new TreeMap<>();
	boolean timerRuns = false;
	int counter = 120;
	Timer timer;
	boolean whenEmpty;
	private static boolean doProxyRestart = true;
	ProxiedPlayer player;
	
	
	
	public Restart(String[] serversArgs, int delay, @Nullable ProxiedPlayer p, boolean whenEmpty) {
		
		stop();
		this.whenEmpty = whenEmpty;
		counter = delay;
		player = p;
		servers.putAll(Servers.fromInput(serversArgs));
		
		if(p != null) {
			if(timerRuns) {
				p.sendMessage(new ComponentBuilder("§b§lBTEG §7> §6Änderungen §6wurden §6übernommen.").create());
			}
			TextComponent comp = new TextComponent("§6oder ");
				comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/bteg restart stop"));
			p.sendMessage(new ComponentBuilder("§b§lBTEG §7> §9/bteg §9restart §9stop ").append(comp).append("§9hier §6klicken, §6um §6den §6Restart §6folgender §6Server §6abzubrechen:").create());
			servers.forEach((k, v) -> {
				p.sendMessage(new ComponentBuilder(" §9" + k).create());
			});
		}
		
		if(whenEmpty) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					restart();
				}
			}, 0, 60000);
		}
		
		if(!timerRuns && !whenEmpty) {
			timerRuns = true;
			BungeeCord.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				
				@Override
				public void run() {
					
					if(counter == 0) {
						restart();
						timerRuns = false;
						counter = 120;
						servers.clear();
						BungeeCord.getInstance().getScheduler().cancel(Main.getInstance());
						return;
					}
					
					int h = counter / 60 / 60;
					int min = (counter / 60) % 60;
					int s = counter % 60;
					
					String hours = "�1hour" + (h != 1 ? "s" : "");
					String minutes = "�1" + min + " �1minute" + (min != 1 ? "s" : "");
					String seconds = "�1" + s + " �1second" + (s != 1 ? "s" : "");
										
					if(counter == delay) {
						if(p != null) p.sendMessage(new ComponentBuilder("§b§lBTEG §7> §6Restarting §6in §9" + h + ":" + min + ":" + s + " �h").create());
					}
					
					// hours
					if(min == 0 && s == 0) {
						sendMessage(ChatMessageType.CHAT, "§b§lBTEG §7> §cServerrestart §cin §1" + hours + "!");
					}
					
					// last hour
					if(h == 0) {
						
						// chat	minutes						
						if((s == 0 && (min == 30 || min == 15 || min == 10 || min == 5 || min == 1)) || (s == 30 && min == 1)) {
							sendMessage(ChatMessageType.CHAT, "§b§lBTEG §7> §cServerrestart §cin " + minutes + (s != 0 ? " §1and §1" + seconds : "") + "!");
						}
						// chat seconds last minute
						if(min == 0 && (s == 30 || s <= 15)) {
							sendMessage(ChatMessageType.CHAT, "§b§lBTEG §7> §cServerrestart §cin " + seconds + "!");
						}
						
						// action bar
						if(min < 15) {
							sendMessage(ChatMessageType.ACTION_BAR, "§c§lServerrestart §c§lin §1" + (min >= 1 ? (s >= 1 ? min + ":" + (s < 10 ? "0" : "") + s + " §1min" : minutes) : seconds));
						}						
					}
					
					counter--;
				}
				
			}, 0, 1, TimeUnit.SECONDS);
		}
		
	}
	
	public void stop() {
		BungeeCord.getInstance().getScheduler().cancel(Main.getInstance());
		timerRuns = false;
		counter = 120;
		if(timer != null) timer.cancel();
		if(player != null) player.sendMessage(new ComponentBuilder("§b§lBTEG §7> §6Der §6Countdown §6wurde §6abgebrochen.").create());
	}
	
	private void sendMessage(ChatMessageType type, String message) {
		BungeeCord.getInstance().getServers().forEach((k, v) -> {
			if(servers.containsKey(k) || servers.containsKey("Proxy-1")) {
				for(ProxiedPlayer p : v.getPlayers()) {
					p.sendMessage(type, new ComponentBuilder(message).create());
				}
			}
		});
	}
	
	public void restart() {
		doProxyRestart = servers.containsKey("Proxy-1");
		BungeeCord.getInstance().getServers().forEach((k, v) -> {
			if(v.getPlayers().size() != 0) {
				doProxyRestart = false;
			}
		});
		servers.forEach((k, v) -> {
			if(!restartedServers.containsKey(k) && (BungeeCord.getInstance().getServers().containsValue(v) && ((whenEmpty && v.getPlayers().size() == 0) || !whenEmpty)) || (k.equals("Proxy-1") && (doProxyRestart || !whenEmpty))) {
				BungeeCord.getInstance().getPluginManager().dispatchCommand(BungeeCord.getInstance().getConsole(), "cloud service " + k + (k.equals("Proxy-1") ? " stop" : " restart"));
				restartedServers.put(k, v);
			}
		});
		if(((doProxyRestart && whenEmpty) || (servers.size() == restartedServers.size())) && timer != null) {
			timer.cancel();
		}
	}
}