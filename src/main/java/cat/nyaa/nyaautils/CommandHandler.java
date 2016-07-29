package cat.nyaa.nyaautils;

import cat.nyaa.utils.BasicItemMatcher;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import cat.nyaa.utils.Message;
import cat.nyaa.utils.internationalizer.I16rEnchantment;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class CommandHandler extends CommandReceiver<NyaaUtils> {

    public CommandHandler(NyaaUtils plugin, Internationalization i18n) {
        super(plugin, i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "addenchsrc", permission = "nu.addenchsrc")
    public void commandAddEnchSrc(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        BasicItemMatcher matcher = new BasicItemMatcher();
        matcher.itemTemplate = item.clone();
        matcher.enchantMatch = BasicItemMatcher.MatchingMode.ARBITRARY;
        matcher.nameMatch = BasicItemMatcher.MatchingMode.EXACT;
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
                    p.spigot().sendMessage(new Message(e.getName() + ": ")
                            .append(I16rEnchantment.fromEnchantment(e).getUnlocalizedName()).inner);
                } else {
                    p.sendMessage(e.getName() + ": " + e.getName());
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

            String enchStr = args.next();
            Enchantment ench = Enchantment.getByName(enchStr);
            if (ench == null) {
                sender.sendMessage(I18n._("user.enchant.invalid_ench"));
                return;
            }

            int level = args.nextInt();

            if (level <= 0) {
                sender.sendMessage(I18n._("user.enchant.invalid_level"));
                return;
            }

            if (off.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) off.getItemMeta();
                int realLvl = meta.getStoredEnchantLevel(ench);
                if (level > realLvl) {
                    sender.sendMessage(I18n._("user.enchant.invalid_level"));
                    return;
                } else if (level == realLvl) {
                    meta.removeStoredEnchant(ench);
                } else {
                    meta.addStoredEnchant(ench, realLvl - level, true);
                }
                off.setItemMeta(meta);
            } else {
                int realLvl = off.getEnchantmentLevel(ench);
                if (level > realLvl) {
                    sender.sendMessage(I18n._("user.enchant.invalid_level"));
                    return;
                } else if (level == realLvl) {
                    off.removeEnchantment(ench);
                } else {
                    off.addUnsafeEnchantment(ench, realLvl - level);
                }
            }

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
    }

    @SubCommand(value = "show", permission = "nu.show")
    public void commandShow(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        new Message("").append(item, I18n._("user.showitem.message", sender.getName())).broadcast();
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
}
