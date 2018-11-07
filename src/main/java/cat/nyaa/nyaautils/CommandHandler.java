package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.ExperienceUtils;
import cat.nyaa.nyaacore.utils.PlayerUtils;
import cat.nyaa.nyaacore.utils.VaultUtils;
import cat.nyaa.nyaautils.elytra.ElytraCommands;
import cat.nyaa.nyaautils.enchant.EnchantCommands;
import cat.nyaa.nyaautils.exhibition.ExhibitionCommands;
import cat.nyaa.nyaautils.expcapsule.ExpCapsuleCommands;
import cat.nyaa.nyaautils.lootprotect.LootProtectListener;
import cat.nyaa.nyaautils.mailbox.MailboxCommands;
import cat.nyaa.nyaautils.particle.ParticleCommands;
import cat.nyaa.nyaautils.realm.RealmCommands;
import cat.nyaa.nyaautils.repair.RepairCommands;
import cat.nyaa.nyaautils.signedit.SignEditCommands;
import cat.nyaa.nyaautils.timer.TimerCommands;
import cat.nyaa.nyaautils.vote.VoteTask;
import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler extends CommandReceiver {
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
    @SubCommand("realm")
    public RealmCommands realmCommands;
    @SubCommand("particle")
    public ParticleCommands particleCommands;
    @SubCommand("se")
    public SignEditCommands signEditCommands;
    @SubCommand("expcap")
    public ExpCapsuleCommands expCapsuleCommands;

    private NyaaUtils plugin;
    private boolean suppressNextCompleteMessage = false;

    public CommandHandler(NyaaUtils plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    private static Vector toVector(double yaw, double pitch, double length) {
        return new Vector(
                Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * length,
                Math.sin(pitch / 180 * Math.PI) * length,
                Math.sin(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * length
        );
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    /* Show off the item in player's hand */
    @SubCommand(value = "show", permission = "nu.show")
    public void commandShow(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        new Message("").append(I18n.format("user.showitem.message", sender.getName()), item).broadcast();
    }

    /* launch the player into the air and open their elytra */
    @SubCommand(value = "launch", permission = "nu.launch")
    public void commandLaunch(CommandSender sender, Arguments args) {
        if (args.top() == null) {
            sender.sendMessage(I18n.format("user.launch.usage"));
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
                    sender.sendMessage(I18n.format("user.launch.missing_name"));
                    return;
                }
            }
            Player p = Bukkit.getPlayer(pName);
            if (p == null) {
                sender.sendMessage(I18n.format("user.launch.player_not_online", pName));
                return;
            }

            ItemStack chestPlate = p.getInventory().getChestplate();
            if (chestPlate == null || chestPlate.getType() != Material.ELYTRA) {
                sender.sendMessage(I18n.format("user.launch.not_ready_to_fly_sender"));
                p.sendMessage(I18n.format("user.launch.not_ready_to_fly"));
                return;
            }

            new BukkitRunnable() {
                private final static int ELYTRA_DELAY = 3;
                final int d = delay;
                final Vector v = toVector(yaw, pitch, speed);
                final Player player = p;
                int current = 0;
                boolean stopped = false;

                @Override
                public void run() {
                    if (player.isOnline() && !stopped) {
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
                            stopped = true;
                        }
                    } else {
                        cancel();
                        stopped = true;
                    }
                }
            }.runTaskTimer(plugin, 1, 1);
        }
    }

    /* launch the entity into the air without open their elytra */
    @SubCommand(value = "project", permission = "nu.project")
    public void commandProject(CommandSender sender, Arguments args) {
        if (args.top() == null) {
            sender.sendMessage(I18n.format("user.project.usage"));
            return;
        }
        double yaw = args.nextDouble();
        double pitch = args.nextDouble();
        double speed = args.nextDouble();
        int duration = args.nextInt();
        String pName = args.next();
        Entity ent;
        final Entity p;
        UUID uid;
        if (pName == null) {
            if (sender instanceof Player) {
                pName = ((Player) sender).getUniqueId().toString();
            } else {
                sender.sendMessage(I18n.format("user.project.missing_name"));
                return;
            }
        }
        try {
            uid = UUID.fromString(pName);
            ent = Bukkit.getEntity(uid);
        } catch (Exception e) {
            ent = Bukkit.getPlayer(pName);
            if (ent == null) {
                sender.sendMessage(I18n.format("user.project.player_not_online", pName));
                return;
            }
        }
        p = ent;

        new BukkitRunnable() {
            final int d = duration;
            final Vector v = toVector(yaw, pitch, speed);
            final Entity entity = p;
            int current = 0;
            boolean stopped = false;

            @Override
            public void run() {
                if (entity != null && entity.isValid() && (!(entity instanceof Player) || ((Player) entity).isOnline()) && !stopped) {
                    if (current < d) {
                        current++;
                        entity.setVelocity(v);
                    } else {
                        cancel();
                        stopped = true;
                    }
                } else {
                    cancel();
                    stopped = true;
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    /* Reload the plugin */
    @SubCommand(value = "reload", permission = "nu.reload")
    public void commandReload(CommandSender sender, Arguments args) {
        NyaaUtils p = NyaaUtils.instance;
        p.getServer().getScheduler().cancelTasks(p);
        p.getCommand("nyaautils").setExecutor(null);
        HandlerList.unregisterAll(p);
        p.onEnable();
    }

    /* Toggle drop protection ON/OFF for the player */
    @SubCommand(value = "dp", permission = "nu.dropprotect")
    public void commandDropProtectToggle(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (plugin.dpListener.toggleStatus(p.getUniqueId())) {
            p.sendMessage(I18n.format("user.dp.turned_on"));
        } else {
            p.sendMessage(I18n.format("user.dp.turned_off"));
        }
        p.sendMessage(I18n.format("user.dp.mode_" + plugin.cfg.dropProtectMode.name(), plugin.cfg.dropProtectSecond));
    }

    /* Toggle loot protection ON/OFF for the player */
    @SubCommand(value = "lp", permission = "nu.lootprotect")
    public void commandLootProtectToggle(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        String sub = args.next();
        if (sub == null) {
            if (plugin.lpListener.toggleStatus(p.getUniqueId())) {
                p.sendMessage(I18n.format("user.lp.turned_on"));
            } else {
                p.sendMessage(I18n.format("user.lp.turned_off"));
            }
        } else {
            switch (sub) {
                case "ig":
                case "ignorevanilla":
                    plugin.lpListener.setVanillaStrategy(p.getUniqueId(), LootProtectListener.VanillaStrategy.IGNORE);
                    p.sendMessage(I18n.format("user.lp.ignore_vanilla"));
                    break;
                case "re":
                case "rejectvanilla":
                    plugin.lpListener.setVanillaStrategy(p.getUniqueId(), LootProtectListener.VanillaStrategy.REJECT);
                    p.sendMessage(I18n.format("user.lp.reject_vanilla"));
                    break;
                case "ac":
                case "acceptvanilla":
                case "includevanilla":
                    plugin.lpListener.setVanillaStrategy(p.getUniqueId(), LootProtectListener.VanillaStrategy.ACCEPT);
                    p.sendMessage(I18n.format("user.lp.include_vanilla"));
                    break;
            }
        }
        p.sendMessage(I18n.format("user.lp.mode_" + plugin.cfg.lootProtectMode.name()));
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
                    !(ExperienceUtils.getExpPoints(p) >= plugin.cfg.custom_fixes_prefix_expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }
        if (plugin.cfg.custom_fixes_prefix_moneyCost > 0) {
            if (!VaultUtils.enoughMoney(p, plugin.cfg.custom_fixes_prefix_moneyCost)) {
                msg(sender, "user.warn.no_enough_money");
                return;
            }
            if (plugin.systemBalance != null) {
                plugin.systemBalance.deposit(plugin.cfg.custom_fixes_prefix_moneyCost, plugin);
            }
        }
        if (plugin.cfg.custom_fixes_prefix_expCost > 0) {
            ExperienceUtils.subtractExpPoints(p, plugin.cfg.custom_fixes_prefix_expCost);
        }
        VaultUtils.withdraw(p, plugin.cfg.custom_fixes_prefix_moneyCost);
        VaultUtils.setPlayerPrefix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_prefix_format).replace("{prefix}", prefix), hasPexOrLp());
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
                    !(ExperienceUtils.getExpPoints(p) >= plugin.cfg.custom_fixes_suffix_expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }
        if (plugin.cfg.custom_fixes_suffix_moneyCost > 0) {
            if (!VaultUtils.enoughMoney(p, plugin.cfg.custom_fixes_suffix_moneyCost)) {
                msg(sender, "user.warn.no_enough_money");
                return;
            }
            if (plugin.systemBalance != null) {
                plugin.systemBalance.deposit(plugin.cfg.custom_fixes_suffix_moneyCost, plugin);
            }
        }
        if (plugin.cfg.custom_fixes_suffix_expCost > 0) {
            ExperienceUtils.subtractExpPoints(p, plugin.cfg.custom_fixes_suffix_expCost);
        }
        VaultUtils.withdraw(p, plugin.cfg.custom_fixes_suffix_moneyCost);
        VaultUtils.setPlayerSuffix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_suffix_format).replace("{suffix}", suffix), hasPexOrLp());
        msg(sender, "user.suffix.success", suffix);
    }

    /* reset player's prefix */
    @SubCommand(value = "resetprefix", permission = "nu.prefix")
    public void commandResetPrefix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(VaultUtils.getPlayerPrefix(p).length() > 0)) {
            return;
        }

        VaultUtils.setPlayerPrefix(p, "", hasPexOrLp());
        msg(sender, "user.resetprefix.success");
    }

    /* reset player's suffix */
    @SubCommand(value = "resetsuffix", permission = "nu.suffix")
    public void commandResetSuffix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(VaultUtils.getPlayerSuffix(p).length() > 0)) {
            return;
        }

        VaultUtils.setPlayerSuffix(p, "", hasPexOrLp());
        msg(sender, "user.resetsuffix.success");
    }

    /* Print format code reference list */
    @SubCommand(value = "format", permission = "nu.format")
    public void commandFormat(CommandSender sender, Arguments args) {
        msg(sender, "user.format.message");
    }

    @SubCommand(value = "rename", permission = "nu.rename")
    public void rename(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.rename.usage");
            return;
        }
        Player p = asPlayer(sender);
        String name = args.next().replace("§", "");
        if (plugin.cfg.renameCharacterLimit != 0 && ChatColor.stripColor(name).length() > NyaaUtils.instance.cfg.renameCharacterLimit) {
            msg(p, "user.rename.name_too_long", name, NyaaUtils.instance.cfg.renameCharacterLimit);
            return;
        }
        for (String k : plugin.cfg.renameDisabledFormattingCodes) {
            if (name.toUpperCase().contains("&" + k.toUpperCase()) && !p.hasPermission("nu.rename.blacklist")) {
                msg(p, "user.warn.blocked_format_codes", "&" + k);
                return;
            }
        }
        name = ChatColor.translateAlternateColorCodes('&', name);
        for (String k : plugin.cfg.renameBlockedWords) {
            if (ChatColor.stripColor(name).toUpperCase().contains(k.toUpperCase()) && !p.hasPermission("nu.rename.blacklist")) {
                msg(p, "user.warn.blocked_words", k);
                return;
            }
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().equals(Material.AIR)) {
            msg(sender, "user.info.no_item_hand");
            return;
        }
        int num = item.getAmount();
        for (String sab : plugin.cfg.renameBlockedMaterials) {
            if (Material.matchMaterial(sab) == item.getType() && !p.hasPermission("nu.rename.blacklist")) {
                msg(sender, "user.warn.blocked_materials", sab);
                return;
            }
        }
        int expCost = plugin.cfg.renameExpCostBase + plugin.cfg.renameExpCostPer * num;
        int moneyCost = plugin.cfg.renameMoneyCostBase + plugin.cfg.renameMoneyCostPer * num;
        if (expCost > 0 &&
                    !(ExperienceUtils.getExpPoints(p) >= expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }
        if (moneyCost > 0) {
            if (!VaultUtils.enoughMoney(p, moneyCost)) {
                msg(sender, "user.warn.no_enough_money");
                return;
            }
            if (plugin.systemBalance != null) {
                plugin.systemBalance.deposit(moneyCost, plugin);
            }
        }
        ItemMeta itemStackMeta = item.getItemMeta();
        itemStackMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(itemStackMeta);
        if (expCost > 0) {
            ExperienceUtils.subtractExpPoints(p, expCost);
        }
        VaultUtils.withdraw(p, moneyCost);
        msg(sender, "user.rename.success", name);
    }

    @SubCommand(value = "setlore", permission = "nu.setlore")
    public void setlore(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.setlore.usage");
            return;
        }
        String lore = args.next().replace("§", "");
        Player p = asPlayer(sender);
        lore = ChatColor.translateAlternateColorCodes('&', lore);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().equals(Material.AIR)) {
            msg(sender, "user.info.no_item_hand");
            return;
        }
        String[] line = lore.split("/n");
        List<String> lines = new ArrayList<>();
        for (String s : line) {
            lines.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        ItemMeta itemStackMeta = item.getItemMeta();
        itemStackMeta.setLore(lines);
        item.setItemMeta(itemStackMeta);
        msg(sender, "user.setlore.success", lore);
    }

    @SubCommand(value = "addlore", permission = "nu.setlore")
    public void addlore(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.addlore.usage");
            return;
        }
        String lore = args.next().replace("§", "");
        Player p = asPlayer(sender);
        lore = ChatColor.translateAlternateColorCodes('&', lore);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().equals(Material.AIR)) {
            msg(sender, "user.info.no_item_hand");
            return;
        }
        String[] line = lore.split("/n");
        List<String> lines = item.getItemMeta().getLore() == null ? new ArrayList<>() : item.getItemMeta().getLore();
        for (String s : line) {
            lines.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        ItemMeta itemStackMeta = item.getItemMeta();
        itemStackMeta.setLore(lines);
        item.setItemMeta(itemStackMeta);
        msg(sender, "user.setlore.success", lore);
    }

    @SubCommand(value = "setbookauthor", permission = "nu.setbook")
    public void setbookauthor(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.setbookauthor.usage");
            return;
        }
        String author = args.next();
        Player p = asPlayer(sender);
        author = ChatColor.translateAlternateColorCodes('&', author);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.getType().equals(Material.WRITTEN_BOOK)) {
            msg(sender, "user.setbook.no_book");
            return;
        }
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setAuthor(author);
        item.setItemMeta(meta);
        msg(sender, "user.setbook.success");
    }

    @SubCommand(value = "setbooktitle", permission = "nu.setbook")
    public void setbooktitle(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.setbooktitle.usage");
            return;
        }
        String title = args.next();
        Player p = asPlayer(sender);
        title = ChatColor.translateAlternateColorCodes('&', title);

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.getType().equals(Material.WRITTEN_BOOK)) {
            msg(sender, "user.setbook.no_book");
            return;
        }
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle(title);
        item.setItemMeta(meta);
        msg(sender, "user.setbook.success");
    }

    @SubCommand(value = "setbookunsigned", permission = "nu.setbook")
    public void setbookunsigned(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.getType().equals(Material.WRITTEN_BOOK)) {
            msg(sender, "user.setbook.no_book");
            return;
        }
        BookMeta meta = (BookMeta) item.getItemMeta();
        ItemStack newbook = new ItemStack(Material.WRITABLE_BOOK, 1);
        BookMeta newbookMeta = (BookMeta) newbook.getItemMeta();
        newbookMeta.setPages(meta.getPages());
        newbook.setItemMeta(newbookMeta);
        p.getInventory().setItemInMainHand(newbook);
        msg(sender, "user.setbook.success");
    }

    private boolean hasPexOrLp() {
        return plugin.getServer().getPluginManager().getPlugin("PermissionsEx") != null || plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null;
    }

    @SubCommand(value = "vote", permission = "nu.vote")
    public void vote(CommandSender sender, Arguments args) {
        if (!plugin.cfg.vote_enable) {
            return;
        }
        if (plugin.voteTask != null && plugin.voteTask.ticks < plugin.voteTask.timeout) {
            if (args.length() == 2) {
                if (sender.isOp() && "STOP".equalsIgnoreCase(args.top())) {
                    plugin.voteTask.ticks = plugin.voteTask.timeout + 1;
                    plugin.voteTask.cancel();
                    return;
                }
                plugin.voteTask.vote(asPlayer(sender), args.nextInt());
            }
        } else {
            if (args.length() < 4) {
                throw new BadCommandException("user.vote.need_options");
            }
            String subject = args.nextString();
            Set<String> options = new HashSet<>();
            for (int i = 0; i < plugin.cfg.vote_max_options; i++) {
                String option = args.top();
                if (option != null && option.length() > 0) {
                    options.add(args.nextString());
                } else {
                    break;
                }
            }
            plugin.voteTask = new VoteTask(subject, plugin.cfg.vote_timeout, plugin.cfg.vote_broadcast_interval, options);
            plugin.voteTask.runTaskTimer(plugin, 1, 1);
        }
    }

    @SubCommand(value = "tps", permission = "nu.tps")
    public void tps(CommandSender sender, Arguments args) {
        if (!plugin.cfg.tps_enable) {
            throw new BadCommandException();
        }
        List<Byte> tpsHistory = plugin.tpsPingTask.tpsHistory();
        int verbose = args.argInt("v", args.argInt("verbose", 0));
        verbose = Math.min(verbose, tpsHistory.size());
        int tick = args.argInt("t", args.argInt("tick", 0));
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeSec = currentTimeMillis / 1000L;
        List<String> lines = new ArrayList<>(30);

        if (tick > 0 && sender.hasPermission("nu.tps.tick")) {
            List<Pair<Long, Long>> tickMillisNano = plugin.tpsPingTask.getTickMillisNano();
            tick = Math.min(tick, tickMillisNano.size());
            msg(sender, "user.tps.header_tick", tick);
            int lineNo = 0;

            do {
                List<Pair<Long, Long>> lineNano = tickMillisNano.stream().skip(tickMillisNano.size() - tick).skip(lineNo * 10).limit(10).collect(Collectors.toList());
                String detail = lineNano.stream().map(Pair::getValue).map(n -> tpsColor(1000.0 * 1000.0 * 1000.0 / n).toString() + String.format("%.2f", n / 1000000.0)).collect(Collectors.joining(", "));
                ChatColor chatColor = tpsColor(1000 * 1000.0 * 1000.0 * lineNano.size() / lineNano.stream().map(Pair::getValue).mapToLong(Long::longValue).sum());
                String line =
                        chatColor.toString()
                                + DateTimeFormatter.ofPattern("HH:mm:ss.SS").format(Instant.ofEpochMilli(lineNano.get(0).getKey()).atZone(ZoneId.systemDefault()))
                                + ChatColor.RESET.toString() + ": " + detail;
                lines.add(line);
            } while (++lineNo * 10 < tick);

            long tickNanoAvg = plugin.tpsPingTask.getTickNanoAvg();
            Pair<Long, Long> tickNanoMax = plugin.tpsPingTask.getTickNanoMax();

            lines.forEach(
                    sender::sendMessage
            );

            msg(sender, "user.tps.tick_statics",
                    tickNanoAvg / (1000 * 1000.0),
                    DateTimeFormatter.ofPattern("HH:mm:ss").format(Instant.ofEpochMilli(tickNanoMax.getKey()).atZone(ZoneId.systemDefault())),
                    tickNanoMax.getValue() / (1000 * 1000.0)
            );

        } else if (verbose > 0 && sender.hasPermission("nu.tps.verbose")) {
            msg(sender, "user.tps.header_verbose", verbose);
            Instant begin = Instant.ofEpochSecond(currentTimeSec - verbose);
            int lineNo = 0;

            do {
                List<Byte> lineTps = tpsHistory.stream().skip(tpsHistory.size() - verbose).skip(lineNo * 20).limit(20).collect(Collectors.toList());
                String detail = lineTps.stream().map(tps -> tpsColor(tps).toString() + String.format("%02d", tps)).collect(Collectors.joining(","));
                ChatColor chatColor = tpsColor(lineTps.stream().mapToInt(Byte::intValue).average().orElse(0));
                String line =
                        chatColor.toString()
                                + DateTimeFormatter.ofPattern("HH:mm:ss").format(begin.plusSeconds(lineNo * 20).atZone(ZoneId.systemDefault()))
                                + ChatColor.RESET.toString() + ":" + detail;
                lines.add(line);
            } while (++lineNo * 20 < verbose);

            lines.forEach(
                    sender::sendMessage
            );

        } else {
            msg(sender, "user.tps.header", plugin.cfg.tps_history);
            List<Byte> last = tpsHistory.stream().skip(Math.max(0, tpsHistory.size() - plugin.cfg.tps_history)).collect(Collectors.toList());
            int lineNo = 0;
            Instant begin = Instant.ofEpochSecond(currentTimeSec - last.size());

            for (Byte tps : last) {
                ChatColor tpsColor = tpsColor(tps);
                StringBuilder detail = new StringBuilder();
                detail.append(tpsColor);
                detail.append(new String(new char[Math.min(tps, 20)]).replace("\0", "#"));
                detail.append(ChatColor.BLACK);
                detail.append(new String(new char[Math.max(20 - tps, 0)]).replace("\0", "#"));

                String line = DateTimeFormatter.ofPattern("HH:mm:ss").format(begin.plusSeconds(lineNo++).atZone(ZoneId.systemDefault()))
                                      + ": " + tpsColor + ChatColor.BOLD + String.format("%02d", tps) + " " + ChatColor.RESET
                                      + detail.toString();
                lines.add(line);
            }

            lines.forEach(
                    sender::sendMessage
            );
        }
        suppressNextCompleteMessage = true;
    }

    @SubCommand(value = "ping", permission = "nu.ping")
    public void ping(CommandSender sender, Arguments args) {
        if (!plugin.cfg.ping_enable) {
            throw new BadCommandException();
        }
        Player p;
        if (sender instanceof Player) {
            p = ((Player) sender);
        } else {
            p = args.nextPlayer();
        }

        Deque<Integer> pings = plugin.tpsPingTask.getPlayerPing30s().get(p);
        double average = pings.stream().mapToInt(Integer::intValue).average().orElse(0);
        int max = pings.stream().mapToInt(Integer::intValue).max().orElse(0);

        List<Integer> last10s = new ArrayList<>(10);
        Iterator<Integer> pit = pings.descendingIterator();
        while (pit.hasNext()) {
            last10s.add(pit.next());
            if (last10s.size() == 10) break;
        }

        String detail = last10s.stream().map(pin -> pingColor(pin).toString() + pin).collect(Collectors.joining(", "));
        msg(sender, "user.ping.history10s", p.getPlayerListName(), detail);
        msg(sender, "user.ping.avgmax30s", pingColor(average) + String.format("%.2f", average) + ChatColor.RESET, pingColor(max) + String.valueOf(max) + ChatColor.RESET);
    }

    @SubCommand(value = "pingtop", permission = "nu.ping.top")
    public void pingTop(CommandSender sender, Arguments args) {
        int perPage = 10;
        int page = args.top() == null ? 0 : args.nextInt() - 1;
        // See https://www.spigotmc.org/resources/spigotping-added-in-tablist-ping.24419/reviews#review-150990-151644
        List<Player> players = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).map(Bukkit::getPlayer).collect(Collectors.toList());
        int max = players.size() / perPage;
        if (page < 0 || page > max) throw new BadCommandException("user.error.not_int");
        PriorityQueue<Pair<Player, Integer>> pingTop = new PriorityQueue<>(Comparator.comparing(Pair::getValue));
        pingTop.addAll(players.stream().map(p -> new Pair<>(p, PlayerUtils.getPing(p))).collect(Collectors.toList()));
        msg(sender, "user.ping.top.avg", page + 1, max + 1, pingTop.stream().mapToInt(Pair::getValue).average().orElse(Double.NaN));
        List<Pair<Player, Integer>> list = Stream.generate(pingTop::poll).limit(pingTop.size()).skip(perPage * page).limit(perPage).collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            Pair<Player, Integer> e = list.get(i);
            msg(sender, "user.ping.top.player", perPage * page + i + 1, pingColor(e.getValue()).toString() + e.getValue() + ChatColor.RESET, e.getKey().getPlayerListName());
        }
    }

    // TODO:
    private ChatColor pingColor(double ping) {
        if (ping <= 30) {
            return ChatColor.GREEN;
        } else if (ping <= 60) {
            return ChatColor.DARK_GREEN;
        } else if (ping <= 100) {
            return ChatColor.YELLOW;
        } else if (ping <= 150) {
            return ChatColor.GOLD;
        }
        return ChatColor.RED;
    }

    private ChatColor tpsColor(double tps) {
        if (tps >= 19) {
            return ChatColor.GREEN;
        } else if (tps >= 17) {
            return ChatColor.DARK_GREEN;
        } else if (tps >= 15) {
            return ChatColor.YELLOW;
        } else if (tps >= 10) {
            return ChatColor.GOLD;
        }
        return ChatColor.RED;
    }

    @Override
    protected boolean showCompleteMessage() {
        try {
            return !suppressNextCompleteMessage;
        } finally {
            suppressNextCompleteMessage = false;
        }
    }
}
