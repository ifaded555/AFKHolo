package cz.Faded.AFKHolo;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Main extends JavaPlugin implements Listener {

    ArrayList<Player> afkplayers = new ArrayList<Player>();
    ArmorStand as;

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("afk").setExecutor(this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }

    public void onDisable() {
        afkplayers.clear();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("afk")) {
            Player p = (Player) sender;
            if (p.hasPermission(getConfig().getString("permission").toString())) {
                if (!afkplayers.contains(p)) {
                    afkplayers.add(p);
                    spawnArmorStand(p);
                    Bukkit.broadcastMessage(getConfig().getString("afk_message").replace("&", "§").replace("%player%", p.getName()));
                } else {
                    afkplayers.remove(p);
                    removeArmorStand(p);
                    Bukkit.broadcastMessage(getConfig().getString("come_back_message").replace("&", "§").replace("%player%", p.getName()));
                }
            } else {
                p.sendMessage(getConfig().getString("permission_message").replace("&", "§"));
            }
        }
        return false;
    }


    public void spawnArmorStand(Player p) {
        String w = p.getWorld().getName();
        Location loc = p.getLocation();
        loc.add(0, 0.2, 0);
        as = (ArmorStand) Bukkit.getWorld(w).spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setCanPickupItems(false);
        as.setBasePlate(false);
        as.setVisible(false);
        as.setCustomNameVisible(true);
        as.setCustomName(getConfig().getString("afk_hologram").replace("&", "§").replace("%player%", p.getName()));
    }

    public void removeArmorStand(Player p) {
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getType().equals(EntityType.ARMOR_STAND)) {
                    if (e.getCustomName().equals(getConfig().getString("afk_hologram").replace("&", "§").replace("%player%", p.getName()))) {
                        e.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (afkplayers.contains(e.getPlayer())) {
            Player p = e.getPlayer();
            Location from = e.getFrom();
            Location to = e.getTo();
            double fromx = from.getX();
            double fromy = from.getY();
            double fromz = from.getZ();
            double tox = to.getX();
            double toy = to.getY();
            double toz = to.getZ();
            if ((fromx > tox) || (fromy > toy) || (fromz > toz) || (fromx < tox) || (fromy < toy) || (fromz < toz)) {
                p.teleport(from);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        p.sendMessage(getConfig().getString("move_in_afk_message").replace("&", "§"));
                    }
                }, 20);

                //p.sendMessage(getConfig().getString("move_in_afk_message").replace("&", "§"));
            }
        }

    }


    public void onQuit(PlayerQuitEvent e) {
        if (afkplayers.contains(e.getPlayer())) {
            afkplayers.remove(e.getPlayer());
            removeArmorStand(e.getPlayer());
        }
    }


}
