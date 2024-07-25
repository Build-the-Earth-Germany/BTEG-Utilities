package de.jaskerx.bteg.utilities.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class PlotsCommand extends Command {

    public PlotsCommand() {
        super("plots");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(commandSender, "server Plot-1");
    }

}
