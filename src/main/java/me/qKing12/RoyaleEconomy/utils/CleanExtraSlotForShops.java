package me.qKing12.RoyaleEconomy.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CleanExtraSlotForShops {

    private static final boolean is18 = Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.21");

    public static void cleanExtraSlotForShops(Player player) {
        if (is18)
            return;

        ItemStack item = player.getInventory().getItemInOffHand();
        if (item == null || item.getType().equals(Material.AIR))
            return;

        NBTItem nbt = new NBTItem(item);
        if (nbt.getBoolean("FreshlyBought") || nbt.getDouble("SellValue") != 0) {
            player.getInventory().setItemInOffHand(null);
            RoyaleEconomy.plugin.getLogger().info("[Shop DUPE Attempt] " + player.getName() + " had an item in a very strange place and is suspected of item dupe attempt!");
        }
    }
}
