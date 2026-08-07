package me.qKing12.RoyaleEconomy.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.RoyaleEconomy.utils.Utils.getSkull;



public class ItemConstructorLegacy implements ItemConstructor {

    public ItemStack getItem(Material material, String name, ArrayList<String> lore){
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags( ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
        meta.setDisplayName(Utils.chat(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItem(Material material, short data, String name, ArrayList<String> lore){
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags( ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(Utils.chat(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItem(String material, String name, ArrayList<String> lore){
        ItemStack item = getItemFromMaterial(material);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(Utils.chat(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        //if(material.contains(" enchanted")){
        //    item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        //}
        return item;
    }

    public ItemStack getItemFromMaterial(String material){
        if(material.startsWith("skull:") || material.startsWith("head:"))
            return getSkull(material.split(":")[1]);
        boolean enchanted = material.contains(" enchanted");
        ItemStack deReturnat;
        material=material.replace(" unbreakable", "").replace(" enchanted", "");
        Material mat = Material.getMaterial(material);
        if(mat!=null)
            deReturnat = new ItemStack(mat, 1);
        else{
            short data;
            if(material.split(":").length==2)
                data = Short.parseShort(material.split(":")[1]);
            else
                data = 0;
            try {
                int id = Integer.parseInt(material.split(":")[0]);
                String finalId = LegacyIDHackyWay.getId(id);
                if (data == 0)
                    deReturnat = new ItemStack(Material.getMaterial(finalId), 1);
                else
                    deReturnat = new ItemStack(Material.getMaterial(finalId), 1, data);
            }catch(Exception x){
                String finalId=material.split(":")[0];
                if (data == 0)
                    deReturnat = new ItemStack(Material.getMaterial(finalId), 1);
                else
                    deReturnat = new ItemStack(Material.getMaterial(finalId), 1, data);
            }
        }

        if(enchanted){
            deReturnat.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta meta = deReturnat.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            deReturnat.setItemMeta(meta);
        }
        return deReturnat;
    }

    /*public ItemStack getItemFromMaterial(String material){
        if(material.startsWith("skull:") || material.startsWith("head:"))
            return getSkull(material.split(":")[1]);
        material=material.replace(" unbreakable", "").replace(" enchanted", "");
        Material mat = Material.getMaterial(material);
        if(mat!=null)
            return new ItemStack(mat, 1);
        else{
            short data;
            if(material.split(":").length==2)
                data = Short.parseShort(material.split(":")[1]);
            else
                data = 0;
            int id = Integer.parseInt(material.split(":")[0]);
            String finalId=LegacyIDHackyWay.getId(id);
            if(data==0)
                return new ItemStack(Material.getMaterial(finalId), 1);
            else
                return new ItemStack(Material.getMaterial(finalId), 1, data);
        }
    }*/

}
