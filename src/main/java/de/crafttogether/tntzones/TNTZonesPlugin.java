package de.crafttogether.tntzones;

import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TNTZonesPlugin extends JavaPlugin {
    private static TNTZonesPlugin plugin;
    private FileConfiguration config;

    public TNTZonesPlugin() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();

        if (pm.getPermission("tntzone.create") == null)
            Bukkit.getServer().getPluginManager().addPermission(new Permission("tntzone.create"));

        if (pm.getPermission("tntzone.remove") == null)
            Bukkit.getServer().getPluginManager().addPermission(new Permission("tntzone.remove"));

        if (pm.getPermission("tntzone.create.multiple") == null)
            Bukkit.getServer().getPluginManager().addPermission(new Permission("tntzone.create.multiple"));

        if (pm.getPermission("tntzone.create.shared") == null)
            Bukkit.getServer().getPluginManager().addPermission(new Permission("tntzone.create.shared"));

        if (pm.getPermission("tntzone.inform") == null)
            Bukkit.getServer().getPluginManager().addPermission(new Permission("tntzone.inform"));

        if (pm.getPermission("tntzone.list") == null)
            Bukkit.getServer().getPluginManager().addPermission(new Permission("tntzone.list"));

        if (pm.getPermission("tntzone.teleport") == null)
            Bukkit.getServer().getPluginManager().addPermission(new Permission("tntzone.teleport"));

        saveDefaultConfig();
        config = getConfig();

        registerCommand("tntzone", new Commands());
    }

    @Override
    public void onDisable() {
        TNTZone.removeAll();
    }

    public void registerCommand(String cmd, TabExecutor executor) {
        this.getCommand(cmd).setExecutor(executor);
        this.getCommand(cmd).setTabCompleter(executor);
    }

    public static String formatDateDiff(int seconds) {
        if (seconds <= 0)
            return "Jetzt";

        long minute = seconds / 60;
        seconds = seconds % 60;
        long hour = minute / 60;
        minute = minute % 60;
        long day = hour / 24;
        hour = hour % 24;

        StringBuilder sb = new StringBuilder();
        if (day != 0) {
            if (day > 1)
                sb.append(day).append(" Tage, ");
            else
                sb.append(day).append(" Tag, ");
        }
        if (hour != 0) {
            if (hour > 1)
                sb.append(hour).append(" Stunden, ");
            else
                sb.append(hour).append(" Stunde, ");
        }
        if (minute != 0) {
            if (minute > 1)
                sb.append(minute).append(" Minuten und ");
            else
                sb.append(minute).append(" Minute und ");
        }
        if (seconds != 0) {
            if (seconds > 1)
                sb.append(seconds).append(" Sekunden,");
            else
                sb.append(seconds).append(" Sekunde, ");
        }

        return sb.toString().trim();
    }

    public void reload() {
        this.reloadConfig();
        this.config = getConfig();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public static TNTZonesPlugin getInstance() {
        return plugin;
    }
}
