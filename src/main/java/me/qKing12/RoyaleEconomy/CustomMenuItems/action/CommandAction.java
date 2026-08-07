package me.qKing12.RoyaleEconomy.CustomMenuItems.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandAction implements ClickAction{
    private final String command;

    public CommandAction(String command){
        this.command = command;
    }

    @Override
    public void executeFor(Player player) {
        Bukkit.dispatchCommand(player, command.replace("%player%", player.getName()));
    }
}
