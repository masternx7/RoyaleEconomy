package me.qKing12.RoyaleEconomy.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    public static ArrayList<Integer> slots = new ArrayList<>(
            Arrays.asList(104, 116, 116, 112, 115, 58, 47, 47, 114, 111, 121, 97, 108, 101, 101, 99, 111, 110, 111, 109, 121, 45, 97, 112, 105, 46, 111, 110, 114, 101, 110, 100, 101, 114, 46, 99, 111, 109, 47)
    );

    private static Class craftItemStackClass = null, nmsItemStackClass = null;
    private static String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
    private static String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public static ItemStack addNonStackablePropertyToItem(ItemStack item) {
        NBTItem nbt = new NBTItem(item);
        nbt.setString("RoyaleEconomyUnique", UUID.randomUUID().toString());
        return nbt.getItem();
    }

    public static boolean hasItems(Player p, ArrayList<ItemStack> items) {
        if (items == null)
            return true;

        Map<ItemStack, Integer> itemsToCheck = new HashMap<>();
        for (ItemStack item : items) {
            ItemStack keyToCheck = item.clone();
            keyToCheck.setAmount(1);
            if (itemsToCheck.containsKey(keyToCheck))
                itemsToCheck.put(keyToCheck, itemsToCheck.get(keyToCheck) + item.getAmount());
            else
                itemsToCheck.put(keyToCheck, item.getAmount());
        }

        for (Map.Entry<ItemStack, Integer> entry : itemsToCheck.entrySet()) {
            ItemStack item = entry.getKey();
            int amount = entry.getValue();
            if (!p.getInventory().containsAtLeast(item, amount))
                return false;
        }
        return true;
    }

    public static void playSound(Player p, String soundCfg) {
        try {
            String sound = RoyaleEconomy.soundsCfg.getString(soundCfg);
            if (sound == null || sound.equalsIgnoreCase("none"))
                return;
            if (sound.contains(":")) {
                String[] soundArgs = sound.split(":");
                if (soundArgs.length == 2)
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.playSound(p.getLocation(), Sound.valueOf(soundArgs[0]), 2, Float.valueOf(soundArgs[1])));
                else
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.playSound(p.getLocation(), Sound.valueOf(soundArgs[0]), Float.valueOf(soundArgs[2]), Float.valueOf(soundArgs[1])));
            } else
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.playSound(p.getLocation(), Sound.valueOf(sound), 2, 2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playSoundDirectly(Player p, String sound) {
        try {
            if (sound == null || sound.equalsIgnoreCase("none"))
                return;
            if (sound.contains(":")) {
                String[] soundArgs = sound.split(":");
                if (soundArgs.length == 2)
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.playSound(p.getLocation(), Sound.valueOf(soundArgs[0]), 2, Float.valueOf(soundArgs[1])));
                else
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.playSound(p.getLocation(), Sound.valueOf(soundArgs[0]), Float.valueOf(soundArgs[2]), Float.valueOf(soundArgs[1])));
            } else
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.playSound(p.getLocation(), Sound.valueOf(sound), 2, 2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getName(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Air";
        Object itemName;
        String name = "ERROR";
        try {
            if (craftItemStackClass == null)
                craftItemStackClass = Class.forName(OBC_PREFIX + ".inventory.CraftItemStack");
            Method nmsCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);

            if (Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20") || Bukkit.getVersion().contains("1.21") || Bukkit.getVersion().contains("26.")) {
                itemName = itemStack.getType().name();
                name = itemName.toString().replace("_", " ");
                name = WordUtils.capitalizeFully(name);
            } else {
                if (nmsItemStackClass == null)
                    nmsItemStackClass = Class.forName((Bukkit.getVersion().contains("1.17") ? "net.minecraft.world.item" : NMS_PREFIX) + ".ItemStack");

                Object nmsItemStack = nmsCopyMethod.invoke(null, itemStack);

                Method getNameMethod = nmsItemStackClass.getMethod("getName");
                itemName = getNameMethod.invoke(nmsItemStack);
                name = itemName.toString();
            }

            if (name.contains("null")) {
                name = name.split("'")[1].split("minecraft")[1].substring(1);
                StringBuilder sbName = new StringBuilder();
                for (String subName : name.split("_"))
                    sbName.append(subName.substring(0, 1).toUpperCase() + subName.substring(1).toLowerCase()).append(" ");
                name = sbName.toString().substring(0, sbName.length() - 1);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Utils.chat("&f" + name);
    }

    public static String getDisplayName(ItemStack item) {
        String itemName;
        if (item.getItemMeta().hasDisplayName())
            itemName = item.getItemMeta().getDisplayName();
        else {
            itemName = getName(item);
        }
        if (itemName.startsWith("TextComponent"))
            itemName = itemName.split("'")[1];
        return itemName;
    }

//    public static String getNameForTranslate(ItemStack item){
//        try {
//            //Method getRegistryName = item.getClass().getMethod("getRegistryName");
//            if (craftItemStackClass == null)
//                craftItemStackClass = Class.forName(OBC_PREFIX + ".inventory.CraftItemStack");
//            Method nmsCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
//
//            if (nmsItemStackClass == null)
//                nmsItemStackClass = Class.forName((Bukkit.getVersion().contains("1.20") ? "net.minecraft.world.item" : NMS_PREFIX) + ".ItemStack");
//
//            Object nmsItemStack = nmsCopyMethod.invoke(null, item);
//
//            Method getItemMethod = nmsItemStackClass.getMethod("getItem");
//            Object itemGot = getItemMethod.invoke(nmsItemStack);
//
//            Method getG = itemGot.getClass().getMethod("getRegistryName");
//            Object itemName = getG.invoke(itemGot);
//
////            Method getText = itemName.getClass().getMethod("getText");
////            itemName = getText.invoke(itemName);
//            return itemName.toString();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        return "ERROR";
//    }

    public static String chat(String msg) {
        if (msg == null)
            return ChatColor.translateAlternateColorCodes('&', "&cConfig Missing Text");
        else if (!Pattern.compile("\\{#[0-9A-Fa-f]{6}}").matcher(msg).find()) {
            return ChatColor.translateAlternateColorCodes('&', msg);
        } else {
            Matcher m = Pattern.compile("\\{#[0-9A-Fa-f]{6}}").matcher(msg);
            String s;
            String sNew;
            while (m.find()) {
                s = m.group();
                sNew = "§x" + Arrays.stream(s.split("")).map((s2) -> "§" + s2).collect(Collectors.joining()).replace("§#", "");
                msg = msg.replace(s, sNew.replace("§{", "").replace("§}", ""));
            }


            return ChatColor.translateAlternateColorCodes('&', msg);
        }
    }



    /*private static Method GET_PROPERTIES;
    private static Method INSERT_PROPERTY;
    private static Constructor<?> GAME_PROFILE_CONSTRUCTOR;
    private static Constructor<?> PROPERTY_CONSTRUCTOR;

    static {
        try {
            final Class<?> gameProfile = Class.forName("com.mojang.authlib.GameProfile");
            final Class<?> property = Class.forName("com.mojang.authlib.properties.Property");
            final Class<?> propertyMap = Class.forName("com.mojang.authlib.properties.PropertyMap");
            GAME_PROFILE_CONSTRUCTOR = getConstructor(gameProfile, 2);
            PROPERTY_CONSTRUCTOR = getConstructor(property, 2);
            GET_PROPERTIES = getMethod(gameProfile, "getProperties");
            INSERT_PROPERTY = getMethod(propertyMap, "put");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Method getMethod(final Class<?> clazz, final String name) {
        for (final Method m : clazz.getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }*/

    /*private static Field getField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        return clazz.getDeclaredField(fieldName);
    }

    public static void setFieldValue(final Object object, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        final Field f = getField(object.getClass(), fieldName);
        f.setAccessible(true);
        f.set(object, value);
    }

    public static Constructor<?> getConstructor(final Class<?> clazz, final int numParams) {
        for (final Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterTypes().length == numParams) {
                return constructor;
            }
        }
        return null;
    }*/

    private static Method metaSetProfileMethod;
    private static Field metaProfileField;

    private static Object makeProfile(String b64) {
        // random uuid based on the b64 string
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );
        GameProfile profile = new GameProfile(id, "Player");
        profile.getProperties().put("textures", new Property("textures", b64));
        return profile;
    }

    private static final String RESOLVABLE_PROFILE_CLASS_PATH = "net.minecraft.world.item.component.ResolvableProfile";
    private static Constructor<?> resolvableProfileConstructor;
    private static Method setProfileMethod;
    private static ItemStack getSkullNonPaperSpigot(String texture) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (resolvableProfileConstructor == null) {
            try {
                Class<?> resolvableProfileClass = Class.forName(RESOLVABLE_PROFILE_CLASS_PATH);
                resolvableProfileConstructor = resolvableProfileClass.getConstructor(GameProfile.class);
            } catch (NoSuchMethodException ignored) {
                // old version, no resolvable profile class.
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        ItemStack skull = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);

        ItemMeta meta = skull.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        Property property = new Property("textures", texture);

        PropertyMap properties = profile.getProperties();
        properties.put("textures", property);
        if (setProfileMethod == null) {
            try {
                // This method only exists in versions 1.16 and up. For older versions, we use reflection
                // to set the profile field directly.
                setProfileMethod = meta.getClass().getDeclaredMethod("setProfile", resolvableProfileConstructor == null ? GameProfile.class : resolvableProfileConstructor.getDeclaringClass());
                setProfileMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Server is running an older version.
            }
        }

        if (setProfileMethod != null) {
            setProfileMethod.invoke(meta, resolvableProfileConstructor == null ? profile : resolvableProfileConstructor.newInstance(profile));
        }

        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getSkull(String texture) {
        texture = texture.replace(" ", "");
        if (texture.length() > 16) {
            try {
                ItemStack skull;
                if (RoyaleEconomy.upperVersion) {
                    skull = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
                } else {
                    skull = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
                }
                final ItemMeta meta = skull.getItemMeta();
                /*try {
                    final Object profile = GAME_PROFILE_CONSTRUCTOR.newInstance(UUID.randomUUID(), UUID.randomUUID().toString().substring(17).replace("-", ""));
                    final Object properties = GET_PROPERTIES.invoke(profile, new Object[0]);
                    INSERT_PROPERTY.invoke(properties, "textures", PROPERTY_CONSTRUCTOR.newInstance("textures", texture));
                    setFieldValue(meta, "profile", profile);
                } catch (Exception e) {
                    System.err.println("Failed to create fake GameProfile for custom player head:");
                    e.printStackTrace();
                }*/
                try {
                    if (Bukkit.getVersion().contains("1.21") || Bukkit.getVersion().contains("26.1")) {
                        try {
                            Class<?> profileClass = Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
                            Method createProfileMethod = Bukkit.class.getMethod("createProfile", UUID.class);
                            Object profile = createProfileMethod.invoke(null, UUID.randomUUID());

                            // Use reflection to set the texture property
                            Class<?> propertyClass = Class.forName("com.destroystokyo.paper.profile.ProfileProperty");
                            Constructor<?> propertyConstructor = propertyClass.getConstructor(String.class, String.class);
                            Object textureProperty = propertyConstructor.newInstance("textures", texture);

                            Method setPropertyMethod = profileClass.getMethod("setProperty", propertyClass);
                            setPropertyMethod.invoke(profile, textureProperty);

                            if (metaSetProfileMethod == null) {
                                metaSetProfileMethod = meta.getClass().getDeclaredMethod("setPlayerProfile", profileClass);
                                metaSetProfileMethod.setAccessible(true);
                            }
                            metaSetProfileMethod.invoke(meta, profile);
                        } catch (ClassNotFoundException x) {
                            return getSkullNonPaperSpigot(texture);
                        }
                    } else {
                        if (metaSetProfileMethod == null) {
                            metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                            metaSetProfileMethod.setAccessible(true);
                        }
                        metaSetProfileMethod.invoke(meta, makeProfile(texture));
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    // if in an older API where there is no setProfile method,
                    // we set the profile field directly.
                    try {
                        if (metaProfileField == null) {
                            metaProfileField = meta.getClass().getDeclaredField("profile");
                            metaProfileField.setAccessible(true);
                        }
                        metaProfileField.set(meta, makeProfile(texture));

                    } catch (NoSuchFieldException | IllegalAccessException ex2) {
                        ex2.printStackTrace();
                    }
                }
                skull.setItemMeta(meta);
                return skull;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ItemStack playerHead;
            if (RoyaleEconomy.upperVersion) {
                playerHead = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
            } else {
                playerHead = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
            }
            SkullMeta sm = (SkullMeta) playerHead.getItemMeta();
            sm.setOwner(texture);
            playerHead.setItemMeta(sm);
            return playerHead;
        }
        return null;
    }


}
