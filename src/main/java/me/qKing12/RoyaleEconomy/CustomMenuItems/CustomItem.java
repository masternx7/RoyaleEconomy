package me.qKing12.RoyaleEconomy.CustomMenuItems;

import me.qKing12.RoyaleEconomy.CustomMenuItems.action.*;
import me.qKing12.RoyaleEconomy.CustomMenuItems.requirements.ItemRequirement;
import me.qKing12.RoyaleEconomy.CustomMenuItems.requirements.MoneyRequirement;
import me.qKing12.RoyaleEconomy.CustomMenuItems.requirements.PermissionRequirement;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomItem {
    private int slot;
    private String name;
    private List<String> lore;
    private ItemStack material;

    private List<ItemRequirement> requirements;
    private List<ClickAction> actions;

    public CustomItem(ConfigurationSection item){
        this.slot = item.getInt("slot");
        this.material = RoyaleEconomy.itemConstructor.getItemFromMaterial(item.getString("material"));
        this.name = Utils.chat(item.getString("name"));
        this.lore = new ArrayList<>();
        for(String line : item.getStringList("lore"))
            lore.add(Utils.chat(line));

        requirements = new ArrayList<>();
        if(item.contains("click-requirements")){
            for(String key : item.getConfigurationSection("click-requirements").getKeys(false)){
                try {
                    String requirement = item.getString("click-requirements." + key + ".requirement");
                    String denyMessage = item.getString("click-requirements." + key + ".deny-message");
                    if (requirement.startsWith("[permission] ")) {
                        requirements.add(new PermissionRequirement(requirement.substring(13), denyMessage));
                    } else if (requirement.startsWith("[money] ")) {
                        requirements.add(new MoneyRequirement(Double.parseDouble(requirement.substring(8)), denyMessage));
                    }
                    else{
                        RoyaleEconomy.plugin.getLogger().warning("Requirement invalid: "+requirement);
                    }
                }catch(Exception x){
                    RoyaleEconomy.plugin.getLogger().warning("There was an error with the requirement "+key+" on the item with the name "+name);
                }
            }
        }

        actions = new ArrayList<>();
        if(item.contains("click-actions")){
            for(String action : item.getStringList("click-actions")){
                try{
                    if(action.equalsIgnoreCase("[close]"))
                        actions.add(new CloseAction());
                    else if(action.startsWith("[message] "))
                        actions.add(new MessageAction(action.substring(10)));
                    else if(action.startsWith("[command] "))
                        actions.add(new CommandAction(action.substring(10)));
                    else if(action.startsWith("[commandC] "))
                        actions.add(new ConsoleCommandAction(action.substring(11)));
                    else
                        RoyaleEconomy.plugin.getLogger().warning("Action invalid: "+action);
                }catch(Exception x){
                    RoyaleEconomy.plugin.getLogger().warning("There was an error with the click action "+action+" on the item with the name "+name);
                }
            }
        }
    }

    public boolean ableToClick(Player player){
        for(ItemRequirement req : requirements){
            if(!req.isValid(player)){
                if(req.getDenyMessage() != null)
                    player.sendMessage(RoyaleEconomy.utilsAPI.chat(player, req.getDenyMessage()));
                return false;
            }
        }

        return true;
    }

    public void runActions(Player player){
        for(ClickAction action : actions)
            action.executeFor(player);
    }

    public int getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemStack getMaterial() {
        return material;
    }

    public List<ItemRequirement> getRequirements() {
        return requirements;
    }

    public List<ClickAction> getActions() {
        return actions;
    }

    public ItemStack getItem(Player player){
        ItemStack copy = material.clone();
        ItemMeta meta = copy.getItemMeta();
        meta.setDisplayName(RoyaleEconomy.utilsAPI.chatApiOnly(player, name));
        List<String> lore = new ArrayList<>();
        for(String line : this.lore)
            lore.add(RoyaleEconomy.utilsAPI.chatApiOnly(player, line));
        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    public ItemStack getItem(){
        ItemStack copy = material.clone();
        ItemMeta meta = copy.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.addAll(this.lore);
        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
