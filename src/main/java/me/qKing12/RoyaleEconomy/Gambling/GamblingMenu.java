package me.qKing12.RoyaleEconomy.Gambling;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.qKing12.RoyaleEconomy.API.Events.GambleFinishEvent;
import me.qKing12.RoyaleEconomy.CustomMenuItems.CustomItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.customItemsHandler;

public class GamblingMenu {
    private Inventory inventory;
    private Player player;
    private ClickListener clickListener = new ClickListener();
    private double coins;
    private double gambleAmount;

    private WrappedTask slot1Shuffle;
    private WrappedTask slot2Shuffle;
    private WrappedTask slot3Shuffle;

    private Iterator<ItemStack> slot1It;
    private Iterator<ItemStack> slot2It;
    private Iterator<ItemStack> slot3It;

    private WrappedTask innerRotation;
    final private int[] innerSlots = {11, 12, 13, 14, 15, 24, 33, 32, 31, 30, 29, 20};

    private void innerRotationStart() {
        final Random random = new Random();
        int size = Gambling.animationColors.size();
        innerRotation = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
            ItemStack toUseItem = Gambling.animationColors.get(random.nextInt(size));

            for (int i : innerSlots) {
                inventory.setItem(i, toUseItem);
            }

            player.updateInventory();
        }, 0, 10);
    }

    public void innerRotationStop(boolean withDelay) {
        if (withDelay) {
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                innerRotation.cancel();
                innerRotation = null;
            }, 80);
        } else {
            innerRotation.cancel();
            innerRotation = null;
        }
    }

    private void startShuffle() {
        ArrayList<ItemStack> slot1 = (ArrayList<ItemStack>) Gambling.shuffleItems.clone();
        ArrayList<ItemStack> slot2 = (ArrayList<ItemStack>) Gambling.shuffleItems.clone();
        ArrayList<ItemStack> slot3 = (ArrayList<ItemStack>) Gambling.shuffleItems.clone();
        Collections.shuffle(slot1);
        Collections.shuffle(slot2);
        Collections.shuffle(slot3);
        slot1It = slot1.iterator();
        slot2It = slot2.iterator();
        slot3It = slot3.iterator();

        slot1Shuffle = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
            if (slot1It.hasNext()) {
                inventory.setItem(21, slot1It.next());
            } else
                slot1It = slot1.iterator();
            player.updateInventory();
        }, 0, 6);

        slot2Shuffle = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
            if (slot2It.hasNext()) {
                inventory.setItem(22, slot2It.next());
            } else
                slot2It = slot2.iterator();
            player.updateInventory();
        }, 2, 6);

        if (Gambling.whileGambleSound.equals("none")) {
            slot3Shuffle = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
                if (slot3It.hasNext()) {
                    inventory.setItem(23, slot3It.next());
                } else
                    slot3It = slot3.iterator();
                player.updateInventory();
            }, 4, 6);
        } else {
            slot3Shuffle = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimer(() -> {
                if (slot3It.hasNext()) {
                    inventory.setItem(23, slot3It.next());
                } else
                    slot3It = slot3.iterator();
                player.updateInventory();
                Gambling.playSound(player, Gambling.whileGambleSound);
            }, 4, 6);
        }
    }

    public void stopShuffle(ItemStack item1, ItemStack item2, ItemStack item3) {
        slot1Shuffle.cancel();
        slot1Shuffle = null;
        inventory.setItem(21, item1);
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
            slot2Shuffle.cancel();
            slot2Shuffle = null;
            inventory.setItem(22, item2);
        }, 40);
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
            slot3Shuffle.cancel();
            slot3Shuffle = null;
            inventory.setItem(23, item3);
        }, 80);
    }

    public GamblingMenu(Player player) {
        this.player = player;
        this.coins = RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString());
        inventory = Bukkit.createInventory(player, 54, Gambling.menuName);

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, Gambling.mainBackground);
        }

        for (int i : innerSlots) {
            inventory.setItem(i, Gambling.smallBackground);
        }

        ItemStack toWorkWith = RoyaleEconomy.itemConstructor.getItem("skull:" + player.getName(), Gambling.playerHeadName, Gambling.playerHeadLore);
        inventory.setItem(4, toWorkWith);

        inventory.setItem(21, Gambling.defaultItem);
        inventory.setItem(22, Gambling.defaultItem);
        inventory.setItem(23, Gambling.defaultItem);

        toWorkWith = Gambling.purseInfoMaterial.clone();
        ItemMeta meta = toWorkWith.getItemMeta();
        meta.setDisplayName(Gambling.purseInfoName.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
        ArrayList<String> lore = new ArrayList<>();
        for (String line : Gambling.purseInfoLore)
            lore.add(line.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
        meta.setLore(lore);
        toWorkWith.setItemMeta(meta);
        inventory.setItem(37, toWorkWith);

        gambleAmount = Gambling.getGambleAmount(player);
        toWorkWith = Gambling.pullItemMaterial.clone();
        meta = toWorkWith.getItemMeta();
        meta.setDisplayName(Gambling.pullItemName.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)));
        lore = new ArrayList<>();
        for (String line : Gambling.pullItemLore)
            lore.add(line.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)));
        meta.setLore(lore);
        toWorkWith.setItemMeta(meta);
        inventory.setItem(40, toWorkWith);

        toWorkWith = Gambling.editGambleItemMaterial.clone();
        meta = toWorkWith.getItemMeta();
        meta.setDisplayName(Gambling.editGambleName.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)));
        lore = new ArrayList<>();
        for (String line : Gambling.editGambleLore)
            lore.add(line.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)));
        meta.setLore(lore);
        toWorkWith.setItemMeta(meta);
        inventory.setItem(43, toWorkWith);

        inventory.setItem(49, Gambling.getGambleLog(player));

        HashMap<Integer, CustomItem> customItems = customItemsHandler.getItems("gambling-menu");
        if (customItems != null) {
            for (Map.Entry<Integer, CustomItem> item : customItems.entrySet()) {
                //RoyaleEconomy.itemConstructor.applyPlaceholdersAndGetClone(player, item.getValue().getItem(player));
                inventory.setItem(item.getKey(), item.getValue().getItem(player));
            }
        }

        player.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(clickListener, RoyaleEconomy.plugin);
    }

    public class ClickListener implements Listener {
        private WrappedTask task1;
        private WrappedTask task2;
        private GamblingChance chance;
        private double finalAmount;

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if (e.getSlot() < 0)
                return;

            if(customItemsHandler.tryClick("gambling-menu", e.getSlot(), (Player)e.getWhoClicked())){
                return;
            }

            if (e.getSlot() == 43) {
                if (slot3Shuffle == null)
                    new GamblingInputMethod(player);
            } else if (e.getSlot() == 40) {
                if (slot3Shuffle == null) {
                    if (coins < gambleAmount) {
                        player.sendMessage(Gambling.notEnoughCoins);
                        return;
                    }
                    chance = Gambling.getRandomChance();
                    finalAmount = chance.getCoinsFromPercent(gambleAmount);
                    RoyaleEconomy.dataManager.removeMoneyFromFile(player.getUniqueId().toString(), gambleAmount);
                    innerRotationStart();
                    startShuffle();
                    ItemStack toReput = inventory.getItem(40).clone();
                    inventory.setItem(40, Gambling.pullItemWaiting);

                    task1 = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                        stopShuffle(chance.item1, chance.item2, chance.item3);
                        task2 = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> {
                            innerRotationStop(false);
                            if (finalAmount != gambleAmount) {
                                Gambling.addGambleLog(player, chance.losing, gambleAmount, finalAmount, chance.percent);
                                inventory.setItem(49, Gambling.getGambleLog(player));
                                if (chance.losing) {
                                    double toRemove = gambleAmount - finalAmount;
                                    RoyaleEconomy.dataManager.addMoneyToFile(player.getUniqueId().toString(), finalAmount);
                                    coins -= toRemove;
                                    if (chance.message != null)
                                        player.sendMessage(chance.message.replace("%player%", player.getName()).replace("%gamble-amount%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)).replace("%return-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%percent%", Double.toString(chance.percent).replace(".0", "")));
                                    else
                                        player.sendMessage(Gambling.losingMessage.replace("%gamble-amount%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)).replace("%return-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%percent%", Double.toString(chance.percent).replace(".0", "")));
                                    if (chance.broadcastMessage != null)
                                        Bukkit.broadcastMessage(chance.broadcastMessage.replace("%player%", player.getName()).replace("%gamble-amount%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)).replace("%return-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%percent%", Double.toString(chance.percent).replace(".0", "")));
                                    for (int i : innerSlots) {
                                        inventory.setItem(i, Gambling.losingBackground);
                                    }
                                    Bukkit.getPluginManager().callEvent(new GambleFinishEvent(player, toRemove, false));
                                    Gambling.playSound(player, Gambling.losingSound);
                                } else {
                                    double toAdd = finalAmount - gambleAmount;
                                    RoyaleEconomy.dataManager.addMoneyToFile(player.getUniqueId().toString(), finalAmount);
                                    coins += toAdd;
                                    if (chance.message != null)
                                        player.sendMessage(chance.message.replace("%player%", player.getName()).replace("%gamble-amount%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)).replace("%return-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%percent%", Double.toString(chance.percent).replace(".0", "")));
                                    else
                                        player.sendMessage(Gambling.winningMessage.replace("%player%", player.getName()).replace("%gamble-amount%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)).replace("%return-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%percent%", Double.toString(chance.percent).replace(".0", "")));
                                    if (chance.broadcastMessage != null)
                                        Bukkit.broadcastMessage(chance.broadcastMessage.replace("%player%", player.getName()).replace("%gamble-amount%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)).replace("%return-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%percent%", Double.toString(chance.percent).replace(".0", "")));
                                    for (int i : innerSlots) {
                                        inventory.setItem(i, Gambling.winningBackground);
                                    }
                                    Bukkit.getPluginManager().callEvent(new GambleFinishEvent(player, toAdd, true));
                                    Gambling.playSound(player, Gambling.winningSound);
                                }

                                ItemStack toWorkWith = Gambling.purseInfoMaterial.clone();
                                ItemMeta meta = toWorkWith.getItemMeta();
                                meta.setDisplayName(Gambling.purseInfoName.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                                ArrayList<String> lore = new ArrayList<>();
                                for (String line : Gambling.purseInfoLore)
                                    lore.add(line.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                                meta.setLore(lore);
                                toWorkWith.setItemMeta(meta);
                                inventory.setItem(37, toWorkWith);
                            }
                            inventory.setItem(40, toReput);
                        }, 80);
                    }, 100);
                } else {
                    player.sendMessage(Gambling.noRushMessage);
                }
            }
        }

        /*@EventHandler
        public void onQuit(PlayerQuitEvent e){
            if(e.getPlayer().equals(player)){
                if (task1 != null)
                    task1.cancel();
                if (task2 != null)
                    task2.cancel();
                HandlerList.unregisterAll(this);

                if(chance.losing){
                    RoyaleEconomy.plugin.getLogger().info("[Gamble Cheat Attempt] Player "+e.getPlayer().getName()+" has left mid gamble.");
                    double toRemove=gambleAmount-finalAmount;
                    RoyaleEconomy.dataManager.removeMoneyFromFile(player.getUniqueId().toString(), toRemove);
                    coins -= toRemove;

                    if(chance.broadcastMessage!=null)
                        Bukkit.broadcastMessage(chance.broadcastMessage.replace("%player%", player.getName()).replace("%gamble-amount%", RoyaleEconomy.messageHelper.numberFormat(gambleAmount)).replace("%return-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%percent%", Double.toString(chance.percent).replace(".0", "")));

                    Bukkit.getPluginManager().callEvent(new GambleFinishEvent(player, toRemove, false));
                }
            }
        }*/

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (event.getInventory().equals(inventory)) {
                try {
                    if (slot3Shuffle != null) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> player.openInventory(inventory));
                        return;
                        /*(slot3Shuffle.cancel();
                        if (slot2Shuffle != null)
                            slot2Shuffle.cancel();
                        if (slot1Shuffle != null)
                            slot1Shuffle.cancel();
                        innerRotation.cancel();*/
                    }
                    if (task1 != null)
                        task1.cancel();
                    if (task2 != null)
                        task2.cancel();
                } catch (Exception x) {
                    x.printStackTrace();
                }
                HandlerList.unregisterAll(this);

                if (!Gambling.closingCommands.isEmpty())
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                        for (String line : Gambling.closingCommands)
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.replace("%player%", player.getName()));
                    });
            }
        }
    }
}
