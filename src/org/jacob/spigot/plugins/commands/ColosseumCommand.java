package org.jacob.spigot.plugins.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jacob.spigot.plugins.FactionsColosseum;
import org.jacob.spigot.plugins.runnables.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jacob.spigot.plugins.runnables.Game.gameList;

public class ColosseumCommand implements CommandExecutor {

    FileConfiguration data = FactionsColosseum.getInstance().getPlayerData();

    Game timer;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (s.equalsIgnoreCase("c")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(ChatColor.RED + "You must be a player!");
                return true;
            }
            Player p = (Player) commandSender;

            if (!p.hasPermission("fc.commands.c")) {
                p.sendMessage(ChatColor.RED + "No permission");
                return true;
            }

            if (strings.length == 0) {
                p.sendMessage(ChatColor.RED + "Usage: /c <create:join:leave:start> [colosseum]");
                return true;

            }

            if (strings.length == 2) {
                if (strings[0].equalsIgnoreCase("create")) {

                    if (!p.hasPermission("fc.commands.c.create")) {
                        p.sendMessage(ChatColor.RED + "No permission");
                        return true;
                    }

                    String region_name = strings[1];

                    Region selection = null;

                    if (data.getConfigurationSection("colosseums." + region_name) != null) {
                        p.sendMessage(ChatColor.RED + "That Colosseum already exists!");
                        return true;
                    }

                    try {
                        selection = FactionsColosseum.getInstance().getWorldEdit().getSession(p).getSelection(BukkitAdapter.adapt(p.getWorld()));
                    } catch (NullPointerException | IncompleteRegionException e) {
                        p.sendMessage(ChatColor.RED + "Your selection is invalid! Please re-select an area with worldedit.");
                        return true;
                    }

                    if (selection.getArea() < 100) {
                        p.sendMessage(ChatColor.RED + "The area you selected (" + ChatColor.YELLOW + selection.getArea() + ChatColor.RED +
                                ") does not meet the minimum size! (" + ChatColor.YELLOW + "100" + ChatColor.RED + ")");
                        return true;
                    }

                    if (selection.getMinimumPoint().getY() != selection.getMaximumPoint().getY()) {
                        p.sendMessage(ChatColor.RED + "Your selection must be on the same level! (" + ChatColor.YELLOW + selection.getMinimumPoint()
                                .getY() + ChatColor.RED + " and " + ChatColor.YELLOW + selection.getMaximumPoint().getY() + ChatColor.RED + ")");
                        return true;
                    }

                    data.set("colosseums." + region_name + ".active", false);
                    data.set("colosseums." + region_name + ".location.x1", selection.getMinimumPoint().getX());
                    data.set("colosseums." + region_name + ".location.y1", selection.getMinimumPoint().getY());
                    data.set("colosseums." + region_name + ".location.z1", selection.getMinimumPoint().getZ());
                    data.set("colosseums." + region_name + ".location.x2", selection.getMaximumPoint().getX());
                    data.set("colosseums." + region_name + ".location.y2", selection.getMaximumPoint().getY());
                    data.set("colosseums." + region_name + ".location.z2", selection.getMaximumPoint().getZ());
                    data.set("colosseums." + region_name + ".joined", new ArrayList<>());
                    data.set("colosseums." + region_name + ".location.world", p.getWorld().getName());
                    FactionsColosseum.getInstance().savePlayerData();

                    p.sendMessage(ChatColor.YELLOW + "Colosseum " + ChatColor.GOLD + region_name + ChatColor.YELLOW + " has been created.");

                } else if (strings[0].equalsIgnoreCase("delete")) {
                    String region_name = strings[1];

                    if (!p.hasPermission("fc.commands.c.delete")) {
                        p.sendMessage(ChatColor.RED + "No permission");
                        return true;
                    }

                    boolean found = false;

                    for (String games : data.getConfigurationSection("colosseums").getKeys(false)) {
                        if (games.equals(region_name)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        p.sendMessage(ChatColor.RED + "That Colosseum doesn't exist.");
                        return true;
                    }
                    data.set("colosseums." + region_name, null);
                    p.sendMessage(ChatColor.YELLOW + "Colosseum " + ChatColor.GOLD + region_name +
                            ChatColor.YELLOW + " has been deleted/removed.");
                    return true;


                } else if (strings[0].equalsIgnoreCase("join")) {
                    if (!p.hasPermission("fc.commands.c.join")) {
                        p.sendMessage(ChatColor.RED + "No permission");
                        return true;
                    }

                    boolean part = false;

                    if (data.getConfigurationSection("colosseums") == null) {
                        p.sendMessage(ChatColor.RED + "That colosseum doesn't exist!");
                        return true;
                    }

                    for (String str : data.getConfigurationSection("colosseums").getKeys(false)) {

                        if (data.getStringList("colosseums." + str + ".joined").contains(p.getUniqueId().toString())) {
                            part = true;
                        }
                    }

                    if (part) {
                        p.sendMessage(ChatColor.RED + "You already have joined a Colosseum!");
                        return true;
                    }

                    String region_name = strings[1];

                    if (data.getConfigurationSection("colosseums." + region_name) == null) {
                        p.sendMessage(ChatColor.RED + "That Colosseum doesn't exist!");
                        return true;
                    }

                    List<String> queued = data.getStringList("colosseums." + region_name + ".joined");

                    if (queued.contains(p.getUniqueId().toString())) {
                        p.sendMessage(ChatColor.RED + "You've already joined this colosseum!");
                        return true;
                    }

                    Bukkit.broadcastMessage(ChatColor.GOLD + p.getName() + ChatColor.YELLOW + " has joined the " + ChatColor.GOLD + region_name
                            + ChatColor.YELLOW + " Colosseum!" + ChatColor.DARK_GREEN + " (#" + (queued.size() + 1) + ")");

                    queued.add(p.getUniqueId().toString());
                    data.set("colosseums." + region_name + ".joined", queued);
                    FactionsColosseum.getInstance().savePlayerData();

                } else if (strings[0].equalsIgnoreCase("leave")) {
                    if (!p.hasPermission("fc.commands.c.leave")) {
                        p.sendMessage(ChatColor.RED + "No permission");
                        return true;
                    }

                    String region_name = strings[1];

                    boolean match = false;

                    for (String games : data.getConfigurationSection("colosseums").getKeys(false)) {
                        if (games.equals(region_name)) {
                            match = true;
                        }
                    }

                    if (!match) {
                        p.sendMessage(ChatColor.RED + "That Colosseum doesn't exist!");
                        return true;
                    }

                    if (!data.getStringList("colosseums." + region_name + ".joined").contains(p.getUniqueId().toString())) {

                        p.sendMessage(ChatColor.RED + "You haven't joined this Colosseum!");
                        return true;
                    }

                    for (Game game : gameList) {
                        if (game.name.equals(region_name)) {
                            List<String> uuids = data.getStringList("colosseums." + region_name + ".joined");
                            uuids.remove(p.getUniqueId().toString());

                            data.set("colosseums." + region_name + ".joined", uuids);
                            FactionsColosseum.getInstance().savePlayerData();

                            Bukkit.broadcastMessage(ChatColor.GOLD + p.getName() + ChatColor.YELLOW + " has left the " + ChatColor.GOLD
                                    + region_name + ChatColor.YELLOW + " Colosseum! (" + ChatColor.DARK_GREEN + "#" + uuids.size()
                                    + ChatColor.YELLOW + ")");

                        }
                    }


                } else if (strings[0].equalsIgnoreCase("start")) {
                    if (!p.hasPermission("fc.commands.c.start")) {
                        p.sendMessage(ChatColor.RED + "No permission");
                        return true;
                    }

                    String region_name = strings[1];

                    if (data.getStringList("colosseums." + region_name + ".joined").size() < 2) {
                        p.sendMessage(ChatColor.RED + "There aren't enough players to start!");
                        return true;
                    }

                    if (data.getConfigurationSection("colosseums." + region_name) == null) {
                        p.sendMessage(ChatColor.RED + "That Colosseum doesn't exist!");
                        return true;
                    }

                    if (data.getBoolean("colosseums." + region_name + ".active")) {
                        p.sendMessage(ChatColor.RED + "This Colosseum is already activated. Please wait until it finishes, or create a new one by typing" +
                                ChatColor.YELLOW + " /c create <name>");
                        return true;
                    }

                    timer = new Game(region_name);
                    timer.startCountDown();

                    gameList.add(timer);

                    List<String> queued = data.getStringList("colosseums." + region_name + ".joined");

                    for (String d : queued) {
                        Player queuedPlayer = Bukkit.getPlayer(UUID.fromString(d));

                    }

                    p.sendMessage(ChatColor.YELLOW + "Colosseum " + ChatColor.GOLD + region_name + ChatColor.YELLOW + " has started!");


                } else {
                    p.sendMessage(ChatColor.RED + "Incorrect arguments!");
                    return true;
                }
            }
        }
        return true;
    }
}
