package cat.nyaa.nyaautils.enchant;

import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.LocaleUtils;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EnchantCommands extends CommandReceiver {
    private final Map<UUID, Long> enchantCooldown;
    private NyaaUtils plugin;

    public EnchantCommands(Object plugin, LanguageRepository i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
        enchantCooldown = new HashMap<>();
    }

    @Override
    public String getHelpPrefix() {
        return "enchant";
    }

    @Override
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        String subCommand = cmd.top();
        if (subCommand == null) subCommand = "";
        switch (subCommand) {
            case "addsrc":
            case "info":
            case "help":
                super.acceptCommand(sender, cmd);
                break;
            default:
                commandEnchantDefault(sender, cmd);
        }
    }

    @SubCommand(value = "addsrc", permission = "nu.addenchsrc")
    public void commandAddEnchSrc(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        if (BasicItemMatcher.containsMatch(NyaaUtils.instance.cfg.enchantSrcConfig.enchantSrc, item)) {
            sender.sendMessage(I18n.format("user.enchant.enchantsrc_already_exists"));
            return;
        }
        BasicItemMatcher matcher = new BasicItemMatcher();
        matcher.itemTemplate = item.clone();
        matcher.enchantMatch = BasicItemMatcher.MatchingMode.ARBITRARY;
        matcher.nameMatch = BasicItemMatcher.MatchingMode.EXACT;
        matcher.repairCostMatch = BasicItemMatcher.MatchingMode.EXACT;
        NyaaUtils.instance.cfg.enchantSrcConfig.enchantSrc.add(matcher);
        NyaaUtils.instance.cfg.save();
    }

    @SubCommand(value = "info", permission = "nu.enchantinfo")
    public void commandEnchantInfo(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ItemStack item = getItemInOffHand(sender);

        if (!BasicItemMatcher.containsMatch(NyaaUtils.instance.cfg.enchantSrcConfig.enchantSrc, item)) {
            sender.sendMessage(I18n.format("user.enchant.invalid_src"));
            return;
        }

        Map<Enchantment, Integer> enchant;
        if (item.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            enchant = meta.getStoredEnchants();
        } else {
            enchant = item.getEnchantments();
        }

        sender.sendMessage(I18n.format("user.enchant.list_ench_header"));
        printEnchant(p, enchant.keySet().toArray(new Enchantment[0]));
        long cooldown = 0;
        if (enchantCooldown.containsKey(p.getUniqueId())) {
            cooldown = enchantCooldown.get(p.getUniqueId()) + (plugin.cfg.enchantCooldown / 20 * 1000);
        }
        float percent;
        msg(sender, "user.enchantinfo.info_0");
        if (cooldown > System.currentTimeMillis()) {
            percent = (plugin.cfg.chanceModerate +
                    plugin.cfg.chanceFail + plugin.cfg.chanceDestroy) / 100.0F;
            msg(sender, "user.enchantinfo.info_1", 0);
        } else {
            percent = (plugin.cfg.chanceSuccess + plugin.cfg.chanceModerate +
                    plugin.cfg.chanceFail + plugin.cfg.chanceDestroy) / 100.0F;
            msg(sender, "user.enchantinfo.info_1", (int) (plugin.cfg.chanceSuccess / percent));
        }
        msg(sender, "user.enchantinfo.info_2", (int) (plugin.cfg.chanceModerate / percent));
        msg(sender, "user.enchantinfo.info_3", (int) (plugin.cfg.chanceFail / percent));
        msg(sender, "user.enchantinfo.info_4", (int) (plugin.cfg.chanceDestroy / percent));
        if (cooldown > System.currentTimeMillis()) {
            msg(sender, "user.enchantinfo.info_cooldown", (int) ((cooldown - System.currentTimeMillis()) / 1000));
        }
    }

    @DefaultCommand(permission = "nu.enchant")
    public void commandEnchantDefault(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (args.top() == null) {
            sender.sendMessage(I18n.format("user.enchant.list_ench_header"));
            printEnchant(p, Enchantment.values());
            sender.sendMessage(I18n.format("manual.enchant.usage"));
        } else {
            ItemStack main = getItemInHand(sender);
            ItemStack off = getItemInOffHand(sender);
            if (!BasicItemMatcher.containsMatch(NyaaUtils.instance.cfg.enchantSrcConfig.enchantSrc, off)) {
                sender.sendMessage(I18n.format("user.enchant.invalid_src"));
                return;
            }

            if (main.getAmount() != 1 || !(main.getType().getMaxDurability() > 0)) {
                sender.sendMessage(I18n.format("user.enchant.invalid_item"));
                return;
            }

            if (main.hasItemMeta() && main.getItemMeta().hasLore()) {
                if (!plugin.cfg.globalLoreBlacklist.canEnchant(main.getItemMeta().getLore())) {
                    sender.sendMessage(I18n.format("user.enchant.invalid_item"));
                    return;
                }
            }

            String enchStr = args.next().toLowerCase();
            Enchantment ench = null;
            try {
                ench = Enchantment.getByKey(NamespacedKey.minecraft(enchStr));
            } catch (IllegalArgumentException e) {
            }
            if (ench == null) {
                sender.sendMessage(I18n.format("user.enchant.invalid_ench", enchStr));
                return;
            }

            int rawlevel = args.nextInt();

            int level = rawlevel;

            if (level <= 0 || level > plugin.cfg.enchantMaxLevel.get(ench)) {
                sender.sendMessage(I18n.format("user.enchant.invalid_level"));
                return;
            }
            long cooldown = 0;
            if (enchantCooldown.containsKey(p.getUniqueId())) {
                cooldown = enchantCooldown.get(p.getUniqueId()) + (plugin.cfg.enchantCooldown / 20 * 1000);
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
                if (rawlevel > realLvl
                        || (rawlevel + main.getEnchantmentLevel(ench) > plugin.cfg.enchantMaxLevel.get(ench))) {
                    sender.sendMessage(I18n.format("user.enchant.invalid_level"));
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
                if (rawlevel > realLvl
                        || (rawlevel + main.getEnchantmentLevel(ench) > plugin.cfg.enchantMaxLevel.get(ench))) {
                    sender.sendMessage(I18n.format("user.enchant.invalid_level"));
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
                p.sendMessage(I18n.format("user.enchant.success"));
                p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getEyeLocation(), 300);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            } else {
                p.sendMessage(I18n.format("user.enchant.fail"));
                p.getWorld().spawnParticle(Particle.SLIME, p.getEyeLocation(), 200);
                if (deleteItem) {
                    main = new ItemStack(Material.AIR);
                }
            }
            enchantCooldown.put(p.getUniqueId(), System.currentTimeMillis());
            p.getInventory().setItemInMainHand(main);
            p.getInventory().setItemInOffHand(off);
        }
    }

    private void printEnchant(Player p, Enchantment[] enchantments) {
        for (Enchantment e : enchantments) {
            Message msg = new Message(e.getKey().getKey() + ": ");
            msg.append(LocaleUtils.getNameComponent(e));
            msg.append(" " + I18n.format("user.enchant.max_level", plugin.cfg.enchantMaxLevel.get(e)));
            msg.inner.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/nu enchant " + e.getKey().getKey()));
            p.spigot().sendMessage(msg.inner);
        }
    }
}
