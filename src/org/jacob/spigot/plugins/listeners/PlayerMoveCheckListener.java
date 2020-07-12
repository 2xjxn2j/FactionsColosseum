package org.jacob.spigot.plugins.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jacob.spigot.plugins.FactionsColosseum;
import org.jacob.spigot.plugins.runnables.Game;
import org.jacob.spigot.plugins.utils.GameStatus;

import java.util.ConcurrentModificationException;

public class PlayerMoveCheckListener implements Listener {

    FileConfiguration data = FactionsColosseum.getInstance().getPlayerData();

    @EventHandler
    public void onMoveCheck(PlayerMoveEvent e) {

        try {


            if (data.getConfigurationSection("colosseums") == null) {
                return;
            }


            for (String games : data.getConfigurationSection("colosseums").getKeys(false)) {
                for (Game game : Game.gameList) {
                    if (games.equals(game.name)) {

                        int minX = data.getInt("colosseums." + game.name + ".location.x1");
                        int minY = data.getInt("colosseums." + game.name + ".location.y1") - 5;
                        int minZ = data.getInt("colosseums." + game.name + ".location.z1");
                        int maxX = data.getInt("colosseums." + game.name + ".location.x2");
                        int maxY = data.getInt("colosseums." + game.name + ".location.y2") + 5;
                        int maxZ = data.getInt("colosseums." + game.name + ".location.z2");

                        if(data.getStringList("colosseums." + game.name + ".joined").contains(e.getPlayer().getUniqueId().toString())) {
                            if(game.status == GameStatus.STARTING) {
                                e.setCancelled(true);
                            }
                        }

                        Location min = new Location(e.getPlayer().getWorld(), minX, minY, minZ);
                        Location max = new Location(e.getPlayer().getWorld(), maxX, maxY, maxZ);

                        if (Game.locationIsInCuboid(e.getPlayer(), e.getFrom(), min, max)) {
                            if (!Game.locationIsInCuboid(e.getPlayer(), e.getTo(), min, max)) {

                                if (!data.getBoolean("colosseums." + games + ".active")) {
                                    return;
                                }

                                if (!data.getStringList("colosseums." + games + ".joined").contains(e.getPlayer().getUniqueId().toString())) {
                                    return;
                                }

                                e.getPlayer().sendMessage(ChatColor.RED + "Oh no! You stepped out!");
                                Game.eliminatePlayer(e.getPlayer(), games);
                            }
                        }

                        if (!Game.locationIsInCuboid(e.getPlayer(), e.getFrom(), min, max)) {
                            if (Game.locationIsInCuboid(e.getPlayer(), e.getTo(), min, max)) {
                                if (!data.getBoolean("colosseums." + games + ".active")) {
                                    return;
                                }

                                if (data.getStringList("colosseums." + games + ".joined").contains(e.getPlayer().getUniqueId().toString())) {
                                    return;
                                }

                                if(e.getPlayer().isFlying()) {
                                    e.getPlayer().sendMessage(ChatColor.RED + "You cannot enter this area!");
                                    e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
                                }

                                e.getPlayer().sendMessage(ChatColor.RED + "You cannot enter this area!");
                                e.getPlayer().teleport(e.getFrom());
                            }
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException ex) {
            return;
        }
    }
}
