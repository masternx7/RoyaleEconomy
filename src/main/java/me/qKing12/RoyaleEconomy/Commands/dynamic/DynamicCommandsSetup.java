package me.qKing12.RoyaleEconomy.Commands.dynamic;

import me.qKing12.RoyaleEconomy.Commands.*;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.noEconomy;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.plugin;

public class DynamicCommandsSetup {
    private final static Map<String, TabExecutor> commandSettings = new HashMap<>();
    private final static List<String> loadedCommands = new ArrayList<>();

    public static Map<String, TabExecutor> getCommandSettings() {
        return commandSettings;
    }

    public DynamicCommandsSetup() {
        if(RoyaleEconomy.plugin.getConfig().getBoolean("use-only-one-bank"))
            commandSettings.put("bank", new OnlyBankCommand());
        else
            commandSettings.put("bank", new BankCommand());
        commandSettings.put("shared-bank", new SharedBankCommand());
        if(RoyaleEconomy.plugin.getConfig().getBoolean("use-interest"))
            commandSettings.put("interest", new InterestCommand());

        for(Map.Entry<String, TabExecutor> commandSetting : commandSettings.entrySet()){
            ConfigurationSection definition = RoyaleEconomy.commandsCfg.getConfigurationSection("definitions."+commandSetting.getKey());
            if(definition==null){
                plugin.getLogger().warning("No definition provided for "+commandSetting.getKey());
                continue;
            }
            if(definition.getString("command").equals("none"))
                continue;

            try {
                Method method = commandSetting.getValue().getClass().getMethod("extraSetup");
                method.invoke(commandSetting.getValue());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                //should never happen
            }
            if(definition.contains("permission")){
                registerCommand(new DynamicCommand(
                        definition.getString("command"),
                        definition.getString("permission"),
                        definition.getString("no-permission"),
                        definition.getStringList("aliases"),
                        commandSetting.getValue()
                ));
            }
            else{
                registerCommand(new DynamicCommand(
                        definition.getString("command"),
                        definition.getStringList("aliases"),
                        commandSetting.getValue()
                ));
            }
        }
    }

    public static void registerCommand(DynamicCommand pluginCommand) {
        loadedCommands.add(pluginCommand.getName());
        getCommandMap().register(plugin.getDescription().getName(), pluginCommand);
    }

    @SuppressWarnings("unchecked")
    public static void unregisterCommands() {
        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            CommandMap commandMap = getCommandMap();
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            for (String commandName : loadedCommands) {
                Command command = commandMap.getCommand(commandName);
                if (command == null)
                    continue;

                for (String alias : command.getAliases())
                    knownCommands.remove(alias);
                knownCommands.remove(command.getName());
                command.unregister(commandMap);
            }
            knownCommandsField.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadedCommands.clear();
    }

    /*
    private static PluginCommand getCommand(String name, Plugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            command = c.newInstance(name, plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return command;
    }
    */

    private static CommandMap commandMap = null;
    private static CommandMap getCommandMap() {
        if (commandMap != null)
            return commandMap;

        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);

                commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return commandMap;
    }
}
