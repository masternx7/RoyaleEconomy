package me.qKing12.RoyaleEconomy.MultiCurrency.internal;

import de.rapha149.signgui.SignGUI;
import me.qKing12.RoyaleEconomy.Gambling.Gambling;
import me.qKing12.RoyaleEconomy.Gambling.GamblingMenu;
import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;

public class CurrencyInputMenu {
    private boolean closed = false;

    public CurrencyInputMenu(Player p, Currency currency, boolean selling){
        p.closeInventory();
        if(RoyaleEconomy.plugin.InputMethod.equals("sign")){
            try {
                SignGUI.builder()
                        .setLine(1, MultiCurrencyHandler.inputMessage.get(0))
                        .setLine(2, MultiCurrencyHandler.inputMessage.get(1))
                        .setLine(3, MultiCurrencyHandler.inputMessage.get(2))
                        .setHandler((player, lines) -> {
                            String input = lines.getLine(0);

                            try{
                                Double toSet= messageHelper.getCoinsFromFormat(input);
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new CurrencyExchangeMenu(p, currency, selling, toSet));
                            } catch (Exception e) {
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.invalid-number"))));
                            }

                            return Collections.emptyList();
                        })
                        .build()
                        .open(p);
            }catch(Exception x){
                x.printStackTrace();
                RoyaleEconomy.plugin.getLogger().warning("An error has occured on the Sign GUIs. Make sure you have ProtocolLib installed.");
            }
        }
        else if(RoyaleEconomy.plugin.InputMethod.equals("anvil")){
            ArrayList<String> lore=new ArrayList<>();
            for(String line : MultiCurrencyHandler.inputMessage)
                lore.add(Utils.chat(line));

            ItemStack paper = RoyaleEconomy.itemConstructor.getItem(Material.PAPER, " ", lore);

            new AnvilGUI.Builder()
                    .onClose(player -> {
                        if (closed)
                            return;
                        new CurrencyExchangeMenu(p, currency, selling);
                    })
                    .onClick((slot, reply) -> {
                        try{
                            if(slot != AnvilGUI.Slot.OUTPUT) {
                                return Collections.emptyList();
                            }

                            Double toSet = messageHelper.getCoinsFromFormat(reply.getText());
                            closed = true;
                            new CurrencyExchangeMenu(p, currency, selling, toSet);
                        } catch (Exception e) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.invalid-number")));
                        }
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .itemLeft(paper)
                    .title(" ")
                    .text(" ")
                    .plugin(RoyaleEconomy.plugin)
                    .open(p);
        }
        else {
            for (String line : MultiCurrencyHandler.inputMessage)
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            new ChatListener(p, (reply) -> {
                try{
                    Double toSet= messageHelper.getCoinsFromFormat(reply);
                    new CurrencyExchangeMenu(p, currency, selling, toSet);
                } catch (Exception e) {
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.invalid-number")));
                }
            });
        }
    }

}
