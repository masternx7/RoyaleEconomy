package me.qKing12.RoyaleEconomy.Shops.SellAll;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.CommandExecutor;

public class SellAllManager {
   static String globalPermission;
   static String noGlobalPermission;
   static String noLocalPermission;
   static String needToClickChest;
   static String noItemsToSell;
   static String sellMessage;
   static boolean sellAllEnchanted;
   static boolean useWands;
   static String cooldownMessage;
   static long commandCooldown;
   static String cooldownBypassPerm;

   public SellAllManager() {
      useWands = RoyaleEconomy.shopsCfg.getBoolean("sell-all.use-sell-wands");
      sellAllEnchanted = RoyaleEconomy.shopsCfg.getBoolean("sell-all.sell-enchanted-items");
      if (RoyaleEconomy.shopsCfg.getBoolean("sell-all.use-sell-command")) {
         if (useWands) {
            SellWand.loadWands();
            new SellWandEvents();
         }

         DynamicCommandsSetup.getCommandSettings().put("sell-all", new SellAllCommand());
         commandCooldown = (long)(RoyaleEconomy.shopsCfg.getInt("sell-all.sell-all-command-cooldown") * 1000);
      } else {
         if (!useWands) {
            return;
         }

         SellWand.loadWands();
         new SellWandEvents();
         DynamicCommandsSetup.getCommandSettings().put("sell-all", new SellAllCommandAdminOnly());
      }

      cooldownBypassPerm = RoyaleEconomy.shopsCfg.getString("sell-all.cooldown-bypass-permission");
      cooldownMessage = Utils.chat(RoyaleEconomy.shopsCfg.getString("sell-all.sell-all-cooldown-message"));
      sellMessage = Utils.chat(RoyaleEconomy.shopsCfg.getString("sell-all.sell-message"));
      noItemsToSell = Utils.chat(RoyaleEconomy.shopsCfg.getString("sell-all.no-items-to-sell"));
      globalPermission = Utils.chat(RoyaleEconomy.shopsCfg.getString("sell-all.global-sell-permission"));
      noGlobalPermission = Utils.chat(RoyaleEconomy.shopsCfg.getString("sell-all.no-permission-to-sell-global"));
      noLocalPermission = Utils.chat(RoyaleEconomy.shopsCfg.getString("sell-all.no-permission-to-sell-shop"));
      needToClickChest = Utils.chat(RoyaleEconomy.shopsCfg.getString("sell-all.need-to-click-chest"));
   }
}