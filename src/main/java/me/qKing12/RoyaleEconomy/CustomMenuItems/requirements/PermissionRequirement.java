package me.qKing12.RoyaleEconomy.CustomMenuItems.requirements;

import org.bukkit.entity.Player;

public class PermissionRequirement implements ItemRequirement{
    private final String permission;
    private final String denyMessage;

    public PermissionRequirement(String permission, String denyMessage){
        this.permission = permission;
        this.denyMessage = denyMessage;
    }

    @Override
    public boolean isValid(Player player) {
        return player.hasPermission(permission);
    }

    @Override
    public String getDenyMessage() {
        return denyMessage;
    }
}
