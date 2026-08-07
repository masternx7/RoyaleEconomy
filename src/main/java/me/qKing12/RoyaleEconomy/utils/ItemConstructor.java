package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;



public interface ItemConstructor {

    ItemStack getItem(Material material, String name, ArrayList<String> lore);

    ItemStack getItem(Material material, short data, String name, ArrayList<String> lore);

    ItemStack getItem(String material, String name, ArrayList<String> lore);

    ItemStack getItemFromMaterial(String material);

    default ItemStack applyPlaceholdersAndGetClone(Player player, ItemStack item) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        try {
            String newName = RoyaleEconomy.utilsAPI.chat(player, meta.getDisplayName());
            if (newName != null) {
                meta.setDisplayName(newName);
            }
        }catch (Exception e){

        }

        try {
            ArrayList<String> newLore = new ArrayList<>();
            for (String line : meta.getLore()) {
                String newLine = RoyaleEconomy.utilsAPI.chat(player, line);
                if (newLine != null) {
                    newLore.add(newLine);
                }
            }
            meta.setLore(newLore);
        }catch (Exception e){

        }

        clone.setItemMeta(meta);
        return clone;
    }

}
