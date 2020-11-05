package cat.nyaa.nyaautils.exhibition;

import cat.nyaa.nyaacore.utils.RayTraceUtils;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Material.AIR;

public class ExhibitionFrame {
    private static final NamespacedKey keyUid = new NamespacedKey(NyaaUtils.instance, "exhibitionKeyUid");
    private static final NamespacedKey keyName = new NamespacedKey(NyaaUtils.instance, "exhibitionKeyName");
    private static final NamespacedKey keyDescription = new NamespacedKey(NyaaUtils.instance, "exhibitionKeyDescription");
    //translate legacy
    private static final String MAGIC_TITLE = ChatColor.translateAlternateColorCodes('&', "&f");
    private static final Base64.Encoder b64Encoder = Base64.getEncoder();
    private static final Base64.Decoder b64Decoder = Base64.getDecoder();
    private final ItemFrame frame;
    private ItemStack baseItem;
    private String ownerUUID;
    private String ownerName;
    private List<String> descriptions;
    private boolean itemSet = false;

    private ExhibitionFrame(ItemFrame frame) {
        if (frame == null) throw new IllegalArgumentException();
        this.frame = frame;
        if (isLegacyItem(frame.getItem())){
            decodeLegacy();
            encodeItem();
            return;
        }
        decodeItem();
    }

    private static boolean isLegacyItem(ItemStack item) {
        if (item == null) return false;
        if (item.getType() == AIR) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasLore()) return false;
        return isLegacyItem(itemMeta.getLore());
    }

    public static ExhibitionFrame fromItemFrame(ItemFrame frame) {
        if (frame == null) return null;
        return new ExhibitionFrame(frame);
    }

    public static ExhibitionFrame fromPlayerEye(Player p) {
        Entity itemF = RayTraceUtils.getTargetEntity(p);
        if (itemF instanceof ItemFrame) {
            return new ExhibitionFrame((ItemFrame) itemF);
        } else {
            return null;
        }
    }

    public static boolean isFrameInnerItem(ItemStack item) {
        return item != null
                && item.hasItemMeta()
                && item.getItemMeta().hasLore()
                && item.getItemMeta().getLore().size() >= 1
                && isLegacyItem(item) || isExhibitionItem(item);
    }

    private static String base64(String str) {
        return b64Encoder.encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private static String deBase64(String base64) {
        try {
            return new String(b64Decoder.decode(base64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static boolean isExhibitionItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
        return persistentDataContainer.has(keyUid, PersistentDataType.STRING);
    }

    private static boolean isLegacyItem(List<String> lore) {
        if (lore.size() < 3) return false;
        boolean isExhibitionItem = false;
        if (lore.get(0).startsWith(MAGIC_TITLE)) {
            try {
                UUID.fromString(lore.get(1));
                isExhibitionItem = true;
            } catch (IllegalArgumentException ignore) {

            }
        }
        return isExhibitionItem;
    }

    private void decodeLegacy(){
        itemSet = false;
        ownerName = "";
        ownerUUID = "";
        descriptions = new ArrayList<>();
        if (frame == null) return;
        if (frame.getItem() == null) return;
        if (frame.getItem().getType() == AIR) return;
        baseItem = frame.getItem().clone();
        if (!frame.getItem().hasItemMeta()) return;
        if (!frame.getItem().getItemMeta().hasLore()) return;

        List<String> lore = frame.getItem().getItemMeta().getLore();
        if (isLegacyItem(lore)) {
            String lenStr = lore.get(0).substring(MAGIC_TITLE.length());
            int len = -1;
            try {
                len = Integer.parseInt(lenStr);
            } catch (NumberFormatException ex) {
                len = -1;
            }
            if (len >= 3 && lore.size() >= len + 1 && lore.get(len).equals(MAGIC_TITLE)) {
                descriptions = new ArrayList<>();
                ownerUUID = lore.get(1);
                ownerName = lore.get(2);
                for (int i = 3; i < len; i++) {
                    String tmp = deBase64(lore.get(i));
                    if (tmp != null && tmp.length() > 0) {
                        descriptions.add(tmp);
                    }
                }
                ItemMeta meta = baseItem.getItemMeta();
                if (len + 1 == lore.size()) {
                    meta.setLore(null);
                } else {
                    meta.setLore(lore.subList(len + 1, lore.size()));
                }
                baseItem.setItemMeta(meta);
            }
            itemSet = true;
        }
    }

    private void decodeItem() {
        itemSet = false;
        ownerName = "";
        ownerUUID = "";
        descriptions = new ArrayList<>();
        if (frame == null) return;
        ItemStack item = frame.getItem();
        if (item == null) return;
        if (item.getType() == AIR) return;
        baseItem = item.clone();
        if (!item.hasItemMeta()) return;
        if (!isExhibitionItem(item)){
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        String uuid = persistentDataContainer.get(keyUid, PersistentDataType.STRING);
        String name = persistentDataContainer.get(keyName, PersistentDataType.STRING);
        String des = persistentDataContainer.get(keyDescription, PersistentDataType.STRING);
        List<String> strings = splitDescription(des).stream()
                .map(ExhibitionFrame::deBase64)
                .collect(Collectors.toList());
        ownerUUID = uuid;
        ownerName = name;
        descriptions = strings;
    }

    private void encodeItem() {
        if (frame == null) return;
        if (baseItem == null || baseItem.getType() == AIR) return;
        if (!itemSet) {
            frame.setItem(baseItem.clone());
            return;
        }
        if (descriptions == null) descriptions = new ArrayList<>();
        if (ownerUUID == null) ownerUUID = "";
        if (ownerName == null) ownerName = "";
        List<String> encodedDescription = descriptions.stream()
                .map(ExhibitionFrame::base64)
                .collect(Collectors.toList());
        String des = joinDescription(encodedDescription);
        ItemStack item = baseItem.clone();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        persistentDataContainer.set(keyUid, PersistentDataType.STRING, ownerUUID);
        persistentDataContainer.set(keyName, PersistentDataType.STRING, ownerName);
        persistentDataContainer.set(keyDescription, PersistentDataType.STRING, des);
        item.setItemMeta(meta);
        frame.setItem(item);
    }

    private String joinDescription(List<String> descriptions) {
        StringBuilder sb = new StringBuilder();
        descriptions.forEach(s -> {
            sb.append(s).append("|");
        });
        if (sb.length()>0) {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    private List<String> splitDescription(String descriptions){
        if (descriptions == null){
            return new ArrayList<>();
        }
        String[] split = descriptions.split("\\|");
        return new ArrayList<>(Arrays.asList(split));
    }

    public boolean hasItem() {
        return frame.getItem() != null && frame.getItem().getType() != AIR;
    }

    public boolean isSet() {
        decodeItem();
        return itemSet;
    }

    public void set(Player owner) {
        if (isSet()) return;
        baseItem = frame.getItem().clone();
        ownerUUID = owner.getUniqueId().toString();
        ownerName = owner.getName();
        descriptions = new ArrayList<>();
        itemSet = true;
        encodeItem();
    }

    public void unset() {
        if (!isSet()) return;
        decodeItem();
        itemSet = false;
        encodeItem();
    }

    public boolean ownerMatch(Player p) {
        if (!isSet()) return true;
        decodeItem();
        return p.getUniqueId().toString().equals(ownerUUID);
    }

    public ItemFrame getItemFrame() {
        return frame;
    }

    public ItemStack getItemInFrame() {
        decodeItem();
        return baseItem;
    }

    public String getOwnerName() {
        decodeItem();
        return ownerName;
    }

    public List<String> getDescriptions() {
        decodeItem();
        return descriptions;
    }

    public void setDescription(int line, String str) {
        decodeItem();
        if (line < 0 || line > descriptions.size()) return;
        if (str == null) {
            if (line == descriptions.size()) return;
            descriptions.remove(line);
        } else {
            if (line == descriptions.size()) descriptions.add(str);
            else descriptions.set(line, str);
        }
        encodeItem();
    }
}
