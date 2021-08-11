package de.crafttogether.tntzones;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Commands implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = null;
        Boolean denyBuild = true;

        if (sender instanceof Player)
            p = (Player) sender;

        if (command.getName().equalsIgnoreCase("tntzone")) {
            if (p == null) {
                sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
                return true;
            }

            // tp
            if (args.length >= 1 && args[0].equalsIgnoreCase("tp")) {
                if (!p.hasPermission("tntzone.teleport")) {
                    p.sendMessage("§cDazu hast du keine Berechtigung.");
                    return true;
                }

                float x, y, z;
                World world = null;

                try {
                    x = Float.parseFloat(args[1]);
                    y = Float.parseFloat(args[2]);
                    z = Float.parseFloat(args[3]);
                }
                catch (Exception ex) {
                    p.sendMessage("§cDas sind keine gültigen Koordinaten.");
                    return true;
                }

                if (args[4] != null)
                    world = Bukkit.getWorld(args[4]);

                p.sendMessage("§6Du wurdest nach §e" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + " §6teleportiert.");
                p.teleport(new Location(world, x,y, z));

                return true;
            }

            // remove
            else if (args.length >= 1 && args[0].equalsIgnoreCase("remove")) {
                if (args.length == 1) {
                    for (TNTZone zone : TNTZone.getZones()) {
                        if (zone.getPlayer() == p)
                            zone.remove();
                    }

                    p.sendMessage("§2Deine bestehenden TNTZones wurden entfernt.");
                    return true;
                }

                if (!p.hasPermission("tntzone.remove")) {
                    p.sendMessage("§cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args[1] == null) {
                    p.sendMessage("§cEs wurde keine ID angegeben.");
                    return true;
                }

                TNTZone zone = TNTZone.get(args[1]);

                if (zone == null) {
                    p.sendMessage("§cEs wurde keine TNTZone mit der ID " + args[1] + " gefunden.");
                    return true;
                }

                Player owner = zone.getPlayer();
                zone.remove();

                p.sendMessage("§2Du hast eine TNTZone von §6" + owner.getName() + " §2entfernt.");

                return true;
            }

            // list
            else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                if (!p.hasPermission("tntzone.list")) {
                    p.sendMessage("§cDazu hast du keine Berechtigung.");
                    return true;
                }

                Collection<TNTZone> zones = TNTZone.getZones();
                if (zones.size() < 1) {
                    p.sendMessage("§eEs bestehen aktuell keine TNTZonen.");
                    return true;
                }

                for (TNTZone zone : zones) {
                    Location location = zone.getLocation();
                    TextComponent message = new TextComponent("");

                    TextComponent delBtn = new TextComponent("§c[X]");
                    delBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tntzone remove " + zone.getID()));
                    delBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§6TNTZone entfernen")).create()));

                    TextComponent item = new TextComponent("§6" + zone.getPlayer() + " §3(" + Math.round(location.getX())  + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()) + ", " + location.getWorld().getName() + ") §e");
                    item.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tntzone tp " + location.getX() + " " + location.getY() + " " + location.getZ() + " " + location.getWorld().getName()));
                    item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§6Teleportiere zum Mittelpunkt der Zone")).create()));

                    message.addExtra(delBtn);
                    message.addExtra(" ");
                    message.addExtra(item);

                    p.spigot().sendMessage(message);
                    p.sendMessage("§e" + TNTZonesPlugin.formatDateDiff(zone.getRemaining()) + " verbleibend");
                    p.sendMessage("");
                }

                return true;
            }

            // shared
            else if (args.length == 1 && args[0].equalsIgnoreCase("shared")){
                if (!p.hasPermission("tntzone.create.shared")) {
                    p.sendMessage("§cDazu hast du keine Berechtigung.");
                    return true;
                }

                denyBuild = false;
            }

            else if (!p.hasPermission("tntzone.create")) {
                p.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }

            if (TNTZone.isRestricted(p.getLocation())) {
                p.sendMessage("§cDu kannst hier keine TNTZone erstellen.");
                return true;
            }

            if (!p.hasPermission("tntzone.create.multiple")) {
                for (TNTZone zone : TNTZone.getZones()) {
                    if (zone.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                        p.sendMessage("§cDu kannst nicht mehrere TNTZonen gleichzeitig haben.");
                        return true;
                    }
                }
            }
            Bukkit.getLogger().info(denyBuild ? "YES" : "NO" + " 2");
            FileConfiguration config = TNTZonesPlugin.getInstance().getConfiguration();
            new TNTZone(p, config.getInt("Radius"), config.getInt("Delay"), denyBuild);
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> newList = new ArrayList<>();
        List<String> proposals = new ArrayList<>();

        proposals.add("remove");

        if (sender.hasPermission("tntzone.shared"))
            proposals.add("shared");

        if (sender.hasPermission("tntzone.list"))
            proposals.add("list");

        if (args.length < 1 || args[args.length - 1].equals("")) {
            newList = proposals;
        } else {
            for (String value : proposals) {
                if (value.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    newList.add(value);
            }
        }

        return newList;
    }
}
