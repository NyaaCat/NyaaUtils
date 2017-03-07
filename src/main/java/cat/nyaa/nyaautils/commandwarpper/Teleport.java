package cat.nyaa.nyaautils.commandwarpper;

import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaautils.api.events.HamsterEcoHelperTransactionApiEvent;

import java.text.DecimalFormat;

import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import net.ess3.api.IEssentials;
import net.ess3.api.IUser;
import org.bukkit.permissions.PermissionAttachment;

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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (!plugin.cfg.teleportEnable) return;
        String cmd = e.getMessage().toLowerCase().trim();
        Player p = e.getPlayer();
        IUser iu = ess.getUser(p);
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
                callEssHome(p, bedLoc, p.getLocation(), "bed");
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
                Location curLoc = p.getLocation();
                callEssHome(p, homeLoc, curLoc, null);
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
                        Location cl = p.getLocation();
                        callEssHome(p, homeLoc, cl, to);
                        return;
                    }
                }
                PermissionAttachment attachment = p.addAttachment(NyaaUtils.instance, 1);
                attachment.setPermission("essentials.home", true);
                Bukkit.dispatchCommand(p, "essentials:home");
            }
        } else if (cmd.equals("/sethome") || cmd.startsWith("/sethome ")) {
            e.setCancelled(true);
            Location curLoc = p.getLocation();
            World defaultWorld = Bukkit.getWorld(plugin.cfg.setHomeDefaultWorld);
            if (defaultWorld == null) {
                defaultWorld = Bukkit.getWorlds().get(0);
            }
            double fee = plugin.cfg.setHomeMax;
            if (curLoc.getWorld() != defaultWorld) {
                fee += plugin.cfg.setHomeWorld;
                fee -= curLoc.distance(curLoc.getWorld().getSpawnLocation()) * (double) plugin.cfg.setHomeDecrement / plugin.cfg.setHomeDistance;
            } else {
                fee -= curLoc.distance(defaultWorld.getSpawnLocation()) * (double) plugin.cfg.setHomeDecrement / plugin.cfg.setHomeDistance;
            }
            if (fee < plugin.cfg.setHomeMin) fee = plugin.cfg.setHomeMin;
            fee = Double.parseDouble(new DecimalFormat("#.00").format(fee));
            if (!plugin.vaultUtil.enoughMoney(p, fee)) {
                msg(p, "user.teleport.money_insufficient", fee);
                return;
            }
            HamsterEcoHelperTransactionApiEvent event = new HamsterEcoHelperTransactionApiEvent(fee);
            plugin.getServer().getPluginManager().callEvent(event);
            msg(p, "user.teleport.ok", fee, I18n._("user.teleport.sethome"));
            PermissionAttachment attachment = p.addAttachment(NyaaUtils.instance, 1);
            attachment.setPermission("essentials.sethome", true);
            Bukkit.dispatchCommand(p, cmd.substring(1).replace("sethome", "essentials:sethome"));
            plugin.vaultUtil.withdraw(p, fee);
        } else if (cmd.equals("/back")) {
            e.setCancelled(true);
            Location curLoc = p.getLocation();
            Location lastLoc = iu.getLastLocation();
            if (lastLoc == null) {
                msg(p, "user.teleport.no_loc");
                return;
            }
            double fee = plugin.cfg.backBase;
            if (curLoc.getWorld() != lastLoc.getWorld()) {
                fee += plugin.cfg.backWorld;
                fee += lastLoc.distance(lastLoc.getWorld().getSpawnLocation()) * (double) plugin.cfg.backIncrement / plugin.cfg.backDistance;
            } else {
                fee += lastLoc.distance(curLoc) * (double) plugin.cfg.backIncrement / plugin.cfg.backDistance;
            }
            if (fee > plugin.cfg.backMax) fee = plugin.cfg.backMax;
            fee = Double.parseDouble(new DecimalFormat("#.00").format(fee));
            if (!plugin.vaultUtil.enoughMoney(p, fee)) {
                msg(p, "user.teleport.money_insufficient", fee);
                return;
            }
            HamsterEcoHelperTransactionApiEvent event = new HamsterEcoHelperTransactionApiEvent(fee);
            plugin.getServer().getPluginManager().callEvent(event);
            msg(p, "user.teleport.ok", fee, I18n._("user.teleport.back"));
            PermissionAttachment attachment = p.addAttachment(NyaaUtils.instance, 1);
            attachment.setPermission("essentials.back", true);
            Bukkit.dispatchCommand(p, "essentials:back");
            plugin.vaultUtil.withdraw(p, fee);
        }
    }

    private void callEssHome(Player p, Location homeLoc, Location curLoc, String home) {
        double fee = plugin.cfg.homeBase;
        if (homeLoc.getWorld() != curLoc.getWorld()) {
            fee += plugin.cfg.homeWorld;
            fee += homeLoc.distance(homeLoc.getWorld().getSpawnLocation()) * (double) plugin.cfg.homeIncrement / plugin.cfg.homeDistance;
        } else {
            fee += homeLoc.distance(curLoc) * (double) plugin.cfg.homeIncrement / plugin.cfg.homeDistance;
        }
        if (fee > plugin.cfg.homeMax) fee = plugin.cfg.homeMax;
        fee = Double.parseDouble(new DecimalFormat("#.00").format(fee));
        if (!plugin.vaultUtil.enoughMoney(p, fee)) {
            msg(p, "user.teleport.money_insufficient", fee);
            return;
        }
        HamsterEcoHelperTransactionApiEvent event = new HamsterEcoHelperTransactionApiEvent(fee);
        plugin.getServer().getPluginManager().callEvent(event);
        msg(p, "user.teleport.ok", fee, I18n._("user.teleport.home"));
        PermissionAttachment attachment = p.addAttachment(NyaaUtils.instance, 1);
        attachment.setPermission("essentials.home", true);
        Bukkit.dispatchCommand(p, home == null ? "essentials:home" : "essentials:home " + home);
        plugin.vaultUtil.withdraw(p, fee);
    }

    private void msg(CommandSender target, String template, Object... args) {
        target.sendMessage(I18n._(template, args));
    }
}