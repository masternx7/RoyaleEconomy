package me.qKing12.RoyaleEconomy.CustomMenuItems;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class CustomItemsHandler {

    private HashMap<String, HashMap<Integer, CustomItem>> items;

    private HashMap<Integer, CustomItem> getMenu(ConfigurationSection menu) {
        HashMap<Integer, CustomItem> menuMap = new HashMap<>();
        for (String itemKey : menu.getKeys(false)) {
            CustomItem item = new CustomItem(menu.getConfigurationSection(itemKey));
            menuMap.put(item.getSlot(), item);
        }

        return menuMap;
    }

    public CustomItemsHandler() {
        if (!RoyaleEconomy.customItemsCfg.getBoolean("use-custom-items"))
            return;

        items = new HashMap<>();

        for (String key : RoyaleEconomy.customItemsCfg.getConfigurationSection("menus").getKeys(false)) {
            items.put(key, getMenu(RoyaleEconomy.customItemsCfg.getConfigurationSection("menus." + key)));
        }
    }

    public HashMap<Integer, CustomItem> getItems(String menu) {
        if(items == null)
            return null;
        return items.getOrDefault(menu, null);
    }

    public boolean tryClick(String menu, Integer slot, Player player) {
        HashMap<Integer, CustomItem> items = getItems(menu);
        if (items == null)
            return false;

        CustomItem item = items.getOrDefault(slot, null);
        if (item == null)
            return false;

        if(item.ableToClick(player)){
            item.runActions(player);
        }

        return true;
    }

}
