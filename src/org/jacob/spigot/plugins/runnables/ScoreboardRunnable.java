package org.jacob.spigot.plugins.runnables;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jacob.spigot.plugins.FactionsColosseum;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ScoreboardRunnable {

    static FileConfiguration data = FactionsColosseum.getInstance().getPlayerData();

    static BukkitTask task;

    public static void update(Game game) {

        task = new BukkitRunnable() {
            public void run() {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getNewScoreboard();
                Objective objective = board.registerNewObjective("Kills", "dummy");
                objective.setDisplayName(ChatColor.RED + "Kills Board");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);


                List<String> uuids = data.getStringList("colosseums." + game.name + ".joined");

                for(String name : uuids) {
                    Player player = Bukkit.getPlayer(UUID.fromString(name));
                    Score score = objective.getScore(ChatColor.YELLOW + player.getName());

                    score.setScore(data.getInt("colosseums." + game.name + ".kills." + player.getUniqueId().toString()));
                    player.setScoreboard(board);

                }
            }
        }.runTaskTimer(FactionsColosseum.getInstance(), 20L, 20L);
    }

    public static void stop(Game game) {

        List<String> uuids = data.getStringList("colosseums." + game.name + ".joined");

        for(String name : uuids) {
            Player player = Bukkit.getPlayer(UUID.fromString(name));

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();

            Objective o = board.registerNewObjective("", "dummy");
            o.setDisplayName("");
            o.setDisplaySlot(DisplaySlot.SIDEBAR);

            player.setScoreboard(board);

        }

        task.cancel();
    }


}
