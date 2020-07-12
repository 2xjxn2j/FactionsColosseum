package org.jacob.spigot.plugins.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> tab = new ArrayList<>();

        if(s.equalsIgnoreCase("c")) {
            if(strings.length == 1) {
                tab.add("create");
                tab.add("delete");
                tab.add("join");
                tab.add("leave");
                tab.add("start");

            }
        }

        return tab;
    }
}
