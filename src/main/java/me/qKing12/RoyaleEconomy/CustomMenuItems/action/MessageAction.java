package me.qKing12.RoyaleEconomy.CustomMenuItems.action;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageAction implements ClickAction {

    private String message;

    public MessageAction(String message){
        this.message = message;
    }

    @Override
    public void executeFor(Player player) {
        player.sendMessage(RoyaleEconomy.utilsAPI.chat(player, message.replace("%player%", player.getName())));
    }
}
