package me.qKing12.RoyaleEconomy.MultiCurrency.internal;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class CurrencyBalanceTop {
    private Currency currency;
    private WrappedTask updater;
    private List<String[]> top = new ArrayList<>();

    private String footer;
    private String header;
    private String playerStructure;

    public CurrencyBalanceTop(Currency currency, String footer, String header, String playerStructure){
        this.currency = currency;
        this.footer = footer;
        this.header = header;
        this.playerStructure = playerStructure;
        updater = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(this::triggerRefresh, 1, 20L*60L*5L);
    }

    public void displayTop(Player player){
        PlayerMessageHandler.messageSend(player, header);
        int position=1;
        for(String[] s : top){
            String message = playerStructure
                    .replace("%top-position%", String.valueOf(position++))
                    .replace("%player-name%", s[0])
                    .replace("%amount%", currency.formatMoney(Double.parseDouble(s[1])));
            PlayerMessageHandler.messageSend(player, message);
        }
        PlayerMessageHandler.messageSend(player, footer);
    }

    public List<String[]> getTop() {
        return top;
    }

    public void triggerRefresh(){
        top = RoyaleEconomy.dataManager.getCurrencyBalanceTop(currency.getCurrencyId());
    }

    public Currency getCurrency() {
        return currency;
    }

    public void stopUpdating(){
        updater.cancel();
    }
}
