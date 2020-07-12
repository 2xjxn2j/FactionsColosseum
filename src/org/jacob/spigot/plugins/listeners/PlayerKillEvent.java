package org.jacob.spigot.plugins.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jacob.spigot.plugins.FactionsColosseum;
import org.jacob.spigot.plugins.runnables.Game;

import java.util.Objects;

public class PlayerKillEvent implements Listener {

    FileConfiguration data = FactionsColosseum.getInstance().getPlayerData();

    @EventHandler
    public void onKill(EntityDeathEvent e) {

        try {

            boolean isPlaying = false;

            for (String game : data.getConfigurationSection("colosseums").getKeys(false)) {
                if (Game.isPlaying((Player) e.getEntity(), game)) {
                    isPlaying = true;
                }
            }

            if (!isPlaying) {
                return;
            }

            try {
                if (e.getEntity().getType().equals(EntityType.PLAYER) && e.getEntity().getKiller().getType().equals(EntityType.PLAYER)) {
                    Player p = (Player) e.getEntity();
                    Player killer = e.getEntity().getKiller();

                    if (p == killer) {
                        for (String game : data.getConfigurationSection("colosseums").getKeys(false)) {
                            if (Game.isPlaying(p, game) && Game.isPlaying(p, game)) {

                                if (!data.getBoolean("colosseums." + game + ".active")) {
                                    return;
                                }

                                Game.eliminatePlayer(p, killer, Game.getGamePlaying(killer));


                            }
                        }
                    }

                    for (String game : data.getConfigurationSection("colosseums").getKeys(false)) {
                        if (Game.isPlaying(p, game) && Game.isPlaying(p, game)) {
                            if (Objects.equals(Game.getGamePlaying(p), Game.getGamePlaying(killer))) {

                                if (!data.getBoolean("colosseums." + game + ".active")) {
                                    return;
                                }

                                Game.eliminatePlayer(p, killer, Game.getGamePlaying(killer));


                            }
                        }
                    }
                }

                if (e.getEntity().getType().equals(EntityType.PLAYER) && e.getEntity().getKiller() == null) {
                    Player p = (Player) e.getEntity();
                    for (String game : data.getConfigurationSection("colosseums").getKeys(false)) {
                        if (Game.isPlaying(p, game)) {
                            Game.eliminatePlayer(p, game);
                        }
                    }
                }

                if (e.getEntity().getType().equals(EntityType.PLAYER) && !e.getEntity().getKiller().getType().equals(EntityType.PLAYER)) {
                    Player p = (Player) e.getEntity();
                    for (String game : data.getConfigurationSection("colosseums").getKeys(false)) {
                        if (Game.isPlaying(p, game)) {
                            Game.eliminatePlayer(p, game);
                        }
                    }
                }

            } catch (Exception ex) {
                System.out.println(ChatColor.RED + "There was a Null error. " +
                        "Please message 2xjtn about this! The player has NOT been elimintated and must be eliminated by another player.");
            }
        } catch (Exception ef) {
            System.out.println(ChatColor.RED + "There was a Null error. " +
                    "Please message 2xjtn about this! The player has NOT been elimintated and must be eliminated by another player.");
        }
    }
}
