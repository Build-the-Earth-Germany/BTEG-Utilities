package de.jaskerx.btegutilities.bungee.main;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatMessageType;
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
	int[] specialTimes = new int[] {1800, 900, 600, 300, 120, 60};
	Timer timer;
	boolean whenEmpty;
	private static boolean doProxyRestart = true;
	ProxiedPlayer player;
	
	
	
	public Restart(String[] serversArgs, int delay, @Nullable ProxiedPlayer p, boolean whenEmpty) {
		
		stop();
		this.whenEmpty = whenEmpty;
		counter = delay;
		player = p;
		servers.putAll(getServers(serversArgs));
		
		if(p != null) {
			if(timerRuns) {
				p.sendMessage(new ComponentBuilder("§b§lBTEG §7» §6Änderungen §6wurden §6übernommen.").create());
			}
			TextComponent comp = new TextComponent("§6oder ");
				comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/bteg restart stop"));
			p.sendMessage(new ComponentBuilder("§b§lBTEG §7» §9/bteg §9restart §9stop ").append(comp).append("§9hier §6klicken, §6um §6den §6Restart §6folgender §6Server §6abzubrechen:").create());
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
					
					sendMessage(ChatMessageType.ACTION_BAR, "§c§lServerrestart §c§lin §c§l" + counter);
					
					for(int t : specialTimes) {
						if(counter == t) {
							if(counter == 60 ) {
								sendMessage(ChatMessageType.CHAT, "§b§lBTEG §7» §cServerrestart §cin §11 §1minute" + "!");
							} else {
								sendMessage(ChatMessageType.CHAT, "§b§lBTEG §7» §cServerrestart §cin §1" + counter/60 + " §1minutes" + "!");
							}
						}
					}
					
					if(counter == 30 || counter <= 15) {
						if(counter == 1) {
							sendMessage(ChatMessageType.CHAT, "§b§lBTEG §7» §cServerrestart §cin §11 §1second!");
						} else {
							sendMessage(ChatMessageType.CHAT, "§b§lBTEG §7» §cServerrestart §cin §1" + counter + " §1seconds!");
						}
					}
					
					if(counter == 0) {
						restart();
						timerRuns = false;
						counter = 120;
						servers.clear();
						BungeeCord.getInstance().getScheduler().cancel(Main.getInstance());
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
		if(player != null) player.sendMessage(new ComponentBuilder("§b§lBTEG §7» §6Der §6Countdown §6wurde §6abgebrochen.").create());
	}
	
	private Map<String, ServerInfo> getServers(String[] serversArgs) {
		
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
				BungeeCord.getInstance().getPluginManager().dispatchCommand(BungeeCord.getInstance().getConsole(), "cloud service " + k + " restart");
				restartedServers.put(k, v);
			}
		});
		if(((doProxyRestart && whenEmpty) || (servers.size() == restartedServers.size())) && timer != null) {
			timer.cancel();
		}
	}
}