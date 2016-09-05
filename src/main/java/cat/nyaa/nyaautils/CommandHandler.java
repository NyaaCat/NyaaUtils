package cat.nyaa.nyaautils;

import cat.nyaa.nyaautils.exhibition.ExhibitionCommands;
import cat.nyaa.nyaautils.mailbox.MailboxCommands;
import cat.nyaa.utils.*;
import cat.nyaa.utils.internationalizer.I16rEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

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

    @SubCommand(value = "addenchsrc", permission = "nu.addenchsrc")
    public void commandAddEnchSrc(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        if (BasicItemMatcher.containsMatch(NyaaUtils.instance.cfg.enchantSrc, item)) {
            sender.sendMessage(I18n._("user.enchant.enchantsrc_already_exists"));
            return;
        }
        BasicItemMatcher matcher = new BasicItemMatcher();
        matcher.itemTemplate = item.clone();
        matcher.enchantMatch = BasicItemMatcher.MatchingMode.ARBITRARY;
        matcher.nameMatch = BasicItemMatcher.MatchingMode.EXACT;
        matcher.repairCostMatch = BasicItemMatcher.MatchingMode.EXACT;
        NyaaUtils.instance.cfg.enchantSrc.add(matcher);
        NyaaUtils.instance.cfg.save();
    }

    @SubCommand(value = "enchant", permission = "nu.enchant")
    public void commandEnchant(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (args.top() == null) {
            sender.sendMessage(I18n._("user.enchant.list_ench_header"));
            for (Enchantment e : Enchantment.values()) {
                if (I16rEnchantment.fromEnchantment(e) != null) {
                    Message msg = new Message(e.getName() + ": ");
                    msg.append(I16rEnchantment.fromEnchantment(e).getUnlocalizedName());
                    msg.append(" " + I18n._("user.enchant.max_level", plugin.cfg.enchantMaxLevel.get(e)));
                    p.spigot().sendMessage(msg.inner);
                } else {
                    if (e == null || e.getName() == null || e.getName().equalsIgnoreCase("Custom Enchantment")) {
                        continue;
                    }
                    p.sendMessage(e.getName() + ": " + e.getName() + " " +
                            I18n._("user.enchant.max_level", plugin.cfg.enchantMaxLevel.get(e)));
                }
            }
            sender.sendMessage(I18n._("manual.enchant.usage"));
        } else {
            ItemStack main = getItemInHand(sender);
            ItemStack off = getItemInOffHand(sender);
            if (!BasicItemMatcher.containsMatch(NyaaUtils.instance.cfg.enchantSrc, off)) {
                sender.sendMessage(I18n._("user.enchant.invalid_src"));
                return;
            }

            if (main.getAmount() != 1 || !(main.getType().getMaxDurability() > 0)) {
                sender.sendMessage(I18n._("user.enchant.invalid_item"));
                return;
            }

            String enchStr = args.next().toUpperCase();
            Enchantment ench = Enchantment.getByName(enchStr);
            if (ench == null) {
                sender.sendMessage(I18n._("user.enchant.invalid_ench", enchStr));
                return;
            }

            int level = args.nextInt();

            if (level <= 0 || level > plugin.cfg.enchantMaxLevel.get(ench)) {
                sender.sendMessage(I18n._("user.enchant.invalid_level"));
                return;
            }
            long cooldown = 0;
            if (plugin.enchantCooldown.containsKey(p.getUniqueId())) {
                cooldown = plugin.enchantCooldown.get(p.getUniqueId()) + (plugin.cfg.enchantCooldown / 20 * 1000);
            }

            int chance1 = plugin.cfg.chanceSuccess;
            int chance2 = plugin.cfg.chanceModerate;
            int chance3 = plugin.cfg.chanceFail;
            int chance4 = plugin.cfg.chanceDestroy;
            if (cooldown > System.currentTimeMillis()) {
                chance1 = 0;
            }
            int rand = new Random().nextInt(chance1 + chance2 + chance3 + chance4) + 1;
            boolean success = true;
            boolean deleteItem = true;
            if (chance1 > 0 && rand <= chance1) {
                success = true;
                deleteItem = false;
            } else if (chance2 > 0 && rand <= chance1 + chance2) {
                success = true;
                deleteItem = false;
                level = (int) Math.floor(level / 2);
                if (level == 0) {
                    success = false;
                }
            } else if (chance3 > 0 && rand <= chance1 + chance2 + chance3) {
                success = false;
                deleteItem = false;
            } else if (chance4 > 0 && rand <= chance1 + chance2 + chance3 + chance4) {
                success = false;
                deleteItem = true;
            }

            if (off.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) off.getItemMeta();
                int realLvl = meta.getStoredEnchantLevel(ench);
                if (level > realLvl
                        || (level + main.getEnchantmentLevel(ench) > plugin.cfg.enchantMaxLevel.get(ench))) {
                    sender.sendMessage(I18n._("user.enchant.invalid_level"));
                    return;
                } else {
                    meta.removeStoredEnchant(ench);
                    off.setItemMeta(meta);
                    if (meta.getStoredEnchants().size() == 0) {
                        off = new ItemStack(Material.AIR);
                    }
                }

            } else {
                int realLvl = off.getEnchantmentLevel(ench);
                if (level > realLvl
                        || (level + main.getEnchantmentLevel(ench) > plugin.cfg.enchantMaxLevel.get(ench))) {
                    sender.sendMessage(I18n._("user.enchant.invalid_level"));
                    return;
                } else {
                    off.removeEnchantment(ench);
                    if (off.getEnchantments().size() == 0) {
                        off = new ItemStack(Material.AIR);
                    }
                }
            }
            if (success && level > 0) {
                if (main.getType() == Material.ENCHANTED_BOOK) {
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) main.getItemMeta();
                    int origLvl = meta.getStoredEnchantLevel(ench);
                    meta.addStoredEnchant(ench, origLvl + level, true);
                    main.setItemMeta(meta);
                } else {
                    int origLvl = main.getEnchantmentLevel(ench);
                    main.addUnsafeEnchantment(ench, origLvl + level);
                }
            }
            if (success) {
                p.sendMessage(I18n._("user.enchant.success"));
            } else {
                p.sendMessage(I18n._("user.enchant.fail"));
                if (deleteItem) {
                    main = new ItemStack(Material.AIR);
                }
            }
            plugin.enchantCooldown.put(p.getUniqueId(), System.currentTimeMillis());
            p.getInventory().setItemInMainHand(main);
            p.getInventory().setItemInOffHand(off);
        }
    }

    @SubCommand(value = "show", permission = "nu.show")
    public void commandShow(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        new Message("").append(item, I18n._("user.showitem.message", sender.getName())).broadcast();
    }

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

    @SubCommand(value = "reload", permission = "nu.reload")
    public void commandReload(CommandSender sender, Arguments args) {
        NyaaUtils p = NyaaUtils.instance;
        p.reloadConfig();
        p.cfg.deserialize(p.getConfig());
        p.cfg.serialize(p.getConfig());
        p.saveConfig();
        p.i18n.reset();
        p.i18n.load(p.cfg.language);
    }

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
        ExpUtil.setTotalExperience(p, p.getTotalExperience() - plugin.cfg.custom_fixes_prefix_expCost);
        plugin.vaultUtil.withdraw(p, plugin.cfg.custom_fixes_prefix_moneyCost);
        plugin.vaultUtil.setPlayerPrefix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_prefix_format).replace("{prefix}", prefix));
        msg(sender, "user.prefix.success", prefix);
    }

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
        plugin.vaultUtil.withdraw(p, plugin.cfg.custom_fixes_suffix_moneyCost);
        ExpUtil.setTotalExperience(p, p.getTotalExperience() - plugin.cfg.custom_fixes_suffix_expCost);
        plugin.vaultUtil.setPlayerSuffix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_suffix_format).replace("{suffix}", suffix));
        msg(sender, "user.suffix.success", suffix);
    }

    @SubCommand(value = "resetprefix", permission = "nu.prefix")
    public void commandResetPrefix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(plugin.vaultUtil.getPlayerPrefix(p).length() > 0)) {
            return;
        }

        plugin.vaultUtil.setPlayerPrefix(p, "");
        msg(sender, "user.resetprefix.success");
    }

    @SubCommand(value = "resetsuffix", permission = "nu.suffix")
    public void commandResetSuffix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(plugin.vaultUtil.getPlayerSuffix(p).length() > 0)) {
            return;
        }

        plugin.vaultUtil.setPlayerSuffix(p, "");
        msg(sender, "user.resetsuffix.success");
    }

    @SubCommand(value = "format", permission = "nu.format")
    public void commandFormat(CommandSender sender, Arguments args) {
        msg(sender, "user.format.message");
    }
}
