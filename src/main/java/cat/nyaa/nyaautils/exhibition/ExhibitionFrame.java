package cat.nyaa.nyaautils.exhibition;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.bukkit.Material.AIR;

public class ExhibitionFrame {
    private static final String MAGIC_TITLE = ChatColor.translateAlternateColorCodes('&', "&b&e&a&3&f");
    private final ItemFrame frame;

    private ItemStack baseItem;
    private String ownerUUID;
    private String ownerName;
    private List<String> descriptions;
    private boolean itemSet = false;

    private void decodeItem() {
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
        if (lore.size() >= 1 && lore.get(0).startsWith(MAGIC_TITLE)) {
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
        List<String> metaList = new ArrayList<>();
        metaList.add(MAGIC_TITLE + Integer.toString(3 + descriptions.size()));
        metaList.add(ownerUUID);
        metaList.add(ownerName);
        for (String line : descriptions) {
            metaList.add(base64(line));
        }
        metaList.add(MAGIC_TITLE);

        ItemStack item = baseItem.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            metaList.addAll(meta.getLore());
        }
        meta.setLore(metaList);
        item.setItemMeta(meta);
        frame.setItem(item);
    }

    private ExhibitionFrame(ItemFrame frame) {
        if (frame == null) throw new IllegalArgumentException();
        this.frame = frame;
        decodeItem();
    }

    public static ExhibitionFrame fromItemFrame(ItemFrame frame) {
        if (frame == null) return null;
        return new ExhibitionFrame(frame);
    }

    public static ExhibitionFrame fromPlayerEye(Player p) {
        Vector eyeVector = p.getEyeLocation().getDirection();
        Vector locVec = p.getEyeLocation().toVector().multiply(-1);
        Optional<Entity> itemF = p.getNearbyEntities(10, 10, 10).stream()
                .filter(entity -> entity instanceof ItemFrame)
                .filter(entity -> entity.getLocation().toVector().add(locVec).angle(eyeVector) < 0.5D)
                .sorted((e1, e2) -> Double.compare(e1.getLocation().distance(p.getLocation()), e2.getLocation().distance(p.getLocation())))
                .findFirst();
        if (itemF.isPresent()) {
            return new ExhibitionFrame((ItemFrame) itemF.get());
        } else {
            return null;
        }
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

    public static boolean isFrameInnerItem(ItemStack item) {
        return item != null
                && item.hasItemMeta()
                && item.getItemMeta().hasLore()
                && item.getItemMeta().getLore().size() >= 1
                && item.getItemMeta().getLore().get(0).startsWith(MAGIC_TITLE);
    }

    private static final Base64.Encoder b64Encoder = Base64.getEncoder();
    private static final Base64.Decoder b64Decoder = Base64.getDecoder();
    private static String base64(String str) {
        try {
            return b64Encoder.encodeToString(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) { // WTF utf8 not supported?
            return "";
        }
    }
    private static String deBase64(String base64) {
        try {
            return new String(b64Decoder.decode(base64), "UTF-8");
        } catch (UnsupportedEncodingException ex) { // WTF utf8 not supported?
            return "";
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
