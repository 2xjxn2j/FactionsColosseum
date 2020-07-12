package org.jacob.spigot.plugins.runnables;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jacob.spigot.plugins.FactionsColosseum;
import org.jacob.spigot.plugins.utils.GameStatus;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game {

    public int gametime;
    public String name;
    public int countdowntime = 5;

    static FileConfiguration data = FactionsColosseum.getInstance().getPlayerData();

    public static List<Game> gameList = new ArrayList<>();

    public GameStatus status;

    public BukkitTask task;
    public BukkitTask countdown;

    public Game(String name) {
        this.name = name;

    }


    public static void init() {

        if (!data.contains("colosseums")) {
            return;
        }

        for (String s : data.getConfigurationSection("colosseums").getKeys(false)) {
            gameList.add(new Game(s));

        }

    }

    public static void eliminatePlayer(Player p, String game) {
        if (p == null) {
            return;
        }

        List<String> players = data.getStringList("colosseums." + game + ".joined");
        for (String games : data.getConfigurationSection("colosseums").getKeys(false)) {
            if (players.contains(p.getUniqueId().toString())) {
                players.remove(p.getUniqueId().toString());

                players.remove(p.toString());
                data.set("colosseums." + games + ".joined", players);
                FactionsColosseum.getInstance().savePlayerData();

                Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

                Objective o = board.registerNewObjective("", "dummy");
                o.setDisplayName("");
                o.setDisplaySlot(DisplaySlot.SIDEBAR);

                p.setScoreboard(board);

                if(data.getBoolean("colosseums." + game + ".active")) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + p.getName() + ChatColor.YELLOW + " has been eliminated from the " + ChatColor.GOLD + games + ChatColor.YELLOW + " Colosseum! ("
                            + ChatColor.DARK_GREEN + "" + players.size() + ChatColor.YELLOW + " remaining)");
                } else {
                    Bukkit.broadcastMessage(ChatColor.GOLD + p.getName() + ChatColor.YELLOW + " has left the " + ChatColor.GOLD + games + ChatColor.YELLOW + " Colosseum! ("
                            + ChatColor.DARK_GREEN + "#" + players.size() + ChatColor.YELLOW + ")");
                }


            }
        }

        if (players.size() < 2) {

            if(data.getBoolean("colosseums." + game + ".active")) {
                for (Game list : gameList) {

                    if(list.name.equals(game)) {
                        list.stop();
                        break;
                    }
                }
            }
        }
    }

    public static void eliminatePlayer(Player p, Player killer, String game) {
        if (p == null) {
            return;
        }

        List<String> players = data.getStringList("colosseums." + game + ".joined");
        if (players.contains(p.getUniqueId().toString())) {
            players.remove(p.getUniqueId().toString());

            players.remove(p.toString());
            data.set("colosseums." + game + ".joined", players);
            FactionsColosseum.getInstance().savePlayerData();

            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

            Objective o = board.registerNewObjective("", "dummy");
            o.setDisplayName("");
            o.setDisplaySlot(DisplaySlot.SIDEBAR);

            p.setScoreboard(board);

            Bukkit.broadcastMessage(ChatColor.GOLD + p.getName() + ChatColor.YELLOW + " has been eliminated from the " + ChatColor.GOLD
                    + game + ChatColor.YELLOW + " Colosseum! ("
                    + ChatColor.DARK_GREEN + "" + players.size() + ChatColor.YELLOW + " remaining)");

            data.set("colosseums." + game + ".kills." + killer.getUniqueId().toString(),
                    data.getInt("colosseums." + game + ".kills." + killer.getUniqueId().toString()) + 1);
            FactionsColosseum.getInstance().savePlayerData();

            for(Game game1 : gameList) {
                if(game1.name == game) {
                    ScoreboardRunnable.stop(game1);
                }
            }


        }

        if (players.size() < 2) {
            for (Game list : gameList) {

                if(list.name.equals(game)) {
                    list.stop();
                    break;
                }
            }
        }
    }

    public static boolean isPlaying(Player p, String gameName) {

        List<String> players = data.getStringList("colosseums." + gameName + ".joined");
        if (players.contains(p.getUniqueId().toString())) {
            return true;
        } else {
            return false;
        }

    }

    public static String getGamePlaying(Player p) {
        for (String games : data.getConfigurationSection("colosseums").getKeys(false)) {
            if (data.getStringList("colosseums." + games + ".joined").contains(p.getUniqueId().toString())) {
                return games;
            }
        }
        return null;
    }

    public void startCountDown() {

        final List<String> uuids = data.getStringList("colosseums." + name + ".joined");

        status = GameStatus.STARTING;

        for(String names : uuids) {
            Player player = Bukkit.getPlayer(UUID.fromString(names));

            if(!data.getStringList("colosseums." + name + ".joined").contains(player.getUniqueId().toString())) {
                return;
            }

            int minX = data.getInt("colosseums." + name + ".location.x1");
            int Y = data.getInt("colosseums." + name + ".location.y1");
            int minZ = data.getInt("colosseums." + name + ".location.z1");
            int maxX = data.getInt("colosseums." + name + ".location.x2");
            int maxZ = data.getInt("colosseums." + name + ".location.z2");;
            String world = data.getString("colosseums." + name + ".location.world");
            int x = ThreadLocalRandom.current().nextInt(minX, maxX);
            int y = Y;
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + player.getName() + " " + world);

            Location randomLocation = new Location(player.getWorld(), x, y, z);

            player.teleport(randomLocation);

        }

        data.set("colosseums." + name + ".active", true);
        FactionsColosseum.getInstance().savePlayerData();

        countdown = Bukkit.getScheduler().runTaskTimer(FactionsColosseum.getInstance(), new Runnable() {

            @Override
            public void run() {

                for (String names : uuids) {
                    Player registered_player = Bukkit.getPlayer(UUID.fromString(names));

                    if (countdowntime == 0) {
                        registered_player.sendTitle(ChatColor.GREEN + "Colosseum started!", "");
                        registered_player.playSound(registered_player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        start();
                        return;
                    }


                    registered_player.sendTitle(ChatColor.YELLOW + "Starting in:", String.valueOf(countdowntime));
                    registered_player.playSound(registered_player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);

                }
                countdowntime--;
            }
        }, 20L, 20L);

    }

    public void start() {

        status = GameStatus.PLAYING;

        try {
            countdown.cancel();

            ScoreboardRunnable.update(this);

            List<String> uuids = data.getStringList("colosseums." + name + ".joined");


            task = Bukkit.getScheduler().runTaskTimer(FactionsColosseum.getInstance(), new Runnable() {
                @Override
                public void run() {

                    if(gametime == -1) {
                        task.cancel();
                    }

                    for (String s : data.getConfigurationSection("colosseums").getKeys(false)) {

                        List<String> uuids = data.getStringList("colosseums." + name + ".joined");

                        for (String uuid : uuids) {
                            Player registered_player = Bukkit.getPlayer(UUID.fromString(uuid));

                            if (registered_player != null) {
                                registered_player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "Colosseum fight time: " + timeConversion(gametime)));

                            }
                        }
                    }
                    gametime++;
                }
            }, 20, 20);


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void stop() {

        status = GameStatus.IDLE;

        List<String> uuids = data.getStringList("colosseums." + this.name + ".joined");

        Bukkit.broadcastMessage(ChatColor.YELLOW + "Colosseume " + ChatColor.GOLD + name + ChatColor.YELLOW + " has ended!");

        for(String s : uuids) {
            Player p = Bukkit.getPlayer(UUID.fromString(s));
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Winner: " + ChatColor.GOLD + p.getName());
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Kills: " + ChatColor.GOLD + data.getInt("colosseums." + name + ".kills."
                    + p.getUniqueId().toString()));

            for(String command : FactionsColosseum.getInstance().getConfig().getStringList("prize")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", p.getName()));
            }

        }

        gametime = 0;

        data.set("colosseums." + name + ".active", false);

        ScoreboardRunnable.stop(this);

        data.set("colosseums." + name + ".kills", null);

        data.set("colosseums." + name + ".joined", null);
        FactionsColosseum.getInstance().savePlayerData();

        if(this.task != null) {
            task.cancel();
            this.task = null;

        }

        data.set("colosseums." + this.name, null);

        gameList.clear();
        gametime = -1;

        init();





    }

    public static String timeConversion(int totalSeconds) {
        int hours = totalSeconds / 60 / 60;
        int minutes = (totalSeconds - (hoursToSeconds(hours)))
                / 60;
        int seconds = totalSeconds
                - ((hoursToSeconds(hours)) + (minutesToSeconds(minutes)));

        return ChatColor.GOLD + "" + hours + ChatColor.YELLOW + " hours " + ChatColor.GOLD + "" + minutes
                + ChatColor.YELLOW + " minutes " + ChatColor.GOLD + seconds + ChatColor.YELLOW + " seconds";
    }

    public static int hoursToSeconds(int hours) {
        return hours * 60 * 60;
    }

    public static int minutesToSeconds(int minutes) {
        return minutes * 60;
    }

    public static boolean locationIsInCuboid(Player p, Location playerLocation, Location min, Location max) {
        boolean trueOrNot = false;
        if (playerLocation.getWorld() == min.getWorld() && playerLocation.getWorld() == max.getWorld()) {
            if (playerLocation.getX() >= min.getX() && playerLocation.getX() <= max.getX()) {
                if (playerLocation.getY() >= min.getY() && playerLocation.getY() <= max.getY()) {
                    if (playerLocation.getZ() >= min.getZ()
                            && playerLocation.getZ() <= max.getZ()) {
                        trueOrNot = true;
                    }
                }
            }
            if (playerLocation.getX() <= min.getX() && playerLocation.getX() >= max.getX()) {
                if (playerLocation.getY() <= min.getY() && playerLocation.getY() >= max.getY()) {
                    if (playerLocation.getZ() <= min.getZ()
                            && playerLocation.getZ() >= max.getZ()) {
                        trueOrNot = true;
                    }
                }
            }
        }
        return trueOrNot;
    }

}
