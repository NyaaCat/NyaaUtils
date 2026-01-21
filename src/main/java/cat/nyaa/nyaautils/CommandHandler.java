package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.ExperienceUtils;
import cat.nyaa.nyaacore.utils.HexColorUtils;
import cat.nyaa.nyaautils.elytra.ElytraCommands;
import cat.nyaa.nyaautils.exhibition.ExhibitionCommands;
import cat.nyaa.nyaautils.expcapsule.ExpCapsuleCommands;
import cat.nyaa.nyaautils.extrabackpack.ExtraBackpackCommands;
import cat.nyaa.nyaautils.lootprotect.LootProtectListener;
import cat.nyaa.nyaautils.mailbox.MailboxCommands;
import cat.nyaa.nyaautils.realm.RealmCommands;
import cat.nyaa.nyaautils.signedit.SignEditCommands;
import cat.nyaa.nyaautils.timer.TimerCommands;
import cat.nyaa.nyaautils.vote.VoteTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class CommandHandler extends CommandReceiver {
    @SubCommand("exhibition")
    public ExhibitionCommands exhibitionCommands;
    @SubCommand("mailbox")
    public MailboxCommands mailboxCommands;
    //@SubCommand("repair")
    //public RepairCommands repairCommands;
    @SubCommand("el")
    public ElytraCommands elytraCommands;
    @SubCommand("timer")
    public TimerCommands timerCommands;
    @SubCommand("realm")
    public RealmCommands realmCommands;
    @SubCommand("se")
    public SignEditCommands signEditCommands;
    @SubCommand("expcap")
    public ExpCapsuleCommands expCapsuleCommands;
    @SubCommand("bp")
    public ExtraBackpackCommands extraBackpackCommands;

    private NyaaUtils plugin;

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
            Player p = args.nextPlayerOrSender();

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
        final Entity p = args.nextEntityOrSender();

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
        msg(sender, "user.info.reload");
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

    @SubCommand(value = "rename", permission = "nu.rename")
    public void rename(CommandSender sender, Arguments args) {
        if (!(args.length() > 1)) {
            msg(sender, "manual.rename.usage");
            return;
        }
        Player p = asPlayer(sender);
        String name = args.next().replace("§", "");
        String s = stripFormatting(name);
        if (plugin.cfg.renameCharacterLimit != 0 && s.length() > NyaaUtils.instance.cfg.renameCharacterLimit) {
            msg(p, "user.rename.name_too_long", name, NyaaUtils.instance.cfg.renameCharacterLimit);
            return;
        }
        for (String k : plugin.cfg.renameDisabledFormattingCodes) {
            if (name.toUpperCase().contains("&" + k.toUpperCase()) && !p.hasPermission("nu.rename.blacklist")) {
                msg(p, "user.warn.blocked_format_codes", "&" + k);
                return;
            }
        }
        name = HexColorUtils.hexColored(name);
        for (String k : plugin.cfg.renameBlockedWords) {
            if (stripFormatting(name).toUpperCase().contains(k.toUpperCase()) && !p.hasPermission("nu.rename.blacklist")) {
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
        if (expCost > 0 &&
                    !(ExperienceUtils.getExpPoints(p) >= expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }
        ItemMeta itemStackMeta = item.getItemMeta();
        itemStackMeta.setDisplayName(HexColorUtils.hexColored(name));
        item.setItemMeta(itemStackMeta);
        if (expCost > 0) {
            ExperienceUtils.subtractExpPoints(p, expCost);
        }
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
        lore = HexColorUtils.hexColored(lore);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().equals(Material.AIR)) {
            msg(sender, "user.info.no_item_hand");
            return;

        }
        String[] line = lore.split("/n");
        List<String> lines = new ArrayList<>();
        for (String s : line) {
            lines.add(HexColorUtils.hexColored(s));
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
        lore = HexColorUtils.hexColored(lore);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().equals(Material.AIR)) {
            msg(sender, "user.info.no_item_hand");
            return;
        }
        String[] line = lore.split("/n");
        List<String> lines = item.getItemMeta().getLore() == null ? new ArrayList<>() : item.getItemMeta().getLore();
        for (String s : line) {
            lines.add(HexColorUtils.hexColored(s));
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
        author = HexColorUtils.hexColored(author);
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
        title = HexColorUtils.hexColored(title);

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

    @SubCommand(value = "sit", permission = "nu.sit")
    public void toggleSit(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (plugin.sitListener.enabledPlayers.contains(p.getUniqueId())) {
            plugin.sitListener.enabledPlayers.remove(p.getUniqueId());
            msg(sender, "user.sit.toggle.disabled");
        } else {
            plugin.sitListener.enabledPlayers.add(p.getUniqueId());
            msg(sender, "user.sit.toggle.enabled");
        }
    }

    //@SubCommand(value = "tpall", permission = "nu.tpall")
    public void tpall(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        int r = args.nextInt();
        Block center = p.getLocation().getBlock();
        int minX = center.getX() - r;
        int minZ = center.getZ() - r;
        int maxX = center.getX() + r;
        int maxZ = center.getZ() + r;
        int maxY = center.getY() + 1;
        List<Location> locations = new ArrayList<>();
        int playerCount = Bukkit.getOnlinePlayers().size() - 1;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int i = 0; i <= 16; i++) {
                    Block b = p.getWorld().getBlockAt(x, maxY - i, z);
                    if (b.getType().isSolid()) {
                        Block b2 = b.getRelative(BlockFace.UP, 1);
                        Block b3 = b.getRelative(BlockFace.UP, 2);
                        if ((b2.isEmpty() || b2.isPassable()) && (b3.isEmpty() || b3.isPassable()) && !b2.isLiquid() && !b3.isLiquid()) {
                            Location loc = b.getBoundingBox().getCenter().toLocation(b.getWorld());
                            loc.setY(b.getBoundingBox().getMaxY());
                            locations.add(loc.clone());
                            break;
                        }
                    }
                }
            }
        }
        if (locations.size() < playerCount) {
            msg(sender, "user.tpall.error");
            return;
        }
        Collections.shuffle(locations);
        int success = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isDead() && player.getUniqueId() != p.getUniqueId()) {
                if (player.getWorld() == p.getWorld() && player.getLocation().distance(p.getLocation()) + 3 <= r) {
                    continue;
                }
                Location old = player.getLocation().clone();
                if (player.teleport(locations.get(success))) {
                    success++;
                    plugin.ess.getUser(player).setLastLocation(old);
                }
            }
        }
        msg(sender, "user.tpall.success", success);
    }

    private String stripFormatting(String text) {
        return ChatColor.stripColor(HexColorUtils.hexColored(text));
    }

}
