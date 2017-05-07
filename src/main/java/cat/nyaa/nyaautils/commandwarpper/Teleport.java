package cat.nyaa.nyaautils.commandwarpper;

import cat.nyaa.nyaacore.utils.VaultUtils;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaautils.api.events.HamsterEcoHelperTransactionApiEvent;
import cat.nyaa.ourtown.api.PlayerSpawn;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.NumberUtil;
import net.ess3.api.IEssentials;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.librazy.nyaautils_lang_checker.LangKey;

import java.text.DecimalFormat;
import java.util.List;

public class Teleport implements Listener {
    private IEssentials ess;
    private NyaaUtils plugin;

    public Teleport(Object pl) {
        this.plugin = (NyaaUtils) pl;
        this.ess = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (plugin.cfg.teleportEnable) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

    }

    private static Location PlayerSpawn(OfflinePlayer player, World world) {
        try {
            Location spawn = PlayerSpawn.getPlayerSpawn(player);
            if (spawn.getWorld().getName().equals(world.getName())) {
                return spawn;
            }
        } catch (NoClassDefFoundError ignored) {
        }
        return world.getSpawnLocation();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (!plugin.cfg.teleportEnable) return;
        String cmd = e.getMessage().toLowerCase().trim();
        Player p = e.getPlayer();
        User iu = ess.getUser(p);
        Location curLoc = p.getLocation();
        if (cmd.equals("/home") || cmd.startsWith("/home ")) {
            e.setCancelled(true);
            List<String> homes = iu.getHomes();
            //For /home bed
            if (cmd.equals("/home bed") || (cmd.equals("/home") && homes.size() < 1)) {
                Location bedLoc = p.getBedSpawnLocation();
                if (bedLoc == null) {
                    msg(p, "user.teleport.bed_not_set_yet");
                    return;
                }
                doHome(p, iu, bedLoc, curLoc);
                return;
            }

            if (homes.size() < 1) {
                msg(p, "user.teleport.not_set_yet");
            } else if (homes.size() == 1 && cmd.equals("/home")) {
                Location homeLoc;
                try {
                    homeLoc = iu.getHome(homes.get(0));
                } catch (InvalidWorldException ex) {
                    msg(p, "user.teleport.invalid_home");
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    msg(p, "user.teleport.error");
                    return;
                }
                doHome(p, iu, homeLoc, curLoc);
            } else {
                String to = cmd.substring(5).trim();
                for (String home : homes) {
                    if (home.equals(to)) {
                        Location homeLoc;
                        try {
                            homeLoc = iu.getHome(to);
                        } catch (InvalidWorldException ex) {
                            msg(p, "user.teleport.invalid_home");
                            return;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            msg(p, "user.teleport.error");
                            return;
                        }
                        doHome(p, iu, homeLoc, curLoc);
                        return;
                    }
                }
                msg(p, "user.teleport.homes", String.join(", ", homes.toArray(new String[0])));
            }
        } else if (cmd.equals("/sethome") || cmd.startsWith("/sethome ")) {
            e.setCancelled(true);
            String name = cmd.replace("/sethome", "").trim();
            if (name.equals("")) {
                name = "home";
            }
            doSetHome(p, iu, curLoc, name);
        } else if (cmd.equals("/back")) {
            e.setCancelled(true);
            Location lastLoc = iu.getLastLocation();
            if (lastLoc == null) {
                msg(p, "user.teleport.no_loc");
                return;
            }
            doBack(p, iu, curLoc, lastLoc);
        }
    }

    private void doSetHome(Player p, User iu, Location curLoc, String name) {
        int n = checkHomeLimit(iu, name);
        if (n == 1) {
            if (!name.equals("home"))
                msg(p, "user.teleport.home_limit_one");
            name = "home";
        } else if (n != 0) {
            msg(p, "user.teleport.home_limit", n);
            return;
        }
        if ("bed".equals(name) || NumberUtil.isInt(name)) {
            msg(p, "user.teleport.invalid_name");
            return;
        }
        if (!ess.getSettings().isTeleportSafetyEnabled() && LocationUtil.isBlockUnsafeForUser(iu, curLoc.getWorld(), curLoc.getBlockX(), curLoc.getBlockY(), curLoc.getBlockZ())) {
            msg(p, "user.teleport.unsafe");
            return;
        }

        double fee = plugin.cfg.setHomeMax;
        World defaultWorld = Bukkit.getWorld(plugin.cfg.setHomeDefaultWorld);
        if (defaultWorld == null) {
            defaultWorld = Bukkit.getWorlds().get(0);
        }
        if (curLoc.getWorld() != defaultWorld) {
            fee += plugin.cfg.setHomeWorld;
            fee -= curLoc.distance(PlayerSpawn(p, curLoc.getWorld())) * (double) plugin.cfg.setHomeDecrement / plugin.cfg.setHomeDistance;
        } else {
            fee -= curLoc.distance(PlayerSpawn(p, defaultWorld)) * (double) plugin.cfg.setHomeDecrement / plugin.cfg.setHomeDistance;
        }
        if (fee < plugin.cfg.setHomeMin) fee = plugin.cfg.setHomeMin;
        fee = Double.parseDouble(new DecimalFormat("#.00").format(fee));
        if (!VaultUtils.withdraw(p, fee)) {
            msg(p, "user.teleport.money_insufficient", fee);
            return;
        }
        iu.setHome(name, curLoc);
        msg(p, "user.teleport.ok", fee, I18n.format("user.teleport.sethome"));
        HamsterEcoHelperTransactionApiEvent event = new HamsterEcoHelperTransactionApiEvent(fee);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    private void doBack(Player p, User iu, Location curLoc, Location lastLoc) {
        if (iu.getWorld() != lastLoc.getWorld() && ess.getSettings().isWorldTeleportPermissions() && !iu.isAuthorized("essentials.worlds." + lastLoc.getWorld().getName())) {
            msg(p, "internal.error.no_required_permission", "essentials.worlds." + lastLoc.getWorld().getName());
            return;
        }

        double fee = plugin.cfg.backBase;
        if (curLoc.getWorld() != lastLoc.getWorld()) {
            fee += plugin.cfg.backWorld;
            fee += lastLoc.distance(PlayerSpawn(p, lastLoc.getWorld())) * (double) plugin.cfg.backIncrement / plugin.cfg.backDistance;
        } else {
            fee += lastLoc.distance(curLoc) * (double) plugin.cfg.backIncrement / plugin.cfg.backDistance;
        }
        if (fee > plugin.cfg.backMax) fee = plugin.cfg.backMax;
        fee = Double.parseDouble(new DecimalFormat("#.00").format(fee));
        if (!VaultUtils.withdraw(p, fee)) {
            msg(p, "user.teleport.money_insufficient", fee);
            return;
        }
        try {
            iu.getTeleport().back(new Trade(0, ess));
            msg(p, "user.teleport.ok", fee, I18n.format("user.teleport.back"));
            HamsterEcoHelperTransactionApiEvent event = new HamsterEcoHelperTransactionApiEvent(fee);
            plugin.getServer().getPluginManager().callEvent(event);
        } catch (Exception e) {
            VaultUtils.deposit(p, fee);
            p.sendMessage(e.getMessage());
        }
    }

    private void doHome(Player p, User iu, Location homeLoc, Location curLoc) {
        if (iu.getWorld() != homeLoc.getWorld() && ess.getSettings().isWorldHomePermissions() && !iu.isAuthorized("essentials.worlds." + homeLoc.getWorld().getName())) {
            msg(p, "internal.error.no_required_permission", "essentials.worlds." + homeLoc.getWorld().getName());
            return;
        }

        double fee = plugin.cfg.homeBase;
        if (homeLoc.getWorld() != curLoc.getWorld()) {
            fee += plugin.cfg.homeWorld;
            fee += homeLoc.distance(PlayerSpawn(p, homeLoc.getWorld())) * (double) plugin.cfg.homeIncrement / plugin.cfg.homeDistance;
        } else {
            fee += homeLoc.distance(curLoc) * (double) plugin.cfg.homeIncrement / plugin.cfg.homeDistance;
        }
        if (fee > plugin.cfg.homeMax) fee = plugin.cfg.homeMax;
        fee = Double.parseDouble(new DecimalFormat("#.00").format(fee));
        if (!VaultUtils.withdraw(p, fee)) {
            msg(p, "user.teleport.money_insufficient", fee);
            return;
        }
        try {
            iu.getTeleport().teleport(homeLoc, new Trade(0, ess), PlayerTeleportEvent.TeleportCause.PLUGIN);
            msg(p, "user.teleport.ok", fee, I18n.format("user.teleport.home"));
            HamsterEcoHelperTransactionApiEvent event = new HamsterEcoHelperTransactionApiEvent(fee);
            plugin.getServer().getPluginManager().callEvent(event);
        } catch (Exception e) {
            VaultUtils.deposit(p, fee);
            p.sendMessage(e.getMessage());
        }
    }

    private int checkHomeLimit(final User user, String name) {
        if (!user.isAuthorized("essentials.sethome.multiple.unlimited")) {
            int limit = ess.getSettings().getHomeLimit(user);
            if (user.getHomes().size() == limit && user.getHomes().contains(name)) {
                return 0;
            }
            if (user.getHomes().size() >= limit) {
                return limit;
            }
            if (limit == 1) {
                return 1;
            }
        }
        return 0;
    }

    private void msg(CommandSender target, @LangKey String template, Object... args) {
        target.sendMessage(I18n.format(template, args));
    }
}