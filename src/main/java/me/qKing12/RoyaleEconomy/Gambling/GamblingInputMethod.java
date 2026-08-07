package me.qKing12.RoyaleEconomy.Gambling;

import de.rapha149.signgui.SignGUI;
import me.qKing12.RoyaleEconomy.API.Events.BankDepositEvent;
import me.qKing12.RoyaleEconomy.API.Events.SharedBankDepositEvent;
import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.Menus.BankDepositMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedBankDepositMenu;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.messageHelper;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class GamblingInputMethod {

    public GamblingInputMethod(Player p){
        p.closeInventory();
        if(RoyaleEconomy.plugin.InputMethod.equals("sign")){
            try {
                SignGUI.builder()
                        .setLine(1, Gambling.gambleInputMessage.get(0))
                        .setLine(2, Gambling.gambleInputMessage.get(1))
                        .setLine(3, Gambling.gambleInputMessage.get(2))
                        .setHandler((player, lines) -> {
                            String input = lines.getLine(0);

                            {
                                try{
                                    double toSet2= messageHelper.getCoinsFromFormat(input);
                                    double toSet = messageHelper.useDecimals ? Math.floor(toSet2*100) / 100 : Math.floor(toSet2);
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Gambling.setGambleAmount(p, toSet));
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> new GamblingMenu(p), 5);
                                } catch (Exception e) {
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.invalid-number"))));
                                }
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
            for(String line : Gambling.gambleInputMessage)
                lore.add(Utils.chat(line));

            ItemStack paper = RoyaleEconomy.itemConstructor.getItem(Material.PAPER, " ", lore);

            new AnvilGUI.Builder()
                    .onClose(player -> {
                        new GamblingMenu(p);
                    })
                    .onClick((slot, reply) -> {
                        try {
                            if(slot != AnvilGUI.Slot.OUTPUT) {
                                return Collections.emptyList();
                            }

                            Double toSet= messageHelper.getCoinsFromFormat(reply.getText());
                            toSet = messageHelper.useDecimals ? Math.floor(toSet*100) / 100 : Math.floor(toSet);
                            Gambling.setGambleAmount(p, toSet);
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> new GamblingMenu(p), 5);
                        }catch(Exception e){
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.invalid-number")));
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
            for (String line : Gambling.gambleInputMessage)
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            new ChatListener(p, (reply) -> {
                try {
                    Double toSet= messageHelper.getCoinsFromFormat(reply);
                    toSet = messageHelper.useDecimals ? Math.floor(toSet*100) / 100 : Math.floor(toSet);
                    Gambling.setGambleAmount(p, toSet);
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> new GamblingMenu(p), 5);
                }catch(Exception e){
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.invalid-number")));
                }
            });
        }
    }
}
