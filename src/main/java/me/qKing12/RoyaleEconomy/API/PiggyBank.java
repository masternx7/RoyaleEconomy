package me.qKing12.RoyaleEconomy.API;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PiggyBank {

    public ItemStack piggyBankGet(){
        ItemStack piggyBank = RoyaleEconomy.staticValues.piggyBank.clone();
        NBTItem nbt = new NBTItem(piggyBank);
        nbt.setString("PiggyBank", UUID.randomUUID().toString());
        return piggyBank;
    }

}
