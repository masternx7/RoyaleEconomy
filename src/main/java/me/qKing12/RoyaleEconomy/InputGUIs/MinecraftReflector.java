package me.qKing12.RoyaleEconomy.InputGUIs;

import org.bukkit.Bukkit;

public class MinecraftReflector {

    private static String MINECRAFT_SERVER_VERSION=getMinecraftServerVersion();

    private static String getMinecraftServerVersion() {
        String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
        return bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
    }

    public static Class<?> getMinecraftServerClass(String className) throws ClassNotFoundException {
        if(MINECRAFT_SERVER_VERSION.equals("v1_17_R1")){
            switch (className) {
                case "Entity":
                    return Class.forName("net.minecraft.world.entity.Entity");
                case "BlockPosition":
                    return Class.forName("net.minecraft.core.BlockPosition");
                case "EntityHuman":
                    return Class.forName("net.minecraft.world.entity.player.EntityHuman");
                case "Packet":
                    return Class.forName("net.minecraft.network.protocol.Packet");
                case "TileEntity":
                    return Class.forName("net.minecraft.world.level.block.entity.TileEntity");
            }
        }
        return Class.forName("net.minecraft.server." + MINECRAFT_SERVER_VERSION + "." + className);
    }
}
