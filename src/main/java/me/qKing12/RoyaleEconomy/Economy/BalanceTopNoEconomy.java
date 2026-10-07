package me.qKing12.RoyaleEconomy.Economy;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import net.essentialsx.api.v2.services.BalanceTop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.qKing12.RoyaleEconomy.Commands.BalanceTopCommand.hiddenPlayers;

public class BalanceTopNoEconomy {
    public Essentials essentials;

    protected static class Pair implements Comparable<Pair> {

        /**
         * Key of this <code>Pair</code>.
         */
        private Double key;

        /**
         * Gets the key for this pair.
         * @return key for this pair
         */
        public Double getKey() { return key; }

        /**
         * Value of this this <code>Pair</code>.
         */
        private String value;

        /**
         * Gets the value for this pair.
         * @return value for this pair
         */
        public String getValue() { return value; }

        /**
         * Creates a new pair
         * @param key The key for this pair
         * @param value The value to use for this pair
         */
        public Pair(Double key, String value) {
            this.key = key;
            this.value = value;
        }

        public int compareTo(Pair that) {
            return this.getKey().compareTo(that.getKey())*-1;
        }
    }

    private boolean firstNotice = false;

    public BalanceTopNoEconomy() {
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        }
    }

    public ArrayList<String> getTop() {
        ArrayList<String> toReturn = new ArrayList<>();

        if (!firstNotice) {
            if (essentials != null) {
                try {
                    final CompletableFuture<Void> future = essentials.getBalanceTop().calculateBalanceTopMapAsync();
                    future.thenRun(() -> {

                        int maximum = RoyaleEconomy.staticValues.balanceTopMaximumPages * RoyaleEconomy.staticValues.balanceTopDisplayPerPage;
                        for (BalanceTop.Entry entry : essentials.getBalanceTop().getBalanceTopCache().values()) {
                            if (!hiddenPlayers.contains(entry.getUuid().toString())) {
                                toReturn.add(RoyaleEconomy.messageHelper.numberFormat(entry.getBalance().doubleValue()));
                                toReturn.add(entry.getDisplayName());
                            }
                            maximum--;
                            if (maximum == 0)
                                break;
                        }

                    });

                    return toReturn;
                } catch (Exception | NoSuchMethodError x) {
                    try{
                        ArrayList<Pair> getOnline = new ArrayList<>();
                        for(UUID uuid : essentials.getUserMap().getAllUniqueUsers()) {
                            User user = essentials.getUserMap().getUser(uuid);
                            if(user.getMoney().doubleValue()!=0d && user.getName()!=null && !user.getName().equals("null"))
                                getOnline.add(new Pair(user.getMoney().doubleValue(), user.getName()));
                        }

                        Collections.sort(getOnline);

                        for (Pair entry : getOnline) {
                            if (!hiddenPlayers.contains(entry.getValue())) {
                                toReturn.add(RoyaleEconomy.messageHelper.numberFormat(entry.getKey()));
                                toReturn.add(entry.getValue());
                            }
                        }

                        return toReturn;
                    }catch(Exception | NoSuchMethodError x2){
                        firstNotice=true;
                        RoyaleEconomy.plugin.getLogger().warning("Error while trying to load balancetop, there is a problem with your Essentials. Please contact me on discord/polymart.org!");
                        RoyaleEconomy.plugin.getLogger().warning("This error will not be shown again until the next restart, please ignore it if balancetop doesn't have issues.");
                    }
                }
            }
        }


        ArrayList<Pair> getOnline = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            getOnline.add(new Pair(RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString()), player.getName()));
        }

        Collections.sort(getOnline);

        for (Pair entry : getOnline) {
            if (!hiddenPlayers.contains(entry.getValue())) {
                toReturn.add(RoyaleEconomy.messageHelper.numberFormat(entry.getKey()));
                toReturn.add(entry.getValue());
            }
        }

        return toReturn;
    }
}
