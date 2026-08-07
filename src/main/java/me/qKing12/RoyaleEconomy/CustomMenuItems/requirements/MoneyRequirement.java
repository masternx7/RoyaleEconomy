package me.qKing12.RoyaleEconomy.CustomMenuItems.requirements;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.entity.Player;

public class MoneyRequirement implements ItemRequirement{
    private final double amount;

    private final String denyMessage;

    public MoneyRequirement(double amount, String denyMessage){
        this.amount = amount;
        this.denyMessage = denyMessage;
    }

    @Override
    public boolean isValid(Player player) {
        return RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString())>=amount;
    }

    @Override
    public String getDenyMessage() {
        return denyMessage;
    }
}
