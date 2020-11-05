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
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ExpCapsuleCommands extends CommandReceiver {
    private final NyaaUtils plugin;
    private final Configuration cfg;

    private static final NamespacedKey expcapKey = new NamespacedKey(NyaaUtils.instance, "expcap");

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

        Long storedExpInt = getStoredExp(item);
        if (storedExpInt == null) storedExpInt = 0L;
        long storedExp = storedExpInt;
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
                storedExp = maxExp;
            }
            new Message(I18n.format("user.expcap.stored_exp",amount, storedExp))
                    .send(p);
            setStoredExp(item, storedExp);
            ExperienceUtils.subtractExpPoints(p, amount);
        }
    }

    @SubCommand(value = "restore", permission = "nu.expcap.restore")
    public void cmdRestoreExp(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ItemStack item = getItemInHand(sender);
        Long storedExp = getStoredExp(item);
        if (storedExp == null) throw new BadCommandException("user.expcap.not_enough_exp_cap");
        if (item.getAmount() > 1) throw new BadCommandException("user.expcap.not_stackable");

        Long amount;
        if (args.top() == null) throw new BadCommandException();
        if ("ALL".equalsIgnoreCase(args.top())) {
            amount = storedExp;
            if (amount <= 0) throw new BadCommandException("user.expcap.not_enough_exp_cap");
        } else {
            amount = args.nextLong();
            if (amount <= 0) throw new BadCommandException("user.expcap.wrong_nbr");
            if (amount > storedExp) throw new BadCommandException("user.expcap.not_enough_exp_cap");
        }

        storedExp -= amount;
        p.giveExp(amount.intValue());
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

    public static Long getStoredExp(ItemStack item) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        Long exp = persistentDataContainer.get(expcapKey, PersistentDataType.LONG);
        return exp;
    }

    public static void setStoredExp(ItemStack item, long exp) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        persistentDataContainer.set(expcapKey, PersistentDataType.LONG, exp);

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        List<String> newLore = new ArrayList<>();
        for (String str : lore) {
            if (str.contains(EXP_CAPSULE_MAGIC)) continue;
            newLore.add(str);
        }
        if (lore.size() == 0 && exp > 0){
            lore.add(0, "");
        }
        if (exp > 0) {
            newLore.set(0, I18n.format("user.expcap.contain_exp", Long.toString(exp)));
        }
        meta.setLore(newLore);
        item.setItemMeta(meta);
    }
}
