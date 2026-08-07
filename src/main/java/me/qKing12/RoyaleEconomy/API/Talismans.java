package me.qKing12.RoyaleEconomy.API;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Talismans {

    public ArrayList<ItemStack> getRandomCoinsTalismans(){
        return (ArrayList<ItemStack>)me.qKing12.RoyaleEconomy.Commands.Talismans.randomCoinsTalismans.clone();
    }

    public ArrayList<ItemStack> getPurseSaverTalismans(){
        return (ArrayList<ItemStack>)me.qKing12.RoyaleEconomy.Commands.Talismans.purseSaver.clone();
    }

    public ArrayList<ItemStack> getDeathSaverTalismans(){
        return (ArrayList<ItemStack>)me.qKing12.RoyaleEconomy.Commands.Talismans.deathSaver.clone();
    }


}
