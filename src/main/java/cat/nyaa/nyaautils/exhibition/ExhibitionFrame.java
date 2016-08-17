package cat.nyaa.nyaautils.exhibition;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Optional;

public class ExhibitionFrame {
    private static final String MAGIC_TITLE = ChatColor.translateAlternateColorCodes('&', "&b&e&a&3&f");

    private final ItemFrame frame;
    private ExhibitionFrame(ItemFrame frame) {
        if (frame == null) throw new IllegalArgumentException();
        this.frame = frame;
    }

    public static ExhibitionFrame fromItemFrame(ItemFrame frame) {
        if (frame == null) return null;
        return new ExhibitionFrame(frame);
    }

    public static ExhibitionFrame fromPlayerEye(Player p) {
        Vector eyeVector = p.getEyeLocation().getDirection();
        Vector locVec = p.getEyeLocation().toVector().multiply(-1);
        Optional<Entity> itemF = p.getNearbyEntities(10,10,10).stream()
                .filter(entity -> entity instanceof ItemFrame)
                .filter(entity -> entity.getLocation().toVector().add(locVec).angle(eyeVector) < 0.5D )
                .sorted((e1,e2)->Double.compare(e1.getLocation().distance(p.getLocation()), e2.getLocation().distance(p.getLocation())))
                .findFirst();
        if (itemF.isPresent()) {
            return new ExhibitionFrame((ItemFrame)itemF.get());
        } else {
            return null;
        }
    }
    public boolean hasItem() {
        return frame.getItem() != null && frame.getItem().getType() != Material.AIR;
    }

    public boolean isSet() {
        ItemStack i = frame.getItem();
        return i != null
                && i.hasItemMeta()
                && i.getItemMeta().hasDisplayName()
                && i.getItemMeta().getDisplayName().startsWith(MAGIC_TITLE);
    }

    public void set() {
        ItemStack i = frame.getItem();
        ItemMeta m = i.getItemMeta();
        if (m.hasDisplayName()) {
            m.setDisplayName(MAGIC_TITLE + m.getDisplayName());
        } else {
            m.setDisplayName(MAGIC_TITLE);
        }
        i.setItemMeta(m);
        frame.setItem(i);
    }

    public void unset() {
        if (!isSet()) return;
        ItemStack i = frame.getItem();
        ItemMeta m = i.getItemMeta();
        String newName = m.getDisplayName().substring(MAGIC_TITLE.length());
        if (newName.length() <= 0) {
            m.setDisplayName(null);
        } else {
            m.setDisplayName(newName);
        }
        i.setItemMeta(m);
        frame.setItem(i);
    }
}
