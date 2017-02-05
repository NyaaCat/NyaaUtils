package cat.nyaa.nyaautils;

import cat.nyaa.nyaautils.api.events.PrefixChangeEvent;
import cat.nyaa.nyaautils.api.events.SuffixChangeEvent;
import cat.nyaa.nyaautils.elytra.ElytraCommands;
import cat.nyaa.nyaautils.enchant.EnchantCommands;
import cat.nyaa.nyaautils.exhibition.ExhibitionCommands;
import cat.nyaa.nyaautils.mailbox.MailboxCommands;
import cat.nyaa.nyaautils.repair.RepairCommands;
import cat.nyaa.nyaautils.timer.TimerCommands;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.ExperienceUtil;
import cat.nyaa.utils.Internationalization;
import cat.nyaa.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CommandHandler extends CommandReceiver<NyaaUtils> {
    private NyaaUtils plugin;

    public CommandHandler(NyaaUtils plugin, Internationalization i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand("exhibition")
    public ExhibitionCommands exhibitionCommands;
    @SubCommand("mailbox")
    public MailboxCommands mailboxCommands;
    @SubCommand("repair")
    public RepairCommands repairCommands;
    @SubCommand("enchant")
    public EnchantCommands enchantCommands;
    @SubCommand("el")
    public ElytraCommands elytraCommands;
    @SubCommand("timer")
    public TimerCommands timerCommands;

    /* Show off the item in player's hand */
    @SubCommand(value = "show", permission = "nu.show")
    public void commandShow(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        new Message("").append(item, I18n._("user.showitem.message", sender.getName())).broadcast();
    }

    /* launch the player into the air and open their elytra */
    @SubCommand(value = "launch", permission = "nu.launch")
    public void commandLaunch(CommandSender sender, Arguments args) {
        if (args.top() == null) {
            sender.sendMessage(I18n._("user.launch.usage"));
        } else {
            double yaw = args.nextDouble();
            double pitch = args.nextDouble();
            double speed = args.nextDouble();
            int delay = args.nextInt();
            int launchSpeed = args.nextInt();
            String pName = args.next();
            if (pName == null) {
                if (sender instanceof Player) {
                    pName = sender.getName();
                } else {
                    sender.sendMessage(I18n._("user.launch.missing_name"));
                    return;
                }
            }
            Player p = Bukkit.getPlayer(pName);
            if (p == null) {
                sender.sendMessage(I18n._("user.launch.player_not_online", pName));
                return;
            }

            ItemStack chestPlate = p.getInventory().getChestplate();
            if (chestPlate == null || chestPlate.getType() != Material.ELYTRA) {
                sender.sendMessage(I18n._("user.launch.not_ready_to_fly_sender"));
                p.sendMessage(I18n._("user.launch.not_ready_to_fly"));
                return;
            }

            new BukkitRunnable() {
                private final static int ELYTRA_DELAY = 3;
                final int d = delay;
                final Vector v = toVector(yaw, pitch, speed);
                final Player player = p;
                int current = 0;


                @Override
                public void run() {
                    if (player.isOnline()) {
                        if (current < d) {
                            current++;
                            player.setVelocity(v);
                            if (current == ELYTRA_DELAY) {
                                player.setGliding(true);
                            }
                        } else {
                            if (!player.isGliding()) {
                                player.setGliding(true);
                            }
                            player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(launchSpeed));
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 1, 1);
        }
    }

    /* launch the player into the air without open their elytra */
    @SubCommand(value = "project", permission = "nu.project")
    public void commandProject(CommandSender sender, Arguments args) {
        if (args.top() == null) {
            sender.sendMessage(I18n._("user.project.usage"));
        } else {
            double yaw = args.nextDouble();
            double pitch = args.nextDouble();
            double speed = args.nextDouble();
            int duration = args.nextInt();
            String pName = args.next();
            if (pName == null) {
                if (sender instanceof Player) {
                    pName = sender.getName();
                } else {
                    sender.sendMessage(I18n._("user.project.missing_name"));
                    return;
                }
            }
            Player p = Bukkit.getPlayer(pName);
            if (p == null) {
                sender.sendMessage(I18n._("user.project.player_not_online", pName));
                return;
            }

            new BukkitRunnable() {
                final int d = duration;
                final Vector v = toVector(yaw, pitch, speed);
                final Player player = p;
                int current = 0;


                @Override
                public void run() {
                    if (player.isOnline()) {
                        if (current < d) {
                            current++;
                            player.setVelocity(v);
                        } else {
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 1, 1);
        }
    }

    private static Vector toVector(double yaw, double pitch, double length) {
        return new Vector(
                Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * length,
                Math.sin(pitch / 180 * Math.PI) * length,
                Math.sin(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * length
        );
    }

    /* Reload the plugin */
    @SubCommand(value = "reload", permission = "nu.reload")
    public void commandReload(CommandSender sender, Arguments args) {
        NyaaUtils p = NyaaUtils.instance;
        p.getServer().getScheduler().cancelTasks(p);
        p.getCommand("nyaautils").setExecutor(null);
        HandlerList.unregisterAll(p);
        p.i18n.reset();
        p.onEnable();
    }

    /* Toggle loot protection ON/OFF for the player */
    @SubCommand(value = "lp", permission = "nu.lootprotect")
    public void commandLootProtectToggle(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (plugin.lpListener.toggleStatus(p.getUniqueId())) {
            p.sendMessage(I18n._("user.lp.turned_on"));
        } else {
            p.sendMessage(I18n._("user.lp.turned_off"));
        }
        p.sendMessage(I18n._("user.lp.mode_" + plugin.cfg.lootProtectMode.name()));
    }

    /* change player's prefix */
    @SubCommand(value = "prefix", permission = "nu.prefix")
    public void commandPrefix(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.prefix.usage");
            return;
        }
        Player p = asPlayer(sender);
        String prefix = args.next().replace("§", "");
        for (String k : plugin.cfg.custom_fixes_prefix_disabledFormattingCodes) {
            if (prefix.toUpperCase().contains("&" + k.toUpperCase())) {
                msg(sender, "user.warn.blocked_format_codes", "&" + k);
                return;
            }
        }
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        for (String k : plugin.cfg.custom_fixes_prefix_blockedWords) {
            if (ChatColor.stripColor(prefix).toUpperCase().contains(k.toUpperCase())) {
                msg(sender, "user.warn.blocked_words", k);
                return;
            }
        }
        if (ChatColor.stripColor(prefix).length() > plugin.cfg.custom_fixes_prefix_maxlength) {
            msg(sender, "user.prefix.prefix_too_long", plugin.cfg.custom_fixes_prefix_maxlength);
            return;
        }
        if (plugin.cfg.custom_fixes_prefix_expCost > 0 &&
                !(p.getTotalExperience() >= plugin.cfg.custom_fixes_prefix_expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }

        if (plugin.cfg.custom_fixes_prefix_moneyCost > 0 &&
                !plugin.vaultUtil.enoughMoney(p, plugin.cfg.custom_fixes_prefix_moneyCost)) {
            msg(sender, "user.warn.no_enough_money");
            return;
        }
        PrefixChangeEvent event = new PrefixChangeEvent(p, plugin.vaultUtil.getPlayerPrefix(p), prefix,
                plugin.cfg.custom_fixes_prefix_expCost, plugin.cfg.custom_fixes_prefix_moneyCost);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        if (plugin.cfg.custom_fixes_prefix_expCost > 0) {
            ExperienceUtil.addPlayerExperience(p, -plugin.cfg.custom_fixes_prefix_expCost);
        }
        plugin.vaultUtil.withdraw(p, plugin.cfg.custom_fixes_prefix_moneyCost);
        plugin.vaultUtil.setPlayerPrefix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_prefix_format).replace("{prefix}", prefix));
        msg(sender, "user.prefix.success", prefix);
    }

    /* change player's suffix */
    @SubCommand(value = "suffix", permission = "nu.suffix")
    public void commandSuffix(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.suffix.usage");
            return;
        }
        Player p = asPlayer(sender);
        String suffix = args.next().replace("§", "");
        for (String k : plugin.cfg.custom_fixes_suffix_disabledFormattingCodes) {
            if (suffix.toUpperCase().contains("&" + k.toUpperCase())) {
                msg(sender, "user.warn.blocked_format_codes", "&" + k);
                return;
            }
        }
        suffix = ChatColor.translateAlternateColorCodes('&', suffix);
        for (String k : plugin.cfg.custom_fixes_suffix_blockedWords) {
            if (ChatColor.stripColor(suffix).toUpperCase().contains(k.toUpperCase())) {
                msg(sender, "user.warn.blocked_words", k);
                return;
            }
        }
        if (ChatColor.stripColor(suffix).length() > plugin.cfg.custom_fixes_suffix_maxlength) {
            msg(sender, "user.suffix.suffix_too_long", plugin.cfg.custom_fixes_suffix_maxlength);
            return;
        }
        if (plugin.cfg.custom_fixes_suffix_expCost > 0 &&
                !(p.getTotalExperience() >= plugin.cfg.custom_fixes_suffix_expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }

        if (plugin.cfg.custom_fixes_suffix_moneyCost > 0 &&
                !plugin.vaultUtil.enoughMoney(p, plugin.cfg.custom_fixes_suffix_moneyCost)) {
            msg(sender, "user.warn.no_enough_money");
            return;
        }
        SuffixChangeEvent event = new SuffixChangeEvent(p, plugin.vaultUtil.getPlayerSuffix(p), suffix,
                plugin.cfg.custom_fixes_suffix_expCost, plugin.cfg.custom_fixes_suffix_moneyCost);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        if (plugin.cfg.custom_fixes_suffix_expCost > 0) {
            ExperienceUtil.addPlayerExperience(p, -plugin.cfg.custom_fixes_suffix_expCost);
        }
        plugin.vaultUtil.withdraw(p, plugin.cfg.custom_fixes_suffix_moneyCost);
        plugin.vaultUtil.setPlayerSuffix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_suffix_format).replace("{suffix}", suffix));
        msg(sender, "user.suffix.success", suffix);
    }

    /* reset player's prefix */
    @SubCommand(value = "resetprefix", permission = "nu.prefix")
    public void commandResetPrefix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(plugin.vaultUtil.getPlayerPrefix(p).length() > 0)) {
            return;
        }

        plugin.vaultUtil.setPlayerPrefix(p, "");
        msg(sender, "user.resetprefix.success");
    }

    /* reset player's suffix */
    @SubCommand(value = "resetsuffix", permission = "nu.suffix")
    public void commandResetSuffix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(plugin.vaultUtil.getPlayerSuffix(p).length() > 0)) {
            return;
        }

        plugin.vaultUtil.setPlayerSuffix(p, "");
        msg(sender, "user.resetsuffix.success");
    }

    /* Print format code reference list */
    @SubCommand(value = "format", permission = "nu.format")
    public void commandFormat(CommandSender sender, Arguments args) {
        msg(sender, "user.format.message");
    }
}
