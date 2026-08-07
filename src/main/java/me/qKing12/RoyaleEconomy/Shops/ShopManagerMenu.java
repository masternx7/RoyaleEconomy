package me.qKing12.RoyaleEconomy.Shops;

import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public class ShopManagerMenu {
    private Inventory inventory;
    private final int page;

    // Constants for inventory layout
    private static final int ITEMS_PER_PAGE = 45; // 5 rows of 9 slots for shops

    public ShopManagerMenu(Player p) {
        // Default to page 0 if no page is specified
        this(p, 0);
    }

    public ShopManagerMenu(Player p, Integer page) {
        // Ensure page is a valid number
        this.page = (page == null || page < 0) ? 0 : page;

        // Calculate total pages
        int maxPages = (int) Math.ceil((double) ShopsLoad.shops.size() / ITEMS_PER_PAGE);
        if (maxPages == 0) {
            maxPages = 1;
        }

        // Create the inventory with a dynamic title showing the page number
        inventory = Bukkit.createInventory(null, 54, Utils.chat("&8Manage Shops - Page " + (this.page + 1)));

        // --- Populate Shops for the Current Page ---

        // Calculate the starting and ending index for the shops list based on the current page
        int startIndex = this.page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, ShopsLoad.shops.size());

        for (int i = startIndex; i < endIndex; i++) {
            Shop shop = ShopsLoad.shops.get(i);
            ArrayList<String> lore = new ArrayList<>();
            lore.add(Utils.chat("&8This is a shop!"));
            lore.add("");
            lore.add(Utils.chat("&fYou can edit the name"));
            lore.add(Utils.chat("&fand add/remove items"));
            lore.add(Utils.chat("&fby clicking this item."));
            lore.add("");
            lore.add(Utils.chat("&fTitle: " + shop.getTitle()));
            lore.add(Utils.chat("&fPermission: &a" + shop.getPermission()));
            lore.add("");
            lore.add(Utils.chat("&bLeft Click &fto edit shop."));
            lore.add(Utils.chat("&bRight Click &fto change title"));
            lore.add(Utils.chat("&fof the shop."));
            lore.add(Utils.chat("&bMiddle Click &fto change permission"));
            ItemStack display = RoyaleEconomy.itemConstructor.getItem(Material.BOOK, Utils.chat("&a" + shop.getShopName()), lore);

            // Place the item in the correct slot (0-44)
            inventory.setItem(i - startIndex, display);
        }


        // --- Add Navigation and Control Buttons to the bottom row ---

        // "Create Shop" button in the first slot of the last row
        ArrayList<String> createLore = new ArrayList<>();
        createLore.add(Utils.chat("&fClick to create a"));
        createLore.add(Utils.chat("&fnew shop!"));
        ItemStack createShop = RoyaleEconomy.itemConstructor.getItem("351:8", Utils.chat("&aCreate Shop"), createLore);
        inventory.setItem(45, createShop);

        // "Previous Page" button - only shown if not on the first page
        if (this.page > 0) {
            ItemStack prevPage = RoyaleEconomy.itemConstructor.getItem(Material.ARROW, Utils.chat("&aPrevious Page"), new ArrayList<>());
            inventory.setItem(48, prevPage);
        }

        // Page indicator item
        ItemStack pageInfo = RoyaleEconomy.itemConstructor.getItem(Material.PAPER, Utils.chat("&6Page " + (this.page + 1) + "/" + maxPages), new ArrayList<>(Collections.singletonList(Utils.chat("&7You are on page " + (this.page + 1)))));
        inventory.setItem(49, pageInfo);

        // "Next Page" button - only shown if there are more pages
        if (endIndex < ShopsLoad.shops.size()) {
            ItemStack nextPage = RoyaleEconomy.itemConstructor.getItem(Material.ARROW, Utils.chat("&aNext Page"), new ArrayList<>());
            inventory.setItem(50, nextPage);
        }


        // Open the inventory for the player and register the event listener
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));
        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            // Basic checks to prevent errors
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;

            // Only handle clicks inside our custom GUI, not the player's inventory
            if (!inventory.equals(e.getClickedInventory())) {
                return;
            }

            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            int slot = e.getSlot();
            ItemStack clickedItem = e.getCurrentItem();

            // --- Handle Clicks on Control Buttons (Bottom Row) ---
            if (slot >= ITEMS_PER_PAGE) {
                // Using display name to identify buttons is more reliable than material type
                String displayName = clickedItem.getItemMeta().getDisplayName();
                if (displayName.equals(Utils.chat("&aCreate Shop"))) {
                    p.closeInventory();
                    new Shop(null, p);
                } else if (displayName.equals(Utils.chat("&aNext Page"))) {
                    new ShopManagerMenu(p, page + 1);
                } else if (displayName.equals(Utils.chat("&aPrevious Page"))) {
                    new ShopManagerMenu(p, page - 1);
                }
                return;
            }

            // --- Handle Clicks on Shop Items ---
            if (clickedItem.getType().equals(Material.BOOK)) {
                // Calculate the actual index in the ShopsLoad.shops list
                int shopIndex = page * ITEMS_PER_PAGE + slot;

                // Safety check in case the shop list changed
                if (shopIndex >= ShopsLoad.shops.size()) {
                    p.closeInventory();
                    p.sendMessage(Utils.chat("&cAn error occurred. That shop could not be found."));
                    return;
                }

                Shop targetShop = ShopsLoad.shops.get(shopIndex);

                if (e.getClick().equals(ClickType.LEFT)) {
                    // Open the editor for the selected shop
                    new ShopEditorMenu(p, targetShop, 0);
                } else if (e.getClick().equals(ClickType.RIGHT)) {
                    // Prompt to change the shop title
                    PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the new title for the &ashop&f!"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("You can use color codes (e.g., &c, &6, &l)."));
                    p.closeInventory();
                    new ChatListener(p, (reply) -> {
                        targetShop.setTitle(reply);
                        PlayerMessageHandler.messageSend(p, Utils.chat("&aTitle successfully updated!"));
                        // Reopen the menu to show the updated info
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick(task -> new ShopManagerMenu(p, page));
                    }, false);
                } else if (e.getClick().equals(ClickType.MIDDLE)) {
                    // Prompt to change the shop permission
                    PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the permission to open this &ashop&f!"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("To remove the permission, type &anone&f."));
                    p.closeInventory();
                    new ChatListener(p, (reply) -> {
                        // A more permissive regex for permissions
                        if (reply.equalsIgnoreCase("none") || reply.matches("[A-Za-z0-9._-]+")) {
                            targetShop.setPermission(reply);
                            PlayerMessageHandler.messageSend(p, Utils.chat("&aPermission successfully updated!"));
                            // Reopen the menu to show the updated info
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick(task -> new ShopManagerMenu(p, page));
                        } else {
                            PlayerMessageHandler.messageSend(p, Utils.chat("&cThat is not a valid permission format."));
                        }
                    }, false);
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            // Unregister the listener when the inventory is closed to prevent memory leaks
            if (e.getInventory().equals(inventory)) {
                HandlerList.unregisterAll(this);
            }
        }
    }
}
