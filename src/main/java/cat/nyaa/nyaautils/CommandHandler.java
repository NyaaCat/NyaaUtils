package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.ExperienceUtils;
import cat.nyaa.nyaacore.utils.IPCUtils;
import cat.nyaa.nyaacore.utils.VaultUtils;
import cat.nyaa.nyaautils.elytra.ElytraCommands;
import cat.nyaa.nyaautils.enchant.EnchantCommands;
import cat.nyaa.nyaautils.exhibition.ExhibitionCommands;
import cat.nyaa.nyaautils.expcapsule.ExpCapsuleCommands;
import cat.nyaa.nyaautils.mailbox.MailboxCommands;
import cat.nyaa.nyaautils.particle.ParticleCommands;
import cat.nyaa.nyaautils.realm.RealmCommands;
import cat.nyaa.nyaautils.repair.RepairCommands;
import cat.nyaa.nyaautils.signedit.SignEditCommands;
import cat.nyaa.nyaautils.timer.TimerCommands;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        if(pName == null){
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
                if ((!(entity instanceof Player) || ((Player) entity).isOnline()) && !stopped){
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
        if (plugin.lpListener.toggleStatus(p.getUniqueId())) {
            p.sendMessage(I18n.format("user.lp.turned_on"));
        } else {
            p.sendMessage(I18n.format("user.lp.turned_off"));
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
                !(p.getTotalExperience() >= plugin.cfg.custom_fixes_prefix_expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }

        if (plugin.cfg.custom_fixes_prefix_moneyCost > 0 &&
                !VaultUtils.enoughMoney(p, plugin.cfg.custom_fixes_prefix_moneyCost)) {
            msg(sender, "user.warn.no_enough_money");
            return;
        }
        if (NyaaUtils.hasHEH) {
            IPCUtils.callMethod("heh_balance_deposit", plugin.cfg.custom_fixes_prefix_moneyCost);
        }
        if (plugin.cfg.custom_fixes_prefix_expCost > 0) {
            ExperienceUtils.addPlayerExperience(p, -plugin.cfg.custom_fixes_prefix_expCost);
        }
        VaultUtils.withdraw(p, plugin.cfg.custom_fixes_prefix_moneyCost);
        VaultUtils.setPlayerPrefix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_prefix_format).replace("{prefix}", prefix), plugin.getServer().getPluginManager().getPlugin("PermissionsEx") != null);
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
                !VaultUtils.enoughMoney(p, plugin.cfg.custom_fixes_suffix_moneyCost)) {
            msg(sender, "user.warn.no_enough_money");
            return;
        }
        if (NyaaUtils.hasHEH) {
            IPCUtils.callMethod("heh_balance_deposit", plugin.cfg.custom_fixes_suffix_moneyCost);
        }
        if (plugin.cfg.custom_fixes_suffix_expCost > 0) {
            ExperienceUtils.addPlayerExperience(p, -plugin.cfg.custom_fixes_suffix_expCost);
        }
        VaultUtils.withdraw(p, plugin.cfg.custom_fixes_suffix_moneyCost);
        VaultUtils.setPlayerSuffix(p, ChatColor.translateAlternateColorCodes('&', plugin.cfg.custom_fixes_suffix_format).replace("{suffix}", suffix), plugin.getServer().getPluginManager().getPlugin("PermissionsEx") != null);
        msg(sender, "user.suffix.success", suffix);
    }

    /* reset player's prefix */
    @SubCommand(value = "resetprefix", permission = "nu.prefix")
    public void commandResetPrefix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(VaultUtils.getPlayerPrefix(p).length() > 0)) {
            return;
        }

        VaultUtils.setPlayerPrefix(p, "", plugin.getServer().getPluginManager().getPlugin("PermissionsEx") != null);
        msg(sender, "user.resetprefix.success");
    }

    /* reset player's suffix */
    @SubCommand(value = "resetsuffix", permission = "nu.suffix")
    public void commandResetSuffix(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (!(VaultUtils.getPlayerSuffix(p).length() > 0)) {
            return;
        }

        VaultUtils.setPlayerSuffix(p, "", plugin.getServer().getPluginManager().getPlugin("PermissionsEx") != null);
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
                !(p.getTotalExperience() >= expCost)) {
            msg(sender, "user.warn.not_enough_exp");
            return;
        }
        if (moneyCost > 0 &&
                !VaultUtils.enoughMoney(p, moneyCost)) {
            msg(sender, "user.warn.no_enough_money");
            return;
        }
        if (NyaaUtils.hasHEH) {
            IPCUtils.callMethod("heh_balance_deposit", moneyCost);
        }
        ItemMeta itemStackMeta = item.getItemMeta();
        itemStackMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(itemStackMeta);
        if (expCost > 0) {
            ExperienceUtils.addPlayerExperience(p, -expCost);
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
        ItemStack newbook = new ItemStack(Material.BOOK_AND_QUILL, 1);
        BookMeta newbookMeta = (BookMeta) newbook.getItemMeta();
        newbookMeta.setPages(meta.getPages());
        newbook.setItemMeta(newbookMeta);
        p.getInventory().setItemInMainHand(newbook);
        msg(sender, "user.setbook.success");
    }
}
