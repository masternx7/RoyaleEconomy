package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.Commands.MoneyBagCommand;
import org.bukkit.inventory.ItemStack;

public class CoinsBags {


    public ItemStack getCoinsBag(double amount){
        return MoneyBagCommand.generateMoneyBag(amount);
    }
}
