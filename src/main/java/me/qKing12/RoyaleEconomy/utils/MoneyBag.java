package me.qKing12.RoyaleEconomy.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.UUID;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.coinBagsAndTalismansCfg;

public class MoneyBag {

    public static ItemStack generateMoneyBag(double coins) {
        String amount = RoyaleEconomy.messageHelper.numberFormat(coins);
        ItemStack bag = RoyaleEconomy.staticValues.moneyBagItem.clone();
        ItemMeta meta = bag.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        for (String line : meta.getLore())
            lore.add(line.replace("%amount%", amount));
        meta.setLore(lore);
        meta.setDisplayName(meta.getDisplayName().replace("%amount%", amount));
        bag.setItemMeta(meta);
        NBTItem nbt = new NBTItem(bag);
        nbt.setDouble("RoyaleEconomyBag", coins);
        if (!coinBagsAndTalismansCfg.getBoolean("money-bags.allow-stack"))
            nbt.setString("RoyaleEconomyUnique", UUID.randomUUID().toString());
        return nbt.getItem();
    }
}
