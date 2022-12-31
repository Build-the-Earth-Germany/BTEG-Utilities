package de.jaskerx.btegutilities.bungee.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import de.jaskerx.btegutilities.bungee.commands.MaintenanceCommand;
import de.jaskerx.btegutilities.bungee.commands.RestartCommand;
import de.jaskerx.btegutilities.bungee.listeners.ServerSwitchListener;
import de.jaskerx.btegutilities.bungee.tabvariables.MaintenancesVariable;
import de.jaskerx.btegutilities.bungee.utils.MaintenanceRunnable;
import de.jaskerx.btegutilities.bungee.utils.Restart;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin {
	
	private static Main instance;
	public static Timer timer;
	public static Restart restart;
	public static JSONArray json;
	private static File dir;
	private static File f;
	private static ScheduledExecutorService scheduledMaintenances;
	
	@Override
	public void onEnable() {
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new MaintenanceCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RestartCommand());
		
		ProxyServer.getInstance().registerChannel("Restart");
		
		ProxyServer.getInstance().getPluginManager().registerListener(this, new ServerSwitchListener());
		
		instance = this;
		
		scheduleRestart();
		
		dir = getDataFolder();
		f = new File(dir, "maintenances.json");
		loadJson();
		scheduleMaintenances();
	}
	
	@Override
	public void onLoad() {
		BungeeTabListPlusAPI.registerVariable(this, new MaintenancesVariable());
	}
	
	@Override
	public void onDisable() {
		timer.cancel();
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public static void scheduleRestart() {
		
		Calendar restartCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
		restartCal.set(Calendar.HOUR_OF_DAY, 4);
		restartCal.set(Calendar.MINUTE, 0);
		restartCal.set(Calendar.SECOND, 0);
		if(restartCal.before(Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")))) {
			restartCal.add(Calendar.DATE, 1);
		}
		if(restart != null) restart.stop();
		if(timer != null) timer.cancel();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				restart = new Restart(new String[] {"all"}, 120, null, true);
			}
		}, restartCal.getTime());
	}
	
	public static void scheduleMaintenances() {
				
		if(scheduledMaintenances != null) scheduledMaintenances.shutdownNow();
		scheduledMaintenances = new ScheduledThreadPoolExecutor(json.length());
		
		for(int i = 0; i < json.length(); i++) {
			
			boolean proxy = false;
			JSONArray servers = json.getJSONObject(i).getJSONArray("servers");
			for(int j = 0; j < servers.length(); j++) {
				if(servers.getString(j).equals("Proxy-1")) {
					proxy = true;
					break;
				}
			}
			
			JSONObject time = json.getJSONObject(i).getJSONObject("time");
			LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
			LocalDateTime search = LocalDateTime.of(time.getInt("year"), time.getInt("month"), time.getInt("day"), time.getInt("hour"), time.getInt("minute"), 0).atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();
			long delay = ChronoUnit.MILLIS.between(now, search);
			
			scheduledMaintenances.schedule(new MaintenanceRunnable(json.getJSONObject(i), proxy), delay, TimeUnit.MILLISECONDS);
		}
	}
	
	private static void loadJson() {
		
		try {
			if(!dir.exists()) dir.mkdir();
			if(!f.exists()) f.createNewFile();
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = br.readLine();
			json = line != null ? new JSONArray(line) : new JSONArray();
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveJson() {
		
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(json.toString());
			fw.close();
			
			scheduleMaintenances();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String convertDate(int year, int month, int day) {
		
		LocalDate search = LocalDate.of(year, month, day);
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
		if(search.isEqual(today)) {
			return "Heute";
		} else if(search.isEqual(tomorrow)) {
			return "Morgen";
		} else {
			return day + "." + (month < 10 ? "0" : "") + month + "." + year;
		}
	}
	
}
