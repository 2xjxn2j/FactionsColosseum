package org.jacob.spigot.plugins;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jacob.spigot.plugins.commands.ColosseumCommand;
import org.jacob.spigot.plugins.listeners.PlayerJoinQuitListener;
import org.jacob.spigot.plugins.listeners.PlayerKillEvent;
import org.jacob.spigot.plugins.listeners.PlayerMoveCheckListener;
import org.jacob.spigot.plugins.runnables.Game;
import org.jacob.spigot.plugins.utils.TabCompleter;

import java.io.File;
import java.io.IOException;

public class FactionsColosseum extends JavaPlugin {

    private static FactionsColosseum instance;

    public FactionsColosseum() {
    }

    public static FactionsColosseum getInstance() {
        return instance;
    }

    private File playerDataFile;
    private FileConfiguration playerData;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveConfig();

        createPlayerData();

        System.out.println(ChatColor.YELLOW + "FactionsColosseum is being loaded...");

        getCommand("c").setExecutor(new ColosseumCommand());

        getConfig().set("enabled", true);
        saveConfig();

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new PlayerJoinQuitListener(), this);
        pm.registerEvents(new PlayerKillEvent(), this);
        pm.registerEvents(new PlayerMoveCheckListener(), this);

        Game.init();

        getCommand("c").setTabCompleter(new TabCompleter());

    }

    public WorldEditPlugin getWorldEdit() {
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        if (p instanceof WorldEditPlugin) {
            return (WorldEditPlugin) p;
        } else {
            return null;
        }
    }

    private void createPlayerData() {
        playerDataFile = new File(getDataFolder(), "data.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            saveResource("data.yml", false);
        }

        playerData = new YamlConfiguration();
        try {
            playerData.load(playerDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public FileConfiguration getPlayerData() {
        return this.playerData;
    }

    public void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}
