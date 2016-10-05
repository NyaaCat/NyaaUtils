package cat.nyaa.utils;

import cat.nyaa.utils.internationalizer.I16rItemName;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public final class Message {
    public final BaseComponent inner;

    public Message(String text) {
        inner = new TextComponent(text);
    }

    public Message append(String text) {
        inner.addExtra(text);
        return this;
    }

    public Message appendFormat(Internationalization i18n, String template, Object... obj) {
        return append(i18n.get(template, obj));
    }

    public Message append(ItemStack item) {
        return append(item, "{itemName} *{amount}");
    }

    public Message append(ItemStack item, String display) {
        boolean rawName = !(item.hasItemMeta() && item.getItemMeta().hasDisplayName());
        BaseComponent nameComponent = rawName ? I16rItemName.getUnlocalizedName(item) : new TextComponent(item.getItemMeta().getDisplayName());
        BaseComponent result;
        String itemJson = "";
        if (item.hasItemMeta() && item.getItemMeta() instanceof BookMeta) {
            ItemStack itemStack = item.clone();
            BookMeta meta = (BookMeta) itemStack.getItemMeta();
            meta.setPages(new ArrayList<String>());
            itemStack.setItemMeta(meta);
            itemJson = ReflectionUtil.convertItemStackToJson(itemStack);
        } else {
            itemJson = ReflectionUtil.convertItemStackToJson(item);
        }
        HoverEvent ev = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemJson)});
        nameComponent.setHoverEvent(ev);


        if ("{itemName}".equals(display)) {
            result = nameComponent;
        } else {
            String[] plain = display.split("\\{itemName\\}");
            result = new TextComponent(plain[0]);
            result.setHoverEvent(ev);
            for (int i = 1; i < plain.length; i++) {
                result.addExtra(nameComponent);
                TextComponent tmp = new TextComponent(plain[i].replace("{amount}", Integer.toString(item.getAmount())));
                tmp.setHoverEvent(ev);
                result.addExtra(tmp);
            }
        }

        result.setHoverEvent(ev);
        inner.addExtra(result);
        return this;
    }

    public Message append(BaseComponent component) {
        inner.addExtra(component);
        return this;
    }

    public Message send(Player p) {
        p.spigot().sendMessage(inner);
        return this;
    }

    public Message broadcast() {
        Bukkit.getServer().spigot().broadcast(inner);
        return this;
    }

    public Message broadcast(String permission) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                this.send(player);
            }
        }
        return this;
    }
}
