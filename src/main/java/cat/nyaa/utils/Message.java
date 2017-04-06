package cat.nyaa.utils;

import cat.nyaa.utils.internationalizer.I16rItemName;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        return append("{itemName} *{amount}", item);
    }

    /**
     * supported syntax
     * {itemName}: when cursor hovered on, item will be displayed, item at index=0
     * {itemName:idx}: the number indicates the index of the item in items list
     * {amount}: a number, item at index=0
     * {amount:idx}: a number, item at index=idx (e.g {amount:0})
     *
     * @param template the template string
     * @param items    item list
     * @return the Message
     */
    public Message append(String template, ItemStack... items) {
        if (items == null || items.length == 0) return this;
        Map<String, BaseComponent> varMap = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack clone = items[i].clone();
            boolean hasCustomName = clone.hasItemMeta() && clone.getItemMeta().hasDisplayName();
            BaseComponent cmp = hasCustomName ? new TextComponent(clone.getItemMeta().getDisplayName()) : I16rItemName.getUnlocalizedName(clone);
            cmp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(getItemJsonStripped(clone))}));
            varMap.put(String.format("{amount:%d}", i), new TextComponent(Integer.toString(clone.getAmount())));
            varMap.put(String.format("{itemName:%d}", i), cmp);
            if (i == 0) {
                varMap.put("{amount}", new TextComponent(Integer.toString(clone.getAmount())));
                varMap.put("{itemName}", cmp);
            }
        }

        String remTemplate = template;
        while (remTemplate.length() > 0) {
            int idx = remTemplate.length();
            String var = null;
            for (String v : varMap.keySet()) {
                int t = remTemplate.indexOf(v);
                if (t >= 0 && t < idx) {
                    idx = t;
                    var = v;
                }
            }

            if (idx == -1) break; // no more variables left
            if (idx == 0) {
                remTemplate = remTemplate.substring(var.length());
                append(varMap.get(var));
            }
            if (idx > 0) {
                append(remTemplate.substring(0, idx));
                remTemplate = remTemplate.substring(idx);
            }
        }
        if (remTemplate.length() > 0) append(remTemplate);
        return this;
    }

    /**
     * @deprecated old buggy method, will be removed in the future
     */
    public Message append(ItemStack item, String display) {
        item = item.clone();
        boolean rawName = !(item.hasItemMeta() && item.getItemMeta().hasDisplayName());
        BaseComponent nameComponent = rawName ? I16rItemName.getUnlocalizedName(item) : new TextComponent(item.getItemMeta().getDisplayName());
        BaseComponent result;
        String itemJson = getItemJsonStripped(item);

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
        return send(p, MessageType.CHAT);
    }

    public Message send(Player p, MessageType type) {
        if (type == MessageType.CHAT) {
            p.spigot().sendMessage(inner);
        } else if (type == MessageType.ACTION_BAR) {
            sendActionBarMessage(p, inner);
        } else if (type == MessageType.TITLE) {
            sendTitle(p, inner, new TextComponent(), 10, 40, 10);
        } else if (type == MessageType.SUBTITLE) {
            sendTitle(p, new TextComponent(), inner, 10, 40, 10);
        }
        return this;
    }

    public Message broadcast() {
        return broadcast(MessageType.CHAT);
    }

    public Message broadcast(MessageType type) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            send(p, type);
        }
        Bukkit.getConsoleSender().sendMessage(inner.toLegacyText());
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

    private String getItemJsonStripped(ItemStack item) {
        ItemStack cloned = item.clone();
        if (cloned.hasItemMeta() && cloned.getItemMeta() instanceof BookMeta) {
            return ReflectionUtil.convertItemStackToJson(removeBookContent(cloned));
        }
        if (cloned.hasItemMeta() && cloned.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) cloned.getItemMeta();
            if (blockStateMeta.hasBlockState() && blockStateMeta.getBlockState() instanceof InventoryHolder) {
                InventoryHolder inventoryHolder = (InventoryHolder) blockStateMeta.getBlockState();
                ArrayList<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < inventoryHolder.getInventory().getSize(); i++) {
                    ItemStack itemStack = inventoryHolder.getInventory().getItem(i);
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (items.size() < 5) {
                            if (itemStack.hasItemMeta()) {
                                if (itemStack.getItemMeta().hasLore()) {
                                    ItemMeta meta = itemStack.getItemMeta();
                                    meta.setLore(new ArrayList<String>());
                                    itemStack.setItemMeta(meta);
                                }
                                if (itemStack.getItemMeta() instanceof BookMeta) {
                                    itemStack = removeBookContent(itemStack);
                                }
                            }
                            items.add(itemStack);
                        } else {
                            items.add(new ItemStack(Material.STONE));
                        }
                    }
                }
                inventoryHolder.getInventory().clear();
                for (int i = 0; i < items.size(); i++) {
                    inventoryHolder.getInventory().setItem(i, items.get(i));
                }
                blockStateMeta.setBlockState((BlockState) inventoryHolder);
                cloned.setItemMeta(blockStateMeta);
                return ReflectionUtil.convertItemStackToJson(cloned);
            }
        }
        return ReflectionUtil.convertItemStackToJson(cloned);
    }

    /**
     * Get a clone of the item where all book pages are removed
     * if not a book, then the same item is returned
     *
     * @param item the book
     * @return book without contents.
     */
    public ItemStack removeBookContent(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() instanceof BookMeta) {
            ItemStack itemStack = item.clone();
            BookMeta meta = (BookMeta) itemStack.getItemMeta();
            meta.setPages(new ArrayList<String>());
            itemStack.setItemMeta(meta);
            return itemStack;
        }
        return item;
    }

    public static void sendActionBarMessage(Player player, BaseComponent msg) {
        try {
            Class craftPlayer = ReflectionUtil.getOBCClass("entity.CraftPlayer");
            Method getHandleMethod = craftPlayer.getMethod("getHandle");
            Object handle = getHandleMethod.invoke(player);
            Class iChatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");
            Class packetPlayOutChat = ReflectionUtil.getNMSClass("PacketPlayOutChat");
            Constructor constructor = packetPlayOutChat.getConstructor(iChatBaseComponent, byte.class);
            Object packet = constructor.newInstance(null, (byte) ChatMessageType.ACTION_BAR.ordinal());
            packet.getClass().getField("components").set(packet, new BaseComponent[]{msg});
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", 
                    ReflectionUtil.getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void sendTitle(Player player, BaseComponent title, BaseComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        player.sendTitle(title.toLegacyText(), subtitle.toLegacyText(), fadeInTicks, stayTicks, fadeOutTicks);
    }
}