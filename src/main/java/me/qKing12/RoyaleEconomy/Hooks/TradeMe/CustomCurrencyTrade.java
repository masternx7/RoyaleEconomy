package me.qKing12.RoyaleEconomy.Hooks.TradeMe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import net.Zrips.CMILib.GUI.CMIGui;
import net.Zrips.CMILib.GUI.CMIGuiButton;
import net.Zrips.CMILib.GUI.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/*import com.Zrips.CMIGUI.CMIGui;
import com.Zrips.CMIGUI.CMIGuiButton;
import com.Zrips.CMIGUI.GUIManager.GUIClickType;
import com.Zrips.CMILib.ActionBarTitleMessages;
import com.Zrips.CMILib.ItemManager.CMIMaterial;*/

import me.Zrips.TradeMe.TradeMe;
import me.Zrips.TradeMe.Containers.Amounts;
import me.Zrips.TradeMe.Containers.OfferButtons;
import me.Zrips.TradeMe.Containers.TradeMap;
import me.Zrips.TradeMe.Containers.TradeModeInterface;
import me.Zrips.TradeMe.Containers.TradeOffer;
import me.Zrips.TradeMe.Containers.TradeResults;
import me.Zrips.TradeMe.Containers.TradeSize;
import me.Zrips.TradeMe.Locale.LC;

public class CustomCurrencyTrade implements TradeModeInterface {

    private String at;
    private final Currency currency;

    List<ItemStack> AmountButtons = new ArrayList<ItemStack>();
    ItemStack OfferedTradeButton;
    OfferButtons offerButton = new OfferButtons();
    Amounts amounts = new Amounts(1, 100, 10000, 1000000);
    private final TradeMe plugin;

    public CustomCurrencyTrade(TradeMe plugin, Currency currency) {
        this.plugin = plugin;
        at = "RoyaleEconomy_"+currency.getCurrencyId();
        this.currency=currency;
        OfferedTradeButton = currency.getIcon().clone();
    }

    @Override
    public HashMap<String, Object> getLocale() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("Button.Name", "&fIncrement by "+currency.getColor()+"[amount] "+currency.getCurrencyName());
        map.put("Button.Lore", Arrays.asList(
                "&fLeft click to add",
                "&fRight click to take",
                "&fHold shift to increase 10 times",
                "&fMaximum available: "+currency.getColor()+"[balance] "+currency.getCurrencyName(),
                "&fCurrent "+currency.getCurrencyName()+" offer: "+currency.getColor()+"[offer] [taxes]"));
        map.put("ToggleButton.Name", currency.getColor()+"Toggle to "+currency.getCurrencyName()+" offer");
        map.put("ToggleButton.Lore", Arrays.asList("&fCurent "+currency.getCurrencyName()+" offer: "+currency.getColor()+"[amount] [taxes]"));
        map.put("OfferedButton.Name", "&f[player]'s "+currency.getColor()+currency.getCurrencyName()+" &foffer");
        map.put("OfferedButton.Lore", Arrays.asList("&fCurent "+currency.getCurrencyName()+" offer: "+currency.getColor()+"[amount] [taxes]"));
        map.put("Error", "&e[playername] doesn't have enough "+currency.getColor()+currency.getCurrencyName()+"&f!");
        map.put("Limit", "&eYou dont have enough "+currency.getColor()+currency.getCurrencyName()+"&f! Amount was set to maximum you can trade: "+currency.getColor()+"[amount]");
        map.put("hardLimit", "&e[playername] &fcant have more than "+currency.getColor()+"10,000,000,000,000 "+currency.getCurrencyName()+"&f!");
        map.put("InLoanTarget", "&eYour offered "+currency.getCurrencyName()+" amount is too low to get &6[playername] &eout of loan! offer atleast &6[amount]");
        map.put("InLoanYou", "&6[playername] &eoffered "+currency.getCurrencyName()+" amount is too low to get you out of loan!");
        map.put("Got", "&fYou have received "+currency.getColor()+"[amount] "+currency.getCurrencyName());
        map.put("CantWidraw", "&cCan't widraw "+currency.getCurrencyName()+" from player! ([playername])");
        map.put("ChangedOffer", "&6[playername] &fhas changed their offer to: "+currency.getColor()+"[amount] "+currency.getCurrencyName());
        map.put("ChangedOfferTitle", "&fOffered "+currency.getColor()+"[amount] "+currency.getCurrencyName());

        map.put("log", currency.getColor()+"[amount] "+currency.getCurrencyName());
        return map;
    }

    @Override
    public void setAmounts(Amounts amounts) {
        this.amounts = amounts;
    }

    @Override
    public List<ItemStack> getAmountButtons() {
        AmountButtons.add(currency.getIcon().clone());
        AmountButtons.add(currency.getIcon().clone());
        AmountButtons.add(currency.getIcon().clone());
        AmountButtons.add(currency.getIcon().clone());
        /*AmountButtons.add(XMaterial.GOLD_NUGGET.parseItem());
        AmountButtons.add(XMaterial.GOLD_INGOT.parseItem());
        AmountButtons.add(XMaterial.GOLD_BLOCK.parseItem());
        AmountButtons.add(XMaterial.DIAMOND.parseItem());*/
        return AmountButtons;
    }

    @Override
    public ItemStack getOfferedTradeButton() {
        return OfferedTradeButton;
    }

    @Override
    public OfferButtons getOfferButtons() {
        offerButton.addOfferOff(currency.getIcon().clone());
        offerButton.addOfferOn(currency.getIcon().clone());
        return offerButton;
    }

    @Override
    public void setTrade(TradeOffer trade, int i) {
        String permission1 = currency.getOwnPermission();
        String permission2 = currency.getSendPermission();
        if ((permission1.equalsIgnoreCase("none") || (trade.getP1().hasPermission(permission1) && trade.getP2().hasPermission(permission1))) && (permission2.equalsIgnoreCase("none") || trade.getP1().hasPermission(permission2))) {
            //trade.setP1Money(currency.getAmount(trade.getP1().getUniqueId().toString()));
            //trade.setP2Money(currency.getAmount(trade.getP2().getUniqueId().toString()));
            trade.getButtonList().add(trade.getPosibleButtons().get(i));
        }
    }

    @Override
    public CMIGui Buttons(final TradeOffer trade, CMIGui GuiInv, final int slot) {

        String firstBalance = plugin.getUtil().TrA((long) currency.getAmount(trade.getP1().getUniqueId().toString()));
        String firstOffer = plugin.getUtil().TrA(trade.getOffer(at));

        ItemStack ob = trade.getOffer(at) == 0 ? offerButton.getOfferOff() : offerButton.getOfferOn();

        String taxes = plugin.getUtil().GetTaxesString(at, trade.getOffer(at));

        String mid = "";
        if (trade.getButtonList().size() > 4)
            mid = "\n" + plugin.getMessage("MiddleMouse");
        if (trade.Size == TradeSize.REGULAR)
            GuiInv.updateButton(new CMIGuiButton(slot, plugin.getUtil().makeSlotItem(ob, plugin.getMessage(at, "ToggleButton.Name"),
                    plugin.getMessageListAsString(at, "ToggleButton.Lore",
                            "[amount]", plugin.getUtil().TrA(trade.getOffer(at)),
                            "[taxes]", taxes) + mid)) {
                @Override
                public void click(GUIManager.GUIClickType click) {
                    trade.toogleMode(at, click, slot);
                }
            });

        if (trade.getAction() == at) {

            String lore = plugin.getMessageListAsString(at, "Button.Lore",
                    "[balance]", firstBalance,
                    "[offer]", firstOffer,
                    "[taxes]", taxes);
            for (int i = 45; i < 49; i++) {
                //TradeMe.getInstance().d(AmountButtons.get(i - 45).getType());
                GuiInv.updateButton(new CMIGuiButton(i, plugin.getUtil().makeSlotItem(AmountButtons.get(i - 45),
                        plugin.getMessage(at, "Button.Name", "[amount]", plugin.getUtil().TrA(amounts.getAmount(i - 45))), lore)) {

                    @Override
                    public void click(GUIManager.GUIClickType click) {
                        trade.amountClick(at, click, this.getSlot() - 45, slot);
                    }
                });
            }
        }

        return GuiInv;
    }

    @Override
    public void Change(TradeOffer trade, int slot, GUIManager.GUIClickType button) {
        Double amount = amounts.getAmount(slot);
        double PlayerMoney = currency.getAmount(trade.getP1().getUniqueId().toString());//trade.getP1Money();
        double targetMoney = currency.getAmount(trade.getP2().getUniqueId().toString());//trade.getP2Money();
        double OfferedMoney = trade.getOffer(at);

        if (button.isShiftClick())
            amount *= 10;

        if (button.isLeftClick()) {
            if (plugin.EssPresent && OfferedMoney + amount + targetMoney >= 10000000000000D) {
                amount = 10000000000000D - OfferedMoney - targetMoney;
                trade.getP1().sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "hardLimit", "[playername]", trade.getP2Name()));
            }

            if (OfferedMoney + amount > PlayerMoney) {
                if (PlayerMoney < 0)
                    trade.setOffer(at, 0);
                else
                    trade.setOffer(at, Math.floor(PlayerMoney));
                trade.getP1().sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Limit", "[amount]", plugin.getUtil().TrA(trade.getOffer(at))));
            } else {
                trade.addOffer(at, amount);
            }
        }
        if (button.isRightClick())
            if (OfferedMoney - amount < 0) {
                trade.setOffer(at, 0);
            } else {
                trade.takeFromOffer(at, amount);
            }

        String msg = plugin.getMessage(at, "ChangedOffer", "[playername]", trade.getP1Name(), "[amount]", plugin.getUtil().TrA(trade.getOffer(at)));

        PlayerMessageHandler.messageSend(trade.getP2(), msg);

        TradeMe.getInstance().getUtil().updateInventoryTitle(trade.getP2(), plugin.getMessage(at, "ChangedOfferTitle", "[playername]", trade.getP1().getName(), "[amount]", trade.getOffer(at)), 1000L);

    }

    @Override
    public ItemStack getOfferedItem(TradeOffer trade) {
        if (trade.getOffer(at) > 0) {
            String taxes = plugin.getUtil().GetTaxesString(at, trade.getOffer(at));
            ItemStack item = plugin.getUtil().makeSlotItem(OfferedTradeButton,
                    plugin.getMessage(at, "OfferedButton.Name",
                            "[player]", trade.getP1().getName()),
                    plugin.getMessageListAsString(at, "OfferedButton.Lore",
                            "[amount]", plugin.getUtil().TrA(trade.getOffer(at)),
                            "[taxes]", taxes));
            return item;
        }
        return null;
    }

    @Override
    public boolean isLegit(TradeMap trade) {
        Player p1 = trade.getP1Trade().getP1();
        Player p2 = trade.getP2Trade().getP1();

        if (check(p1, p2, trade.getP1Trade().getOffer(at), trade.getP2Trade().getOffer(at)))
            return true;
        return false;
    }

    private boolean check(Player p1, Player p2, Double offer1, Double offer2) {
        String permission = currency.getOwnPermission();
        String permission2 = currency.getSendPermission();
        if (permission.equalsIgnoreCase("none") || (p1.hasPermission(permission) && p2.hasPermission(permission))) {
            Double balance = currency.getAmount(p1.getUniqueId().toString());

            if (balance < offer1) {
                p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
                p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
                return false;
            }
            else if(offer1>0 && !permission2.equalsIgnoreCase("none") && !p1.hasPermission(permission2)){
                p1.sendMessage(MultiCurrencyHandler.getNoPermissionMessage());
                p2.sendMessage(MultiCurrencyHandler.getNoPermissionMessage());
                return false;
            }

            balance = currency.getAmount(p2.getUniqueId().toString());

            if (balance < offer2) {
                p1.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
                p2.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
                return false;
            }
            else if(offer2> 0 && !permission2.equalsIgnoreCase("none") && !p2.hasPermission(permission2)){
                p1.sendMessage(MultiCurrencyHandler.getNoPermissionMessage());
                p2.sendMessage(MultiCurrencyHandler.getNoPermissionMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean finish(TradeOffer trade) {
        Player target = trade.getP2();
        Player source = trade.getP1();
        if (!check(source, target, trade.getOffer(this.at), 0D))
            return false;
        if (trade.getOffer(this.at) <= 0.0D)
            return true;

        double amount = trade.getOffer(this.at);
        if (amount < 0)
            return false;
        if (source != null) {
            if (currency.getAmount(source.getUniqueId().toString())<amount)
                return false;
            boolean done = currency.removeAmount(source.getUniqueId().toString(), amount);
            if (!done)
                return false;
        }
        double tamount = plugin.getUtil().CheckTaxes(this.at, amount);
        if (tamount < 0) {
            return false;
        }
        trade.setOffer(this.at, tamount);
        if (target != null) {
            currency.addAmount(target.getUniqueId().toString(), tamount);
            target.sendMessage(plugin.getMsg(LC.info_prefix) + plugin.getMessage(at, "Got", "[amount]", plugin.getUtil().TrA(trade.getOffer(at))));
        }
        return true;
    }

    @Override
    public void getResults(TradeOffer trade, TradeResults TR) {
        if (trade.getOffer(at) > 0) {
            double amount = trade.getOffer(at);
            amount = amount - plugin.getUtil().CheckFixedTaxes(at, amount);
            amount = amount - plugin.getUtil().CheckPercentageTaxes(at, amount);
            TR.add(at, amount);
        }
    }

    @Override
    public String Switch(TradeOffer trade, GUIManager.GUIClickType button) {
        return null;
    }
}
