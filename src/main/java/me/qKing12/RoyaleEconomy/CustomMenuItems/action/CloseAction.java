package me.qKing12.RoyaleEconomy.CustomMenuItems.action;

import org.bukkit.entity.Player;

public class CloseAction implements ClickAction{

    @Override
    public void executeFor(Player player) {
        player.closeInventory();
    }
}
