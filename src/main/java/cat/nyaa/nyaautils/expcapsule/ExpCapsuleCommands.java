package cat.nyaa.nyaautils.expcapsule;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.ExperienceUtils;
import cat.nyaa.nyaautils.Configuration;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ExpCapsuleCommands extends CommandReceiver {
    private final NyaaUtils plugin;
    private final Configuration cfg;

    public ExpCapsuleCommands(NyaaUtils plugin, ILocalizer i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
        cfg = plugin.cfg;
    }

    @Override
    public String getHelpPrefix() {
        return "expcap";
    }

    @SubCommand(value = "store", permission = "nu.expcap.store")
    public void cmdStoreExp(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        int exp = ExperienceUtils.getExpPoints(p);
        p.sendMessage(I18n.format("user.expcap.current_exp", exp));

        if (args.top() == null) return;
        int amount;
        if ("ALL".equalsIgnoreCase(args.top())) {
            amount = exp;
            if (amount <= 0) throw new BadCommandException("user.expcap.not_enough_exp");
        } else {
            amount = args.nextInt();
            if (amount <= 0) throw new BadCommandException("user.expcap.wrong_nbr");
            if (exp < amount) throw new BadCommandException("user.expcap.not_enough_exp");
        }

        ItemStack item = getItemInHand(sender);
        if (item.getType() != plugin.cfg.expCapsuleType) {
            throw new BadCommandException("user.expcap.wrong_cap_type");
        }
        if (item.getAmount() > 1) {
            throw new BadCommandException("user.expcap.not_stackable");
        }

        Integer storedExpInt = getStoredExp(item);
        if (storedExpInt == null) storedExpInt = 0;
        long storedExp = Integer.toUnsignedLong(storedExpInt);
        int maxExp = cfg.expcap_max_stored_exp;
        if (storedExp > maxExp) {
            new Message(I18n.format("user.expcap.bottle_full", maxExp))
                    .send(p);
        } else {
            storedExp += amount;
            if (storedExp > maxExp){
                new Message(I18n.format("user.expcap.bottle_full"))
                    .send(p);
                amount = Math.toIntExact(amount - (storedExp - maxExp));
                storedExp = (long) maxExp;
            }
            new Message(I18n.format("user.expcap.stored_exp",amount, storedExp))
                    .send(p);
            setStoredExp(item, (int) storedExp);
            ExperienceUtils.subtractExpPoints(p, amount);
        }
    }

    @SubCommand(value = "restore", permission = "nu.expcap.restore")
    public void cmdRestoreExp(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ItemStack item = getItemInHand(sender);
        Integer storedExp = getStoredExp(item);
        if (storedExp == null) throw new BadCommandException("user.expcap.not_enough_exp_cap");
        if (item.getAmount() > 1) throw new BadCommandException("user.expcap.not_stackable");

        int amount;
        if (args.top() == null) throw new BadCommandException();
        if ("ALL".equalsIgnoreCase(args.top())) {
            amount = storedExp;
            if (amount <= 0) throw new BadCommandException("user.expcap.not_enough_exp_cap");
        } else {
            amount = args.nextInt();
            if (amount <= 0) throw new BadCommandException("user.expcap.wrong_nbr");
            if (amount > storedExp) throw new BadCommandException("user.expcap.not_enough_exp_cap");
        }

        storedExp -= amount;
        p.giveExp(amount);
        setStoredExp(item, storedExp);
    }

    @SubCommand(value = "set", permission = "nu.expcap.set")
    public void cmdSetExp(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ItemStack item = getItemInHand(sender);
        int amount = args.nextInt();
        setStoredExp(item, amount);
    }

    public static final String EXP_CAPSULE_MAGIC = ChatColor.translateAlternateColorCodes('&', "&e&c&a&r");

    public static Integer getStoredExp(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        ItemMeta meta = item.getItemMeta();
        List<String> lores = meta.getLore();
        for (String str : lores) {
            if (str.contains(EXP_CAPSULE_MAGIC)) {
                int offset = str.lastIndexOf(EXP_CAPSULE_MAGIC) + EXP_CAPSULE_MAGIC.length();
                String rem = str.substring(offset);
                Integer exp = null;
                try {
                    exp = Integer.parseInt(rem);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
                return exp;
            }
        }
        return null;
    }

    public static void setStoredExp(ItemStack item, int exp) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        List<String> newLore = new ArrayList<>();
        for (String str : lore) {
            if (str.contains(EXP_CAPSULE_MAGIC)) continue;
            newLore.add(str);
        }
        if (exp > 0) {
            newLore.add(0, I18n.format("user.expcap.contain_exp") + EXP_CAPSULE_MAGIC + Integer.toString(exp));
        }
        meta.setLore(newLore);
        item.setItemMeta(meta);
    }
}
