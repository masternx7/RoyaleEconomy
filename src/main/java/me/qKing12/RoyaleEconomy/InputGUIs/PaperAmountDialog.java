package me.qKing12.RoyaleEconomy.InputGUIs;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.qKing12.RoyaleEconomy.API.Events.BankDepositEvent;
import me.qKing12.RoyaleEconomy.API.Events.BankWithdrawEvent;
import me.qKing12.RoyaleEconomy.API.Events.PreBankDepositEvent;
import me.qKing12.RoyaleEconomy.API.Events.PreBankWithdrawEvent;
import me.qKing12.RoyaleEconomy.API.Events.PreSharedBankDepositEvent;
import me.qKing12.RoyaleEconomy.API.Events.PreSharedBankWithdrawEvent;
import me.qKing12.RoyaleEconomy.API.Events.SharedBankDepositEvent;
import me.qKing12.RoyaleEconomy.API.Events.SharedBankWithdrawEvent;
import me.qKing12.RoyaleEconomy.Menus.BankDepositMenu;
import me.qKing12.RoyaleEconomy.Menus.BankMenuCooldown;
import me.qKing12.RoyaleEconomy.Menus.BankWithdrawMenu;
import me.qKing12.RoyaleEconomy.Menus.MainBankMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedBankDepositMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedBankWithdrawMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedMainBankMenu;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.bankUpgradesCfg;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.dataManager;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.messageHelper;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.plugin;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.staticValues;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

/**
 * Config driven Paper dialog used to enter bank amounts.
 *
 * Every visible text, the dialog body, input options and both buttons can be
 * changed from config.yml under input-guis.dialog.
 */
public final class PaperAmountDialog {

    private static final String DEFAULT_DEPOSIT_TITLE = "&8Deposit coins";
    private static final String DEFAULT_WITHDRAW_TITLE = "&8Withdraw coins";
    private static final String DEFAULT_INVALID_NUMBER = "&cPlease enter a valid number.";
    private static final String DEFAULT_DEPOSIT_SUCCESS = "&fYou have deposited &6%amount% coins &fin your bank!";
    private static final String DEFAULT_DEPOSIT_FAIL = "&fYou don't have &6%amount% coins &fin your purse!";
    private static final String DEFAULT_WITHDRAW_SUCCESS = "&fYou have withdrawn &6%amount% coins &ffrom your bank!";
    private static final String DEFAULT_WITHDRAW_FAIL = "&fYou don't have &6%amount% coins &fin your bank!";
    private static final List<String> DEFAULT_DEPOSIT_BODY = Arrays.asList(
            "&7Available: &6%balance%",
            "&7Bank limit: &6%limit%");
    private static final List<String> DEFAULT_WITHDRAW_BODY = Collections.singletonList(
            "&7Available: &6%balance%");

    private PaperAmountDialog() {
    }

    public static boolean open(Player player, String inventoryType, double arg1, double arg2, String bankID) {
        if (player == null || !player.isOnline())
            return false;

        boolean deposit = inventoryType.equals("deposit-coins");
        if (!deposit && !inventoryType.equals("withdraw-coins"))
            return false;

        FileConfiguration config = plugin.getConfig();
        String base = "input-guis.dialog." + (deposit ? "deposit" : "withdraw");
        double balance = arg1;
        double limit = deposit ? arg2 : arg1;

        Component title = color(player, format(config.getString(base + ".title",
                deposit ? DEFAULT_DEPOSIT_TITLE : DEFAULT_WITHDRAW_TITLE), player, balance, limit, 0));

        DialogBase.Builder baseBuilder = DialogBase.builder(title)
                .canCloseWithEscape(config.getBoolean(base + ".can-close-with-escape", true))
                .pause(config.getBoolean(base + ".pause", false))
                .afterAction(parseAfterAction(config.getString(base + ".after-action", "CLOSE")))
                .body(buildBody(player, config, base, deposit, balance, limit))
                .inputs(Collections.singletonList(buildInput(player, config, base, balance, limit)));

        String externalTitle = config.getString(base + ".external-title", "");
        if (externalTitle != null && !externalTitle.trim().isEmpty())
            baseBuilder.externalTitle(color(player, format(externalTitle, player, balance, limit, 0)));

        ActionButton confirm = buildButton(player, config, base + ".confirm-button", "&aConfirm", 100,
                balance, limit, (view, audience) -> submit(audience, view, inventoryType, arg1, arg2, bankID));
        ActionButton cancel = buildButton(player, config, base + ".cancel-button", "&cCancel", 100,
                balance, limit, (view, audience) -> cancel(audience, inventoryType, bankID));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(baseBuilder.build())
                .type(DialogType.confirmation(confirm, cancel)));

        Utils.runOnPlayer(player, () -> {
            if (!player.isOnline())
                return;
            try {
                player.closeInventory();
                ((Audience) player).showDialog(dialog);
            } catch (Throwable throwable) {
                plugin.getLogger().warning("Could not show the Paper amount dialog: " + throwable.getMessage());
            }
        });
        return true;
    }

    /**
     * Direct command input. These methods do not open or reopen any menu.
     */
    public static void depositCommand(Player player, String amountText, String bankID) {
        if (player == null || !player.isOnline())
            return;

        Double purseAmount = dataManager.getMoneyFromFile(player.getUniqueId().toString());
        double purse = purseAmount == null ? 0 : purseAmount;
        if (purse < 0)
            purse = 0;

        processDeposit(player, amountText, purse, getMaximumCoins(player, bankID), bankID, false);
    }

    public static void withdrawCommand(Player player, String amountText, String bankID) {
        if (player == null || !player.isOnline())
            return;

        processWithdraw(player, amountText, getBankBalance(player, bankID), 0, bankID, false);
    }

    private static double getMaximumCoins(Player player, String bankID) {
        int upgrade;
        String path;

        if (bankID == null) {
            upgrade = dataManager.getBankUpgrade(player.getUniqueId().toString());
            path = "bank-upgrades.";
        } else {
            upgrade = dataManager.getSharedBankManager().getSharedBankUpgrade(bankID);
            path = "shared-bank-upgrades.";
        }

        return bankUpgradesCfg.getDouble(path + upgrade + ".maximum-balance");
    }

    private static double getBankBalance(Player player, String bankID) {
        Double balance = bankID == null
                ? dataManager.getBankMoneyFromFile(player.getUniqueId().toString())
                : dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
        return balance == null ? 0 : balance;
    }

    private static List<DialogBody> buildBody(Player player, FileConfiguration config, String base,
                                              boolean deposit, double balance, double limit) {
        List<String> lines = config.contains(base + ".body")
                ? config.getStringList(base + ".body")
                : (deposit ? DEFAULT_DEPOSIT_BODY : DEFAULT_WITHDRAW_BODY);

        List<DialogBody> body = new ArrayList<>();
        for (String line : lines)
            body.add(DialogBody.plainMessage(color(player, format(line, player, balance, limit, 0))));
        return body;
    }

    private static DialogInput buildInput(Player player, FileConfiguration config, String base,
                                          double balance, double limit) {
        String label = config.getString(base + ".input.label", "&eAmount");
        int width = clamp(config.getInt(base + ".input.width", 300), 1, 1024, 300);
        int maxLength = clamp(config.getInt(base + ".input.max-length", 32), 1, 1024, 32);
        boolean labelVisible = config.getBoolean(base + ".input.label-visible", true);
        String initial = config.getString(base + ".input.initial", "");

        TextDialogInput.Builder input = DialogInput
                .text("amount", color(player, format(label, player, balance, limit, 0)))
                .width(width)
                .labelVisible(labelVisible)
                .maxLength(maxLength);

        if (initial != null && !initial.isEmpty())
            input.initial(format(initial, player, balance, limit, 0));

        return input.build();
    }

    private static ActionButton buildButton(Player player, FileConfiguration config, String path,
                                            String defaultLabel, int defaultWidth,
                                            double balance, double limit,
                                            DialogActionCallback callback) {
        String label = format(config.getString(path + ".label", defaultLabel), player, balance, limit, 0);
        String tooltip = format(config.getString(path + ".tooltip", ""), player, balance, limit, 0);
        int width = clamp(config.getInt(path + ".width", defaultWidth), 1, 1024, defaultWidth);
        Component tooltipComponent = tooltip == null || tooltip.trim().isEmpty()
                ? null
                : color(player, tooltip);

        return ActionButton.create(color(player, label), tooltipComponent, width,
                DialogAction.customClick(callback, ClickCallback.Options.builder().uses(1).build()));
    }

    private static void submit(Audience audience, DialogResponseView view, String inventoryType,
                               double arg1, double arg2, String bankID) {
        if (!(audience instanceof Player))
            return;

        Player player = (Player) audience;
        String text = view == null ? null : view.getText("amount");
        Utils.runOnPlayer(player, () -> {
            if (!player.isOnline())
                return;
            ((Audience) player).closeDialog();
            if (inventoryType.equals("deposit-coins"))
                processDeposit(player, text, arg1, arg2, bankID, true);
            else
                processWithdraw(player, text, arg1, arg2, bankID, true);
        });
    }

    private static void cancel(Audience audience, String inventoryType, String bankID) {
        if (!(audience instanceof Player))
            return;

        Player player = (Player) audience;
        Utils.runOnPlayer(player, () -> {
            if (!player.isOnline())
                return;
            ((Audience) player).closeDialog();
            reopen(player, inventoryType, false, bankID);
        });
    }

    private static void processDeposit(Player player, String reply, double purse, double maximumCoins, String bankID, boolean reopenMenus) {
        String base = "input-guis.dialog.deposit";

        if (reply == null || reply.trim().isEmpty()) {
            send(player, base, "invalid-number", DEFAULT_INVALID_NUMBER, purse, maximumCoins, 0);
            if (reopenMenus)
                reopen(player, "deposit-coins", false, bankID);
            return;
        }

        boolean mainMenu = false;
        boolean success = false;
        double amount = 0;

        try {
            amount = Math.abs(messageHelper.getCoinsFromFormat(reply));
            amount = messageHelper.useDecimals ? Math.floor(amount * 100) / 100 : Math.floor(amount);

            if (amount <= 0) {
                send(player, base, "invalid-number", DEFAULT_INVALID_NUMBER, purse, maximumCoins, amount);
            } else if (amount > purse) {
                send(player, base, "fail-message", DEFAULT_DEPOSIT_FAIL, purse, maximumCoins, amount);
            } else {
                if (bankID != null) {
                    PreSharedBankDepositEvent event = new PreSharedBankDepositEvent(bankID, player, amount, false);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        if (reopenMenus)
                            reopen(player, "deposit-coins", false, bankID);
                        return;
                    }

                    Double sharedBalance = dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                    double bankBalance = sharedBalance == null ? 0 : sharedBalance;
                    if (bankBalance + amount > maximumCoins) {
                        amount = maximumCoins - bankBalance;
                        mainMenu = true;
                    }
                    if (amount <= 0) {
                        if (reopenMenus)
                            reopen(player, "deposit-coins", true, bankID);
                        else
                            PlayerMessageHandler.messageSend(player, staticValues.bankFull);
                        return;
                    }

                    if (dataManager.removeMoneyFromFile(player.getUniqueId().toString(), amount)) {
                        dataManager.getSharedBankManager().addSharedBankMoneyToFile(bankID, amount);
                        if (plugin.bankLogger != null)
                            plugin.bankLogger.getLogger().info("[DEPOSIT SHARED VIA DIALOG] " + player.getName() + " (" + player.getUniqueId() + ")" + " deposited " + amount + " coins.");
                        final double finalAmount = amount;
                        Utils.runOnPlayer(player, () -> Bukkit.getPluginManager().callEvent(new SharedBankDepositEvent(player, finalAmount)));
                        dataManager.getSharedBankManager().addSharedTransactionLog(bankID, player.getName(), "&a+", amount);
                        success = true;
                    }
                } else {
                    PreBankDepositEvent event = new PreBankDepositEvent(player, amount, false);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        if (reopenMenus)
                            reopen(player, "deposit-coins", false, bankID);
                        return;
                    }

                    Double personalBalance = dataManager.getBankMoneyFromFile(player.getUniqueId().toString());
                    double bankBalance = personalBalance == null ? 0 : personalBalance;
                    if (bankBalance + amount > maximumCoins) {
                        amount = maximumCoins - bankBalance;
                        mainMenu = true;
                    }
                    if (amount <= 0) {
                        if (reopenMenus)
                            reopen(player, "deposit-coins", true, bankID);
                        else
                            PlayerMessageHandler.messageSend(player, staticValues.bankFull);
                        return;
                    }

                    if (dataManager.removeMoneyFromFile(player.getUniqueId().toString(), amount)) {
                        dataManager.addBankMoneyToFile(player.getUniqueId().toString(), amount);
                        if (plugin.bankLogger != null)
                            plugin.bankLogger.getLogger().info("[DEPOSIT VIA DIALOG] " + player.getName() + " (" + player.getUniqueId() + ")" + " deposited " + amount + " coins.");
                        final double finalAmount = amount;
                        Utils.runOnPlayer(player, () -> Bukkit.getPluginManager().callEvent(new BankDepositEvent(player, finalAmount)));
                        dataManager.addTransactionLog(player.getUniqueId().toString(), player.getName(), "&a+", amount);
                        success = true;
                    }
                }

                if (success) {
                    Utils.playSound(player, "menus.bank-deposit");
                    send(player, base, "success-message", DEFAULT_DEPOSIT_SUCCESS, purse, maximumCoins, amount);
                } else {
                    send(player, base, "fail-message", DEFAULT_DEPOSIT_FAIL, purse, maximumCoins, amount);
                }
            }
        } catch (Exception exception) {
            send(player, base, "invalid-number", DEFAULT_INVALID_NUMBER, purse, maximumCoins, amount);
        }

        if (reopenMenus)
            reopen(player, "deposit-coins", mainMenu, bankID);
    }

    private static void processWithdraw(Player player, String reply, double bankBalance, double withdrawAsBag, String bankID, boolean reopenMenus) {
        String base = "input-guis.dialog.withdraw";

        if (reply == null || reply.trim().isEmpty()) {
            send(player, base, "invalid-number", DEFAULT_INVALID_NUMBER, bankBalance, bankBalance, 0);
            if (reopenMenus)
                reopen(player, "withdraw-coins", false, bankID);
            return;
        }

        boolean mainMenu = false;

        try {
            double amount = Math.abs(messageHelper.getCoinsFromFormat(reply));
            amount = messageHelper.useDecimals ? Math.floor(amount * 100) / 100 : Math.floor(amount);

            if (amount <= 0) {
                send(player, base, "invalid-number", DEFAULT_INVALID_NUMBER, bankBalance, bankBalance, amount);
            } else if (amount > bankBalance) {
                send(player, base, "fail-message", DEFAULT_WITHDRAW_FAIL, bankBalance, bankBalance, amount);
            } else {
                if (amount == bankBalance)
                    mainMenu = true;

                if (bankID != null) {
                    PreSharedBankWithdrawEvent event = new PreSharedBankWithdrawEvent(bankID, player, amount, false);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        if (reopenMenus)
                            reopen(player, "withdraw-coins", false, bankID);
                        return;
                    }

                    if (withdrawAsBag == 1 && player.getInventory().firstEmpty() != -1) {
                        SharedBankWithdrawMenu.tryBagCreator(amount, player, bankID);
                    } else {
                        if (dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, amount))
                            dataManager.addMoneyToFile(player.getUniqueId().toString(), amount);
                        if (plugin.bankLogger != null)
                            plugin.bankLogger.getLogger().info("[WITHDRAW SHARED VIA DIALOG] " + player.getName() + " (" + player.getUniqueId() + ")" + " withdrew " + amount + " coins.");
                    }
                    final double finalAmount = amount;
                    Utils.runOnPlayer(player, () -> Bukkit.getPluginManager().callEvent(new SharedBankWithdrawEvent(player, finalAmount)));
                    dataManager.getSharedBankManager().addSharedTransactionLog(bankID, player.getName(), "&c-", amount);
                } else {
                    PreBankWithdrawEvent event = new PreBankWithdrawEvent(player, amount, false);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        if (reopenMenus)
                            reopen(player, "withdraw-coins", false, bankID);
                        return;
                    }

                    if (withdrawAsBag == 1 && player.getInventory().firstEmpty() != -1) {
                        BankWithdrawMenu.tryBagCreator(amount, player);
                    } else {
                        dataManager.addMoneyToFile(player.getUniqueId().toString(), amount);
                        dataManager.removeBankMoneyToFile(player.getUniqueId().toString(), amount);
                        if (plugin.bankLogger != null)
                            plugin.bankLogger.getLogger().info("[WITHDRAW VIA DIALOG] " + player.getName() + " (" + player.getUniqueId() + ")" + " withdrew " + amount + " coins.");
                    }
                    final double finalAmount = amount;
                    Utils.runOnPlayer(player, () -> Bukkit.getPluginManager().callEvent(new BankWithdrawEvent(player, finalAmount)));
                    dataManager.addTransactionLog(player.getUniqueId().toString(), player.getName(), "&c-", amount);
                }

                Utils.playSound(player, "menus.bank-withdraw");
                send(player, base, "success-message", DEFAULT_WITHDRAW_SUCCESS, bankBalance, bankBalance, amount);
            }
        } catch (Exception exception) {
            send(player, base, "invalid-number", DEFAULT_INVALID_NUMBER, bankBalance, bankBalance, 0);
        }

        if (reopenMenus)
            reopen(player, "withdraw-coins", mainMenu, bankID);
    }

    private static void send(Player player, String base, String key, String defaultValue,
                             double balance, double limit, double amount) {
        String text = plugin.getConfig().getString(base + "." + key, defaultValue);
        if (text == null || text.isEmpty())
            return;
        PlayerMessageHandler.messageSend(player, utilsAPI.chat(player,
                format(text, player, balance, limit, amount)));
    }

    private static void reopen(Player player, String inventoryType, boolean mainMenu, String bankID) {
        plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
            if (!player.isOnline())
                return;

            BankMenuCooldown.getInstance().removeCooldown(player.getUniqueId());

            if (mainMenu) {
                if (bankID == null)
                    new MainBankMenu(player);
                else
                    new SharedMainBankMenu(player);
                return;
            }

            if (inventoryType.equals("deposit-coins")) {
                if (bankID == null)
                    new BankDepositMenu(player);
                else
                    new SharedBankDepositMenu(player);
            } else {
                if (bankID == null)
                    new BankWithdrawMenu(player);
                else
                    new SharedBankWithdrawMenu(player);
            }
        }, 5);
    }

    private static String format(String text, Player player, double balance, double limit, double amount) {
        if (text == null)
            return "";
        return text.replace("%player%", player.getName())
                .replace("%balance%", messageHelper.numberFormat(balance))
                .replace("%limit%", messageHelper.numberFormat(limit))
                .replace("%amount%", messageHelper.numberFormat(amount));
    }

    private static DialogBase.DialogAfterAction parseAfterAction(String value) {
        if (value == null)
            return DialogBase.DialogAfterAction.CLOSE;
        try {
            return DialogBase.DialogAfterAction.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return DialogBase.DialogAfterAction.CLOSE;
        }
    }

    private static int clamp(int value, int minimum, int maximum, int fallback) {
        return value < minimum || value > maximum ? fallback : value;
    }

    private static Component color(Player player, String text) {
        return LegacyComponentSerializer.legacySection().deserialize(utilsAPI.chat(player, text));
    }
}
