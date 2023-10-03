package de.jaskerx.bteg.utilities.bungee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import de.jaskerx.bteg.utilities.bungee.maintenance.Maintenance;
import de.jaskerx.bteg.utilities.bungee.registry.MaintenancesRegistry;
import de.jaskerx.bteg.utilities.bungee.registry.RestartsRegistry;
import de.jaskerx.bteg.utilities.bungee.restart.RestartsIDsManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import de.jaskerx.bteg.utilities.bungee.commands.MaintenanceCommand;
import de.jaskerx.bteg.utilities.bungee.commands.RestartCommand;
import de.jaskerx.bteg.utilities.bungee.listeners.ServerSwitchListener;
import de.jaskerx.bteg.utilities.bungee.maintenance.MaintenanceRunnable;
import de.jaskerx.bteg.utilities.bungee.restart.Restart;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BTEGUtilitiesBungeeCord extends Plugin {
	
	private ScheduledExecutorService scheduledExecutorServiceDailyRestart;
	private ScheduledExecutorService scheduledExecutorServiceMaintenance;
	private Restart dailyRestart;
	
	@Override
	public void onEnable() {
		RestartsIDsManager restartsIDsManager = new RestartsIDsManager();
		RestartsRegistry restartsRegistry = new RestartsRegistry(restartsIDsManager);
		MaintenancesRegistry maintenancesRegistry = new MaintenancesRegistry(this, "maintenances.json");
		maintenancesRegistry.loadMaintenances();

		ProxyServer.getInstance().getPluginManager().registerCommand(this, new MaintenanceCommand(maintenancesRegistry));
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RestartCommand(this, restartsRegistry, restartsIDsManager));

		ProxyServer.getInstance().registerChannel("Restart");
		
		ProxyServer.getInstance().getPluginManager().registerListener(this, new ServerSwitchListener(restartsRegistry, maintenancesRegistry));
		
		this.scheduleDailyRestart(restartsRegistry, restartsIDsManager);
		this.scheduleMaintenances(maintenancesRegistry);

		Function<TabPlayer, Object> placeholderFunction = tabPlayer -> {
			if(maintenancesRegistry.getMaintenances().size() == 0) {
				return "";
			}

			StringBuilder builder = new StringBuilder();

			for(Maintenance maintenance : maintenancesRegistry.getMaintenances().values()) {
				if (!maintenance.proxy() && maintenance.servers().stream().noneMatch(serverInfo -> serverInfo.equals(((ProxiedPlayer) tabPlayer.getPlayer()).getServer().getInfo()))) {
					continue;
				}
				String date = BTEGUtilitiesBungeeCord.convertDate(maintenance.time().getYear(), maintenance.time().getMonthValue(), maintenance.time().getDayOfMonth());
				String time = maintenance.time().getHour() + ":" + (maintenance.time().getMinute() < 10 ? "0" : "") + maintenance.time().getMinute();
				builder.append("\n§6").append(maintenance.name()).append(": §c").append(date).append(" §c").append(time);
			}
			if(builder.length() == 0) return "";

			builder.insert(0, "\n§6§lGeplante Wartungsarbeiten");
			builder.append("\n");

			return builder.toString();
		};

		TabAPI.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%maintenances-display%", 1000, placeholderFunction);
	}
	
	@Override
	public void onDisable() {
		TabAPI.getInstance().getPlaceholderManager().unregisterPlaceholder("%maintenances-display%");
		this.scheduledExecutorServiceDailyRestart.shutdownNow();
		this.scheduledExecutorServiceMaintenance.shutdownNow();
	}
	
	public void scheduleDailyRestart(RestartsRegistry restartsRegistry, RestartsIDsManager restartsIDsManager) {
		ZonedDateTime dateTimeNow = LocalDateTime.now(ZoneId.of("Europe/Berlin")).atZone(ZoneId.of("Europe/Berlin"));
		ZonedDateTime dateTimeRestart = dateTimeNow.withHour(3).withMinute(58).withSecond(0);
		if(dateTimeRestart.isBefore(dateTimeNow)) {
			dateTimeRestart = dateTimeRestart.plusDays(1);
		}

		if(this.dailyRestart != null) {
			this.dailyRestart.cancel();
			restartsRegistry.unregister(this.dailyRestart);
		}
		if(this.scheduledExecutorServiceDailyRestart != null) {
			this.scheduledExecutorServiceDailyRestart.shutdownNow();
		}

		this.scheduledExecutorServiceDailyRestart = Executors.newSingleThreadScheduledExecutor();

		this.dailyRestart = new Restart(restartsIDsManager.getAndClaimNextId(), "Daily Restart", this, Servers.fromInput("all"), 120, null, true, restartsRegistry);
		restartsRegistry.register(this.dailyRestart);
		this.scheduledExecutorServiceDailyRestart.schedule(() -> {
			if(!restartsRegistry.getRestarts().containsValue(this.dailyRestart)) {
				return;
			}
			this.dailyRestart.start();
		}, dateTimeNow.until(dateTimeRestart, ChronoUnit.SECONDS), TimeUnit.SECONDS);
	}

	public void scheduleMaintenances(MaintenancesRegistry maintenancesRegistry) {
		if(this.scheduledExecutorServiceMaintenance != null) {
			this.scheduledExecutorServiceMaintenance.shutdownNow();
		}
		this.scheduledExecutorServiceMaintenance = new ScheduledThreadPoolExecutor(maintenancesRegistry.getMaintenances().size());

		for(Maintenance maintenance : maintenancesRegistry.getMaintenances().values()) {
			ZonedDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin")).atZone(ZoneId.of("Europe/Berlin"));
			long delay = ChronoUnit.MILLIS.between(now, maintenance.time());
			this.scheduledExecutorServiceMaintenance.schedule(new MaintenanceRunnable(maintenance), delay, TimeUnit.MILLISECONDS);
		}
	}
	
	public static String convertDate(int year, int month, int day) {
		LocalDate search = LocalDate.of(year, month, day);
		LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));
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
