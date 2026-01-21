package cat.nyaa.nyaautils.commandwarpper;

import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.NumberUtil;
import net.ess3.api.IEssentials;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TeleportCmdWarpper implements Listener {
    private IEssentials ess;
    private NyaaUtils plugin;

    public TeleportCmdWarpper(Object pl) {
        this.plugin = (NyaaUtils) pl;
        this.ess = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (plugin.cfg.teleportEnable) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
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
                Location homeLoc = iu.getHome(homes.get(0));
                if (homeLoc == null) {
                    msg(p, "user.teleport.invalid_home");
                    return;
                }
                doHome(p, iu, homeLoc, curLoc);
            } else {
                String to = cmd.substring(5).trim();
                for (String home : homes) {
                    if (home.equals(to)) {
                        Location homeLoc = iu.getHome(to);
                        if (homeLoc == null) {
                            msg(p, "user.teleport.invalid_home");
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
        } else if (cmd.equals("/back") || cmd.startsWith("/back ")) {
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
        if (!ess.getSettings().isTeleportSafetyEnabled()
                && LocationUtil.isBlockUnsafeForUser(ess, iu, curLoc.getWorld(), curLoc.getBlockX(), curLoc.getBlockY(), curLoc.getBlockZ())) {
            msg(p, "user.teleport.unsafe");
            return;
        }

        double fee = 0;
        iu.setHome(name, curLoc);
        msg(p, "user.teleport.ok", fee, I18n.format("user.teleport.sethome"));
    }

    private void doBack(Player p, User iu, Location curLoc, Location lastLoc) {
        if (iu.getWorld() != lastLoc.getWorld() && ess.getSettings().isWorldTeleportPermissions() && !iu.isAuthorized("essentials.worlds." + lastLoc.getWorld().getName())) {
            msg(p, "internal.error.no_required_permission", "essentials.worlds." + lastLoc.getWorld().getName());
            return;
        }

        double fee = 0;
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        iu.getAsyncTeleport().back(new Trade(0, ess), future);
        future.whenComplete((success, throwable) -> {
            if (throwable != null) {
                p.sendMessage(throwable.getMessage());
                return;
            }
            if (Boolean.TRUE.equals(success)) {
                msg(p, "user.teleport.ok", fee, I18n.format("user.teleport.back"));
            } else {
                msg(p, "user.teleport.error");
            }
        });
    }

    private void doHome(Player p, User iu, Location homeLoc, Location curLoc) {
        if (iu.getWorld() != homeLoc.getWorld() && ess.getSettings().isWorldHomePermissions() && !iu.isAuthorized("essentials.worlds." + homeLoc.getWorld().getName())) {
            msg(p, "internal.error.no_required_permission", "essentials.worlds." + homeLoc.getWorld().getName());
            return;
        }

        double fee = 0;
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        iu.getAsyncTeleport().teleport(homeLoc, new Trade(0, ess), PlayerTeleportEvent.TeleportCause.PLUGIN, future);
        future.whenComplete((success, throwable) -> {
            if (throwable != null) {
                p.sendMessage(throwable.getMessage());
                return;
            }
            if (Boolean.TRUE.equals(success)) {
                msg(p, "user.teleport.ok", fee, I18n.format("user.teleport.home"));
            } else {
                msg(p, "user.teleport.error");
            }
        });
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

    private void msg(CommandSender target, String template, Object... args) {
        target.sendMessage(I18n.format(template, args));
    }
}
