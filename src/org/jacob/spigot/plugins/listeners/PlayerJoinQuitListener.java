package org.jacob.spigot.plugins.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jacob.spigot.plugins.FactionsColosseum;
import org.jacob.spigot.plugins.runnables.Game;

public class PlayerJoinQuitListener implements Listener {

    FileConfiguration data = FactionsColosseum.getInstance().getPlayerData();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        try{
            for(String games : data.getConfigurationSection("colosseums").getKeys(false)) {
                if(data.getStringList("colosseums." + games + ".joined").contains(player.getUniqueId().toString())) {
                    Game.eliminatePlayer(player, games);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("No running game found!");
        }
    }
}
