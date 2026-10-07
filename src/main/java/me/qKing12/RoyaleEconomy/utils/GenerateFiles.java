package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



public class GenerateFiles {

    public static void archiveDatabase() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyDataBackup.zip"));
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fis.close();
            fos.close();
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    public GenerateFiles(){
        ArrayList<String> files = new ArrayList<>();
        files.add("bankUpgrades.yml");
        files.add("coinBagsAndTalismans.yml");
        files.add("commands.yml");
        files.add("menus.yml");
        files.add("permissions.yml");
        files.add("sounds.yml");

        for(String file : files) {
            File toCreate = new File(RoyaleEconomy.plugin.getDataFolder(), file);
            if (!toCreate.exists()) {
                try {
                    toCreate.createNewFile();
                    InputStream input = this.getClass().getResourceAsStream("/"+file);
                    OutputStream output = new FileOutputStream(toCreate);
                    int realLength;
                    byte[] buffer = new byte[1024];

                    while (input != null && (realLength = input.read(buffer)) > 0) {
                        output.write(buffer, 0, realLength);
                    }
                    output.flush();
                    output.close();
                    RoyaleEconomy.plugin.getLogger().info("Loading "+file+" config...");
                } catch (IOException e) {
                    RoyaleEconomy.plugin.getLogger().info("Could not create the "+file+" config.");
                }
            }
        }

        UpdateFiles.loadFiles();
    }
}
