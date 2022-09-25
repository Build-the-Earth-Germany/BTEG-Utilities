package de.jaskerx.btegutilities.bungee.main;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.jaskerx.btegutilities.bungee.commands.RestartCommand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin {
	
	private static Main instance;
	public static Timer timer;
	public static Restart restart;
	
	@Override
	public void onEnable() {
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RestartCommand());
		ProxyServer.getInstance().registerChannel("Restart");
		
		instance = this;
		
		scheduleRestart();
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

}
