package me.qKing12.RoyaleEconomy.Commands.dynamic;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class DynamicCommand extends Command implements PluginIdentifiableCommand {

    private final CommandExecutor executor;

    public DynamicCommand(String name, String permission, String permissionMessage, List<String> aliases, CommandExecutor executor) {
        super(name);
        this.executor = executor;
        super.setAliases(aliases);
        super.setPermission(permission);
        super.setPermissionMessage(Utils.chat(permissionMessage));
    }

    public DynamicCommand(String name, List<String> aliases, CommandExecutor executor) {
        super(name);
        this.executor = executor;
        super.setAliases(aliases);
    }

    @Override
    public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(getPermissionMessage());
            return false;
        }
        return executor.onCommand(sender, this, commandLabel, args);
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (executor instanceof TabCompleter) {
            List<String> completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
            if (completions != null) {
                return completions;
            }
        }
        return super.tabComplete(sender, alias, args);
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return RoyaleEconomy.plugin;
    }
}
