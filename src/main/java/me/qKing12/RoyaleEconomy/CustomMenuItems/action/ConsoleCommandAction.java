package me.qKing12.RoyaleEconomy.CustomMenuItems.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConsoleCommandAction implements ClickAction{
    private final String command;

    public ConsoleCommandAction(String command){
        this.command = command;
    }

    @Override
    public void executeFor(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
    }
}
