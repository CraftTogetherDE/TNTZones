package de.crafttogether.tntzones;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TNTZone extends BukkitRunnable {
    private static ConcurrentHashMap<String, TNTZone> storage = new ConcurrentHashMap<String, TNTZone>();

    private String id;
    private String regionName;
    private Player player;

    private Integer radius;
    private Integer remaining;
    private Location location;
    private boolean denyBuild;

    private ProtectedRegion region;
    private BukkitRunnable removeTask;

    public TNTZone (Player player, Integer radius, Integer remaining, Boolean denyBuild) {
        this.id = UUID.randomUUID().toString();
        this.regionName = player.getName() + "/" + id;
        this.player = player;
        this.location = player.getLocation();
        this.radius = radius;
        this.remaining = remaining;
        this.denyBuild = denyBuild;
        create();
    }

    private void create() {
        Bukkit.getLogger().info(denyBuild ? "YES" : "NO" + " 1");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));

        BlockVector3 min = BlockVector3.at(location.getX() - radius, location.getY() - radius, location.getZ() - radius);
        BlockVector3 max = BlockVector3.at(location.getX() + radius, location.getY() + radius, location.getZ() + radius);
        region = new ProtectedCuboidRegion(regionName, min, max);

        region.setFlag(Flags.TNT, StateFlag.State.ALLOW);

        if (!denyBuild)
            region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);

        region.setPriority(5000);

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        DefaultDomain domain = new DefaultDomain();
        domain.addPlayer(localPlayer);
        region.setOwners(domain);

        manager.addRegion(region);

        // Start timer
        runTaskTimerAsynchronously(TNTZonesPlugin.getInstance(), 20L, 20L);
        storage.put(id, this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            Location pLoc = p.getLocation();

            if (p == player)
                continue;

            if (p.hasPermission("tntzone.inform") || region.contains(BlockVector3.at(pLoc.getX(), pLoc.getY(), pLoc.getZ()))) {
                p.sendMessage("§c§lAchtung§r: §e" + player.getName() + " §6hat eine" + (denyBuild ? "": " §4ÖFFENTLICHE") + " §c§lTNTZone §6erstellt.");

                TextComponent message = new TextComponent("§6Für die nächsten §e" + TNTZonesPlugin.formatDateDiff(remaining) + " §6ist §cTNT aktiviert. §2[" + Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()) + "]");

                if (p.hasPermission("tntzone.teleport")) {
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tntzone tp " + location.getX() + " " + location.getY() + " " + location.getZ() + " " + location.getWorld().getName()));
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§6Teleportiere zum Mittelpunkt der Zone")).create()));
                }

                p.spigot().sendMessage(message);
                p.sendMessage("§6Dies betrifft einen Bereich von §e" + radius + " Blöcken§6 in jede Richtung.");

                if (!denyBuild)
                    p.sendMessage("§4§lWarnung: §cIn dieser Region kann nun jeder TNT zünden!");
            }
        }

        player.sendMessage("§c§lAchtung§r: §6Du hast eine" + (denyBuild ? "" : " §4ÖFFENTLICHE") + " §c§lTNTZone §6erstellt!");
        player.sendMessage("§6Für die nächsten §e" + TNTZonesPlugin.formatDateDiff(remaining) + " §6ist §cTNT aktiviert.");
        player.sendMessage("§6Dies betrifft einen Bereich von §e" + radius + " Blöcken§6 in jede Richtung.");

        if (!denyBuild) {
            player.sendMessage("");
            player.sendMessage("§4§lWarnung: §cIn dieser Region kann nun jeder TNT zünden!");
            player.sendMessage("§4§lWarnung: §cDu trägst die Verantwortung für möglicherweise entstehende Schäden!");
        }
    }

    public void remove() {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));
        manager.removeRegion(regionName);

        this.cancel();
        storage.remove(id);

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getUniqueId().equals(player.getUniqueId()))
                player.sendMessage("§2Deine temporäre §6TNTZone §2wurde aufgehoben.");
        }
    }

    @Override
    public void run() {
        remaining--;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cDeine TNTZone verbleibt noch §e" + TNTZonesPlugin.formatDateDiff(remaining)));

        if (remaining < 1)
            remove();
    }

    public static boolean isRestricted(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));
        ApplicableRegionSet applicableRegions = manager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));

        Bukkit.getLogger().info("Check if location[" + location.getX() + ", " + location.getY() + ", " + location.getZ() + "] is inside of restricted region");

        for (ProtectedRegion region : applicableRegions) {
            if (region.getFlag(Flags.BUILD) != StateFlag.State.ALLOW && region.getFlag(Flags.TNT) != StateFlag.State.ALLOW)
                return true;
        }

        return false;
    }

    public Player getPlayer() {
        return player;
    }


    public String getID() {
        return id;
    }

    public String getRegionName() {
        return regionName;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isDenyBuild() {
        return denyBuild;
    }

    public static Collection<TNTZone> getZones() {
        return storage.values();
    }

    public static TNTZone get(String id) {
        return storage.get(id);
    }

    public static void removeAll() {
        for (Map.Entry<String, TNTZone> entry : storage.entrySet())
            entry.getValue().remove();
    }

    public static void remove(String id) {
        TNTZone zone = storage.get(id);
        if (zone != null)
            zone.remove();
    }
}
