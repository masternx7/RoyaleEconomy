package me.qKing12.RoyaleEconomy.CustomMenuItems.requirements;

import org.bukkit.entity.Player;

public interface ItemRequirement {

    boolean isValid(Player player);

    String getDenyMessage();
}
