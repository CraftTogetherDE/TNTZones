package de.crafttogether.tntzones;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerListener implements Listener {

    public PlayerListener(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Remove existing TNTZones if player quits
    public void onPlayerQuit(PlayerQuitEvent e) {
        for (TNTZone zone : TNTZone.getZones()) {
            if (zone.getPlayer() == e.getPlayer())
                zone.remove();
        }
    }
}
